/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.dmdirc.addons.dcc;

import com.dmdirc.parser.IRCParser;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.commands.GlobalCommand;
import com.dmdirc.ui.interfaces.InputWindow;

/**
 * This command allows starting dcc chats/file transfers
 *
 * @author Shane "Dataforce" Mc Cormack
 * @version $Id: DCCCommand.java 969 2007-04-30 18:38:20Z ShaneMcC $
 */
public final class DCCCommand extends GlobalCommand {
	/** My Plugin */
	final DCCPlugin myPlugin;

	/**
	 * Creates a new instance of DCCCommand.
	 */
	public DCCCommand(final DCCPlugin plugin) {
		super();
		myPlugin = plugin;
		CommandManager.registerCommand(this);
	}
		
	/**
	 * Executes this command.
	 *
	 * @param origin The frame in which this command was issued
	 * @param server The server object that this command is associated with
	 * @param isSilent Whether this command is silenced or not
	 * @param args The user supplied arguments
	 */
	@Override
	public void execute(final InputWindow origin, final boolean isSilent, final String... args) {
		if (args.length > 1) {
			final String type = args[0];
			final String target = args[1];
			if (type.equalsIgnoreCase("chat")) {
				final IRCParser parser = origin.getContainer().getServer().getParser();
				final String myNickname = parser.getMyNickname();
				DCCChat chat = new DCCChat();
				chat.listen();
				DCCChatWindow window = new DCCChatWindow(myPlugin, chat, "*Chat: "+target, myNickname, target);
				parser.sendCTCP(target, "DCC", "CHAT chat "+DCC.ipToLong(chat.getHost())+" "+chat.getPort());
				sendLine(origin, isSilent, FORMAT_OUTPUT, "Starting DCC Chat with: "+target+" on "+chat.getHost()+":"+chat.getPort());
				window.getFrame().addLine(FORMAT_OUTPUT, "Starting DCC Chat with: "+target+" on "+chat.getHost()+":"+chat.getPort());
			} else {
				sendLine(origin, isSilent, FORMAT_ERROR, "Unknown DCC Type: '"+type+"'");
			}
		} else {
			sendLine(origin, isSilent, FORMAT_ERROR, "Syntax: dcc <type> <target> [params]");
		}
	}

	/**
	 * Returns this command's name.
	 *
	 * @return The name of this command
	 */
	@Override
	public String getName() { return "dcc"; }
	
	/**
	 * Returns whether or not this command should be shown in help messages.
	 *
	 * @return True iff the command should be shown, false otherwise
	 */
	@Override
	public boolean showInHelp() { return true; }
	
	/**
	 * Returns a string representing the help message for this command.
	 *
	 * @return the help message for this command
	 */
	@Override
	public String getHelp() { return "dcc - Allows DCC"; }
	
	/**
	 * Get SVN Version information.
	 *
	 * @return SVN Version String
	 */
	public static String getSvnInfo() { return "$Id: DCCCommand.java 969 2007-04-30 18:38:20Z ShaneMcC $"; }	
}

