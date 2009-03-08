/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.parser.irc.IRCParser;
import com.dmdirc.parser.irc.callbacks.interfaces.*;

import java.util.Hashtable;
import java.util.Map;

/**
 * IRC Parser Callback Manager.
 * Manages adding/removing/calling callbacks.
 *
 * @author            Shane Mc Cormack
 */
public final class CallbackManager {

    private static final Class[] CLASSES = {
        IAwayState.class, IAwayStateOther.class, IChannelAwayStateOther.class,
        IChannelAction.class, IChannelCTCP.class, IChannelCTCPReply.class,
        IChannelGotListModes.class, IChannelGotNames.class, IChannelJoin.class,
        IChannelKick.class, IChannelMessage.class, IChannelModeChanged.class,
        IChannelNickChanged.class, IChannelNonUserModeChanged.class,
        IChannelNotice.class, IChannelPart.class, IChannelQuit.class,
        IChannelSelfJoin.class, IChannelSingleModeChanged.class,
        IChannelTopic.class, IChannelUserModeChanged.class, IConnectError.class,
        IDataIn.class, IDataOut.class, IDebugInfo.class, IErrorInfo.class,
        IGotNetwork.class, IInvite.class, IMOTDEnd.class, IMOTDLine.class,
        IMOTDStart.class, INickChanged.class, INickInUse.class,
        INoticeAuth.class, INumeric.class, IPasswordRequired.class,
        IPingFailed.class, IPingSuccess.class, IPingSent.class, IPrivateAction.class,
        IPrivateCTCP.class, IPrivateCTCPReply.class, IPrivateMessage.class,
        IPrivateNotice.class, IPost005.class, IQuit.class, IServerError.class,
        IServerReady.class, ISocketClosed.class, IUnknownAction.class,
        IUnknownCTCP.class, IUnknownCTCPReply.class, IUnknownMessage.class,
        IUnknownNotice.class, IUserModeChanged.class, IUserModeDiscovered.class,
        IWallDesync.class, IWallop.class, IWalluser.class,
    };

	/** Reference to the parser object that owns this CallbackManager. */
	IRCParser myParser;
	
	/** Hashtable used to store the different types of callback known. */
	private final Map<String, CallbackObject> callbackHash = new Hashtable<String, CallbackObject>();
	
	/**
	 * Constructor to create a CallbackManager.
	 *
	 * @param parser IRCParser that owns this callback manager.
	 */
	public CallbackManager(final IRCParser parser) {
		myParser = parser;
		
        for (Class<?> type : CLASSES) {
            if (type.isAnnotationPresent(SpecificCallback.class)) {
                addCallbackType(new CallbackObjectSpecific(myParser, this,
                        type.asSubclass(ICallbackInterface.class)));
            } else {
                addCallbackType(new CallbackObject(myParser, this,
                        type.asSubclass(ICallbackInterface.class)));
            }
        }
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
		} catch (CallbackNotFoundException e) { return false; }
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
		} catch (CallbackNotFoundException e) { return false;	}
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
