package net.seleucus.wsp.db;

import java.nio.CharBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import net.seleucus.wsp.crypto.WebSpaEncoder;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WSUsers {

    private final static Logger LOGGER = LoggerFactory.getLogger(WSUsers.class);

    private Connection wsConnection;

    protected WSUsers(Connection wsConnection) {

        this.wsConnection = wsConnection;

    }

    public synchronized void addUser(String fullName, String[] passSeq, String eMail,
            String phone) {

        String sqlPassPhrase = "INSERT INTO PASSPHRASES (USID,PASSPHRASE, CREATED) VALUES ("
                + "SELECT USID FROM PUBLIC.USERS WHERE FULLNAME = ?,?, CURRENT_TIMESTAMP);";

        String sqlUsers = "INSERT INTO PUBLIC.USERS ( FULLNAME, EMAIL, PHONE, CREATED, MODIFIED) VALUES "
                + "( ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);";

        LOGGER.info("Adding user {} to the database...", fullName);

        try {

            PreparedStatement psUsers = wsConnection.prepareStatement(sqlUsers);
//            psUsers.setString(1, passSeq.toString());
            psUsers.setString(1, fullName);
            psUsers.setString(2, eMail);
            psUsers.setString(3, phone);
            psUsers.executeUpdate();

            psUsers.close();

            PreparedStatement psPassPhrase = null;
            for (int i = 0; i < passSeq.length; i++) {
                psPassPhrase = wsConnection.prepareStatement(sqlPassPhrase);
                psPassPhrase.setString(1, fullName);
                psPassPhrase.setString(2, passSeq[i]);

                psPassPhrase.executeUpdate();
                psPassPhrase.close();
            }
            LOGGER.info("User {} added.", fullName);

        } catch (SQLException ex) {

            LOGGER.error("User Add - A Database error occured: {}", ex.getMessage());

        }

    }

    public synchronized void addUserOld(String fullName, CharSequence passSeq, String eMail,
            String phone) {

        String sqlPassPhrase = "INSERT INTO PASSPHRASES (PASSPHRASE, CREATED) VALUES (?, CURRENT_TIMESTAMP);";

        String sqlUsers = "INSERT INTO PUBLIC.USERS (PPID, FULLNAME, EMAIL, PHONE, CREATED, MODIFIED) VALUES "
                + "(SELECT PPID FROM PUBLIC.PASSPHRASES WHERE PASSPHRASE = ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);";

        LOGGER.info("Adding user {} to the database...", fullName);

        try {

            PreparedStatement psPassPhrase = wsConnection.prepareStatement(sqlPassPhrase);
            psPassPhrase.setString(1, passSeq.toString());
            psPassPhrase.executeUpdate();

            psPassPhrase.close();

            PreparedStatement psUsers = wsConnection.prepareStatement(sqlUsers);
            psUsers.setString(1, passSeq.toString());
            psUsers.setString(2, fullName);
            psUsers.setString(3, eMail);
            psUsers.setString(4, phone);
            psUsers.executeUpdate();

            psUsers.close();

            LOGGER.info("User {} added.", fullName);

        } catch (SQLException ex) {

            LOGGER.error("User Add - A Database error occured: {}", ex.getMessage());

        }

    }

    public synchronized String getUsersFullName(int usID) {

        String fullName = "Invalid User";

        if (usID > 0) {

            String sqlActivationLookup = "SELECT FULLNAME FROM USERS WHERE USID = ? ;";
            try {
                PreparedStatement psPassPhrase = wsConnection.prepareStatement(sqlActivationLookup);
                psPassPhrase.setInt(1, usID);
                ResultSet rs = psPassPhrase.executeQuery();

                if (rs.next()) {
                    fullName = rs.getString(1);
                }

                rs.close();
                psPassPhrase.close();

            } catch (SQLException ex) {

                LOGGER.error("User Full Name - A Database error occured: {}", ex.getMessage());

            }
        }

        return fullName;
    }

    public synchronized String showUsers() {

        StringBuffer resultsBuffer = new StringBuffer();
        resultsBuffer.append('\n');
        resultsBuffer.append("Users:");
        resultsBuffer.append('\n');
        resultsBuffer.append("___________________________________________________________");
        resultsBuffer.append('\n');
        resultsBuffer.append(StringUtils.rightPad("ID", 4));
        resultsBuffer.append(StringUtils.rightPad("Active", 8));
        resultsBuffer.append(StringUtils.rightPad("Full Name", 24));
        resultsBuffer.append(StringUtils.rightPad("Last Modified", 25));
        resultsBuffer.append('\n');
        resultsBuffer.append("-----------------------------------------------------------");
        resultsBuffer.append('\n');

        final String sqlPassUsers = "SELECT USID,active, FULLNAME, MODIFIED FROM  USERS ;";

        try {

            Statement stmt = wsConnection.createStatement();
            ResultSet rs = stmt.executeQuery(sqlPassUsers);

            while (rs.next()) {
                resultsBuffer.append(StringUtils.rightPad(rs.getString(1), 4));
                resultsBuffer.append(StringUtils.rightPad(rs.getString(2), 8));
                resultsBuffer.append(StringUtils.rightPad(StringUtils.abbreviate(rs.getString(3), 23), 24));
                resultsBuffer.append(rs.getString(4).substring(0, 23));
                resultsBuffer.append('\n');
            }
            resultsBuffer.append("___________________________________________________________");
            resultsBuffer.append('\n');

            rs.close();
            stmt.close();

        } catch (SQLException ex) {

            LOGGER.error("User Show - A Database error occured: {}", ex.getMessage());

        }

        return resultsBuffer.toString();
    }

    public synchronized int[] getUSIDFromRequest(final String webSpaRequest) {

        int[] output = {-1, -1};
        final String sqlPassPhrases = "SELECT PASSPHRASE, USID,PPID FROM PASSPHRASES;";

        try {
            boolean recordFound = false;
            Statement stmt = wsConnection.createStatement();
            ResultSet rs = stmt.executeQuery(sqlPassPhrases);

            while (rs.next()) {

                char[] dbPassPhraseArray = rs.getString(1).toCharArray();
                final int dbUSID = rs.getInt(2);
                final int dbPPID = rs.getInt(3);
                CharSequence rawPassword = CharBuffer.wrap(dbPassPhraseArray);
                if (WebSpaEncoder.matches(rawPassword, webSpaRequest)) {
                    recordFound = true;
                    output[0] = dbUSID;
                    output[1] = dbPPID;
                    break;

                }

            }	// while loop...
            if (recordFound) {
                LOGGER.error("---- passphrase is correct " + output[1]);
            } else {
                LOGGER.error("---- passphrase is incorrect!!! ");

            }
            rs.close();
            stmt.close();

        } catch (SQLException ex) {

            LOGGER.error("Get USID,PPID From Request - A Database exception has occured: {}.", ex.getMessage());

        }

        return output;

    }

    public synchronized boolean getActivationStatus(int usID) {

        boolean activationStatus = false;

        if (usID > 0) {

            String sqlActivationLookup = "SELECT ACTIVE FROM USERS WHERE USID = ? ;";
            try {
                PreparedStatement psPassPhrase = wsConnection.prepareStatement(sqlActivationLookup);
                psPassPhrase.setInt(1, usID);
                ResultSet rs = psPassPhrase.executeQuery();

                if (rs.next()) {
                    activationStatus = rs.getBoolean(1);
                }

                rs.close();
                psPassPhrase.close();

            } catch (SQLException ex) {

                LOGGER.error("Get Activation Status - A Database exception has occured: {}.", ex.getMessage());

            }

        } // ppID > 0

        return activationStatus;
    }

    public synchronized String getActivationStatusString(final int usID) {

        StringBuilder outputStatusBuffer = new StringBuilder(Byte.MAX_VALUE);
        outputStatusBuffer.append("User with ID: ");
        outputStatusBuffer.append(usID);
        outputStatusBuffer.append(' ');

        String sqlActivationLookup = "SELECT ACTIVE FROM USERS WHERE USID = ? ;";

        PreparedStatement psPassPhrase;
        try {
            psPassPhrase = wsConnection.prepareStatement(sqlActivationLookup);
            psPassPhrase.setInt(1, usID);
            ResultSet rs = psPassPhrase.executeQuery();

            if (rs.next()) {

                boolean activationStatus = rs.getBoolean(1);

                if (activationStatus == true) {

                    outputStatusBuffer.append("is active");

                } else {

                    outputStatusBuffer.append("is in-active");

                }

            } else {

                outputStatusBuffer.append("does not exist");

            }

            rs.close();
            psPassPhrase.close();

        } catch (SQLException ex) {

            LOGGER.error("Get Activation Status String - A Database exception has occured: {}.", ex.getMessage());

        }

        return outputStatusBuffer.toString();

    }

    public boolean addToWaitingList(int usId, int ppId) {
        boolean result = false;
        try {
            String sql = "INSERT INTO USERS_VALIDATION_QUEUE (USID,P_INDEX,IS_VALID,IS_WAITING, CREATED) VALUES ("
                    + "?,?,?,?, CURRENT_TIMESTAMP);";

//            LOGGER.info("Adding RECORD {} to the INTO USERS_VALIDATION_QUEUE...", usId);
            LOGGER.info(".");
            PreparedStatement ps = wsConnection.prepareStatement(sql);
//            psUsers.setString(1, passSeq.toString());
            ps.setString(1, String.valueOf(usId));
            ps.setString(2, String.valueOf(ppId));
            ps.setString(3, String.valueOf(false));
            ps.setString(4, String.valueOf(true));
            ps.executeUpdate();

            ps.close();

            LOGGER.info("record {} added.", usId);
            LOGGER.info("..");
            result = true;
        } catch (Exception ee) {
            result = false;
        }
        return result;
    }

    public boolean updateWaitingList(int usId, int ppId, boolean isValid) {
        boolean result = false;
        try {
            String sql = "update  USERS_VALIDATION_QUEUE set IS_VALID=?,IS_WAITING=?,MODIFIED=CURRENT_TIMESTAMP "
                    + " WHERE USID=? AND P_INDEX=? AND IS_WAITING=TRUE;";

//            LOGGER.info("UPDATING RECORD {} OF the INTO USERS_VALIDATION_QUEUE...", usId);
            LOGGER.info(".");
            PreparedStatement ps = wsConnection.prepareStatement(sql);
//            psUsers.setString(1, passSeq.toString());
            ps.setString(1, String.valueOf(isValid));
            ps.setString(2, String.valueOf(false));
            ps.setString(3, String.valueOf(usId));
            ps.setString(4, String.valueOf(ppId));
            ps.executeUpdate();

            ps.close();

            LOGGER.info("user {} result:{}", usId, isValid);
            result = true;
        } catch (Exception ee) {
            ee.printStackTrace();
            result = false;
        }
        return result;
    }
}
