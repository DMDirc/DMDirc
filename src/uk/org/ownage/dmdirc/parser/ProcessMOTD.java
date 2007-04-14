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
 * SVN: $Id$
 */

package uk.org.ownage.dmdirc.parser;

import uk.org.ownage.dmdirc.parser.callbacks.CallbackOnMOTDEnd;
import uk.org.ownage.dmdirc.parser.callbacks.CallbackOnMOTDLine;
import uk.org.ownage.dmdirc.parser.callbacks.CallbackOnMOTDStart;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IMOTDEnd;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IMOTDLine;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IMOTDStart;

/**
 * Process a MOTD Related Line
 */
public class ProcessMOTD extends IRCProcessor {
	/**
	 * Process a MOTD Related Line.
	 *
	 * @param sParam Type of line to process ("375", "372", "376", "422")
	 * @param token IRCTokenised line to process
	 */
	public void process(String sParam, String[] token) {
		if (sParam.equals("375")) {
			callMOTDStart(token[token.length-1]);
		} else if (sParam.equals("372")) {
			callMOTDLine(token[token.length-1]);
		} else {
			myParser.startPingTimer();
			if (!myParser.h005Info.containsKey("CHANTYPES")) { myParser.parseChanPrefix(); }
			if (!myParser.h005Info.containsKey("PREFIX")) { myParser.parsePrefixModes(); }
			if (!myParser.h005Info.containsKey("USERMODES")) { myParser.parseUserModes(); }
			if (!myParser.h005Info.containsKey("CHANMODES")) { myParser.parseChanModes(); }
			callMOTDEnd(sParam.equals("422"));
		}
	}
	
	/**
	 * Callback to all objects implementing the MOTDEnd Callback.
	 *
	 * @param noMOTD Was this an MOTDEnd or NoMOTD
	 * @see IMOTDEnd
	 */
	protected boolean callMOTDEnd(boolean noMOTD) {
		CallbackOnMOTDEnd cb = (CallbackOnMOTDEnd)getCallbackManager().getCallbackType("OnMOTDEnd");
		if (cb != null) { return cb.call(noMOTD); }
		return false;
	}
	
	/**
	 * Callback to all objects implementing the MOTDLine Callback.
	 *
	 * @see IMOTDLine
	 * @param data Incomming Line.
	 */
	protected boolean callMOTDLine(String data) {
		CallbackOnMOTDLine cb = (CallbackOnMOTDLine)getCallbackManager().getCallbackType("OnMOTDLine");
		if (cb != null) { return cb.call(data); }
		return false;
	}
	
	/**
	 * Callback to all objects implementing the MOTDStart
	 *
	 * @see IMOTDStart
	 * @param data Incomming Line.
	 */
	protected boolean callMOTDStart(String data) {
		CallbackOnMOTDStart cb = (CallbackOnMOTDStart)getCallbackManager().getCallbackType("OnMOTDStart");
		if (cb != null) { return cb.call(data); }
		return false;
	}
	
	/**
	 * What does this IRCProcessor handle.
	 *
	 * @return String[] with the names of the tokens we handle.
	 */
	public String[] handles() {
		String[] iHandle = new String[4];
		iHandle[0] = "372";
		iHandle[1] = "375";
		iHandle[2] = "376";
		iHandle[3] = "422";
		return iHandle;
	} 
	
	/**
	 * Create a new instance of the IRCProcessor Object
	 *
	 * @param parser IRCParser That owns this IRCProcessor
	 * @param manager ProcessingManager that is in charge of this IRCProcessor
	 */
	protected ProcessMOTD (IRCParser parser, ProcessingManager manager) { super(parser, manager); }
	
	/**
	 * Get SVN Version information.
	 *
	 * @return SVN Version String
	 */
	public static String getSvnInfo () { return "$Id$"; }	
}
