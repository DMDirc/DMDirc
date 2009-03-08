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

import java.util.ArrayList;

/**
 * Process a Quit message.
 */
public class ProcessQuit extends IRCProcessor {

	/**
	 * Process a Quit message.
	 *
	 * @param sParam Type of line to process ("QUIT")
	 * @param token IRCTokenised line to process
	 */
	@Override
	public void process(final String sParam, final String[] token) {
		// :nick!ident@host QUIT
		// :nick!ident@host QUIT :reason
		if (token.length < 2) { return; }
		ClientInfo iClient;
		ChannelClientInfo iChannelClient;
		
		iClient = getClientInfo(token[0]);
		
		if (iClient == null) { return; }
		if (IRCParser.ALWAYS_UPDATECLIENT && iClient.getHost().isEmpty()) {
			// This may seem pointless - updating before they leave - but the formatter needs it!
			iClient.setUserBits(token[0],false);
		}
		String sReason = "";
		if (token.length > 2) { sReason = token[token.length-1]; }
		
		ArrayList<ChannelInfo> channelList = new ArrayList<ChannelInfo>(myParser.getChannels());
		for (ChannelInfo iChannel : channelList) {
			iChannelClient = iChannel.getUser(iClient);
			if (iChannelClient != null) {
				if (myParser.removeAfterCallback) { callChannelQuit(iChannel,iChannelClient,sReason); }
				if (iClient == myParser.getMyself()) {
					iChannel.emptyChannel();
					myParser.removeChannel(iChannel);
				} else {
					iChannel.delClient(iClient);
				}
				if (!myParser.removeAfterCallback) { callChannelQuit(iChannel,iChannelClient,sReason); }
			}
		}

		if (myParser.removeAfterCallback) { callQuit(iClient,sReason); }
		if (iClient == myParser.getMyself()) {
			myParser.clearClients();
		} else {
			myParser.removeClient(iClient);
		}
		if (!myParser.removeAfterCallback) { callQuit(iClient,sReason); }
	}	
	
	/**
	 * Callback to all objects implementing the ChannelQuit Callback.
	 *
	 * @see IChannelQuit
	 * @param cChannel Channel that user was on
	 * @param cChannelClient User thats quitting
	 * @param sReason Quit reason
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callChannelQuit(final ChannelInfo cChannel, final ChannelClientInfo cChannelClient, final String sReason) {
		return getCallbackManager().getCallbackType("OnChannelQuit").call(cChannel, cChannelClient, sReason);
	}
	
	/**
	 * Callback to all objects implementing the Quit Callback.
	 *
	 * @see IQuit
	 * @param cClient Client Quitting
	 * @param sReason Reason for quitting (may be "")
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callQuit(final ClientInfo cClient, final String sReason) {
		return getCallbackManager().getCallbackType("OnQuit").call(cClient, sReason);
	}
	
	/**
	 * What does this IRCProcessor handle.
	 *
	 * @return String[] with the names of the tokens we handle.
	 */
	@Override
	public String[] handles() {
		return new String[]{"QUIT"};
	} 
	
	/**
	 * Create a new instance of the IRCProcessor Object.
	 *
	 * @param parser IRCParser That owns this IRCProcessor
	 * @param manager ProcessingManager that is in charge of this IRCProcessor
	 */
	protected ProcessQuit (IRCParser parser, ProcessingManager manager) { super(parser, manager); }

}
