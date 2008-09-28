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
 */

package com.dmdirc.parser.irc.callbacks;

import java.util.ArrayList;

import com.dmdirc.parser.irc.IRCParser;
import com.dmdirc.parser.irc.ParserError;
import com.dmdirc.parser.irc.callbacks.interfaces.ICallbackInterface;

/**
 * CallbackObject.
 * Superclass for all callback types.
 *
 * @author            Shane Mc Cormack
 */
public abstract class CallbackObject {
	/** Arraylist for storing callback information related to the callback. */
	protected volatile ArrayList<ICallbackInterface> callbackInfo = new ArrayList<ICallbackInterface>();
	
	/** Reference to the IRCParser that owns this callback. */
	protected IRCParser myParser;
	/** Reference to the CallbackManager in charge of this callback. */
	protected CallbackManager myManager;
	
	/**
	 * Create a new instance of the Callback Object.
	 *
	 * @param parser IRCParser That owns this callback
	 * @param manager CallbackManager that is in charge of this callback
	 */
	protected CallbackObject(final IRCParser parser, final CallbackManager manager) {
		this.myParser = parser;
		this.myManager = manager;
	}
	
	/**
	 * Add a callback pointer to the appropriate ArrayList.
	 *
	 * @param eMethod OBject to callback to.
	 */
	protected final void addCallback(final ICallbackInterface eMethod) {
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
	protected final void delCallback(final ICallbackInterface eMethod) {
		for (int i = 0; i < callbackInfo.size(); i++) {
			if (eMethod.equals(callbackInfo.get(i))) { callbackInfo.remove(i); break; }
		}
	}
	
	/**
	 * Call the OnErrorInfo callback.
	 *
	 * @param errorInfo ParserError object to pass as error.
	 * @return true if error call succeeded, false otherwise
	 */
	protected final boolean callErrorInfo(final ParserError errorInfo) {
		return ((CallbackOnErrorInfo) myManager.getCallbackType("OnErrorInfo")).call(errorInfo);
	}
	
	/**
	 * Add a new callback.
	 *
	 * @param eMethod Object to callback to.
	 */
	public void add(final ICallbackInterface eMethod) { addCallback(eMethod); }
	
	/**
	 * Remove a callback.
	 *
	 * @param eMethod Object to remove callback to.
	 */
	public void del(final ICallbackInterface eMethod) { delCallback(eMethod); }
	
	/**
	 * Get the name for this callback.
	 *
	 * @return Name of callback
	 */
	public String getName() {
		final Package thisPackage = this.getClass().getPackage();
		int packageLength = 0;
		if (thisPackage != null) {
			packageLength = thisPackage.getName().length() + 1;
		}
		return this.getClass().getName().substring(packageLength + 8); // 8 is the length of "Callback"
	}
	
	/**
	 * Get the name for this callback in lowercase.
	 *
	 * @return Name of callback, in lowercase
	 */
	public final String getLowerName() { return this.getName().toLowerCase(); }

}
