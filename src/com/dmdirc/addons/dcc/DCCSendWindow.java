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

import com.dmdirc.ui.swing.components.TextFrame;
import com.dmdirc.ui.swing.components.TextLabel;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JButton;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import net.miginfocom.swing.MigLayout;

/**
 * This class links DCC Send objects to a window.
 *
 * @author Shane 'Dataforce' McCormack
 * @version $Id: DCC.java 969 2007-04-30 18:38:20Z ShaneMcC $
 */
public class DCCSendWindow extends DCCFrame implements DCCSendInterface, ActionListener {
	/** The DCCSend object we are a window for */
	private final DCCSend dcc;
	
	/** My Nickname */
	private final String nickname;
	
	/** Other Nickname */
	private final String otherNickname;
	
	/** Total data transfered */
	private long transferCount = 0;
	
	/** Progress Bar */
	private final JProgressBar progress = new JProgressBar();
	
	/** Status Label */
	private final JLabel status = new JLabel("Status: Waiting");
	
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
		
		getContentPane().setLayout(new MigLayout());
		
		transferCount = dcc.getFileStart();
		
		progress.setMinimum(0);
		progress.setMaximum(100);
		progress.setStringPainted(true);
		progress.setValue(0);
		
		if (dcc.getType() == DCCSend.TransferType.SEND) {
			getContentPane().add(new JLabel("Sending: "+dcc.getFileName()), "wrap");
			getContentPane().add(new JLabel("To: "+targetNick), "wrap");
		} else {
			getContentPane().add(new JLabel("Recieving: "+dcc.getFileName()), "wrap");
			getContentPane().add(new JLabel("From: "+targetNick), "wrap");
		}
		getContentPane().add(status, "wrap");
		getContentPane().add(progress, "growx, wrap");
		
		final JButton button = new JButton("Cancel");
		button.addActionListener(this);
		
		getContentPane().add(button, "wrap, align right");
		
		plugin.addWindow(this);
	}
	
	/** {@inheritDoc} */
	public void actionPerformed(final ActionEvent e) {
		if (e.getActionCommand().equals("Cancel")) {
			((JButton)e.getSource()).setEnabled(false);
			dcc.close();
		}
	}
	
	/**
	 * Called when data is sent/recieved
	 *
	 * @param dcc The DCCSend that this message is from
	 * @param bytes The number of new bytes that were transfered
	 */
	public void dataTransfered(final DCCSend dcc, final int bytes) {
		transferCount += bytes;
		final double percent = (100.00 / dcc.getFileSize()) * transferCount;
		
		if (dcc.getType() == DCCSend.TransferType.SEND) {
			status.setText("Status: Sending");
		} else {
			status.setText("Status: Recieving");
		}
		
		System.out.printf("Data Transfered. [ %d / %f ]\n", bytes, percent);
		
		progress.setValue((int)Math.floor(percent));
	}
	
	/**
	 * Called when the socket is closed
	 *
	 * @param dcc The DCCSend that this message is from
	 */
	@Override
	public void socketClosed(final DCCSend dcc) {
		if (transferCount == dcc.getFileSize()) {
			status.setText("Status: Transfer Compelete.");
		} else {
			status.setText("Status: Transfer Failed.");
		}
	}
	
	/**
	 * Called when the socket is opened
	 *
	 * @param dcc The DCCSend that this message is from
	 */
	@Override
	public void socketOpened(final DCCSend dcc) {
		status.setText("Status: Socket Opened");
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
