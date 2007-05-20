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

package com.dmdirc.parser.callbacks;

import com.dmdirc.parser.IRCParser;
import com.dmdirc.parser.ParserError;
import com.dmdirc.parser.callbacks.interfaces.IPrivateCTCPReply;

/**
 * Callback to all objects implementing the IPrivateCTCPReply Interface.
 */
public final class CallbackOnPrivateCTCPReply extends CallbackObjectSpecific {
	
	/**
	 * Create a new instance of the Callback Object.
	 *
	 * @param parser IRCParser That owns this callback
	 * @param manager CallbackManager that is in charge of this callback
	 */
	public CallbackOnPrivateCTCPReply(final IRCParser parser, final CallbackManager manager) { super(parser, manager); }
	
	/**
	 * Callback to all objects implementing the IPrivateCTCPReply Interface.
	 *
	 * @see IPrivateCTCPReply
	 * @param sType Type of CTCPRReply (VERSION, TIME etc)
	 * @param sMessage Reply Contents
	 * @param sHost Hostname of sender (or servername)
	 * @return true if a callback was called, else false
	 */
	public boolean call(final String sType, final String sMessage, final String sHost) {
		boolean bResult = false;
		IPrivateCTCPReply eMethod = null;
		for (int i = 0; i < callbackInfo.size(); i++) {
			eMethod = (IPrivateCTCPReply) callbackInfo.get(i);
			if (!this.isValidUser(eMethod, sHost)) { continue; }
			try {
				eMethod.onPrivateCTCPReply(myParser, sType, sMessage, sHost);
			} catch (Exception e) {
				final ParserError ei = new ParserError(ParserError.ERROR_ERROR, "Exception in onPrivateCTCPReply");
				ei.setException(e);
				callErrorInfo(ei);
			}
			bResult = true;
		}
		return bResult;
	}	
	
	/**
	 * Get SVN Version information.
	 *
	 * @return SVN Version String
	 */
	public static String getSvnInfo() { return "$Id$"; }
}
