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

package uk.org.ownage.dmdirc.parser.callbacks.interfaces;

import uk.org.ownage.dmdirc.parser.ProcessMessage;
import uk.org.ownage.dmdirc.parser.IRCParser;

/**
 * Called when a person sends an action not aimed specifically at you or a channel (ie $*).
 * sHost is the hostname of the person sending the message. (Can be a server or a person)<br>
 * cClient is null if user is a server, or not on any common channel.
 */
public interface IUnknownAction extends ICallbackInterface {
	/**
	 * Called when a person sends an action not aimed specifically at you or a channel (ie $*).
	 * sHost is the hostname of the person sending the message. (Can be a server or a person)<br>
	 * cClient is null if user is a server, or not on any common channel.
	 * 
	 * @param tParser Reference to the parser object that made the callback.
	 * @param sMessage Action contents
	 * @param sTarget Actual target of action
	 * @param sHost Hostname of sender (or servername)
	 * @see ProcessMessage#callUnknownAction
	 */
	public void onUnknownAction(IRCParser tParser, String sMessage, String sTarget, String sHost );
}
