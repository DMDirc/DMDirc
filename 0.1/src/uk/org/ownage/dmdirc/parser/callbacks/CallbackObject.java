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

import uk.org.ownage.dmdirc.parser.callbacks.interfaces.ICallbackInterface;
import uk.org.ownage.dmdirc.parser.*;
import uk.org.ownage.dmdirc.parser.callbacks.CallbackManager;
import java.util.Hashtable;
import java.util.ArrayList;
/**
 * CallbackObject.
 * Superclass for all callback types.
 *
 * @author            Shane Mc Cormack
 * @version           $Id: IRCParser.java 178 2007-02-28 20:36:16Z ShaneMcC $
 */
public abstract class CallbackObject {
	/**
	 * Arraylist for storing callback information related to the callback.
	 */
	protected ArrayList<ICallbackInterface> callbackInfo = new ArrayList<ICallbackInterface>();
	
	/** Add a callback pointer to the appropriate ArrayList. */
	protected void addCallback(ICallbackInterface eMethod) {
		for (int i = 0; i < callbackInfo.size(); i++) {
			if (eMethod.equals(callbackInfo.get(i))) { return; }
		}
		callbackInfo.add((ICallbackInterface)eMethod);
	}
	/** Delete a callback pointer from the appropriate ArrayList. */
	protected void delCallback(ICallbackInterface eMethod) {
		for (int i = 0; i < callbackInfo.size(); i++) {
			if (eMethod.equals(callbackInfo.get(i))) { callbackInfo.remove(i); break; }
		}
	}
	
	protected boolean callErrorInfo(ParserError errorInfo) {
		CallbackOnErrorInfo cb = (CallbackOnErrorInfo)myManager.getCallbackType("ErrorInfo");
		if (cb != null) { return cb.call(errorInfo); }
		return false;
	}
	
	protected CallbackObject (IRCParser parser, CallbackManager manager) {
		this.myParser = parser;
		this.myManager = manager;
	}
	
	IRCParser myParser = null;
	CallbackManager myManager = null;
	
	public void add(ICallbackInterface eMethod) { addCallback(eMethod); }
	public void del(ICallbackInterface eMethod) { delCallback(eMethod); }
	
	/** Get the name for this callback - must be overridden */
//	public abstract String getName();
	public String getName() {
		Package thisPackage = this.getClass().getPackage();
		int packageLength = 0;
		if (thisPackage != null) {
			packageLength = thisPackage.getName().length()+1;
		}
		return this.getClass().getName().substring(packageLength+8); // 8 is the length of "Callback"
	}
	
	/** Get the name for this callback in lowercase */
	public String getLowerName() {
		return this.getName().toLowerCase();
	}
}