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

public abstract class WSCommandOption {

	protected WSChecker myServer;

	public WSCommandOption(WSChecker myServer) {
		this.myServer = myServer;
	}
	
	protected abstract void execute();
	public abstract boolean handle(final String cmd);
	protected abstract boolean isValid(final String cmd);

}

