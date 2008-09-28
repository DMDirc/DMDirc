/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.parser.callbacks;

import com.dmdirc.parser.ClientInfo;
import com.dmdirc.parser.ChannelClientInfo;
import com.dmdirc.parser.ChannelInfo;
import com.dmdirc.parser.IRCParser;
import com.dmdirc.parser.ParserError;
import com.dmdirc.parser.callbacks.interfaces.IChannelAction;

/**
 * Callback to all objects implementing the IChannelAction Interface.
 */
public final class CallbackOnChannelAction extends CallbackObjectSpecific {
	
	/**
	 * Create a new instance of the Callback Object.
	 *
	 * @param parser IRCParser That owns this callback
	 * @param manager CallbackManager that is in charge of this callback
	 */
	public CallbackOnChannelAction(final IRCParser parser, final CallbackManager manager) { super(parser, manager); }
	
	/**
	 * Callback to all objects implementing the IChannelAction Interface.
	 *
	 * @see IChannelAction
	 * @param cChannel Channel where the action was sent to
	 * @param myChannelClient ChannelClient who sent the action (may be null if server)
	 * @param sMessage action contents
	 * @param sHost Hostname of sender (or servername)
	 * @return true if a callback was called, else false
	 */
	public boolean call(final ChannelInfo cChannel, final ChannelClientInfo myChannelClient, final String sMessage, final String sHost) {
		ChannelClientInfo cChannelClient;
		if (myChannelClient == null && myParser.getCreateFake()) {
			cChannelClient = new ChannelClientInfo(cChannel.getParser(), new ClientInfo(cChannel.getParser(), sHost).setFake(true) ,cChannel);
		} else {
			cChannelClient = myChannelClient;
		}
		boolean bResult = false;
		IChannelAction eMethod = null;
		for (int i = 0; i < callbackInfo.size(); i++) {
			eMethod = (IChannelAction) callbackInfo.get(i);
			if (!this.isValidChan(eMethod, cChannel)) { continue; }
			try {
				eMethod.onChannelAction(myParser, cChannel, cChannelClient, sMessage, sHost);
			} catch (Exception e) {
				final ParserError ei = new ParserError(ParserError.ERROR_ERROR, "Exception in onChannelAction ("+e.getMessage()+")", myParser.getLastLine());
				ei.setException(e);
				callErrorInfo(ei);
			}
			bResult = true;
		}
		return bResult;
	}

}
