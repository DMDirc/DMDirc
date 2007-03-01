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

package uk.org.ownage.dmdirc.parser.callbacks;

import uk.org.ownage.dmdirc.parser.*;
import uk.org.ownage.dmdirc.parser.callbacks.CallbackManager;
import  uk.org.ownage.dmdirc.parser.callbacks.interfaces.IErrorInfo;

public class CallbackOnErrorInfo extends CallbackObject {
	/**
	 * Callback to all objects implementing the IErrorInfo Interface.
	 *
	 * @see IErrorInfo
	 * @param level Debugging Level (errFatal, errWarning etc)
	 * @param data Error Information
	 */
	public boolean call(ParserError errorInfo) {
		if (IRCParser.bDebug) { myParser.doDebug("[ERROR] {%d} %s\n", errorInfo.getLevel(), errorInfo.getData()); }
		boolean bResult = false;
		for (int i = 0; i < callbackInfo.size(); i++) {
			try {
				((IErrorInfo)callbackInfo.get(i)).onErrorInfo(myParser, errorInfo);
			} catch (Exception e) {
				// This will not callErrorInfo or we would get an infinite loop!
				System.out.println("Exception in onError Callback. ["+e.getMessage()+"]");
				e.printStackTrace();
			}
			bResult = true;
		}
		return bResult;
	}	
	
	// Stupid lack of Constructor inheritance...
	public CallbackOnErrorInfo (IRCParser parser, CallbackManager manager) { super(parser, manager); }
}