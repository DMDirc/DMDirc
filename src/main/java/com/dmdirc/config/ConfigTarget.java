/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.config;

import java.io.Serializable;

import javax.annotation.Nonnull;

/**
 * Represents the target of a particular config source.
 * <p>
 * Note: this class has a natural ordering that is inconsistent with equals.
 */
public class ConfigTarget implements Comparable<ConfigTarget>, Serializable {

    /** The possible target types. */
    public enum TYPE {

        /** Client-wide default settings. */
        GLOBALDEFAULT,
        /** Client-wide settings. */
        GLOBAL,
        /** Settings for a theme. */
        THEME,
        /** Settings for an IRCd. */
        IRCD,
        /** Settings for a network. */
        NETWORK,
        /** Settings for a server. */
        SERVER,
        /** Settings for a channel. */
        CHANNEL,
        /** Settings for a protocol (parser). */
        PROTOCOL,
        /** A custom identity, which doesn't contain settings to be loaded. */
        CUSTOM,

    }
    /** A version number for this class. */
    private static final long serialVersionUID = 2;
    /** The type of this target. */
    protected TYPE type = ConfigTarget.TYPE.GLOBAL;
    /** The data of this target. */
    protected String data;
    /** The user-defined ordering for this target. */
    protected int order = 50000;

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

    /**
     * Sets this target to be a custom identity.
     *
     * @param customType The type of custom identity
     *
     * @since 0.6.4
     */
    public void setCustom(final String customType) {
        type = TYPE.CUSTOM;
        data = customType;
    }

    /**
     * Determines if this target is the specified custom type.
     *
     * @param customType The type of custom identity
     *
     * @return True if this target is a CUSTOM type with the specified type.
     *
     * @since 0.6.4
     */
    public boolean isCustom(final String customType) {
        return type == TYPE.CUSTOM && customType.equals(data);
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
     * Sets this target to target a protocol.
     *
     * @param protocol The protocol to target
     *
     * @since 0.6.3
     */
    public void setProtocol(final String protocol) {
        type = TYPE.PROTOCOL;
        data = protocol;
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
        return type.toString().toLowerCase();
    }

    /**
     * Retrieves the data associated with this target.
     *
     * @return This target's data
     */
    public String getData() {
        return data;
    }

    @Override
    public int hashCode() {
        return type.ordinal() + data.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof ConfigTarget
                && type == ((ConfigTarget) obj).getType()
                && data.equals(((ConfigTarget) obj).getData());
    }

    /**
     * Compares this target to another to determine which is more specific.
     *
     * @param target The target to compare to
     *
     * @return a negative integer if this config is less specific, 0 if they're equal, or a positive
     *         integer if this is more specific
     */
    @Override
    public int compareTo(@Nonnull final ConfigTarget target) {
        if (type == target.getType()) {
            return target.getOrder() - order;
        } else {
            return type.compareTo(target.getType());
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
            case CUSTOM:
                return "Custom: " + data;
            case IRCD:
                return "Ircd specific: " + data;
            case NETWORK:
                return "Network specific: " + data;
            case SERVER:
                return "Server specific: " + data;
            case CHANNEL:
                return "Channel specific: " + data;
            case PROTOCOL:
                return "Protocol specific: " + data;
            default:
                return "Global config";
        }
    }

}
