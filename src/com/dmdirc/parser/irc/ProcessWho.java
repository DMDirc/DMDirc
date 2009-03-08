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
 * Process a /who reply.
 */
public class ProcessWho extends IRCProcessor {
	/**
	 * Process a /who reply.
	 *
	 * @param sParam Type of line to process ("352")
	 * @param token IRCTokenised line to process
	 */
	@Override
	public void process(final String sParam, final String[] token) {
		// :blueyonder2.uk.quakenet.org 352 Dataforce #mdbot shane Tobavaj.users.quakenet.org *.quakenet.org Tobavaj G+x :3 Tobavaj - http://shane.dmdirc.com/scriptbot.php
		//              0               1      2        3     4              5                      6           7     8        9
		// :blueyonder2.uk.quakenet.org 352 Dataforce #mdbot ~Dataforce ResNetUser-BrynDinas-147.143.246.102.bangor.ac.uk *.quakenet.org Dataforce H@ :0 Dataforce
		//              0               1      2        3      4                               5                            6              7      8        9
		// :blueyonder2.uk.quakenet.org 352 Dataforce #mdbot shane soren.dataforce.org.uk *.quakenet.org DF|Soren H :3 Unknown
		//              0               1      2        3     4              5                      6       7     8      9
		// :server 352 mynickname channel username address server nick flags :hops info
		//     0    1      2         3     4          5      6      7    8        9
		
//		ChannelInfo channel = myParser.getChannelInfo(token[3]);
//		ChannelClientInfo channelClient = channel.getUser(token[7]);
//		ClientInfo client = channelClient.getClient();
		final ClientInfo client = myParser.getClientInfo(token[7]);
		if (client != null) {
			// Update ident/host
			client.setUserBits(token[7]+"!"+token[4]+"@"+token[5], false);
			// Update real name
			if (client.getRealName().isEmpty()) {
				final String name = token[9].split(" ", 2)[1];
				client.setRealName(name);
			}
			// Update away state
			final String mode = token[8];
			final boolean isAway = mode.indexOf('G') != -1;
			if (client.getAwayState() != isAway) {
//				System.out.println("Away state for '"+client+"' changed to: "+isAway);
				client.setAwayState(isAway);
				if (!isAway) { client.setAwayReason(""); }
				if (client == myParser.getMyself()) {
					callAwayState(client.getAwayState(), client.getAwayReason());
				} else {
					callAwayStateOther(client, isAway);
					
					ChannelClientInfo iChannelClient;
					for (ChannelInfo iChannel : myParser.getChannels()) {
						iChannelClient = iChannel.getUser(client);
						if (iChannelClient != null) {
							callChannelAwayStateOther(iChannel,iChannelClient,isAway);
						}
					}
				}
			}
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
		return getCallbackManager().getCallbackType("OnAwayState").call(currentState, reason);
	}
	
	/**
	 * Callback to all objects implementing the onAwayStateOther Callback.
	 *
	 * @see IAwayStateOther
	 * @param client Client this is for
	 * @param state Away State (true if away, false if here)
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callAwayStateOther(final ClientInfo client, final boolean state) {
		return getCallbackManager().getCallbackType("OnAwayStateOther").call(client, state);
	}
	
	/**
	 * Callback to all objects implementing the onChannelAwayStateOther Callback.
	 *
	 * @see IAwayStateOther
	 * @param channel Channel this is for
	 * @param channelClient ChannelClient this is for
	 * @param state Away State (true if away, false if here)
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callChannelAwayStateOther(final ChannelInfo channel, final ChannelClientInfo channelClient, final boolean state) {
		return getCallbackManager().getCallbackType("OnChannelAwayStateOther").call(channel, channelClient, state);
	}
	
	/**
	 * What does this IRCProcessor handle.
	 *
	 * @return String[] with the names of the tokens we handle.
	 */
	@Override
	public String[] handles() {
		return new String[]{"352"};
	} 
	
	/**
	 * Create a new instance of the IRCProcessor Object.
	 *
	 * @param parser IRCParser That owns this IRCProcessor
	 * @param manager ProcessingManager that is in charge of this IRCProcessor
	 */
	protected ProcessWho (IRCParser parser, ProcessingManager manager) { super(parser, manager); }

}
