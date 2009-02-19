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

import com.dmdirc.parser.irc.ClientInfo;
import com.dmdirc.parser.irc.IRCParser;
/** 
 * Called When we, or another client quits IRC (Called once in total).
 * This is called BEFORE client has been removed from the channel.
 */
public interface IQuit extends ICallbackInterface {
	/**
	 * Called When we, or another client quits IRC (Called once in total).
	 * This is called BEFORE client has been removed from the channel.
	 *
	 * @param tParser Reference to the parser object that made the callback.
	 * @param cClient Client Quitting
	 * @param sReason Reason for quitting (may be "")
	 * @see com.dmdirc.parser.irc.ProcessQuit#callQuit
	 */
	void onQuit(IRCParser tParser, ClientInfo cClient, String sReason);
}
