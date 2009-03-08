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
 * Process a channel join.
 */
public class ProcessJoin extends IRCProcessor {

	/**
	 * Process a channel join.
	 *
	 * @param sParam Type of line to process ("JOIN")
	 * @param token IRCTokenised line to process
	 */
	@Override
	public void process(final String sParam, final String[] token) {
		if (sParam.equals("329")) {
			if (token.length < 5) { return; }
			ChannelInfo iChannel = myParser.getChannelInfo(token[3]);
			if (iChannel != null) {
				try {
					iChannel.setCreateTime(Integer.parseInt(token[4]));
				} catch (NumberFormatException nfe) { /* Oh well, not a normal ircd I guess */ }
			}
		} else {
			// :nick!ident@host JOIN (:)#Channel
			Byte nTemp;
			if (token.length < 3) { return; }
			ClientInfo iClient;
			ChannelInfo iChannel;
			ChannelClientInfo iChannelClient;
			
			iClient = myParser.getClientInfo(token[0]);
			iChannel = myParser.getChannelInfo(token[token.length-1]);
			
			if (iClient == null) { 
				iClient = new ClientInfo(myParser, token[0]);
				myParser.addClient(iClient);
			}
			// Check to see if we know the host/ident for this client to facilitate dmdirc Formatter
			if (iClient.getHost().isEmpty()) { iClient.setUserBits(token[0],false); }
			if (iChannel != null) {
				if (iClient == myParser.getMyself()) {
					try {
						if (iChannel.getUser(iClient) != null) {
							// If we are joining a channel we are already on, fake a part from
							// the channel internally, and rejoin.
							myParser.getProcessingManager().process("PART", token);
						} else {
							// Otherwise we have a channel known, that we are not in?
							myParser.callErrorInfo(new ParserError(ParserError.ERROR_FATAL, "Joined known channel that we wern't already on..", myParser.getLastLine()));
						}
					} catch (ProcessorNotFoundException e) { }
				} else if (iChannel.getUser(iClient) != null) {
					// Client joined channel that we already know of.
					return;
				} else {
					// This is only done if we are already the channel, and it isn't us that
					// joined.
					iChannelClient = iChannel.addClient(iClient);
					callChannelJoin(iChannel, iChannelClient);
					return;
				}
			}
			//if (iClient != myParser.getMyself()) {
				// callErrorInfo(new ParserError(ParserError.ERROR_WARNING, "Got join for channel ("+token[token.length-1]+") that I am not on. [Me: "+myParser.getMyself()+"]", myParser.getLastLine()));
			//}
			iChannel = new ChannelInfo(myParser, token[token.length-1]);
			// Add ourself to the channel, this will be overridden by the NAMES reply
			iChannel.addClient(iClient);
			myParser.addChannel(iChannel);
			sendString("MODE "+iChannel.getName());
			
			callChannelSelfJoin(iChannel);
		}
	}
	

	/**
	 * Callback to all objects implementing the ChannelJoin Callback.
	 *
	 * @see IChannelJoin
	 * @param cChannel Channel Object
	 * @param cChannelClient ChannelClient object for new person
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callChannelJoin(final ChannelInfo cChannel, final ChannelClientInfo cChannelClient) {
		return getCallbackManager().getCallbackType("OnChannelJoin").call(cChannel, cChannelClient);
	}
	
	/**
	 * Callback to all objects implementing the ChannelSelfJoin Callback.
	 *
	 * @see IChannelSelfJoin
	 * @param cChannel Channel Object
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callChannelSelfJoin(final ChannelInfo cChannel) {
		return  getCallbackManager().getCallbackType("OnChannelSelfJoin").call(cChannel);
	}
	
	/**
	 * What does this IRCProcessor handle.
	 *
	 * @return String[] with the names of the tokens we handle.
	 */
	@Override
	public String[] handles() {
		return new String[]{"JOIN", "329"};
	} 
	
	/**
	 * Create a new instance of the IRCProcessor Object.
	 *
	 * @param parser IRCParser That owns this IRCProcessor
	 * @param manager ProcessingManager that is in charge of this IRCProcessor
	 */
	protected ProcessJoin (IRCParser parser, ProcessingManager manager) { super(parser, manager); }

}
