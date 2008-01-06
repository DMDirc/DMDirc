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

import com.dmdirc.Main;

/**
 * This class links DCC Chat objects to a window.
 *
 * @author Shane 'Dataforce' McCormack
 * @version $Id: DCC.java 969 2007-04-30 18:38:20Z ShaneMcC $
 */
public class DCCChatWindow extends DCCFrame implements DCCChatInterface {
	/** The DCCChat object we are a window for */
	private final DCCChat dcc;
	
	/** My Nickname */
	private final String nickname;
	
	/** Other Nickname */
	private final String otherNickname;
	
	/**
	 * Creates a new instance of DCCChatWindow with a given DCCChat object.
	 *
	 * @param plugin the DCC Plugin responsible for this window
	 * @param dcc The DCCChat object this window wraps around
	 * @param title The title of this window
	 * @param nick My Current Nickname
	 * @param targetNick Nickname of target
	 */
	public DCCChatWindow(final DCCPlugin plugin, final DCCChat dcc, final String title, final String nick, final String targetNick) {
		super(plugin, title, false);
		this.dcc = dcc;
		dcc.setHandler(this);
		nickname = nick;
		otherNickname = targetNick;
		
		myWindow = Main.getUI().getInputWindow(this, getDCCCommandParser());
		plugin.addWindow(this);
		
		myWindow.setTitle(title);
		myWindow.setFrameIcon(icon);
		myWindow.setVisible(true);
	}
	
	/**
	 * Sends a line of text to this container's source.
	 *
	 * @param line The line to be sent
	 */
	public void sendLine(final String line) {
		if (dcc.isWriteable()) {
			myWindow.addLine("OUT<< "+line, false);
			dcc.sendLine(line);
		} else {
			myWindow.addLine("<<ERROR>> Socket is closed.", false);
		}
		return;
	}
	
	/**
	 * Handle a recieved message
	 *
	 * @param dcc The DCCChat that this message is from
	 * @param message The message
	 */
	public void handleChatMessage(final DCCChat dcc, final String message) {
		myWindow.addLine(" IN>> "+message, false);
	}
	
	/**
	 * Called when the socket is closed
	 *
	 * @param dcc The DCCChat that this message is from
	 */
	public void socketClosed(final DCCChat dcc) {
		myWindow.addLine(" -- Socket closed -- ", false);
	}
	
	/**
	 * Called when the socket is opened
	 *
	 * @param dcc The DCCChat that this message is from
	 */
	public void socketOpened(final DCCChat dcc) {
		myWindow.addLine(" ++ Socket opened ++ ", false);
	}
	
	/**
	 * Closes this container (and it's associated frame).
	 */
        @Override
	public void windowClosing() {
		dcc.close();
		super.windowClosing();
	}
}
