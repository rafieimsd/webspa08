package net.seleucus.wsp.checker;

/**
 *
 * @author masoud
 */
import java.util.ArrayList;
import net.seleucus.wsp.checker.WSChecker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.seleucus.wsp.checker.commands.WSCommandOption;

import net.seleucus.wsp.checker.commands.WSServiceStart;
import net.seleucus.wsp.checker.commands.WSServiceStatus;
import net.seleucus.wsp.checker.commands.WSServiceStop;

public class WSCheckerConsole {

    private final static Logger LOGGER = LoggerFactory.getLogger(WSCheckerConsole.class);

    protected static final String UNKNOWN_CMD_MESSAGE
            = "Unknown Command - Type \"help\" for more options";

    private WSChecker myServer;
    private ArrayList<WSCommandOption> commands;

    protected WSCheckerConsole(WSChecker myServer) {

        this.commands = new ArrayList<WSCommandOption>();
        this.myServer = myServer;

//        this.commands.add(new WSActionAdd(this.myServer));
//        this.commands.add(new WSActionShow(this.myServer));
//        this.commands.add(new WSConfigShow(this.myServer));
//        this.commands.add(new WSHelpOptions(this.myServer));
//        this.commands.add(new WSPassPhraseShow(this.myServer));
//        this.commands.add(new WSPassPhraseModify(this.myServer));
        this.commands.add(new WSServiceStart(this.myServer));
        this.commands.add(new WSServiceStatus(this.myServer));
        this.commands.add(new WSServiceStop(this.myServer));
//        this.commands.add(new WSUserActivate(this.myServer));
//        this.commands.add(new WSUserAdd(this.myServer));
//        this.commands.add(new WSUserShow(this.myServer));

    }

    public void executeCommand(final String command) {

        boolean commandFound = false;

        for (WSCommandOption action : commands) {

            if (action.handle(command)) {
                commandFound = true;
                return;
            }
        }

        if ((commandFound == false) && (command.isEmpty() == false)) {

            LOGGER.info(UNKNOWN_CMD_MESSAGE);

        }

    }

}
