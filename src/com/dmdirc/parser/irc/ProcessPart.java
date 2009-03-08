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

/**
 * Process a channel part.
 */
public class ProcessPart extends IRCProcessor {

	/**
	 * Process a channel part.
	 *
	 * @param sParam Type of line to process ("PART")
	 * @param token IRCTokenised line to process
	 */
	@Override
	public void process(final String sParam, final String[] token) {
		// :nick!ident@host PART #Channel
		// :nick!ident@host PART #Channel :reason
		if (token.length < 3) { return; }
		ClientInfo iClient;
		ChannelInfo iChannel;
		ChannelClientInfo iChannelClient;
		
		iClient = getClientInfo(token[0]);
		iChannel = getChannelInfo(token[2]);
		
		if (iClient == null) { return; }
		if (IRCParser.ALWAYS_UPDATECLIENT && iClient.getHost().isEmpty()) {
			// This may seem pointless - updating before they leave - but the formatter needs it!
			iClient.setUserBits(token[0],false);
		}
		if (iChannel == null) { 
			if (iClient != myParser.getMyself()) {
				callErrorInfo(new ParserError(ParserError.ERROR_WARNING, "Got part for channel ("+token[2]+") that I am not on. [User: "+token[0]+"]", myParser.getLastLine()));
			}
			return;
		} else {
			String sReason = "";
			if (token.length > 3) { sReason = token[token.length-1]; }
			iChannelClient = iChannel.getUser(iClient);
			if (iChannelClient == null) {
				// callErrorInfo(new ParserError(ParserError.ERROR_WARNING, "Got part for channel ("+token[2]+") for a non-existant user. [User: "+token[0]+"]", myParser.getLastLine()));
				return;
			}
			if (myParser.removeAfterCallback) { callChannelPart(iChannel,iChannelClient,sReason); }
			callDebugInfo(IRCParser.DEBUG_INFO, "Removing %s from %s",iClient.getNickname(),iChannel.getName());
			iChannel.delClient(iClient);
			if (!myParser.removeAfterCallback) { callChannelPart(iChannel,iChannelClient,sReason); }
			if (iClient == myParser.getMyself()) {
				iChannel.emptyChannel();
				myParser.removeChannel(iChannel);
			}
		}
	}
	
	/**
	 * Callback to all objects implementing the ChannelPart Callback.
	 *
	 * @see IChannelPart
	 * @param cChannel Channel that the user parted
	 * @param cChannelClient Client that parted
	 * @param sReason Reason given for parting (May be "")
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callChannelPart(final ChannelInfo cChannel, final ChannelClientInfo cChannelClient, final String sReason) {
		return getCallbackManager().getCallbackType("OnChannelPart").call(cChannel, cChannelClient, sReason);
	}
	
	/**
	 * What does this IRCProcessor handle.
	 *
	 * @return String[] with the names of the tokens we handle.
	 */
	@Override
	public String[] handles() {
		return new String[]{"PART"};
	} 
	
	/**
	 * Create a new instance of the IRCProcessor Object.
	 *
	 * @param parser IRCParser That owns this IRCProcessor
	 * @param manager ProcessingManager that is in charge of this IRCProcessor
	 */
	protected ProcessPart (IRCParser parser, ProcessingManager manager) { super(parser, manager); }

}
