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
 * @version           $Id$
 */
public abstract class CallbackObject {
	/** Arraylist for storing callback information related to the callback. */
	protected ArrayList<ICallbackInterface> callbackInfo = new ArrayList<ICallbackInterface>();

	/** Reference to the IRCParser that owns this callback. */
	protected IRCParser myParser = null;
	/** Reference to the CallbackManager in charge of this callback. */
	protected CallbackManager myManager = null;

	/**
	 * Add a callback pointer to the appropriate ArrayList.
	 *
	 * @param eMethod OBject to callback to.
	 */
	protected final void addCallback(ICallbackInterface eMethod) {
		for (int i = 0; i < callbackInfo.size(); i++) {
			if (eMethod.equals(callbackInfo.get(i))) { return; }
		}
		callbackInfo.add(eMethod);
	}
	/**
	 * Delete a callback pointer from the appropriate ArrayList.
	 *
	 * @param eMethod Object that was being called back to.
	 */
	protected final void delCallback(ICallbackInterface eMethod) {
		for (int i = 0; i < callbackInfo.size(); i++) {
			if (eMethod.equals(callbackInfo.get(i))) { callbackInfo.remove(i); break; }
		}
	}
	
	/**
	 * Call the OnErrorInfo callback.
	 *
	 * @param errorInfo ParserError object to pass as error.
	 */
	protected final boolean callErrorInfo(ParserError errorInfo) {
		CallbackOnErrorInfo cb = (CallbackOnErrorInfo)myManager.getCallbackType("OnErrorInfo");
		if (cb != null) { return cb.call(errorInfo); }
		return false;
	}
	
	/**
	 * Create a new instance of the Callback Object
	 *
	 * @param parser IRCParser That owns this callback
	 * @param manager CallbackManager that is in charge of this callback
	 */
	protected CallbackObject (IRCParser parser, CallbackManager manager) {
		this.myParser = parser;
		this.myManager = manager;
	}
	
	/**
	 * Add a new callback.
	 *
	 * @param eMethod Object to callback to.
	 */
	public void add(ICallbackInterface eMethod) { addCallback(eMethod); }
	/**
	 * Remove a callback.
	 *
	 * @param eMethod Object to remove callback to.
	 */
	public void del(ICallbackInterface eMethod) { delCallback(eMethod); }
	
	/**
	 * Get the name for this callback
	 *
	 * @return Name of callback
	 */
	public String getName() {
		Package thisPackage = this.getClass().getPackage();
		int packageLength = 0;
		if (thisPackage != null) {
			packageLength = thisPackage.getName().length()+1;
		}
		return this.getClass().getName().substring(packageLength+8); // 8 is the length of "Callback"
	}
	
	/**
	 * Get the name for this callback in lowercase.
	 *
	 * @return Name of callback, in lowercase
	 */
	public final String getLowerName() {
		return this.getName().toLowerCase();
	}
	
	/**
	 * Get SVN Version information.
	 *
	 * @return SVN Version String
	 */
	public static String getSvnInfo () { return "$Id$"; }	
}