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
 *
 * SVN: $Id$
 */

package com.dmdirc.parser;

import com.dmdirc.parser.ClientInfo;
import com.dmdirc.parser.callbacks.CallbackOnAwayState;
import com.dmdirc.parser.callbacks.interfaces.IAwayState;

/**
 * Process an Away/Back message.
 */
public class ProcessAway extends IRCProcessor {
	/**
	 * Process an Away/Back message.
	 *
	 * @param sParam Type of line to process ("305", "306")
	 * @param token IRCTokenised line to process
	 */
	public void process(String sParam, String[] token) {
		if (sParam.equals("301")) {
			ClientInfo iClient = myParser.getClientInfo(token[3]);
			if (iClient != null) { iClient.setAwayReason(token[token.length-1]); }
		} else {
			myParser.getMyself().setAwayState(sParam.equals("306"));
			callAwayState(myParser.getMyself().getAwayState(), myParser.getMyself().getAwayReason());
		}
	}
	
	/**
	 * Callback to all objects implementing the onAwayState Callback.
	 *
	 * @see IAwayState
	 * @param currentState Set to true if we are now away, else false.
	 * @param reason Best guess at away reason
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callAwayState(boolean currentState, String reason) {
		CallbackOnAwayState cb = (CallbackOnAwayState)myParser.getCallbackManager().getCallbackType("OnAwayState");
		if (cb != null) { return cb.call(currentState, reason); }
		return false;
	}
	
	/**
	 * What does this IRCProcessor handle.
	 *
	 * @return String[] with the names of the tokens we handle.
	 */
	public String[] handles() {
		String[] iHandle = new String[3];
		iHandle[0] = "301"; // Whois Away
		iHandle[1] = "305";
		iHandle[2] = "306";
		return iHandle;
	} 
	
	/**
	 * Create a new instance of the IRCProcessor Object
	 *
	 * @param parser IRCParser That owns this IRCProcessor
	 * @param manager ProcessingManager that is in charge of this IRCProcessor
	 */
	protected ProcessAway (IRCParser parser, ProcessingManager manager) { super(parser, manager); }
	
	/**
	 * Get SVN Version information.
	 *
	 * @return SVN Version String
	 */
	public static String getSvnInfo () { return "$Id$"; }	
}
