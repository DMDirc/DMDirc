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

package com.dmdirc.parser.callbacks;

import java.util.Hashtable;

import com.dmdirc.parser.IRCParser;
import com.dmdirc.parser.callbacks.interfaces.ICallbackInterface;

/**
 * IRC Parser Callback Manager.
 * Manages adding/removing/calling callbacks.
 *
 * @author            Shane Mc Cormack
 * @version           $Id$
 */
public final class CallbackManager {
	/** Reference to the parser object that owns this CallbackManager. */
	IRCParser myParser;
	
	/** Hashtable used to store the different types of callback known. */
	private Hashtable<String, CallbackObject> callbackHash = new Hashtable<String, CallbackObject>();
	
	/**
	 * Constructor to create a CallbackManager.
	 *
	 * @param parser IRCParser that owns this callback manager.
	 */
	public CallbackManager(final IRCParser parser) {
		myParser = parser;
		// Add callbacks
		addCallbackType(new CallbackOnAwayState(myParser, this));
		addCallbackType(new CallbackOnAwayStateOther(myParser, this));
		addCallbackType(new CallbackOnChannelAwayStateOther(myParser, this));
		addCallbackType(new CallbackOnChannelAction(myParser, this));
		addCallbackType(new CallbackOnChannelCTCP(myParser, this));
		addCallbackType(new CallbackOnChannelCTCPReply(myParser, this));
		addCallbackType(new CallbackOnChannelGotListModes(myParser, this));
		addCallbackType(new CallbackOnChannelGotNames(myParser, this));
		addCallbackType(new CallbackOnChannelJoin(myParser, this));
		addCallbackType(new CallbackOnChannelKick(myParser, this));
		addCallbackType(new CallbackOnChannelMessage(myParser, this));
		addCallbackType(new CallbackOnChannelModeChanged(myParser, this));
		addCallbackType(new CallbackOnChannelNickChanged(myParser, this));
		addCallbackType(new CallbackOnChannelNonUserModeChanged(myParser, this));
		addCallbackType(new CallbackOnChannelNotice(myParser, this));
		addCallbackType(new CallbackOnChannelPart(myParser, this));
		addCallbackType(new CallbackOnChannelQuit(myParser, this));
		addCallbackType(new CallbackOnChannelSelfJoin(myParser, this));
		addCallbackType(new CallbackOnChannelSingleModeChanged(myParser, this));
		addCallbackType(new CallbackOnChannelTopic(myParser, this));
		addCallbackType(new CallbackOnChannelUserModeChanged(myParser, this));
		addCallbackType(new CallbackOnConnectError(myParser, this));
		addCallbackType(new CallbackOnDataIn(myParser, this));
		addCallbackType(new CallbackOnDataOut(myParser, this));
		addCallbackType(new CallbackOnDebugInfo(myParser, this));
		addCallbackType(new CallbackOnErrorInfo(myParser, this));
		addCallbackType(new CallbackOnGotNetwork(myParser, this));
		addCallbackType(new CallbackOnInvite(myParser, this));
		addCallbackType(new CallbackOnMOTDEnd(myParser, this));
		addCallbackType(new CallbackOnMOTDLine(myParser, this));
		addCallbackType(new CallbackOnMOTDStart(myParser, this));
		addCallbackType(new CallbackOnNickChanged(myParser, this));
		addCallbackType(new CallbackOnNickInUse(myParser, this));
		addCallbackType(new CallbackOnNoticeAuth(myParser, this));
		addCallbackType(new CallbackOnNumeric(myParser, this));
		addCallbackType(new CallbackOnPasswordRequired(myParser, this));
		addCallbackType(new CallbackOnPingFailed(myParser, this));
		addCallbackType(new CallbackOnPingSent(myParser, this));
		addCallbackType(new CallbackOnPingSuccess(myParser, this));
		addCallbackType(new CallbackOnPrivateAction(myParser, this));
		addCallbackType(new CallbackOnPrivateCTCP(myParser, this));
		addCallbackType(new CallbackOnPrivateCTCPReply(myParser, this));
		addCallbackType(new CallbackOnPrivateMessage(myParser, this));
		addCallbackType(new CallbackOnPrivateNotice(myParser, this));
		addCallbackType(new CallbackOnPost005(myParser, this));
		addCallbackType(new CallbackOnQuit(myParser, this));
		addCallbackType(new CallbackOnServerError(myParser, this));
		addCallbackType(new CallbackOnServerReady(myParser, this));
		addCallbackType(new CallbackOnSocketClosed(myParser, this));
		addCallbackType(new CallbackOnUnknownAction(myParser, this));
		addCallbackType(new CallbackOnUnknownCTCP(myParser, this));
		addCallbackType(new CallbackOnUnknownCTCPReply(myParser, this));
		addCallbackType(new CallbackOnUnknownMessage(myParser, this));
		addCallbackType(new CallbackOnUnknownNotice(myParser, this));
		addCallbackType(new CallbackOnUserModeChanged(myParser, this));
		addCallbackType(new CallbackOnUserModeDiscovered(myParser, this));
		addCallbackType(new CallbackOnWallDesync(myParser, this));
		addCallbackType(new CallbackOnWallop(myParser, this));
		addCallbackType(new CallbackOnWalluser(myParser, this));
	}

	/**
	 * Add new callback type.
	 *
	 * @param callback CallbackObject subclass for the callback.
	 * @return if adding succeeded or not.
	 */
	public boolean addCallbackType(final CallbackObject callback) {
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
	public boolean delCallbackType(final CallbackObject callback) {
		if (callbackHash.containsKey(callback.getLowerName())) {
			callbackHash.remove(callback.getLowerName());
			return true;
		}
		return false;
	}
	
	/**
	 * Get reference to callback object.
	 *
	 * @param callbackName Name of callback object.
	 * @return CallbackObject returns the callback object for this type
	 */
	public CallbackObject getCallbackType(final String callbackName) {
		if (!callbackHash.containsKey(callbackName.toLowerCase())) {
			throw new CallbackNotFoundException("Callback not found: " + callbackName);
		}
        
		return callbackHash.get(callbackName.toLowerCase());
	}
	
	/**
	 * Remove all callbacks associated with a specific object.
	 *
	 * @param o instance of ICallbackInterface to remove.
	 */
	public void delAllCallback(final ICallbackInterface o) {
		for (CallbackObject cb : callbackHash.values()) {
			if (cb != null) { cb.del(o); }
		}
	}
	
	/**
	 * Add all callbacks.
	 *
	 * @param o instance of ICallbackInterface to add.
	 */
	public void addAllCallback(final ICallbackInterface o) {
		for (CallbackObject cb : callbackHash.values()) {
			if (cb != null) { cb.add(o); }
		}
	}
	
	/**
	 * Add a callback.
 	 * This method will throw a CallbackNotFoundException if the callback does not exist.
	 *
	 * @param callbackName Name of callback object.
	 * @param o instance of ICallbackInterface to add.
	 * @throws CallbackNotFoundException If callback is not found.
	 * @throws NullPointerException If 'o' is null
	 */
	public void addCallback(final String callbackName, final ICallbackInterface o) throws CallbackNotFoundException {
		if (o == null) {
            throw new NullPointerException("CallbackInterface is null");
        }
        
        final CallbackObject cb = getCallbackType(callbackName);
		
        if (cb != null) { cb.add(o); }
	}
	
	/**
	 * Add a callback with a specific target.
 	 * This method will throw a CallbackNotFoundException if the callback does not exist.
	 *
	 * @param callbackName Name of callback object.
	 * @param o instance of ICallbackInterface to add.
	 * @param target Parameter to specify that a callback should only fire for specific things
	 * @throws CallbackNotFoundException If callback is not found.
	 * @throws NullPointerException If 'o' is null
	 */
	public void addCallback(final String callbackName, final ICallbackInterface o, final String target) throws CallbackNotFoundException {
		if (o == null) { throw new NullPointerException("CallbackInterface is null"); }

        ((CallbackObjectSpecific) getCallbackType(callbackName)).add(o,target);
	}
	
	/**
	 * Add a callback without an exception.
	 * This should be used if a callback is not essential for execution (ie the DebugOut callback)
	 *
	 * @param callbackName Name of callback object.
	 * @param o instance of ICallbackInterface to add.
	 * @return true/false if the callback was added or not.
	 */
	public boolean addNonCriticalCallback(final String callbackName, final ICallbackInterface o)  {
		try {
			addCallback(callbackName, o);
			return true;
		} catch (Exception e) { return false; }
	}
	
	/**
	 * Add a callback with a specific target.
	 * This should be used if a callback is not essential for execution
	 *
	 * @param callbackName Name of callback object.
	 * @param o instance of ICallbackInterface to add.
	 * @param target Parameter to specify that a callback should only fire for specific things
	 * @return true/false if the callback was added or not.
	 */
	public boolean addNonCriticalCallback(final String callbackName, final ICallbackInterface o, final String target) {
		try {
			addCallback(callbackName, o, target);
			return true;
		} catch (Exception e) { return false;	}
	}
	
	
	/**
	 * Remove a callback.
	 *
	 * @param callbackName Name of callback object.
	 * @param o instance of ICallbackInterface to remove.
	 */
	public void delCallback(final String callbackName, final ICallbackInterface o) {
		getCallbackType(callbackName).del(o);
	}

}
