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
package com.dmdirc.parser.common;

import com.dmdirc.parser.irc.callbacks.*;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.interfaces.callbacks.*;

import java.util.Hashtable;
import java.util.Map;

/**
 * IRC Parser Callback Manager.
 * Manages adding/removing/calling callbacks.
 *
 * @param <T> The type of parser which this manager managers callbacks for
 * @author            Shane Mc Cormack
 */
public abstract class CallbackManager<T extends Parser> {

    protected static final Class[] CLASSES = {
        AwayStateListener.class, OtherAwayStateListener.class,
        ChannelOtherAwayStateListener.class, ChannelActionListener.class,
        ChannelCtcpListener.class, ChannelCtcpReplyListener.class,
        ChannelListModeListener.class, ChannelNamesListener.class,
        ChannelJoinListener.class, ChannelKickListener.class,
        ChannelMessageListener.class, ChannelModeChangeListener.class,
        ChannelNickChangeListener.class, ChannelNonUserModeChangeListener.class,
	ChannelModeMessageListener.class, ChannelModeNoticeListener.class,
        ChannelNoticeListener.class, ChannelPartListener.class, ChannelQuitListener.class,
        ChannelSelfJoinListener.class, ChannelSingleModeChangeListener.class,
        ChannelTopicListener.class, ChannelUserModeChangeListener.class, ConnectErrorListener.class,
        DataInListener.class, DataOutListener.class, DebugInfoListener.class, ErrorInfoListener.class,
        NetworkDetectedListener.class, InviteListener.class, MotdEndListener.class, MotdLineListener.class,
        MotdStartListener.class, NickChangeListener.class, NickInUseListener.class,
        AuthNoticeListener.class, NumericListener.class, PasswordRequiredListener.class,
        PingFailureListener.class, PingSuccessListener.class, PingSentListener.class, PrivateActionListener.class,
        PrivateCtcpListener.class, PrivateCtcpReplyListener.class, PrivateMessageListener.class,
        PrivateNoticeListener.class, Post005Listener.class, QuitListener.class, ServerErrorListener.class,
        ServerReadyListener.class, SocketCloseListener.class, UnknownActionListener.class,
        UnknownCtcpListener.class, UnknownCtcpReplyListener.class, UnknownMessageListener.class,
        UnknownNoticeListener.class, UserModeChangeListener.class, UserModeDiscoveryListener.class,
        WallDesyncListener.class, WallopListener.class, WalluserListener.class,
    };

    /** Reference to the parser object that owns this CallbackManager. */
    private final Parser myParser;
    
    /** Hashtable used to store the different types of callback known. */
    private final Map<Class<? extends CallbackInterface>, CallbackObject> callbackHash
            = new Hashtable<Class<? extends CallbackInterface>, CallbackObject>();

    /**
     * Constructor to create a CallbackManager.
     *
     * @param parser Parser that owns this callback manager.
     */
    public CallbackManager(final T parser) {
        myParser = parser;

        initialise(parser);
    }

    /**
     * Initialises this callback manager by calling
     * {@link #addCallbackType(CallbackObject)} with a relevant
     * {@link CallbackObject} instance for each entry in the
     * <code>CLASSES</code> array.
     *
     * @param parser The parser associated with this CallbackManager
     */
    protected abstract void initialise(T parser);

    /**
     * Add new callback type.
     *
     * @param callback CallbackObject subclass for the callback.
     * @return if adding succeeded or not.
     */
    public boolean addCallbackType(final CallbackObject callback) {
        if (!callbackHash.containsKey(callback.getType())) {
            callbackHash.put(callback.getType(), callback);
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
        if (callbackHash.containsKey(callback.getType())) {
            callbackHash.remove(callback.getType());
            return true;
        }

        return false;
    }

    /**
     * Get reference to callback object.
     *
     * @param callback Name of type of callback object.
     * @return CallbackObject returns the callback object for this type
     */
    public CallbackObject getCallbackType(final Class<? extends CallbackInterface> callback) {
        if (!callbackHash.containsKey(callback)) {
            throw new CallbackNotFoundException("Callback not found: " + callback.getName());
        }

        return callbackHash.get(callback);
    }

    /**
     * Remove all callbacks associated with a specific object.
     *
     * @param o instance of ICallbackInterface to remove.
     */
    public void delAllCallback(final CallbackInterface o) {
        for (CallbackObject cb : callbackHash.values()) {
            if (cb != null) {
                cb.del(o);
            }
        }
    }

    /**
     * Add all callbacks.
     *
     * @param o instance of ICallbackInterface to add.
     */
    public void addAllCallback(final CallbackInterface o) {
        for (CallbackObject cb : callbackHash.values()) {
            if (cb != null) {
                cb.add(o);
            }
        }
    }

    /**
     * Add a callback.
     * This method will throw a CallbackNotFoundException if the callback does not exist.
     *
     * @param <T> The type of callback
     * @param callback Type of callback object
     * @param o instance of ICallbackInterface to add.
     * @throws CallbackNotFoundException If callback is not found.
     * @throws NullPointerException If 'o' is null
     */
    public <T extends CallbackInterface> void addCallback(
            final Class<T> callback, final T o) throws CallbackNotFoundException {
        if (o == null) {
            throw new NullPointerException("CallbackInterface is null");
        }

        final CallbackObject cb = getCallbackType(callback);

        if (cb != null) {
            cb.add(o);
        }
    }

    /**
     * Add a callback with a specific target.
     * This method will throw a CallbackNotFoundException if the callback does not exist.
     *
     * @param <T> The type of callback
     * @param callback Type of callback object.
     * @param o instance of ICallbackInterface to add.
     * @param target Parameter to specify that a callback should only fire for specific things
     * @throws CallbackNotFoundException If callback is not found.
     * @throws NullPointerException If 'o' is null
     */
    public <T extends CallbackInterface> void addCallback(
            final Class<T> callback,
            final T o, final String target) throws CallbackNotFoundException {
        if (o == null) {
            throw new NullPointerException("CallbackInterface is null");
        }
        
        ((CallbackObjectSpecific) getCallbackType(callback)).add(o, target);
    }

    /**
     * Add a callback without an exception.
     * This should be used if a callback is not essential for execution (ie the DebugOut callback)
     *
     * @param <T> The type of callback object
     * @param callback Type of callback object.
     * @param o instance of ICallbackInterface to add.
     * @return true/false if the callback was added or not.
     */
    public <T extends CallbackInterface> boolean addNonCriticalCallback(
            final Class<T> callback, final T o) {
        try {
            addCallback(callback, o);
            return true;
        } catch (CallbackNotFoundException e) {
            return false;
        }
    }

    /**
     * Add a callback with a specific target.
     * This should be used if a callback is not essential for execution
     *
     * @param <T> The type of callback
     * @param callback Type of callback object.
     * @param o instance of ICallbackInterface to add.
     * @param target Parameter to specify that a callback should only fire for specific things
     * @return true/false if the callback was added or not.
     */
    public <T extends CallbackInterface> boolean addNonCriticalCallback(
            final Class<T> callback, final T o, final String target) {
        try {
            addCallback(callback, o, target);
            return true;
        } catch (CallbackNotFoundException e) {
            return false;
        }
    }

    /**
     * Remove a callback.
     *
     * @param callback Type of callback object.
     * @param o instance of ICallbackInterface to remove.
     */
    public void delCallback(final Class<? extends CallbackInterface> callback,
            final CallbackInterface o) {
        getCallbackType(callback).del(o);
    }
}
