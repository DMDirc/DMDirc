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
 * Called when a person is kicked.
 * cKickedByClient can be null if kicked by a server. sKickedByHost is the hostname of the person/server doing the kicking.
 */
@SpecificCallback
public interface IChannelKick extends ICallbackInterface {
	/**
	 * Called when a person is kicked.
	 * cKickedByClient can be null if kicked by a server. sKickedByHost is the hostname of the person/server doing the kicking.
	 *
	 * @param tParser Reference to the parser object that made the callback.
	 * @param cChannel Channel where the kick took place
	 * @param cKickedClient ChannelClient that got kicked
	 * @param cKickedByClient ChannelClient that did the kicking (may be null if server)
	 * @param sReason Reason for kick (may be "")
	 * @param sKickedByHost Hostname of Kicker (or servername)
	 * @see com.dmdirc.parser.irc.ProcessKick#callChannelKick
	 */
	void onChannelKick(@FakableSource IRCParser tParser,
            @FakableSource ChannelInfo cChannel,
            ChannelClientInfo cKickedClient,
            @FakableArgument ChannelClientInfo cKickedByClient,
            String sReason,
            @FakableSource String sKickedByHost);
}
