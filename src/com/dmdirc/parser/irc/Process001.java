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

package com.dmdirc.parser.irc;

import com.dmdirc.parser.interfaces.callbacks.ServerReadyListener;

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
	@Override
	public void process(final String sParam, final String[] token) {
		myParser.got001 = true;
		// << :demon1.uk.quakenet.org 001 Java-Test :Welcome to the QuakeNet IRC Network, Java-Test
		myParser.serverName = token[0].substring(1,token[0].length());
		final String sNick = token[2];
				
		// myself will be fake if we havn't recieved a 001 yet
		if (myParser.getLocalClient().isFake()) {
			// Update stored information
			myParser.getLocalClient().setUserBits(sNick, true, true);
			myParser.getLocalClient().setFake(false);
			myParser.addClient(myParser.getLocalClient());
		} else {
			// Another 001? if nicknames change then we need to update the hashtable
			if (!myParser.getLocalClient().getNickname().equalsIgnoreCase(sNick)) {
				// Nick changed, remove old me
				myParser.forceRemoveClient(myParser.getLocalClient());
				/// Update stored information
				myParser.getLocalClient().setUserBits(sNick, true, true);
				// Check that we don't already know someone by this name
				if (getClientInfo(myParser.getLocalClient().getNickname()) == null) {
					// And add to list
					myParser.addClient(myParser.getLocalClient());
				} else {
					// Someone else already know? this is bad!
					myParser.callErrorInfo(new ParserError(ParserError.ERROR_FATAL, "001 overwrites existing client?", myParser.getLastLine()));
				}
			}
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
		return getCallbackManager().getCallbackType(ServerReadyListener.class).call();
	}
	
	/**
	 * What does this IRCProcessor handle.
	 *
	 * @return String[] with the names of the tokens we handle.
	 */
	@Override
	public String[] handles() {
		return new String[]{"001"};
	} 
	
	/**
	 * Create a new instance of the IRCProcessor Object.
	 *
	 * @param parser IRCParser That owns this IRCProcessor
	 * @param manager ProcessingManager that is in charge of this IRCProcessor
	 */
	protected Process001 (IRCParser parser, ProcessingManager manager) { super(parser, manager); }

}
