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
 *
 * SVN: $Id$
 */

package com.dmdirc.parser;

import com.dmdirc.parser.callbacks.CallbackOnServerReady;

/**
 * Process a 001 message.
 */
public class Process001 extends IRCProcessor {
	/**
	 * Process a 001 message.
	 *
	 * @param sParam Type of line to process ("001")
	 * @param token IRCTokenised line to process
	 */
	public void process(String sParam, String[] token) {
		myParser.got001 = true;
		// << :demon1.uk.quakenet.org 001 Java-Test :Welcome to the QuakeNet IRC Network, Java-Test
		String sNick;
		myParser.sServerName = token[0].substring(1,token[0].length());
		sNick = token[2];
		
		/* Code below is here incase relying on token[2] breaks somewhere
		String[] temp = token[token.length-1].split(" ");
		sConfirmedNickname = temp[temp.length-1];
		// Some servers give a full host in 001
		temp = sNick.split("!",2);
		sNick = temp[0];  /* */
		
		if (myParser.getMyself().isFake()) {
			myParser.getMyself().setUserBits(sNick, true, true);
			myParser.getMyself().setFake(false);
			myParser.addClient(myParser.getMyself());
		}
		
		callServerReady();
		myParser.startPingTimer();
	}
	
	/**
	 * Callback to all objects implementing the ServerReady Callback.
	 *
	 * @see IServerReady
         * @return true if a method was called, false otherwise
	 */	
	protected boolean callServerReady() {
		CallbackOnServerReady cb = (CallbackOnServerReady)getCallbackManager().getCallbackType("OnServerReady");
		if (cb != null) { return cb.call(); }
		return false;
	}
	
	/**
	 * What does this IRCProcessor handle.
	 *
	 * @return String[] with the names of the tokens we handle.
	 */
	public String[] handles() {
		String[] iHandle = new String[1];
		iHandle[0] = "001";
		return iHandle;
	} 
	
	/**
	 * Create a new instance of the IRCProcessor Object.
	 *
	 * @param parser IRCParser That owns this IRCProcessor
	 * @param manager ProcessingManager that is in charge of this IRCProcessor
	 */
	protected Process001 (IRCParser parser, ProcessingManager manager) { super(parser, manager); }

}
