package net.seleucus.wsp.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.seleucus.wsp.checker.WSCheckerListener;
import net.seleucus.wsp.client.WSConnection;
import net.seleucus.wsp.client.WSRequestBuilder;

import net.seleucus.wsp.config.WSConfiguration;
import net.seleucus.wsp.db.WSDatabase;
import net.seleucus.wsp.main.WSVersion;
import net.seleucus.wsp.util.WSConstants;
import net.seleucus.wsp.util.WSUtil;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WSLogListener extends TailerListenerAdapter {

    final static Logger LOGGER = LoggerFactory.getLogger(WSLogListener.class);

    private WSServer myServer;
    private WSDatabase myDatabase;
    private WSConfiguration myConfiguration;

    public WSLogListener(WSServer myServer) {

        this.myServer = myServer;
        this.myDatabase = myServer.getWSDatabase();
        this.myConfiguration = myServer.getWSConfiguration();

    }

    @Override
    public void handle(final String requestLine) {
        long beforeSearchInDBMiliS = 0, afterSearchInDBMiliS = 0, beforeSearchInDBNanoS = 0, afterSearchInDBNanoS;
//        long userRequestRecievedTime = System.currentTimeMillis();
        // Check if the line length is more than 65535 chars
        if (requestLine.length() > Character.MAX_VALUE) {
            return;
        }

        // Check if the regex pattern has been found
        Pattern wsPattern = Pattern.compile(myConfiguration.getLoginRegexForEachRequest());
        Matcher wsMatcher = wsPattern.matcher(requestLine);

        if (!wsMatcher.matches()
                || 2 != wsMatcher.groupCount()) {
            LOGGER.info("Regex Problem?");
            LOGGER.info("Request line is {}.", requestLine);
            LOGGER.info("The regex is {}.", myConfiguration.getLoginRegexForEachRequest());
            return;
        }

        final String ipAddress = wsMatcher.group(1);
        String webSpaRequest = wsMatcher.group(2);
        if (webSpaRequest.endsWith("/")) {
            webSpaRequest = webSpaRequest.substring(0, webSpaRequest.length() - 1);
        }

        if (webSpaRequest.length() == 100) {
            LOGGER.info(" --- The Client chars received are {}.", webSpaRequest);

            beforeSearchInDBMiliS = System.currentTimeMillis();
            beforeSearchInDBNanoS = System.nanoTime();
            final int userID[] = myDatabase.users.getUSIDFromRequest(webSpaRequest);
            afterSearchInDBNanoS = System.nanoTime();
            afterSearchInDBMiliS = System.currentTimeMillis();
            LOGGER.info("Database Check Pass time(nano second): " + String.valueOf(afterSearchInDBNanoS - beforeSearchInDBNanoS));
            LOGGER.info("Database Check Pass time(nano second): " + String.valueOf(afterSearchInDBMiliS - beforeSearchInDBMiliS));
            if (userID[0] != -1) {
//                beforeSendToCheckerTime = System.currentTimeMillis();
                boolean isValidUser = sendRequestToChecker(userID);
//                afterSendToCheckerTime = System.currentTimeMillis();
            }

        } else if (webSpaRequest.length() < 100) {
            LOGGER.info(" --- The Checker chars received are {}.", webSpaRequest);
//            LOGGER.info("\n --- The checker ipAddress is {}.", ipAddress);
            String[] responseItems = processRequest(webSpaRequest);
            int resUsId = Integer.valueOf(responseItems[0]);
            int resPPIndex = Integer.valueOf(responseItems[1]);
            boolean resUserIsValid = Boolean.valueOf(responseItems[2]);
            myDatabase.users.updateWaitingList(resUsId, resPPIndex, resUserIsValid);
            if (resUserIsValid) {// todo amir

                if (resUsId < 0) {
                    LOGGER.info("No User Found");
                } else {

                    String username = myDatabase.users.getUsersFullName(resUsId);
                    LOGGER.info("User Found {}.", username);
                    // Check the user's activation status
                    final boolean userActive = myDatabase.users.getActivationStatus(resUsId);
                    LOGGER.info(myDatabase.users.getActivationStatusString(resUsId));

                    if (userActive) {

                        final int action = myDatabase.actionsAvailable.getActionNumberFromRequest(resUsId, webSpaRequest);
                        LOGGER.info("Action Number {}.", action);

                        if ((action >= 0) && (action <= 9)) {

                            // Log this in the actions received table...
                            final int aaID = myServer.getWSDatabase().actionsAvailable.getAAID(resUsId, action);
                            myServer.getWSDatabase().actionsReceived.addAction(ipAddress, webSpaRequest, aaID);

                            // Log this on the screen for the user
                            final String osCommand = myServer.getWSDatabase().actionsAvailable.getOSCommand(resUsId, action);
                            LOGGER.info(ipAddress + " ->  '" + osCommand + "'");

                            // Fetch and execute the O/S command...        		
                            myServer.runOSCommand(resUsId, action, ipAddress);

                        }
                    }

                }
            }
        }
        long afterRecievedFromCheckerTime = System.currentTimeMillis();
        
//        LOGGER.info("Checker time(nano second): " + String.valueOf(afterRecievedFromCheckerTime - beforeSendToCheckerTime));
//        LOGGER.info("Total time(nano second): " + String.valueOf(afterRecievedFromCheckerTime - userRequestRecievedTime));
    }

    @Override
    public void handle(Exception arg0) {

    }

    @Override
    public void init(Tailer arg0) {
        // TODO Auto-generated method stub
    }

    public boolean sendRequestToChecker(int userID[]) {

        LOGGER.info("");
        LOGGER.info("WebSpa - Single HTTP/S Request Authorisation");
        String checkerURL = WSUtil.readURL();//"http://192.168.1.70";                    //configProperties.getProperty(WSConstants.CHECKER_IP);//"http://10.20.205.248";//readLineRequired("Host [e.g. https://localhost/]");
        CharSequence usId = String.valueOf(userID[0]);          //readPasswordRequired("Your pass-phrase for that host");
        int ppId = userID[1];                                   //readLineRequiredInt("The action number", 0, 9);
        String newKnock = checkerURL + "/usId=" + usId + "?ppid=" + ppId + "/";
        WSConnection myConnection = new WSConnection(newKnock);
        LOGGER.info(myConnection.getActionToBeTaken());
        myConnection.sendRequest();

        // is the connection HTTPS
        if (myConnection.isHttps()) {
            // TODO add known hosts check and handling here
            // get fingerprint and algorithm from certificate
            // myConnection.getCertificateAlgorithm()
            // myConnection.getCertificateFingerprint()

            // get fingerprint from known hosts file
            // WSKnownHosts.getFingerprint(host-ip, algorithm)
            // if a fingerprint is found compare fingerprints, if not equal warn and exit
            // else ask to store new fingerprint to known hosts
            // WSKnownHosts.store...(host-ip, algorithm, fingerprint);
            try {

                LOGGER.info(myConnection.getCertSHA1Hash());

            } catch (NullPointerException npEx) {

                LOGGER.info("Couldn't get the SHA1 hash of the server certificate - probably a self signed certificate.");

                if (!WSUtil.hasMinJreRequirements(1, 7)) {
                    LOGGER.error("Be sure to run WebSpa with a JRE 1.7 or greater.");
                } else {
                    LOGGER.error("An exception was raised when reading the server certificate.");
                    npEx.printStackTrace();
                }
            }

//                final String trustChoice = readLineOptional("Continue connecting [Y/n]");
//
//                if (WSUtil.isAnswerPositive(trustChoice) || sendChoice.isEmpty()) {
            myConnection.sendRequest();
            LOGGER.info(myConnection.responseMessage());
            LOGGER.info("HTTPS Response Code: {}", myConnection.responseCode());
            myDatabase.users.addToWaitingList(userID[0], ppId);

        } else {
            LOGGER.info("---server send request");
            myConnection.sendRequest();

            LOGGER.info("--- response message: " + myConnection.responseMessage());
            LOGGER.info("--- HTTP Response Code: {}", myConnection.responseCode());
//            LOGGER.info("---server send request after");
            myDatabase.users.addToWaitingList(userID[0], ppId);

        }
        return false;
    }

    private String[] processRequest(String webSpaRequest) {
        String result[] = new String[3];
        int startIndex = "usid=".length();
        result[0] = webSpaRequest.substring(startIndex, webSpaRequest.indexOf("?"));
        startIndex = startIndex + result[0].length() + "?ppid=".length();
        result[1] = webSpaRequest.substring(startIndex, webSpaRequest.indexOf("?", startIndex));
        startIndex = startIndex + result[1].length() + "?isvalid=".length();
        result[2] = webSpaRequest.substring(startIndex);
//        System.out.println("---server--- usid=" + result[0] + " ?ppid=" + result[1] + " ?isvalid=" + result[2]);
        return result;

    }

}
