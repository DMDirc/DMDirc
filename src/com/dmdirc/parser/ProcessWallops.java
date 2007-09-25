/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
 * SVN: $Id: ProcessWallops.java 1970 2007-09-05 18:33:06Z chris87 $
 */

package com.dmdirc.parser;

import com.dmdirc.parser.callbacks.CallbackOnWallDesync;
import com.dmdirc.parser.callbacks.CallbackOnWallop;
import com.dmdirc.parser.callbacks.CallbackOnWalluser;

/**
 * Process a WALLOPS Message.
 */
public class ProcessWallops extends IRCProcessor {
	/**
	 * Process a Wallops Message.
	 *
	 * @param sParam Type of line to process ("WALLOPS")
	 * @param token IRCTokenised line to process
	 */
	public void process(String sParam, String[] token) {
		if (token.length < 3) { return; }
		
		String user = token[0];
		String message = token[token.length-1];
		if (user.charAt(0) == ':' && user.length() > 1) { user = user.substring(1); }
		String[] bits = message.split(" ", 2);

		if (bits.length > 1) {
			if (message.charAt(0) == '*') {
				callWallop(bits[1], user);
				return;
			} else if (message.charAt(0) == '*') {
				callWalluser(bits[1], user);
				return;
			}
		}
		callWallDesync(message, user);
	}
	
	/**
	 * Callback to all objects implementing the Wallop Callback.
	 *
	 * @see IWallop
	 * @param host Host of the user who sent the wallop
	 * @param message The message
         * @return true if a method was called, false otherwise
	 */
	protected boolean callWallop(String message, String host) {
		CallbackOnWallop cb = (CallbackOnWallop)getCallbackManager().getCallbackType("OnWallop");
		if (cb != null) { return cb.call(message, host); }
		return false;
	}
	
	/**
	 * Callback to all objects implementing the Walluser Callback.
	 *
	 * @see IWalluser
	 * @param host Host of the user who sent the walluser
	 * @param message The message
         * @return true if a method was called, false otherwise
	 */
	protected boolean callWalluser(String message, String host) {
		CallbackOnWalluser cb = (CallbackOnWalluser)getCallbackManager().getCallbackType("OnWalluser");
		if (cb != null) { return cb.call(message, host); }
		return false;
	}
	
	/**
	 * Callback to all objects implementing the WallDesync Callback.
	 *
	 * @see IWallDesync
	 * @param host Host of the user who sent the WallDesync
	 * @param message The message
         * @return true if a method was called, false otherwise
	 */
	protected boolean callWallDesync(String message, String host) {
		CallbackOnWallDesync cb = (CallbackOnWallDesync)getCallbackManager().getCallbackType("OnWallDesync");
		if (cb != null) { return cb.call(message, host); }
		return false;
	}
	
	
	/**
	 * What does this IRCProcessor handle.
	 *
	 * @return String[] with the names of the tokens we handle.
	 */
	public String[] handles() {
		String[] iHandle = new String[]{"WALLOPS"};
		return iHandle;
	} 
	
	/**
	 * Create a new instance of the IRCProcessor Object
	 *
	 * @param parser IRCParser That owns this IRCProcessor
	 * @param manager ProcessingManager that is in charge of this IRCProcessor
	 */
	protected ProcessWallops (IRCParser parser, ProcessingManager manager) { super(parser, manager); }
	
	/**
	 * Get SVN Version information.
	 *
	 * @return SVN Version String
	 */
	public static String getSvnInfo () { return "$Id: ProcessWallops.java 1970 2007-09-05 18:33:06Z chris87 $"; }	
}
