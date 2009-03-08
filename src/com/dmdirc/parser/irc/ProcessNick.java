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
 * Process a Nick change.
 */
public class ProcessNick extends IRCProcessor {
	/**
	 * Process a Nick change.
	 *
	 * @param sParam Type of line to process ("NICK")
	 * @param token IRCTokenised line to process
	 */
	@Override
	public void process(String sParam, String[] token) {
		ClientInfo iClient;
		ChannelClientInfo iChannelClient;
		String oldNickname;
		
		iClient = getClientInfo(token[0]);
		if (iClient != null) {
			oldNickname = myParser.getIRCStringConverter().toLowerCase(iClient.getNickname());
			// Remove the client from the known clients list
			final boolean isSameNick = myParser.getIRCStringConverter().equalsIgnoreCase(oldNickname, token[token.length-1]);
			
			if (!isSameNick) {
				myParser.forceRemoveClient(getClientInfo(oldNickname));
			}
			// Change the nickame
			iClient.setUserBits(token[token.length-1],true);
			// Readd the client
			if (!isSameNick && myParser.getClientInfo(iClient.getNickname()) != null) {
//				myParser.onPostErrorInfo(new ParserError(ParserError.ERROR_FATAL, "Nick change would overwrite existing client", myParser.getLastLine()), false);
				myParser.callErrorInfo(new ParserError(ParserError.ERROR_FATAL + ParserError.ERROR_USER, "Nick change would overwrite existing client", myParser.getLastLine()));
			} else {
				if (!isSameNick) {
					myParser.addClient(iClient);
				}
				
				for (ChannelInfo iChannel : myParser.getChannels()) {
					// Find the user (using the old nickname)
					iChannelClient = iChannel.getUser(oldNickname);
					if (iChannelClient != null) {
						// Rename them. This uses the old nickname (the key in the hashtable)
						// and the channelClient object has access to the new nickname (by way
						// of the ClientInfo object we updated above)
						if (!isSameNick) {
							iChannel.renameClient(oldNickname, iChannelClient);
						}
						callChannelNickChanged(iChannel,iChannelClient,ClientInfo.parseHost(token[0]));
					}
				}
				
				callNickChanged(iClient, ClientInfo.parseHost(token[0]));
			}
		}
		
	}
	
	/**
	 * Callback to all objects implementing the ChannelNickChanged Callback.
	 *
	 * @see IChannelNickChanged
	 * @param cChannel One of the channels that the user is on
	 * @param cChannelClient Client changing nickname
	 * @param sOldNick Nickname before change
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callChannelNickChanged(ChannelInfo cChannel, ChannelClientInfo cChannelClient, String sOldNick) {
		return getCallbackManager().getCallbackType("OnChannelNickChanged").call(cChannel, cChannelClient, sOldNick);
	}
	
	/**
	 * Callback to all objects implementing the NickChanged Callback.
	 *
	 * @see INickChanged
	 * @param cClient Client changing nickname
	 * @param sOldNick Nickname before change
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callNickChanged(ClientInfo cClient, String sOldNick) {
		return getCallbackManager().getCallbackType("OnNickChanged").call(cClient, sOldNick);
	}
	
	/**
	 * What does this IRCProcessor handle.
	 *
	 * @return String[] with the names of the tokens we handle.
	 */
	@Override
	public String[] handles() {
		return new String[]{"NICK"};
	} 
	
	/**
	 * Create a new instance of the IRCProcessor Object.
	 *
	 * @param parser IRCParser That owns this IRCProcessor
	 * @param manager ProcessingManager that is in charge of this IRCProcessor
	 */
	protected ProcessNick (IRCParser parser, ProcessingManager manager) { super(parser, manager); }

}
