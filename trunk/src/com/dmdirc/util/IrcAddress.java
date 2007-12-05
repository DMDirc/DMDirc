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

package com.dmdirc.util;

import com.dmdirc.Server;
import com.dmdirc.ServerManager;
import com.dmdirc.config.Identity;
import com.dmdirc.config.IdentityManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses an IRC address. IRC addresses take the following form:
 * irc[s]://[[username][:password]@]<server>[:[+]port][/channel1[,channel2[,...]]]
 * 
 * @author Chris
 */
public class IrcAddress implements Serializable {
    
    private final static long serialVersionUID = 1;

    /** Whether or not this address uses SSL. */
    private boolean usesSSL;
    /** The server name for this address. */
    private String server;
    /** The port number for this address. */
    private int port = 6667;
    /** A list of channels to auto-connect to. */
    private List<String> channels = new ArrayList<String>();
    /** The password for this address. */
    private String pass = "";

    /**
     * Creates a new instance of IrcAddress.
     * 
     * @param address The address to parse
     * @throws InvalidAddressException If an invalid address is passed
     */
    public IrcAddress(final String address) throws InvalidAddressException {
        StringBuilder builder;

        if (address.toLowerCase().startsWith("ircs://")) {
            usesSSL = true;
            builder = new StringBuilder(address.substring(7));
        } else if (address.toLowerCase().startsWith("irc://")) {
            usesSSL = false;
            builder = new StringBuilder(address.substring(6));
        } else {
            throw new InvalidAddressException("Invalid protocol specified");
        }

        final int atIndex = builder.indexOf("@");
        if (atIndex > -1) {
            doPass(builder.substring(0, atIndex));
            builder.delete(0, atIndex + 1);
        }

        final int slashIndex = builder.indexOf("/");
        if (slashIndex > -1) {
            doChannels(builder.substring(slashIndex + 1));
            builder.delete(slashIndex, builder.length());
        }

        final int colonIndex = builder.indexOf(":");
        if (colonIndex > -1) {
            doPort(builder.substring(colonIndex + 1));
            builder.delete(colonIndex, builder.length());
        }

        doServer(builder.toString());
    }

    /**
     * Processes the password part of this address.
     *
     * @param pass The password part of this address
     */
    private void doPass(final String pass) {
        this.pass = pass;
    }

    /**
     * Processes the channels part of this address.
     *
     * @param channels The channels part of this address
     */
    private void doChannels(final String channels) {
        for (String channel : channels.split(",")) {
            this.channels.add(channel);
        }
    }

    /**
     * Processes the port part of this address.
     *
     * @param port The port part of this address
     * @throws InvalidAddressException if the port is non-numeric
     */
    private void doPort(final String port) throws InvalidAddressException {
        String actualPort = port;

        if (port.charAt(0) == '+') {
            usesSSL = true;
            actualPort = port.substring(1);
        }

        try {
            this.port = Integer.valueOf(actualPort);
        } catch (NumberFormatException ex) {
            throw new InvalidAddressException("Invalid port number", ex);
        }
    }

    /**
     * Processes the server part of this address.
     *
     * @param server The server part of this address
     */
    private void doServer(final String server) {
        this.server = server;
    }

    /**
     * Determines if this address requires the use of SSL or not.
     *
     * @return True if the address requires SSL, false otherwise
     */
    public boolean isSSL() {
        return usesSSL;
    }

    /**
     * Retrieves the server from this address.
     *
     * @return This address's server
     */
    public String getServer() {
        return server;
    }

    /**
     * Retrieves the port used for this address.
     *
     * @return This address's port
     */
    public int getPort() {
        return port;
    }

    /**
     * Retrieves the password used for this address.
     *
     * @return This address's password
     */
    public String getPassword() {
        return pass;
    }

    /**
     * Retrieves the list of channels for this address.
     *
     * @return This address's channels
     */
    public List<String> getChannels() {
        return channels;
    }
    
    /**
     * Connects to a server represented by this address.
     */
    public void connect() {
        connect(IdentityManager.getProfiles().get(0));
    }

    /**
     * Connects to a server represented by this address.
     * 
     * @param profile Profile to use when connecting
     */
    public void connect(final Identity profile) {
        final List<Server> servers = ServerManager.getServerManager().
                getServersByAddress(getServer());
        if (servers.isEmpty()) {
            new Server(getServer(), getPort(), getPassword(), isSSL(), 
                    profile, getChannels());
        } else {
            final Server thisServer = servers.get(0);
            for (String channel : getChannels()) {
                thisServer.join(channel);
            }
        }
    }
}
