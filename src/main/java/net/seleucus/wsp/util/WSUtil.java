package net.seleucus.wsp.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class WSUtil {

    private static Properties configProperties;

    /**
     * Private constructor to avoid instantiation of this class.
     */
    private WSUtil() {

    }

    /**
     * Checks if the major and minor version for a JRE is satisfied. Returns
     * true if the minimum requirement are fulfilled, false otherwise.
     *
     * @param majorVersion
     * @param minorVersion
     * @return
     */
    public static boolean hasMinJreRequirements(final int majorVersion,
            final int minorVersion) {

        String javaSpecVersion[] = System.getProperty(
                "java.specification.version").split("\\.");

        if (Integer.valueOf(javaSpecVersion[0]) >= majorVersion
                && Integer.valueOf(javaSpecVersion[1]) >= minorVersion) {
            return true;
        }

        return false;
    }

    public static boolean isAnswerPositive(final String answer) {
        return "yes".equalsIgnoreCase(answer) || "y".equalsIgnoreCase(answer);
    }

    public static String readURL() {
        System.out.println("--1.2--");
        String fileName=configProperties.getProperty(WSConstants.FILENAME);
        System.out.println("--1.7--"+fileName);
        String ip = readFile(fileName);
        System.out.println("--1.3--" + ip);
        String URL = "http://" + ip;
        return URL;
    }

    public static String readFile(final String FILENAME) {
        String sCurrentLine = "";

        try {
            System.out.println("--1.2.1--" + FILENAME);
            BufferedReader br = new BufferedReader(new FileReader(FILENAME));
            sCurrentLine = br.readLine();
            if (sCurrentLine.length() > 15 || sCurrentLine.length() < 7) {
                throw new IOException();
            }
//            while ((sCurrentLine = br.readLine()) != null) {
//                System.out.println("----- test reading from file: " + sCurrentLine);
//            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sCurrentLine;
    }
}
