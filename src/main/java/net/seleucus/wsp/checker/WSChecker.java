package net.seleucus.wsp.checker;

/**
 *
 * @author masoud
 */
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import net.seleucus.wsp.crypto.ActionNumberCrypto;
import net.seleucus.wsp.crypto.PassPhraseCrypto;
import net.seleucus.wsp.main.WSGestalt;
import net.seleucus.wsp.main.WSVersion;
import net.seleucus.wsp.main.WebSpa;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ArrayUtils;
import java.util.Random;
import net.seleucus.wsp.client.WSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WSChecker extends WSGestalt {

    private final static Logger LOGGER = LoggerFactory.getLogger(WSClient.class);

    public WSChecker(final WebSpa myWebSpa) {
        super(myWebSpa);
    }

    @Override
    public void exitConsole() {
        LOGGER.info("Goodbye!\n");
    }

    @Override
    public void runConsole() {

        LOGGER.info("");
        LOGGER.info("WebSpa - Single HTTP/S Request Authorisation- ");
//		println("version " + WSVersion.getValue() + " (webspa@seleucus.net)"); 		
        LOGGER.info("-this is checker mode!");
        LOGGER.info("test scipts: ");
        LOGGER.info("Enter the number of password you want: ");

        int counter = readLineRequiredInt("Enter the number of passwords you want:", 1, 20);
        int lenght = readLineRequiredInt("Enter the lenght of passwords you want:", 6, 12);
        printPassphraseSet(generatePassphraseSet(counter, lenght));

//        CharSequence password = readPasswordRequired("Your pass-phrase for that host");
//        println("password is:" + password);
//        println("allbytes:"+getKnock(password, 8));
//        byte[] passKnockBytes = PassPhraseCrypto.getHashedPassPhraseNow(password);
//        println("encoded password is:" + passKnockBytes);
        LOGGER.info("test scipts end. ");

    }

//    public String getKnock(CharSequence passPhrase, int actionNumber) {
//        byte[] passKnockBytes = PassPhraseCrypto.getHashedPassPhraseNow(passPhrase);
//        byte[] actionKnockBytes = ActionNumberCrypto.getHashedActionNumberNow(passPhrase, actionNumber);
//
//        byte[] allBytes = ArrayUtils.addAll(passKnockBytes, actionKnockBytes);
//
//        return Base64.encodeBase64URLSafeString(allBytes);
//    }
    private String[] generatePassphraseSet(int counter, int lenght) {

        String passPhraseSet[] = new String[counter];
        for (int i = 0; i < counter; i++) {
            String tempPass = "";
            char tempChar = 'x';
            for (int j = 0; j < lenght; j++) {
//                System.out.println("jj: "+j+" tempPass "+tempPass);
                int tempInt = (Character.getNumericValue(tempChar)==0 ? i+1 : Character.getNumericValue(tempChar));
                tempInt=((tempInt%4)==0 ? (tempInt%4)+1 : tempInt%4 );
//                System.out.println("tempInt: "+tempInt+" i+j: "+(i+j)+"%%%% "+((j+i) % tempInt));
                tempChar = ( ((j+i) % tempInt) == 0 ? getCharSeed() : getIntSeed());
                tempPass += tempChar;
//                System.out.println("tempPass: " + tempPass);

            }
            passPhraseSet[i] = tempPass;
            System.out.println("tempPass" + (i + 1) + ": " + tempPass);

        }

        return passPhraseSet;
    }

    private void printPassphraseSet(String[] passPhraseSet) {

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
