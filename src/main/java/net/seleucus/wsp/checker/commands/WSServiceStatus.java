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

public class WSServiceStatus extends WSCommandOption {

	public WSServiceStatus(WSChecker myServer) {
		super(myServer);
	}

	@Override
	protected void execute() {

		myServer.serverStatus();
		
	} // execute method

	@Override
	public boolean handle(final String cmd) {

		boolean validCommand = false;

		if(isValid(cmd)) {
			validCommand = true;
			this.execute();
		}
		
		return validCommand;
		
	} // handle method

	@Override
	protected boolean isValid(final String cmd) {
		
		boolean valid = false;
		
		if(cmd.equalsIgnoreCase("service status")) {
			
			valid = true;
		
		}
		
		return valid;
		
	}  // isValid method

}