/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.seleucus.wsp.checker.commands;

/**
 *
 * @author masoud
 */
import net.seleucus.wsp.checker.WSChecker;

public class WSServiceStop extends net.seleucus.wsp.checker.commands.WSCommandOption {

	public WSServiceStop(WSChecker myServer) {
		
		super(myServer);
		
	}

	@Override
	protected void execute() {
		
		myServer.serverStop();

	} // execute method

	@Override
	public boolean handle(String cmd) {

		boolean validCommand = false;

		if(isValid(cmd)) {
			validCommand = true;
			this.execute();
		}
		
		return validCommand;
		
	} // handle method

	@Override
	protected boolean isValid(String cmd) {
		
		boolean valid = false;
		
		if(cmd.equalsIgnoreCase("service stop")) {
			
			valid = true;
		
		}
		
		return valid;
		
	}  // isValid method

}

