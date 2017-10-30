/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.seleucus.wsp.checker;

/**
 *
 * @author masoud
 */
import java.util.ArrayList;
import net.seleucus.wsp.server.WSServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.seleucus.wsp.server.commands.WSActionAdd;
import net.seleucus.wsp.server.commands.WSActionShow;
import net.seleucus.wsp.server.commands.WSCommandOption;
import net.seleucus.wsp.server.commands.WSConfigShow;
import net.seleucus.wsp.server.commands.WSHelpOptions;
import net.seleucus.wsp.server.commands.WSPassPhraseModify;
import net.seleucus.wsp.server.commands.WSPassPhraseShow;
import net.seleucus.wsp.server.commands.WSServiceStart;
import net.seleucus.wsp.server.commands.WSServiceStatus;
import net.seleucus.wsp.server.commands.WSServiceStop;
import net.seleucus.wsp.server.commands.WSUserActivate;
import net.seleucus.wsp.server.commands.WSUserAdd;
import net.seleucus.wsp.server.commands.WSUserShow;

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
