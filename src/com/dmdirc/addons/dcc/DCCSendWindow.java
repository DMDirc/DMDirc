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

import com.dmdirc.ui.swing.JWrappingLabel;
import com.dmdirc.ui.swing.components.TextFrame;

import javax.swing.SwingConstants;

/**
 * This class links DCC Send objects to a window.
 *
 * @author Shane 'Dataforce' McCormack
 * @version $Id: DCC.java 969 2007-04-30 18:38:20Z ShaneMcC $
 */
public class DCCSendWindow extends DCCFrame implements DCCSendInterface {
	/** The DCCSend object we are a window for */
	private final DCCSend dcc;
	
	/** My Nickname */
	private final String nickname;
	
	/** Other Nickname */
	private final String otherNickname;
	
	/**
	 * Creates a new instance of DCCSendWindow with a given DCCSend object.
	 *
	 * @param plugin the DCC Plugin responsible for this window
	 * @param dcc The DCCSend object this window wraps around
	 * @param title The title of this window
	 * @param nick My Current Nickname
	 * @param targetNick Nickname of target
	 */
	public DCCSendWindow(final DCCPlugin plugin, final DCCSend dcc, final String title, final String nick, final String targetNick) {
		super(plugin, title);
		this.dcc = dcc;
		dcc.setHandler(this);
		nickname = nick;
		otherNickname = targetNick;
		
		JWrappingLabel label = new JWrappingLabel("This is a placeholder dcc send window.", SwingConstants.CENTER);
		
		((TextFrame)getFrame()).getContentPane().add(label);
		plugin.addWindow(this);
	}
	
	/**
	 * Called when the socket is closed
	 *
	 * @param dcc The DCCSend that this message is from
	 */
    @Override
	public void socketClosed(final DCCSend dcc) {

	}
	
	/**
	 * Called when the socket is opened
	 *
	 * @param dcc The DCCSend that this message is from
	 */
    @Override
	public void socketOpened(final DCCSend dcc) {

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
