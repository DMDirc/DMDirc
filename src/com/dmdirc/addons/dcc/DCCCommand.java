/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.Main;
import com.dmdirc.Server;
import com.dmdirc.parser.IRCParser;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.commands.GlobalCommand;
import com.dmdirc.ui.interfaces.InputWindow;

import com.dmdirc.actions.ActionManager;
import com.dmdirc.addons.dcc.actions.DCCActions;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

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
	 *
	 * @param plugin The DCC Plugin that this command belongs to
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
	 * @param isSilent Whether this command is silenced or not
	 * @param args The user supplied arguments
	 */
	@Override
	public void execute(final InputWindow origin, final boolean isSilent, final String... args) {
		if (args.length > 1) {
			final String type = args[0];
			final String target = args[1];
			if (type.equalsIgnoreCase("chat")) {
				final Server server = origin.getContainer().getServer();
				final IRCParser parser = server.getParser();
				final String myNickname = parser.getMyNickname();
				final DCCChat chat = new DCCChat();
				chat.listen();
				final DCCChatWindow window = new DCCChatWindow(myPlugin, chat, "*Chat: "+target, myNickname, target);
				
				parser.sendCTCP(target, "DCC", "CHAT chat "+DCC.ipToLong(DCCPlugin.getListenIP(parser))+" "+chat.getPort());
				
				ActionManager.processEvent(DCCActions.DCC_CHAT_REQUEST_SENT, null, server, target);
				
				sendLine(origin, isSilent, "DCCChatStarting", target, chat.getHost(), chat.getPort());
				window.getFrame().addLine("DCCChatStarting", target, chat.getHost(), chat.getPort());
			} else if (type.equalsIgnoreCase("send")) {
				sendFile(target, origin, isSilent);
			} else {
				sendLine(origin, isSilent, FORMAT_ERROR, "Unknown DCC Type: '"+type+"'");
			}
		} else {
			sendLine(origin, isSilent, FORMAT_ERROR, "Syntax: dcc <type> <target> [params]");
		}
	}

	/**
	 * Ask for the file to send, then start the send.
	 *
	 * @param target Person this dcc is to.
	 * @param origin The InputWindow this command was issued on
	 * @param isSilent Whether this command is silenced or not
	 */
	public void sendFile(final String target, final InputWindow origin, final boolean isSilent) {
		// New thread to ask the user what file to senfd
		final Thread dccThread = new Thread(new Runnable() {
			/** {@inheritDoc} */
			@Override
			public void run() {
				final JFileChooser jc = new JFileChooser();
				jc.setDialogTitle("Send file to "+target+" - DMDirc ");
				jc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				jc.setMultiSelectionEnabled(false);
				int result = jc.showOpenDialog((JFrame)Main.getUI().getMainWindow());
				if (result == JFileChooser.APPROVE_OPTION) {
					final IRCParser parser = origin.getContainer().getServer().getParser();
					final String myNickname = parser.getMyNickname();
					DCCSend send = new DCCSend();
					send.setTurbo(IdentityManager.getGlobalConfig().getOptionBool(DCCPlugin.getDomain(), "send.forceturbo", false));
					send.setType(DCCSend.TransferType.SEND);
					
					sendLine(origin, isSilent, FORMAT_OUTPUT, "Starting DCC Send with: "+target);
				
					send.setFileName(jc.getSelectedFile().getAbsolutePath());
					send.setFileSize(jc.getSelectedFile().length());
					
					if (IdentityManager.getGlobalConfig().getOptionBool(DCCPlugin.getDomain(), "send.reverse", false)) {
						new DCCSendWindow(myPlugin, send, "*Send: "+target, myNickname, target);
						parser.sendCTCP(target, "DCC", "SEND \""+jc.getSelectedFile().getName()+"\" "+DCC.ipToLong(DCCPlugin.getListenIP(parser))+" 0 "+send.getFileSize()+" "+send.makeToken()+((send.isTurbo()) ? "T" : ""));
					} else {
						new DCCSendWindow(myPlugin, send, "Send: "+target, myNickname, target);
						send.listen();
						parser.sendCTCP(target, "DCC", "SEND \""+jc.getSelectedFile().getName()+"\" "+DCC.ipToLong(DCCPlugin.getListenIP(parser))+" "+send.getPort()+" "+send.getFileSize()+((send.isTurbo()) ? " T" : ""));
					}
				}
			}
		}, "openFileThread");
		// Start the thread
		dccThread.start();
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

}

