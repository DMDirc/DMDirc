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

package com.dmdirc.parser.irc.callbacks;

import com.dmdirc.parser.irc.ClientInfo;
import com.dmdirc.parser.irc.ChannelClientInfo;
import com.dmdirc.parser.irc.ChannelInfo;
import com.dmdirc.parser.irc.IRCParser;
import com.dmdirc.parser.irc.ParserError;
import com.dmdirc.parser.irc.callbacks.interfaces.IChannelNonUserModeChanged;

/**
 * Callback to all objects implementing the IChannelNonUserModeChanged Interface.
 */
public final class CallbackOnChannelNonUserModeChanged extends CallbackObjectSpecific {
	
	/**
	 * Create a new instance of the Callback Object.
	 *
	 * @param parser IRCParser That owns this callback
	 * @param manager CallbackManager that is in charge of this callback
	 */
	public CallbackOnChannelNonUserModeChanged(final IRCParser parser, final CallbackManager manager) { super(parser, manager); }
	
	/**
	 * Callback to all objects implementing the IChannelNonUserModeChanged Interface.
	 *
	 * @see IChannelNonUserModeChanged
	 * @param cChannel Channel where modes were changed
	 * @param myChannelClient Client changing the modes (null if server)
	 * @param sHost Host doing the mode changing (User host or server name)
	 * @param sModes Exact String parsed (not including user modes)
	 * @return true if a callback was called, else false
	 */
	public boolean call(final ChannelInfo cChannel, final ChannelClientInfo myChannelClient, final String sHost, final String sModes) {
		ChannelClientInfo cChannelClient;
		if (myChannelClient == null && myParser.getCreateFake()) {
			cChannelClient = new ChannelClientInfo(cChannel.getParser(), (new ClientInfo(cChannel.getParser(), sHost)).setFake(true) ,cChannel);
		} else {
			cChannelClient = myChannelClient;
		}
		boolean bResult = false;
		IChannelNonUserModeChanged eMethod = null;
		for (int i = 0; i < callbackInfo.size(); i++) {
			eMethod = (IChannelNonUserModeChanged) callbackInfo.get(i);
			if (!this.isValidChan(eMethod, cChannel)) { continue; }
			try {
				eMethod.onChannelNonUserModeChanged(myParser, cChannel, cChannelClient, sHost, sModes);
			} catch (Exception e) {
				final ParserError ei = new ParserError(ParserError.ERROR_ERROR, "Exception in onChannelNonUserModeChanged ("+e.getMessage()+")", myParser.getLastLine());
				ei.setException(e);
				callErrorInfo(ei);
			}
			bResult = true;
		}
		return bResult;
	}

}
