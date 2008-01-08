/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.parser.callbacks.interfaces;

import com.dmdirc.parser.IRCParser;

/**
 * Called when "End of MOTD" or "No MOTD" is received.
 */
public interface IMOTDEnd extends ICallbackInterface { 
	/**
	 * Called when "End of MOTD" or "No MOTD".
	 *
	 * @param tParser Reference to the parser object that made the callback.
	 * @param noMOTD Set to true if this was a "No MOTD Found" message rather than an "End of MOTD"
	 * @param sData The contents of the line (incase of language changes or so)
	 * @see com.dmdirc.parser.ProcessMOTD#callMOTDEnd
	 */
	void onMOTDEnd(IRCParser tParser, boolean noMOTD, String sData);
}
