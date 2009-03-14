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
import com.dmdirc.parser.irc.callbacks.FakableArgument;
import com.dmdirc.parser.irc.callbacks.FakableSource;
import com.dmdirc.parser.irc.callbacks.SpecificCallback;

/**
 * Called when a person sends a notice to a channel.
 * sHost is the hostname of the person sending the notice. (Can be a server or a person)<br>
 * cChannelClient is null if user is a server, or not on the channel.
 */
@SpecificCallback
public interface IChannelNotice extends ICallbackInterface {
	/**
	 * Called when a person sends a notice to a channel.
	 * sHost is the hostname of the person sending the notice. (Can be a server or a person)<br>
	 * cChannelClient is null if user is a server, or not on the channel.
	 * 
	 * @param tParser Reference to the parser object that made the callback.
	 * @param cChannel Channel where the notice was sent to
	 * @param cChannelClient ChannelClient who sent the notice (may be null if server)
	 * @param sMessage notice contents
	 * @param sHost Hostname of sender (or servername)
	 * @see com.dmdirc.parser.irc.ProcessMessage#callChannelNotice
	 */
	void onChannelNotice(@FakableSource IRCParser tParser,
            @FakableSource ChannelInfo cChannel,
            @FakableArgument ChannelClientInfo cChannelClient,
            String sMessage,
            @FakableSource String sHost);
}
