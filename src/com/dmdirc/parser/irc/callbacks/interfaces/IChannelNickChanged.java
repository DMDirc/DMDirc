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

import com.dmdirc.parser.irc.ChannelClientInfo;
import com.dmdirc.parser.irc.ChannelInfo;
import com.dmdirc.parser.irc.IRCParser;
import com.dmdirc.parser.irc.callbacks.SpecificCallback;

/** 
 * Called when we or another user change nickname (Called once per channel the user is on).
 * This is called after the nickname change has been done internally
 */
@SpecificCallback
public interface IChannelNickChanged extends ICallbackInterface {
	/**
	 * Called when we or another user change nickname (Called once per channel the user is on).
	 * This is called after the nickname change has been done internally
	 * 
	 * @param tParser Reference to the parser object that made the callback.
	 * @param cChannel One of the channels that the user is on
	 * @param cChannelClient Client changing nickname
	 * @param sOldNick Nickname before change
	 * @see com.dmdirc.parser.irc.ProcessNick#callChannelNickChanged
	 */
	 void onChannelNickChanged(IRCParser tParser, ChannelInfo cChannel, ChannelClientInfo cChannelClient, String sOldNick);
}
