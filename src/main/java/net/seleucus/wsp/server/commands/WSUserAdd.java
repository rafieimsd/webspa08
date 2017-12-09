package net.seleucus.wsp.server.commands;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.Random;
import net.seleucus.wsp.console.WSConsole;
import net.seleucus.wsp.main.WSGestalt;
import net.seleucus.wsp.main.WebSpa;
import net.seleucus.wsp.server.WSServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WSUserAdd extends WSCommandOption {

    public WSUserAdd(WSServer myServer) {
        super(myServer);
    }

    @Override
    protected void execute() {

        String fullName = myServer.readLineRequired("Enter the New User's Full Name");

        boolean passPhraseInUse = false;
//        CharSequence passSeq;
        String[] passSeq = null;
        do {

//            passSeq = myServer.readPasswordRequired("Enter the New User's Pass-Phrase");
            int counter = myServer.readLineRequiredInt("Enter the number of passwords you want:", 1, 200000);
            int lenght = myServer.readLineRequiredInt("Enter the lenght of passwords you want:", 4, 12);
            passSeq = generatePassphraseSet(counter, lenght);
//            passPhraseInUse = myServer.getWSDatabase().passPhrases.isPassPhraseInUse(passSeq);

            if (passPhraseInUse == true) {
                myServer.println("This Pass-Phrase is already taken and in use by another user");
                myServer.println("WebSpa pass-phrases have to be unique for each user");
            }

        } while (passPhraseInUse);

        String eMail = myServer.readLineOptional("Please enter the New User's Email Address");
        String phone = myServer.readLineOptional("Please enter the New User's Phone Number");

        myServer.getWSDatabase().users.addUser(fullName, passSeq, eMail, phone);

    } // execute method

    @Override
    public boolean handle(final String cmd) {

        boolean validCommand = false;

        if (isValid(cmd)) {
            validCommand = true;
            this.execute();
        }

        return validCommand;

    } // handle method

    @Override
    protected boolean isValid(final String cmd) {

        boolean valid = false;

        if (cmd.equalsIgnoreCase("user add")) {

            valid = true;

        }

        return valid;

    }  // isValid method

    private String[] generatePassphraseSet(int counter, int lenght) {

        String passPhraseSet[] = new String[counter];
        boolean firstPass = true;
        for (int i = 0; i < counter; i++) {
            String tempPass = "";
            char tempChar = 'x';
            for (int j = 0; j < lenght; j++) {
//                System.out.println("jj: "+j+" tempPass "+tempPass);
                int tempInt = (Character.getNumericValue(tempChar) == 0 ? i + 1 : Character.getNumericValue(tempChar));
                tempInt = ((tempInt % 4) == 0 ? (tempInt % 4) + 1 : tempInt % 4);
//                System.out.println("tempInt: "+tempInt+" i+j: "+(i+j)+"%%%% "+((j+i) % tempInt));
                tempChar = (((j + i) % tempInt) == 0 ? getCharSeed() : getIntSeed());
                tempPass += tempChar;
//                System.out.println("tempPass: " + tempPass);

            }
//            if (i==(counter-2)) {   //todo   amir
//                passPhraseSet[i] = "pass";
//                firstPass = false;
//            } else {
                passPhraseSet[i] = tempPass;
//            }
//            passPhraseSet[i] = tempPass;
            System.out.println("tempPass" + (i + 1) + ": " + tempPass);

        }

        return passPhraseSet;
    }

    private char getCharSeed() {
        Date dateobj = new Date();
//        System.out.println("raw date: " + dateobj.getTime());
//        System.out.println("8 date: " + String.valueOf(dateobj.getTime()).substring(8));
        String temp = String.valueOf(dateobj.getTime());
        String last9Char = temp.substring(temp.length() - 9);
//        System.out.println("last date: " + last9Char);
        int randomValue = Integer.valueOf(last9Char);
        Random rand = new Random();
        randomValue += (rand.nextInt(8888) + 1);
        char seed = (char) (97 + (randomValue % 26));
//        System.out.println("seed: " + seed);

        return seed;
    }

    private char getIntSeed() {
        Date dateobj = new Date();
//        System.out.println("raw date: " + dateobj.getTime());
//        System.out.println("8 date: " + String.valueOf(dateobj.getTime()).substring(8));
        String temp = String.valueOf(dateobj.getTime());
        String lastChar = temp.substring(temp.length() - 1);
//        System.out.println("last date: " + last9Char);
        int seedInt = Integer.valueOf(lastChar);
        Random rand = new Random();
        seedInt += (rand.nextInt(8888) + 1);
//        System.out.println("seed: " + seed);
        String seed = String.valueOf(seedInt);

        return seed.substring(seed.length() - 1).toCharArray()[0];
    }
}
