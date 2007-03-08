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

import uk.org.ownage.dmdirc.parser.callbacks.CallbackOnChannelTopic;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IChannelTopic;
import java.util.Calendar;

/**
 * Process a topic change.
 */
public class ProcessTopic extends IRCProcessor {
	/**
	 * Process a topic change.
	 *
	 * @param sParam Type of line to process ("TOPIC", "332", "333")
	 * @param token IRCTokenised line to process
	 */
	public void process(String sParam, String[] token) {
		ChannelInfo iChannel;
		if (sParam.equals("332")) {
			iChannel = getChannelInfo(token[3]);
			if (iChannel == null) { return; };
			iChannel.setTopic(token[token.length-1]);
		} else if (sParam.equals("333")) {
			iChannel = getChannelInfo(token[3]);
			if (iChannel == null) { return; };
			iChannel.setTopicTime(Long.parseLong(token[5]));
			iChannel.setTopicUser(token[4]);
			callChannelTopic(iChannel,true);
		} else {
			if (myParser.alwaysUpdateClient) {
				ClientInfo iClient = getClientInfo(token[0]);
				if (iClient != null) {
					if (iClient.getHost().equals("")) {iClient.setUserBits(token[0],false); }
				}
			}
			iChannel = getChannelInfo(token[2]);
			if (iChannel == null) { return; };
			iChannel.setTopicTime(Calendar.getInstance().getTimeInMillis() / 1000);
			String sTemp[] = token[0].split(":",2);
			if (sTemp.length > 1) { token[0] = sTemp[1]; }
			iChannel.setTopicUser(token[0]);
			iChannel.setTopic(token[token.length-1]);
			callChannelTopic(iChannel,false);
		}
	}
	
	/**
	 * Callback to all objects implementing the ChannelTopic Callback.
	 *
	 * @see IChannelTopic
	 * @param cChannel Channel that topic was set on
	 * @param bIsJoinTopic True when getting topic on join, false if set by user/server
	 */
	protected boolean callChannelTopic(ChannelInfo cChannel, boolean bIsJoinTopic) {
		CallbackOnChannelTopic cb = (CallbackOnChannelTopic)getCallbackManager().getCallbackType("OnChannelTopic");
		if (cb != null) { return cb.call(cChannel, bIsJoinTopic); }
		return false;
	}
	
	/**
	 * What does this IRCProcessor handle.
	 *
	 * @return String[] with the names of the tokens we handle.
	 */
	public String[] handles() {
		String[] iHandle = new String[3];
		iHandle[0] = "TOPIC";
		iHandle[1] = "332";
		iHandle[2] = "333";
		return iHandle;
	} 
	
	/**
	 * Create a new instance of the IRCProcessor Object
	 *
	 * @param parser IRCParser That owns this IRCProcessor
	 * @param manager ProcessingManager that is in charge of this IRCProcessor
	 */
	protected ProcessTopic (IRCParser parser, ProcessingManager manager) { super(parser, manager); }
}
