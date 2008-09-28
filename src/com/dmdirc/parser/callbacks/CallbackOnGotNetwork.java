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

import com.dmdirc.parser.IRCParser;
import com.dmdirc.parser.ParserError;
import com.dmdirc.parser.callbacks.interfaces.IGotNetwork;

/**
 * Callback to all objects implementing the IGotNetwork Interface.
 */
public final class CallbackOnGotNetwork extends CallbackObject {
	
	/**
	 * Create a new instance of the Callback Object.
	 *
	 * @param parser IRCParser That owns this callback
	 * @param manager CallbackManager that is in charge of this callback
	 */
	public CallbackOnGotNetwork(final IRCParser parser, final CallbackManager manager) { super(parser, manager); }
	
	/**
	 * Callback to all objects implementing the IGotNetwork Interface.
	 *
	 * @see IGotNetwork
	 * @param networkName The name of the network.
	 * @param ircdVersion The version of the ircd.
	 * @param ircdType The Guessed type of the ircd based on the name.
	 * @return true if a callback was called, else false
	 */
	public boolean call(final String networkName, final String ircdVersion, final String ircdType) {
		boolean bResult = false;
		for (int i = 0; i < callbackInfo.size(); i++) {
			try {
				((IGotNetwork) callbackInfo.get(i)).onGotNetwork(myParser, networkName, ircdVersion, ircdType);
			} catch (Exception e) {
				final ParserError ei = new ParserError(ParserError.ERROR_ERROR, "Exception in onGotNetwork ("+e.getMessage()+")", myParser.getLastLine());
				ei.setException(e);
				callErrorInfo(ei);
			}
			bResult = true;
		}
		return bResult;
	}

}
