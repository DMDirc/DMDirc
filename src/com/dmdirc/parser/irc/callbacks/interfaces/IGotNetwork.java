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

/**
 * Interface Used when the Network=blah 005 token is recieved.
 */
public interface IGotNetwork extends ICallbackInterface {
	/**
	 * Called when the Network=blah 005 token is recieved.
	 * if NETWORK= is not sent in 005, this will not be called.
	 * 
	 * @param tParser Reference to the parser object that made the callback.
	 * @param networkName The name of the network.
	 * @param ircdVersion The version of the ircd.
	 * @param ircdType The Guessed type of the ircd based on the name.
	 * @see com.dmdirc.parser.irc.Process004005#callGotNetwork
	 */
	void onGotNetwork(IRCParser tParser, String networkName, String ircdVersion, String ircdType);
}
