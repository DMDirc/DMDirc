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

package uk.org.ownage.dmdirc.parser;

import uk.org.ownage.dmdirc.parser.callbacks.CallbackOnChannelPart;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IChannelPart;

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
	public void process(String sParam, String[] token) {
		// :nick!ident@host PART #Channel
		// :nick!ident@host PART #Channel :reason
		if (token.length < 3) { return; }
		ClientInfo iClient;
		ChannelInfo iChannel;
		ChannelClientInfo iChannelClient;
		
		iClient = getClientInfo(token[0]);
		iChannel = getChannelInfo(token[2]);
		
		if (iClient == null) { return; }
		if (myParser.alwaysUpdateClient) {
			// This may seem pointless - updating before they leave - but the formatter needs it!
			if (iClient.getHost().equals("")) {iClient.setUserBits(token[0],false); }
		}
		if (iChannel == null) { 
			if (iClient != myParser.cMyself) {
				callErrorInfo(new ParserError(ParserError.errWarning, "Got part for channel ("+token[2]+") that I am not on. [User: "+token[0]+"]"));
			}
			return;
		} else {
			String sReason = "";
			if (token.length > 3) { sReason = token[token.length-1]; }
			iChannelClient = iChannel.getUser(iClient);
			if (iChannelClient == null) {
				callErrorInfo(new ParserError(ParserError.errWarning, "Got part for channel ("+token[2]+") for a non-existant user. [User: "+token[0]+"]"));
				return;
			}
			callChannelPart(iChannel,iChannelClient,sReason);
			callDebugInfo(myParser.ndInfo, "Removing %s from %s",iClient.getNickname(),iChannel.getName());
			iChannel.delClient(iClient);
			if (iClient == myParser.cMyself) {
				iChannel.emptyChannel();
				myParser.hChannelList.remove(iChannel.getName().toLowerCase());
			} else { iClient.checkVisability(); }
		}
	}
	
	/**
	 * Callback to all objects implementing the ChannelPart Callback.
	 *
	 * @see IChannelPart
	 * @param cChannel Channel that the user parted
	 * @param cChannelClient Client that parted
	 * @param sReason Reason given for parting (May be "")
	 */
	protected boolean callChannelPart(ChannelInfo cChannel, ChannelClientInfo cChannelClient, String sReason) {
		CallbackOnChannelPart cb = (CallbackOnChannelPart)getCallbackManager().getCallbackType("OnChannelPart");
		if (cb != null) { return cb.call(cChannel, cChannelClient, sReason); }
		return false;
	}
	
	/**
	 * What does this IRCProcessor handle.
	 *
	 * @return String[] with the names of the tokens we handle.
	 */
	public String[] handles() {
		String[] iHandle = new String[1];
		iHandle[0] = "PART";
		return iHandle;
	} 
	
	/**
	 * Create a new instance of the IRCProcessor Object
	 *
	 * @param parser IRCParser That owns this IRCProcessor
	 * @param manager ProcessingManager that is in charge of this IRCProcessor
	 */
	protected ProcessPart (IRCParser parser, ProcessingManager manager) { super(parser, manager); }
}
