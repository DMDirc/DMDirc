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

import com.dmdirc.parser.callbacks.CallbackOnChannelKick;

/**
 * Process a channel kick.
 */
public class ProcessKick extends IRCProcessor {
	/**
	 * Process a channel kick.
	 *
	 * @param sParam Type of line to process ("KICK")
	 * @param token IRCTokenised line to process
	 */
	public void process(String sParam, String[] token) {
		ChannelClientInfo iChannelClient;
		ChannelClientInfo iChannelKicker;
		ChannelInfo iChannel;
		ClientInfo iClient;
		ClientInfo iKicker;
		String sReason = "";
		
		iClient = getClientInfo(token[3]);
		iKicker = getClientInfo(token[0]);
		iChannel = getChannelInfo(token[2]);
		
		if (iClient == null) { return; }
		
		if (myParser.ALWAYS_UPDATECLIENT && iKicker != null) {
			// To facilitate dmdirc formatter, get user information
			if (iKicker.getHost().isEmpty()) { iKicker.setUserBits(token[0],false); }
		}

		if (iChannel == null) { 
			if (iClient != myParser.getMyself()) {
				callErrorInfo(new ParserError(ParserError.ERROR_WARNING, "Got kick for channel ("+token[2]+") that I am not on. [User: "+token[3]+"]", myParser.getLastLine()));
			}
			return;
		} else {
			if (token.length > 4) { sReason = token[token.length-1]; }
			iChannelClient = iChannel.getUser(iClient);
			iChannelKicker = iChannel.getUser(token[0]);
			if (myParser.removeAfterCallback) { callChannelKick(iChannel,iChannelClient,iChannelKicker,sReason,token[0]); }
			iChannel.delClient(iClient);
			if (!myParser.removeAfterCallback) { callChannelKick(iChannel,iChannelClient,iChannelKicker,sReason,token[0]); }
			if (iClient == myParser.getMyself()) {
				iChannel.emptyChannel();
				myParser.removeChannel(iChannel);
			}
		}
	}
	
	/**
	 * Callback to all objects implementing the ChannelKick Callback.
	 *
	 * @see IChannelKick
	 * @param cChannel Channel where the kick took place
	 * @param cKickedClient ChannelClient that got kicked
	 * @param cKickedByClient ChannelClient that did the kicking (may be null if server)
	 * @param sReason Reason for kick (may be "")
	 * @param sKickedByHost Hostname of Kicker (or servername)
         * @return true if a method was called, false otherwise
	 */
	protected boolean callChannelKick(ChannelInfo cChannel, ChannelClientInfo cKickedClient, ChannelClientInfo cKickedByClient, String sReason, String sKickedByHost) {
		CallbackOnChannelKick cb = (CallbackOnChannelKick)getCallbackManager().getCallbackType("OnChannelKick");
		if (cb != null) { return cb.call(cChannel, cKickedClient, cKickedByClient, sReason, sKickedByHost); }
		return false;
	}
	
	/**
	 * What does this IRCProcessor handle.
	 *
	 * @return String[] with the names of the tokens we handle.
	 */
	public String[] handles() {
		String[] iHandle = new String[1];
		iHandle[0] = "KICK";
		return iHandle;
	} 
	
	/**
	 * Create a new instance of the IRCProcessor Object
	 *
	 * @param parser IRCParser That owns this IRCProcessor
	 * @param manager ProcessingManager that is in charge of this IRCProcessor
	 */
	protected ProcessKick (IRCParser parser, ProcessingManager manager) { super(parser, manager); }
	
	/**
	 * Get SVN Version information.
	 *
	 * @return SVN Version String
	 */
	public static String getSvnInfo () { return "$Id$"; }	
}
