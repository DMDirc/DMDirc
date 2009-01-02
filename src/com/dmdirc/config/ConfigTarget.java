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

package com.dmdirc.config;

import java.io.Serializable;

/**
 * Represents the target of a particular config source.
 * <p>
 * Note: this class has a natural ordering that is inconsistent with equals.
 *
 * @author chris
 */
public class ConfigTarget implements Comparable, Serializable {

    /** The possible target types. */
    public static enum TYPE {
        /** Client-wide default settings. */
        GLOBALDEFAULT,
        /** Client-wide settings. */
        GLOBAL,
        /** Settings for a theme. */
        THEME,
        /** Settings for a profile. */
        PROFILE,
        /** Settings for an IRCd. */
        IRCD,
        /** Settings for a network. */
        NETWORK,
        /** Settings for a server. */
        SERVER,
        /** Settings for a channel. */
        CHANNEL,
    }

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;

    /** The type of this target. */
    protected TYPE type = ConfigTarget.TYPE.GLOBAL;

    /** The data of this target. */
    protected String data;

    /** The user-defined ordering for this target. */
    protected int order = 50000;

    /** Creates a new instance of ConfigTarget. */
    public ConfigTarget() {
        //Do nothing.
    }

    /**
     * Sets the ordering value for this target. Lower means higher preference.
     *
     * @param order The new order to use
     */
    public void setOrder(final int order) {
        this.order = order;
    }

    /**
     * Retrieves the ordering value for this target. Lower means higher preference.
     *
     * @return This target's order
     */
    public int getOrder() {
        return order;
    }

    /** Sets this target to be a global config source. */
    public void setGlobal() {
        type = TYPE.GLOBAL;
        data = "";
    }

    /** Sets this target to be a global default source. */
    public void setGlobalDefault() {
        type = TYPE.GLOBALDEFAULT;
        data = "";
    }

    /** Sets this target to be a theme source. */
    public void setTheme() {
        type = TYPE.THEME;
        data = "";
    }

    /** Sets this target to be a profile source. */
    public void setProfile() {
        type = TYPE.PROFILE;
        data = "";
    }

    /**
     * Sets this target to target an ircd.
     *
     * @param ircd The ircd to target
     */
    public void setIrcd(final String ircd) {
        type = TYPE.IRCD;
        data = ircd;
    }

    /**
     * Sets this target to target a network.
     *
     * @param network The network to target
     */
    public void setNetwork(final String network) {
        type = TYPE.NETWORK;
        data = network;
    }

    /**
     * Sets this target to target a server.
     *
     * @param server The server to target
     */
    public void setServer(final String server) {
        type = TYPE.SERVER;
        data = server;
    }

    /**
     * Sets this target to target a channel.
     *
     * @param channel The channel to target, in the form of channel@network
     */
    public void setChannel(final String channel) {
        type = TYPE.CHANNEL;
        data = channel;
    }

    /**
     * Retrieves the type of this target.
     *
     * @return This target's type
     */
    public TYPE getType() {
        return type;
    }

    /**
     * Returns a string representation of the type of this target.
     *
     * @return A string describing this target's type
     */
    public String getTypeName() {
        switch(type) {
        case GLOBALDEFAULT:
            return "globaldefault";
        case THEME:
            return "theme";
        case PROFILE:
            return "profile";
        case IRCD:
            return "ircd";
        case NETWORK:
            return "network";
        case SERVER:
            return "server";
        case CHANNEL:
            return "channel";
        default:
            return "global";
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
        return type.ordinal() + data.hashCode();
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
        if (type.equals(((ConfigTarget) target).getType())) {
            return ((ConfigTarget) target).getOrder() - order;
        } else {
            return type.compareTo(((ConfigTarget) target).getType());
        }
    }

    /**
     * Returns a string representation of this object.
     *
     * @return A string representation of this object
     */
    @Override
    public String toString() {
        switch (type) {
        case GLOBALDEFAULT:
            return "Global defaults";
        case THEME:
            return "Theme";
        case PROFILE:
            return "Profile";
        case IRCD:
            return "Ircd specific: " + data;
        case NETWORK:
            return "Network specific: " + data;
        case SERVER:
            return "Server specific: " + data;
        case CHANNEL:
            return "Channel specific: " + data;
        default:
            return "Global config";
        }
    }

}
