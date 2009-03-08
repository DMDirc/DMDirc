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
 * Called When we, or another client parts a channel.
 * This is called BEFORE client has been removed from the channel.
 */
@SpecificCallback
public interface IChannelPart extends ICallbackInterface {
	/**
	 * Called When we, or another client parts a channel.
	 * This is called BEFORE client has been removed from the channel.
	 * 
	 * @param tParser Reference to the parser object that made the callback.
	 * @param cChannel Channel that the user parted
	 * @param cChannelClient Client that parted
	 * @param sReason Reason given for parting (May be "")
	 * @see com.dmdirc.parser.irc.ProcessPart#callChannelPart
	 */
	void onChannelPart(IRCParser tParser, ChannelInfo cChannel, ChannelClientInfo cChannelClient, String sReason);
}
