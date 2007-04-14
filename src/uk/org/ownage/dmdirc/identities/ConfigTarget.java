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

package uk.org.ownage.dmdirc.identities;

/**
 * Represents the target of a particular config source.
 * @author chris
 */
public final class ConfigTarget implements Comparable {
    
    /** Indicates that the target is a global config source. */
    public static final int TYPE_GLOBAL = 1;
    /** Indicates that the target targets an ircd. */
    public static final int TYPE_IRCD = 2;
    /** Indicates that the target targets a network. */
    public static final int TYPE_NETWORK = 3;
    /** Indicates that the target targets a server. */
    public static final int TYPE_SERVER = 4;
    /** Indicates that the target targets a channel. */
    public static final int TYPE_CHANNEL = 5;
    
    /** The type of this target. */
    private int type;
    /** The data of this target. */
    private String data;
    
    /** Creates a new instance of ConfigTarget. */
    public ConfigTarget() {
        //Do nothing.
    }
    
    /** Sets this target to be a global config source. */
    public void setGlobal() {
        type = TYPE_GLOBAL;
    }
    
    /**
     * Sets this target to target an ircd.
     * @param ircd The ircd to target
     */
    public void setIrcd(final String ircd) {
        type = TYPE_IRCD;
        data = ircd;
    }
    
    /**
     * Sets this target to target a network.
     * @param network The network to target
     */
    public void setNetwork(final String network) {
        type = TYPE_NETWORK;
        data = network;
    }
    
    /**
     * Sets this target to target a server.
     * @param server The server to target
     */
    public void setServer(final String server) {
        type = TYPE_SERVER;
        data = server;
    }
    
    /**
     * Sets this target to target a channel.
     * @param channel The channel to target, in the form of channel@network
     */
    public void setChannel(final String channel) {
        type = TYPE_CHANNEL;
        data = channel;
    }
    
    /**
     * Retrieves the type of this target.
     * @return This target's type
     */
    public int getType() {
        return type;
    }
    
    /**
     * Returns a string representation of the type of this target.
     * @return A string describing this target's type
     */
    public String getTypeName() {
        switch(type) {
            case TYPE_GLOBAL:
                return "global";
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
     * @return This target's data
     */
    public String getData() {
        return data;
    }
    
    /**
     * Compares this target to another to determine which is more specific.
     * @param target The target to compare to
     * @return -1 if this config is less specific, 0 if they're equal, +1 if
     * this is more specific
     */
    public int compareTo(final Object target) {
        return type - ((ConfigTarget) target).getType();
    }
    
    /**
     * Returns a string representation of this object.
     * @return A string representation of this object
     */
    public String toString() {
        switch (type) {
            case TYPE_GLOBAL:
                return "Global config";
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
