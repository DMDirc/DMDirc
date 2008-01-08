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

import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.plugins.Plugin;
import com.dmdirc.ui.swing.JWrappingLabel;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.swing.components.TextFrame;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.Identity;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.parser.IRCParser;
import com.dmdirc.parser.ClientInfo;
import com.dmdirc.Server;
import com.dmdirc.Main;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.interfaces.ActionType;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.interfaces.ActionListener;

import java.io.File;
import java.io.IOException;

import java.util.List;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.SwingConstants;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

/**
 * This plugin adds DCC to dmdirc
 *
 * @author Shane 'Dataforce' McCormack
 * @version $Id: DCCPlugin.java 969 2007-04-30 18:38:20Z ShaneMcC $
 */
public final class DCCPlugin extends Plugin implements ActionListener {
	/** The DCCCommand we created */
	private DCCCommand command = null;
	
	/** Our DCC Container window. */
	private DCCFrame container;
	
	/** What domain do we store all settings in the global config under. */
	private static final String MY_DOMAIN = "plugin-DCC";
	
	/** Child Frames */
	private List<DCCFrame> childFrames = new ArrayList<DCCFrame>();
	
//	/** Pending Sends (This uses DCCFrame not DCCSend so we can check if the window was closed or not) */
//	private Map<String, DCCFrame> pendingSends = new HashMap<String, DCCFrame>();
	
	/**
	 * Creates a new instance of the DCC Plugin.
	 */
	public DCCPlugin() {
		super();
	}
	
	/**
	 * Ask a question, if the answer is the answer required, then recall handleProcessEvent
	 *
	 * @param question Question to ask
	 * @param title Title of question dialog
	 * @param desiredAnswer Answer required
	 * @param type Actiontype to pass back
	 * @param format StringBuffer to pass back
	 * @param arguments arguments to pass back
	 */
	public void askQuestion(final String question, final String title, final int desiredAnswer, final ActionType type, final StringBuffer format, final Object... arguments) {
		// New thread to ask the question in to stop us locking the UI
		Thread questionThread = new Thread(new Runnable() {
			public void run() {
				int result = JOptionPane.showConfirmDialog((JFrame)Main.getUI().getMainWindow(), question, title, JOptionPane.YES_NO_OPTION);
				if (result == desiredAnswer) {
					handleProcessEvent(type, format, true, arguments);
				}
			}
		}, "QuestionThread: "+title);
		// Start the thread
		questionThread.start();
	}
	
	/**
	 * Ask the location to save a file, then start the download.
	 *
	 * @param nickname Person this dcc is from.
	 * @param send The DCCSend to save for.
	 * @param parser The parser this send was received on
	 * @param reverse Is this a reverse dcc?
	 * @param token Token used in reverse dcc.
	 */
	public void saveFile(final String nickname, final DCCSend send, final IRCParser parser, final boolean reverse, final String sendFilename, final String token) {
		// New thread to ask the user where to save in to stop us locking the UI
		Thread dccThread = new Thread(new Runnable() {
			public void run() {
				final JFileChooser jc = new JFileChooser(IdentityManager.getGlobalConfig().getOption(MY_DOMAIN, "recieve.savelocation"));
				jc.setDialogTitle("Save "+sendFilename+" As - DMDirc ");
				jc.setFileSelectionMode(jc.FILES_AND_DIRECTORIES);
				jc.setMultiSelectionEnabled(false);
				jc.setSelectedFile(new File(send.getFileName()));
				int result = jc.showSaveDialog((JFrame)Main.getUI().getMainWindow());
				if (result == JFileChooser.APPROVE_OPTION) {
					send.setFileName(jc.getSelectedFile().getPath());
					if (reverse && !token.isEmpty()) {
						new DCCSendWindow(DCCPlugin.this, send, "*Recieve: "+nickname, parser.getMyNickname(), nickname);
						send.listen();
						parser.sendCTCP(nickname, "DCC", "SEND "+sendFilename+" "+DCC.ipToLong(send.getHost())+" "+send.getPort()+" "+send.getFileSize()+" "+token);
					} else {
						new DCCSendWindow(DCCPlugin.this, send, "Recieve: "+nickname, parser.getMyNickname(), nickname);
						send.connect();
					}
				}
			}
		}, "saveFileThread: "+sendFilename);
		// Start the thread
		dccThread.start();
	}
	
	/**
	 * Process an event of the specified type.
	 *
	 * @param type The type of the event to process
	 * @param format Format of messages that are about to be sent. (May be null)
	 * @param arguments The arguments for the event
	 */
	@Override
	public void processEvent(final ActionType type, final StringBuffer format, final Object... arguments) {
		handleProcessEvent(type, format, false, arguments);
	}
	
	/**
	 * Process an event of the specified type.
	 *
	 * @param type The type of the event to process
	 * @param format Format of messages that are about to be sent. (May be null)
	 * @param dontAsk Don't ask any questions, assume yes.
	 * @param arguments The arguments for the event
	 */
	public void handleProcessEvent(final ActionType type, final StringBuffer format, final boolean dontAsk, final Object... arguments) {
		if (type == CoreActionType.SERVER_CTCP) {
			final String ctcpType = (String)arguments[2];
			final String[] ctcpData = ((String)arguments[3]).split(" ");
			if (ctcpType.equalsIgnoreCase("DCC")) {
				if (ctcpData[0].equalsIgnoreCase("chat") && ctcpData.length > 3) {
					final String nickname = ((ClientInfo)arguments[1]).getNickname();
					if (!dontAsk) {
						askQuestion("User "+nickname+" on "+((Server)arguments[0]).toString()+" would like to start a DCC Chat with you.\n\nDo you want to continue?", "DCC Chat Request", JOptionPane.YES_OPTION, type, format, arguments);
						return;
					} else {
						DCCChat chat = new DCCChat();
						try {
							chat.setAddress(Long.parseLong(ctcpData[2]), Integer.parseInt(ctcpData[3]));
						} catch (NumberFormatException nfe) { return; }
						String myNickname = ((Server)arguments[0]).getParser().getMyNickname();
						new DCCChatWindow(this, chat, "Chat: "+nickname, myNickname, nickname);
						chat.connect();
					}
				} else if (ctcpData[0].equalsIgnoreCase("send") && ctcpData.length > 3) {
					final String nickname = ((ClientInfo)arguments[1]).getNickname();
					final String filename;
					// Clients tend to put files with spaces in the name in "" so lets look for that.
					final StringBuilder filenameBits = new StringBuilder();
					int i;
					boolean quoted = ctcpData[1].startsWith("\"");
					if (quoted) {
						for (i = 1; i < ctcpData.length; i++) {
							String bit = ctcpData[i];
							if (i == 1) { bit = bit.substring(1); }
							if (bit.endsWith("\"")) {
								filenameBits.append(" "+bit.substring(0, bit.length()-1));
								break;
							} else {
								filenameBits.append(" "+bit);
							}
						}
						filename = filenameBits.toString().trim();
					} else {
						filename = ctcpData[1];
						i = 1;
					}
					
					final String ip = ctcpData[++i];
					final String port = ctcpData[++i];
					long size;
					long startpos;
					if (ctcpData.length+1 > i) {
						try {
							size = Long.parseLong(ctcpData[++i]);
						} catch (NumberFormatException nfe) { size = -1; }
					} else { size = -1; }
					// Add support for resume later
					startpos = 0;
					
					if (!dontAsk) {
						askQuestion("User "+nickname+" on "+((Server)arguments[0]).toString()+" would like to send you a file over DCC.\n\nFile: "+filename+"\n\nDo you want to continue?", "DCC Chat Request", JOptionPane.YES_OPTION, type, format, arguments);
						return;
					} else {
						DCCSend send = new DCCSend();
						try {
							if (!port.equals("0")) {
								send.setAddress(Long.parseLong(ip), Integer.parseInt(port));
							}
						} catch (NumberFormatException nfe) { return; }
						send.setFileName(filename);
						send.setFileSize(size);
						send.setFileStart(startpos);
						saveFile(nickname, send, ((Server)arguments[0]).getParser(), port.equals("0"), (quoted) ? "\""+filename+"\"" : filename, (ctcpData.length-1 > i) ? ctcpData[++i] : "");
					}
				}
			}
		}
	}
	
	/**
	 * Create the container window
	 */
	protected void createContainer() {
		container = new DCCFrame(this, "DCCs"){};
		JWrappingLabel label = new JWrappingLabel("This is a placeholder window to group DCCs together.", SwingConstants.CENTER);
		label.setText(label.getText()+"\n\nClosing this window will close all the active DCCs");
		((TextFrame)container.getFrame()).getContentPane().add(label);
		WindowManager.addWindow(container.getFrame());
	}
	
	/**
	 * Add a window to the container window
	 *
	 * @param window Window to remove
	 */
	protected void addWindow(final DCCFrame window) {
		if (window == container) { return; }
		if (container == null) { createContainer(); }
		
		WindowManager.addWindow(container.getFrame(), window.getFrame());
		childFrames.add(window);
	}
	
	/**
	 * Remove a window from the container window
	 *
	 * @param window Window to remove
	 */
	protected void delWindow(final DCCFrame window) {
		if (container == null) { return; }
		if (window == container) {
			container = null;
			for (DCCFrame win : childFrames) {
				if (win != window) {
					win.close();
				}
			}
			childFrames.clear();
		} else {
			childFrames.remove(window);
			if (childFrames.size() == 0) {
				container.close();
				container = null;
			}
		}
	}
	
	/**
	 * Called when the plugin is loaded.
	 */
	@Override
	public void onLoad() {
		Properties defaults = new Properties();
		defaults.setProperty(MY_DOMAIN + ".recieve.savelocation", Main.getConfigDir() + "downloads" + System.getProperty("file.separator"));
		defaults.setProperty("identity.name", "DCC Plugin Defaults");
		IdentityManager.addIdentity(new Identity(defaults));
	
		final File dir = new File(IdentityManager.getGlobalConfig().getOption(MY_DOMAIN, "recieve.savelocation"));
		if (!dir.exists()) {
			try {
				dir.mkdirs();
				dir.createNewFile();
			} catch (IOException ex) {
				Logger.userError(ErrorLevel.LOW, "Unable to create download dir");
			}
		} else {
			if (!dir.isDirectory()) {
				Logger.userError(ErrorLevel.LOW, "Unable to create download dir (file exists instead)");
			}
		}
	
		command = new DCCCommand(this);
		ActionManager.addListener(this, CoreActionType.SERVER_CTCP);
	}
	
	/**
	 * Called when this plugin is Unloaded
	 */
	@Override
	public void onUnload() {
		CommandManager.unregisterCommand(command);
		ActionManager.removeListener(this);
		if (container != null) {
			container.close();
		}
	}
	
	/**
	 * Get SVN Version information.
	 *
	 * @return SVN Version String
	 */
	public static String getSvnInfo() { return "$Id: DCCPlugin.java 969 2007-04-30 18:38:20Z ShaneMcC $"; }	
}

