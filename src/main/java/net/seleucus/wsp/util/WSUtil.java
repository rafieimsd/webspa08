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
//        System.out.println("--1.2--");
        String fileName = "/webspa/var.txt";//configProperties.getProperty(WSConstants.FILENAME);
        String ip = readIp(fileName);
        String URL = "http://" + ip;
//        System.out.println("--read IP-4--" + URL);

        return URL;
    }

    public static String readIp(final String FILENAME) {
        String sCurrentLine = "", ip = "";
        boolean ipDetected = false;
//        System.out.println("--read IP--");
        int co = 1;
        try {
            BufferedReader br = new BufferedReader(new FileReader(FILENAME));
//            sCurrentLine = br.readLine();
            while ((sCurrentLine = br.readLine()) != null && !ipDetected) {
                if (sCurrentLine.startsWith("ip:")) {
                    if (sCurrentLine.length() > 18 || sCurrentLine.length() < 10) {
//                        System.out.println("--read IP-lenght- " + sCurrentLine.length());
                        throw new IOException();
                    } else {
                        ip = sCurrentLine.substring(3);
                        ipDetected = true;
                    }
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ip;
    }

    public static String readUserIndex(String usId) {
        String sCurrentLine = "",result="";
        boolean idDetected = false;
        String fileName = "/webspa/var.txt";
        System.out.println("--check pass--");
        try {
//            System.out.println("--1.2.1--" + FILENAME);
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            while ((sCurrentLine = br.readLine()) != null && !idDetected) {
//                System.out.println("----- test reading from file: " + sCurrentLine);
                if (sCurrentLine.startsWith("user:")) {

                    if (usId.equals(sCurrentLine.substring(5, sCurrentLine.indexOf(",")))) {
                        result=sCurrentLine.substring(sCurrentLine.indexOf(",")+1);
                        idDetected = true;
                    }
                }
            }
            if (!idDetected) {
                throw new SecurityException();
            }
            br.close();
        } catch (IOException e) {

            e.printStackTrace();
        } catch (SecurityException e) {
            System.out.println("--ERROR!! user " + usId + " not found in file!");
        }
        return result;
    }
}
