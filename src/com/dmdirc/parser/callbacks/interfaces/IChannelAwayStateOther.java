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
 * SVN: $Id: IChannelAwayStateOther.java 719 2007-03-28 13:20:56Z ShaneMcC $
 */

package com.dmdirc.parser.callbacks.interfaces;

import com.dmdirc.parser.IRCParser;
import com.dmdirc.parser.ChannelClientInfo;
import com.dmdirc.parser.ChannelInfo;

/**
 * Called when we go away, or come back.
 */
public interface IChannelAwayStateOther extends ICallbackInterface { 
	/**
	 * Called when we go away, or come back.
	 *
	 * @param tParser Reference to the parser object that made the callback.
	 * @param channel Channel this is for
	 * @param channelclient Client this is for
	 * @param state Away State (true if away, false if here)
	 * @see com.dmdirc.parser.ProcessAway#callChannelAwayStateOther
	 */
	void onChannelAwayStateOther(IRCParser tParser, ChannelInfo channel, ChannelClientInfo channelClient, boolean state);
}
