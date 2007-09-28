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
 */

package com.dmdirc.config;

import java.io.Serializable;

/**
 * Represents the target of a particular config source.
 * <p>
 * Note: this class has a natural ordering that is inconsistent with equals.
 * 
 * @author chris
 */
public final class ConfigTarget implements Comparable, Serializable {
    
    // TODO: Convert this lot to an enum.
    
    /** Indicates that the target is the global default config. */
    public static final int TYPE_GLOBALDEFAULT = 0;
    /** Indicates that the target is a global config source. */
    public static final int TYPE_GLOBAL = 1;
    /** Indicates that the target is a theme. */
    public static final int TYPE_THEME = 2;
    /** Indicates that the target is a profile. */
    public static final int TYPE_PROFILE = 3;    
    /** Indicates that the target targets an ircd. */
    public static final int TYPE_IRCD = 4;
    /** Indicates that the target targets a network. */
    public static final int TYPE_NETWORK = 5;
    /** Indicates that the target targets a server. */
    public static final int TYPE_SERVER = 6;
    /** Indicates that the target targets a channel. */
    public static final int TYPE_CHANNEL = 7;
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    
    /** The type of this target. */
    private int type = 1;
    /** The data of this target. */
    private String data;
    
    /** Creates a new instance of ConfigTarget. */
    public ConfigTarget() {
        //Do nothing.
    }
    
    /** Sets this target to be a global config source. */
    public void setGlobal() {
        type = TYPE_GLOBAL;
        data = "";
    }
    
    /** Sets this target to be a global default source. */
    public void setGlobalDefault() {
        type = TYPE_GLOBALDEFAULT;
        data = "";
    }
    
    /** Sets this target to be a theme source. */
    public void setTheme() {
        type = TYPE_THEME;
        data = "";
    }
    
    /** Sets this target to be a profile source. */
    public void setProfile() {
        type = TYPE_PROFILE;
        data = "";
    }
    
    /**
     * Sets this target to target an ircd.
     * 
     * @param ircd The ircd to target
     */
    public void setIrcd(final String ircd) {
        type = TYPE_IRCD;
        data = ircd;
    }
    
    /**
     * Sets this target to target a network.
     * 
     * @param network The network to target
     */
    public void setNetwork(final String network) {
        type = TYPE_NETWORK;
        data = network;
    }
    
    /**
     * Sets this target to target a server.
     * 
     * @param server The server to target
     */
    public void setServer(final String server) {
        type = TYPE_SERVER;
        data = server;
    }
    
    /**
     * Sets this target to target a channel.
     * 
     * @param channel The channel to target, in the form of channel@network
     */
    public void setChannel(final String channel) {
        type = TYPE_CHANNEL;
        data = channel;
    }
    
    /**
     * Retrieves the type of this target.
     * 
     * @return This target's type
     */
    public int getType() {
        return type;
    }
    
    /**
     * Returns a string representation of the type of this target.
     * 
     * @return A string describing this target's type
     */
    public String getTypeName() {
        switch(type) {
        case TYPE_GLOBALDEFAULT:
            return "globaldefault";
        case TYPE_GLOBAL:
            return "global";
        case TYPE_THEME:
            return "theme";
        case TYPE_PROFILE:
            return "profile";
        case TYPE_IRCD:
            return "ircd";
        case TYPE_NETWORK:
            return "network";
        case TYPE_SERVER:
            return "server";
        case TYPE_CHANNEL:
            return "channel";
        default:
            return "Unknown";
        }
    }
    
    /**
     * Retrieves the data associated with this target.
     * 
     * @return This target's data
     */
    public String getData() {
        return data;
    }
    
    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return type + data.hashCode();
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof ConfigTarget
                && type == ((ConfigTarget) obj).getType()
                && data.equals(((ConfigTarget) obj).getData())) {
            return true;
        }
        return false;
    }
    
    /**
     * Compares this target to another to determine which is more specific.
     * 
     * @param target The target to compare to
     * @return a negative integer if this config is less specific, 0 if they're
     * equal, or a positive integer if this is more specific
     */
    public int compareTo(final Object target) {
        return type - ((ConfigTarget) target).getType();
    }
    
    /**
     * Returns a string representation of this object.
     * 
     * @return A string representation of this object
     */
    public String toString() {
        switch (type) {
        case TYPE_GLOBALDEFAULT:
            return "Global defaults";
        case TYPE_GLOBAL:
            return "Global config";
        case TYPE_THEME:
            return "Theme";
        case TYPE_PROFILE:
            return "Profile";
        case TYPE_IRCD:
            return "Ircd specific: " + data;
        case TYPE_NETWORK:
            return "Network specific: " + data;
        case TYPE_SERVER:
            return "Server specific: " + data;
        case TYPE_CHANNEL:
            return "Channel specific: " + data;
        default:
            return "Unknown";
        }
    }
    
}
