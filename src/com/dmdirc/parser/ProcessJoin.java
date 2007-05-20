/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack
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

import com.dmdirc.parser.callbacks.CallbackOnChannelJoin;
import com.dmdirc.parser.callbacks.CallbackOnChannelSelfJoin;
import com.dmdirc.parser.callbacks.interfaces.IChannelJoin;
import com.dmdirc.parser.callbacks.interfaces.IChannelSelfJoin;
import java.util.Enumeration;

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
	public void process(String sParam, String[] token) {
		// :nick!ident@host JOIN (:)#Channel
		Character cTemp;
		Byte nTemp;
		if (token.length < 3) { return; }
		ClientInfo iClient;
		ChannelInfo iChannel;
		ChannelClientInfo iChannelClient;
		
		iClient = myParser.getClientInfo(token[0]);
		iChannel = myParser.getChannelInfo(token[token.length-1]);
		
		if (iClient == null) { 
			iClient = new ClientInfo(myParser, token[0]);
			myParser.hClientList.put(iClient.getNickname().toLowerCase(),iClient);
		}
		// Check to see if we know the host/ident for this client to facilitate dmdirc Formatter
		if (iClient.getHost().equals("")) { iClient.setUserBits(token[0],false); }
		if (iChannel == null) { 
			if (iClient != myParser.cMyself) {
				callErrorInfo(new ParserError(ParserError.ERROR_WARNING, "Got join for channel ("+token[token.length-1]+") that I am not on. [User: "+token[0]+"]"));
			}
			iChannel = new ChannelInfo(myParser, token[token.length-1]);
			myParser.hChannelList.put(iChannel.getName().toLowerCase(),iChannel);
			sendString("MODE "+iChannel.getName());
			
			// Find out the lists currently in use
			for (Enumeration e = myParser.hChanModesOther.keys(); e.hasMoreElements();) {
				cTemp = (Character)e.nextElement();
				nTemp = myParser.hChanModesOther.get(cTemp);
				if (nTemp == myParser.MODE_LIST) { sendString("MODE "+iChannel.getName()+" "+cTemp); }
			}
			callChannelSelfJoin(iChannel);
		} else {
			// This is only done if we are on the channel. Else we wait for names.
			iChannelClient = iChannel.addClient(iClient);
			callChannelJoin(iChannel, iChannelClient);
		}
	}	
	
	/**
	 * Callback to all objects implementing the ChannelJoin Callback.
	 *
	 * @see IChannelJoin
	 * @param cChannel Channel Object
	 * @param cChannelClient ChannelClient object for new person
	 */
	protected boolean callChannelJoin(ChannelInfo cChannel, ChannelClientInfo cChannelClient) {
		CallbackOnChannelJoin cb = (CallbackOnChannelJoin)getCallbackManager().getCallbackType("OnChannelJoin");
		if (cb != null) { return cb.call(cChannel, cChannelClient); }
		return false;
	}
	
	/**
	 * Callback to all objects implementing the ChannelSelfJoin Callback.
	 *
	 * @see IChannelSelfJoin
	 * @param cChannel Channel Object
	 */
	protected boolean callChannelSelfJoin(ChannelInfo cChannel) {
		CallbackOnChannelSelfJoin cb = (CallbackOnChannelSelfJoin)getCallbackManager().getCallbackType("OnChannelSelfJoin");
		if (cb != null) { return cb.call(cChannel); }
		return false;
	}
	
	/**
	 * What does this IRCProcessor handle.
	 *
	 * @return String[] with the names of the tokens we handle.
	 */
	public String[] handles() {
		String[] iHandle = new String[1];
		iHandle[0] = "JOIN";
		return iHandle;
	} 
	
	/**
	 * Create a new instance of the IRCProcessor Object
	 *
	 * @param parser IRCParser That owns this IRCProcessor
	 * @param manager ProcessingManager that is in charge of this IRCProcessor
	 */
	protected ProcessJoin (IRCParser parser, ProcessingManager manager) { super(parser, manager); }
	
	/**
	 * Get SVN Version information.
	 *
	 * @return SVN Version String
	 */
	public static String getSvnInfo () { return "$Id$"; }	
}
