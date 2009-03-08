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

package com.dmdirc.parser.irc.callbacks.interfaces;

import com.dmdirc.parser.irc.IRCParser;
import com.dmdirc.parser.irc.callbacks.SpecificCallback;

/**
 * Called when a person sends a CTCPRReply to you directly.
 * sHost is the hostname of the person sending the CTCPRReply. (Can be a server or a person)<br>
 * cClient is null if user is a server, or not on any common channels.
 */
@SpecificCallback
public interface IPrivateCTCPReply extends ICallbackInterface {
	/**
	 * Called when a person sends a CTCPRReply to you directly.
	 * sHost is the hostname of the person sending the CTCPRReply. (Can be a server or a person)<br>
	 * cClient is null if user is a server, or not on any common channels.
	 * 
	 * @param tParser Reference to the parser object that made the callback.
	 * @param sType Type of CTCPRReply (VERSION, TIME etc)
	 * @param sMessage Reply Contents
	 * @param sHost Hostname of sender (or servername)
	 * @see com.dmdirc.parser.irc.ProcessMessage#callPrivateCTCPReply
	 */
	void onPrivateCTCPReply(IRCParser tParser, String sType, String sMessage, String sHost);
}
