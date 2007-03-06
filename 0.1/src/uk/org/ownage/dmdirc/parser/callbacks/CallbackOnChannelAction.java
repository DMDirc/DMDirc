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
 * SVN: $Id: IRCParser.java 178 2007-02-28 20:36:16Z ShaneMcC $
 */

package uk.org.ownage.dmdirc.parser.callbacks;

import uk.org.ownage.dmdirc.parser.*;
import uk.org.ownage.dmdirc.parser.callbacks.CallbackManager;
import  uk.org.ownage.dmdirc.parser.callbacks.interfaces.IChannelAction;

public class CallbackOnChannelAction extends CallbackObjectSpecific {
	/**
	 * Callback to all objects implementing the IChannelAction Interface.
	 *
	 * @see IChannelAction
	 * @param cChannel Channel where the action was sent to
	 * @param cChannelClient ChannelClient who sent the action (may be null if server)
	 * @param sMessage action contents
	 * @param sHost Hostname of sender (or servername)
	 */
	public boolean call(ChannelInfo cChannel, ChannelClientInfo cChannelClient, String sMessage, String sHost) {
		boolean bResult = false;
		IChannelAction eMethod = null;
		for (int i = 0; i < callbackInfo.size(); i++) {
			eMethod = (IChannelAction)callbackInfo.get(i);
			if (!this.isValidChan(eMethod, cChannel)) { continue; }
			try {
				eMethod.onChannelAction(myParser, cChannel, cChannelClient, sMessage, sHost);
			} catch (Exception e) {
				ParserError ei = new ParserError(ParserError.errError, "Exception in onChannelAction");
				ei.setException(e);
				callErrorInfo(ei);
			}
			bResult = true;
		}
		return bResult;
	}	

	// Stupid lack of Constructor inheritance...
	public CallbackOnChannelAction (IRCParser parser, CallbackManager manager) { super(parser, manager); }
}