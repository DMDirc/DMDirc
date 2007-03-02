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
 * SVN: $Id: IRCParser.java 178 2007-02-28 20:36:16Z ShaneMcC $
 */

package uk.org.ownage.dmdirc.parser.callbacks.interfaces;

import uk.org.ownage.dmdirc.parser.*;

/** 
 * Called when user modes are changed.
 * cClient represents the user who's modes were changed (should ALWAYS be us)<br>
 * sSetby is the host of the person who set the mode (usually us, may be an oper or server in some cases)
 */
public interface IUserModeChanged extends ICallbackInterface {
	/**
	 * Called when user modes are changed.
	 * cClient represents the user who's modes were changed (should ALWAYS be us)<br>
	 * sSetby is the host of the person who set the mode (usually us, may be an oper or server in some cases)
	 * 
	 * @param tParser Reference to the parser object that made the callback.
	 * @param cClient Client that had the mode changed (almost always us)
	 * @param sSetBy Host that set the mode (us or servername)
	 * @see IRCParser#callUserModeChanged
	 */
	public void onUserModeChanged(IRCParser tParser, ClientInfo cClient, String sSetBy);
}