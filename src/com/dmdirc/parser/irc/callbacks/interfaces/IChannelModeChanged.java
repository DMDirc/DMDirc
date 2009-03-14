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
 * Called when the channel modes are changed or discovered.
 * cChannelClient is null if the modes were found from raw 324 (/MODE #Chan reply) or if a server set the mode.<br>
 * If a Server set the mode, sHost is the servers name, else it is the full host of the user who set it
 */
@SpecificCallback
public interface IChannelModeChanged extends ICallbackInterface {
	/**
	 * Called when the channel modes are changed or discovered.
	 * cChannelClient is null if the modes were found from raw 324 (/MODE #Chan reply) or if a server set the mode.<br>
	 * If a Server set the mode, sHost is the servers name, else it is the full host of the user who set it
	 * 
	 * @param tParser Reference to the parser object that made the callback.
	 * @param cChannel Channel where modes were changed
	 * @param cChannelClient Client chaning the modes (null if server)
	 * @param sHost Host doing the mode changing (User host or server name)
	 * @param sModes String showing the exact mode change parsed.
	 * @see com.dmdirc.parser.irc.ProcessMode#callChannelModeChanged
	 */
	void onChannelModeChanged(@FakableSource IRCParser tParser,
            @FakableSource ChannelInfo cChannel,
            @FakableArgument ChannelClientInfo cChannelClient,
            @FakableSource String sHost,
            String sModes);
}
