package net.seleucus.wsp.server;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.seleucus.wsp.client.WSConnection;
import net.seleucus.wsp.client.WSRequestBuilder;

import net.seleucus.wsp.config.WSConfiguration;
import net.seleucus.wsp.db.WSDatabase;
import net.seleucus.wsp.main.WSVersion;
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

            // Nest the world away!
            LOGGER.info("The 100 chars received are {}.", webSpaRequest);
            // Get the unique user ID from the request
//            final int ppID = myDatabase.passPhrases.getPPIDFromRequest(webSpaRequest);
            final int userID[] = myDatabase.users.getUSIDFromRequest(webSpaRequest);
            //TODO amir connect to checker for check password index
            boolean isValidUser = checker(userID);
            if (userID[0] < 0) {

                LOGGER.info("No User Found");

            } else {

                String username = myDatabase.users.getUsersFullName(userID[0]);
                LOGGER.info("User Found {}.", username);
                // Check the user's activation status
                final boolean userActive = myDatabase.users.getActivationStatus(userID[0]);
                LOGGER.info(myDatabase.users.getActivationStatusString(userID[0]));

                if (userActive) {

                    final int action = myDatabase.actionsAvailable.getActionNumberFromRequest(userID[0], webSpaRequest);
                    LOGGER.info("Action Number {}.", action);

                    if ((action >= 0) && (action <= 9)) {

                        // Log this in the actions received table...
                        final int aaID = myServer.getWSDatabase().actionsAvailable.getAAID(userID[0], action);
                        myServer.getWSDatabase().actionsReceived.addAction(ipAddress, webSpaRequest, aaID);

                        // Log this on the screen for the user
                        final String osCommand = myServer.getWSDatabase().actionsAvailable.getOSCommand(userID[0], action);
                        LOGGER.info(ipAddress + " ->  '" + osCommand + "'");

                        // Fetch and execute the O/S command...        		
                        myServer.runOSCommand(userID[0], action, ipAddress);

                    }
                }

            }

        }

    }

    @Override
    public void handle(Exception arg0) {

    }

    @Override
    public void init(Tailer arg0) {
        // TODO Auto-generated method stub
    }

    public boolean checker(int userID[]) {

        LOGGER.info("");
        LOGGER.info("WebSpa - Single HTTP/S Request Authorisation");
//        LOGGER.info("version " + WSVersion.getValue() + " (webspa@seleucus.net)");
//        LOGGER.info("");

        String host = "http://10.20.205.248";//readLineRequired("Host [e.g. https://localhost/]");
        CharSequence usId = String.valueOf(userID[0]);//readPasswordRequired("Your pass-phrase for that host");
        int ppId = userID[1];//readLineRequiredInt("The action number", 0, 9);

        WSRequestBuilder myClient = new WSRequestBuilder(host, usId, ppId);
        String knock = myClient.getKnock();

//        LOGGER.info("Your WebSpa Knock is: {}", knock);

        // URL nonsense 
//        final String sendChoice = readLineOptional("Send the above URL [Y/n]");


            WSConnection myConnection = new WSConnection(knock);

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

//                } else {
//
//                    LOGGER.info("Nothing was sent.");
//
//                }

            } else {

                myConnection.sendRequest();
                LOGGER.info(myConnection.responseMessage());
                LOGGER.info("HTTP Response Code: {}", myConnection.responseCode());

            }

        
        return false;
    }
}
