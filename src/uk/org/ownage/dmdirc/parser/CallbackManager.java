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

package uk.org.ownage.dmdirc.parser;

import java.util.Hashtable;
import java.util.Enumeration;

/**
 * IRC Parser Callback Manager.
 * Manages adding/removing/calling callbacks.
 *
 * @author            Shane Mc Cormack
 * @version           $Id: IRCParser.java 178 2007-02-28 20:36:16Z ShaneMcC $
 */
public class CallbackManager {
	/** Reference to the parser object that owns this CallbackManager*/
	IRCParser myParser = null;
			
	/** Constructor to create a CallbackManager */
	public CallbackManager(IRCParser parser) {
		myParser = parser;
		// Add callbacks
		addCallbackType(new CallbackOnChannelAction(myParser, this));
		addCallbackType(new CallbackOnChannelCTCP(myParser, this));
		addCallbackType(new CallbackOnChannelCTCPReply(myParser, this));
		addCallbackType(new CallbackOnChannelGotNames(myParser, this));
		addCallbackType(new CallbackOnChannelJoin(myParser, this));
		addCallbackType(new CallbackOnChannelKick(myParser, this));
		addCallbackType(new CallbackOnChannelMessage(myParser, this));
		addCallbackType(new CallbackOnChannelModeChanged(myParser, this));
		addCallbackType(new CallbackOnChannelNotice(myParser, this));
		addCallbackType(new CallbackOnChannelPart(myParser, this));
		addCallbackType(new CallbackOnChannelQuit(myParser, this));
		addCallbackType(new CallbackOnChannelSelfJoin(myParser, this));
		addCallbackType(new CallbackOnChannelTopic(myParser, this));
		addCallbackType(new CallbackOnChannelUserModeChanged(myParser, this));
		addCallbackType(new CallbackOnDataIn(myParser, this));
		addCallbackType(new CallbackOnDataOut(myParser, this));
		addCallbackType(new CallbackOnDebugInfo(myParser, this));
		addCallbackType(new CallbackOnErrorInfo(myParser, this));
		addCallbackType(new CallbackOnMOTDEnd(myParser, this));
		addCallbackType(new CallbackOnNickChanged(myParser, this));
		addCallbackType(new CallbackOnNickInUse(myParser, this));
		addCallbackType(new CallbackOnPrivateAction(myParser, this));
		addCallbackType(new CallbackOnPrivateCTCP(myParser, this));
		addCallbackType(new CallbackOnPrivateCTCPReply(myParser, this));
		addCallbackType(new CallbackOnPrivateMessage(myParser, this));
		addCallbackType(new CallbackOnPrivateNotice(myParser, this));
		addCallbackType(new CallbackOnQuit(myParser, this));
		addCallbackType(new CallbackOnServerReady(myParser, this));
		addCallbackType(new CallbackOnUnknownAction(myParser, this));
		addCallbackType(new CallbackOnUnknownCTCP(myParser, this));
		addCallbackType(new CallbackOnUnknownCTCPReply(myParser, this));
		addCallbackType(new CallbackOnUnknownMessage(myParser, this));
		addCallbackType(new CallbackOnUnknownNotice(myParser, this));
		addCallbackType(new CallbackOnUserModeChanged(myParser, this));
	}
	
	/** Don't allow cloning to get more copies of the CallbackManager */
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	
	private Hashtable<String,CallbackObject> callbackHash = new Hashtable<String,CallbackObject>();
	/**
	 * Add new callback type.
	 *
	 * @param callback CallbackObject subclass for the callback.
	 * @return if adding succeeded or not.
	 */
	public boolean addCallbackType(CallbackObject callback) {
		if (!callbackHash.containsKey(callback.getLowerName())) {
			callbackHash.put(callback.getLowerName(), callback);
			return true;
		}
		return false;
	}
		
	/**
	 * Remove a callback type.
	 *
	 * @param callback CallbackObject subclass to remove.
	 * @return if removal succeeded or not.
	 */
	public boolean delCallbackType(CallbackObject callback) {
		if (callbackHash.containsKey(callback.getLowerName())) {
			callbackHash.remove(callback.getLowerName());
			return true;
		}
		return false;
	}
		
	/**
	 * Get reference to callback object
	 *
	 * @param callbackName Name of callback object.
	 */
	public CallbackObject getCallbackType(String callbackName) {
		if (callbackHash.containsKey(callbackName.toLowerCase())) {
			CallbackObject res = callbackHash.get(callbackName.toLowerCase());
			return res;
		}
		return null;
	}
	
	/**
	 * Remove all callbacks.
	 *
	 * @param o instance of ICallbackInterface to remove.
	 */
	public void delAllCallback(ICallbackInterface o) {
		CallbackObject cb = null;
		for (Enumeration e = callbackHash.keys(); e.hasMoreElements();) {
			cb = callbackHash.get(e.nextElement());
			if (cb != null) { cb.del(o); }
		}
	}
	
	/**
	 * Add all callbacks.
	 *
	 * @param o instance of ICallbackInterface to add.
	 */
	public void addAllCallback(ICallbackInterface o) {
		CallbackObject cb = null;
		for (Enumeration e = callbackHash.keys(); e.hasMoreElements();) {
			cb = callbackHash.get(e.nextElement());
			if (cb != null) { cb.add(o); }
		}
	}
	
	/**
	 * Add a callback
	 *
	 * @param callbackName Name of callback object.
	 * @param o instance of ICallbackInterface to add.
	 */
	public void addCallback(String callbackName, ICallbackInterface o) {
		CallbackObject cb = getCallbackType(callbackName);
		if (cb != null) { cb.add(o); }
	}
	
	/**
	 * Add a critical callback.
	 * This method will throw a CallbackNotFound Exception if the callback does not exist.
	 * This should be used if a callback is essential for execution
	 *
	 * @param callbackName Name of callback object.
	 * @param o instance of ICallbackInterface to add.
	 */
	public void addCriticalCallback(String callbackName, ICallbackInterface o) throws CallbackNotFound {
		CallbackObject cb = getCallbackType(callbackName);
		if (cb != null) { cb.add(o); }
		else { throw new CallbackNotFound("Callback '"+callbackName+"' could not be found.");  }
	}
	
	/**
	 * Remove a callback
	 *
	 * @param callbackName Name of callback object.
	 * @param o instance of ICallbackInterface to remove.
	 */
	public void delCallback(String callbackName, ICallbackInterface o) {
		CallbackObject cb = getCallbackType(callbackName);
		if (cb != null) { cb.del(o); }
	}
	
	/**
	 * Add a callback with a specific target
	 *
	 * @param callbackName Name of callback object.
	 * @param o instance of ICallbackInterface to add.
	 * @param specific instance of ICallbackInterface to add.
	 */
	public void addCallback(String callbackName, ICallbackInterface o, String target) {
		CallbackObjectSpecific cb = (CallbackObjectSpecific)getCallbackType(callbackName);
		if (cb != null) { cb.add(o,target); }
	}

}