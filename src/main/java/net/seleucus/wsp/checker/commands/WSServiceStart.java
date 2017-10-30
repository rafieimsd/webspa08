package net.seleucus.wsp.checker.commands;

/**
 *
 * @author masoud
 */
import net.seleucus.wsp.checker.WSChecker;

public class WSServiceStart extends net.seleucus.wsp.checker.commands.WSCommandOption {

    public WSServiceStart(WSChecker myChecker) {

        super(myChecker);

    }

    @Override
    protected void execute() {

        myServer.serverStart();

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

        if (cmd.equalsIgnoreCase("service start")) {

            valid = true;

        }

        return valid;

    }  // isValid method

}
