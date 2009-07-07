/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
import com.dmdirc.actions.ActionManager;
import com.dmdirc.addons.dcc.actions.DCCActions;
import com.dmdirc.addons.dcc.kde.KFileChooser;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.ServerCommand;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.input.TabCompletionType;
import com.dmdirc.ui.interfaces.InputWindow;

import java.io.File;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * This command allows starting dcc chats/file transfers
 *
 * @author Shane "Dataforce" Mc Cormack
 */
public final class DCCCommand extends ServerCommand implements IntelligentCommand {

	/** My Plugin */
	private final DCCPlugin myPlugin;

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
		
	/** {@inheritDoc} */
	@Override
	public void execute(final InputWindow origin, final Server server,
            final boolean isSilent, final CommandArguments args) {
		if (args.getArguments().length > 1) {
			final String type = args.getArguments()[0];
			final String target = args.getArguments()[1];
			final Parser parser = server.getParser();
			final String myNickname = parser.getLocalClient().getNickname();
				
			if (parser.isValidChannelName(target) || parser.getStringConverter().equalsIgnoreCase(target, myNickname)) {
				final Thread errorThread = new Thread(new Runnable() {
					/** {@inheritDoc} */
					@Override
					public void run() {
						if (parser.getStringConverter().equalsIgnoreCase(target, myNickname)) {
							JOptionPane.showMessageDialog(null, "You can't DCC yourself.", "DCC Error", JOptionPane.ERROR_MESSAGE);
						} else {
							JOptionPane.showMessageDialog(null, "You can't DCC a channel.", "DCC Error", JOptionPane.ERROR_MESSAGE);
						}
					}
				});
				errorThread.start();
				return;
			}
			if (type.equalsIgnoreCase("chat")) {
				final DCCChat chat = new DCCChat();
				if (myPlugin.listen(chat)) {
					final DCCChatWindow window = new DCCChatWindow(myPlugin, chat, "*Chat: "+target, myNickname, target);
					
					parser.sendCTCP(target, "DCC", "CHAT chat "+DCC.ipToLong(myPlugin.getListenIP(parser))+" "+chat.getPort());
					
					ActionManager.processEvent(DCCActions.DCC_CHAT_REQUEST_SENT, null, server, target);
					
					sendLine(origin, isSilent, "DCCChatStarting", target, chat.getHost(), chat.getPort());
					window.getFrame().addLine("DCCChatStarting", target, chat.getHost(), chat.getPort());
				} else {
					sendLine(origin, isSilent, "DCCChatError", "Unable to start chat with "+target+" - unable to create listen socket");
				}
			} else if (type.equalsIgnoreCase("send")) {
				sendFile(target, origin, server, isSilent, args.getArgumentsAsString(2));
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
	 * @param server The server instance that this command is being executed on
	 * @param isSilent Whether this command is silenced or not
	 * @param filename The file to send
     * @since 0.6.3m1
	 */
	public void sendFile(final String target, final InputWindow origin, final Server server, final boolean isSilent, final String filename) {
		// New thread to ask the user what file to send
		final File givenFile = new File(filename);
		final Thread dccThread = new Thread(new Runnable() {
			/** {@inheritDoc} */
			@Override
			public void run() {
				final JFileChooser jc = (givenFile.exists()) ? KFileChooser.getFileChooser(myPlugin, givenFile) : KFileChooser.getFileChooser(myPlugin);
				int result;
				if (!givenFile.exists() || !givenFile.isFile() ) {
					jc.setDialogTitle("Send file to "+target+" - DMDirc ");
					jc.setFileSelectionMode(JFileChooser.FILES_ONLY);
					jc.setMultiSelectionEnabled(false);
					result = jc.showOpenDialog((JFrame)Main.getUI().getMainWindow());
				} else {
					jc.setSelectedFile(givenFile);
					result = JFileChooser.APPROVE_OPTION;
				}
				if (result == JFileChooser.APPROVE_OPTION) {
					if (jc.getSelectedFile().length() == 0) {
						JOptionPane.showMessageDialog(null, "You can't send empty files over DCC.", "DCC Error", JOptionPane.ERROR_MESSAGE);
						return;
					} else if (!jc.getSelectedFile().exists()) {
						JOptionPane.showMessageDialog(null, "Invalid file specified", "DCC Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					final Parser parser = server.getParser();
					DCCSend send = new DCCSend(IdentityManager.getGlobalConfig().getOptionInt(myPlugin.getDomain(), "send.blocksize"));
					send.setTurbo(IdentityManager.getGlobalConfig().getOptionBool(myPlugin.getDomain(), "send.forceturbo"));
					send.setType(DCCSend.TransferType.SEND);
					
					ActionManager.processEvent(DCCActions.DCC_SEND_REQUEST_SENT, null, server, target, jc.getSelectedFile());
					
					sendLine(origin, isSilent, FORMAT_OUTPUT, "Starting DCC Send with: "+target);
				
					send.setFileName(jc.getSelectedFile().getAbsolutePath());
					send.setFileSize(jc.getSelectedFile().length());
					
					if (IdentityManager.getGlobalConfig().getOptionBool(myPlugin.getDomain(), "send.reverse")) {
						new DCCSendWindow(myPlugin, send, "Send: "+target, target, server);
						parser.sendCTCP(target, "DCC", "SEND \""+jc.getSelectedFile().getName()+"\" "+DCC.ipToLong(myPlugin.getListenIP(parser))+" 0 "+send.getFileSize()+" "+send.makeToken()+((send.isTurbo()) ? " T" : ""));
					} else {
						if (myPlugin.listen(send)) {
							new DCCSendWindow(myPlugin, send, "*Send: "+target, target, server);
							parser.sendCTCP(target, "DCC", "SEND \""+jc.getSelectedFile().getName()+"\" "+DCC.ipToLong(myPlugin.getListenIP(parser))+" "+send.getPort()+" "+send.getFileSize()+((send.isTurbo()) ? " T" : ""));
						} else {
							sendLine(origin, isSilent, "DCCSendError", "Unable to start dcc send with "+target+" - unable to create listen socket");
						}
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
	
	/**
	 * Returns a list of suggestions for the specified argument, given the list
	 * of previous arguments.
	 * @param arg The argument that is being completed
	 * @param previousArgs The contents of the previous arguments, if any
	 * @return A list of suggestions for the argument
	 */
	@Override
	public AdditionalTabTargets getSuggestions(final int arg, final List<String> previousArgs) {
		final AdditionalTabTargets res = new AdditionalTabTargets();
		
		if (arg == 0) {
			res.add("SEND");
			res.add("CHAT");
			res.excludeAll();
		} else if (arg == 1) {
			res.exclude(TabCompletionType.COMMAND);
			res.exclude(TabCompletionType.CHANNEL);
		} else {
			res.excludeAll();
		}
		
		return res;
	}

}

