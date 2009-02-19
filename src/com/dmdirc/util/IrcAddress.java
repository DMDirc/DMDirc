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

package com.dmdirc.util;

import com.dmdirc.Server;
import com.dmdirc.ServerManager;
import com.dmdirc.config.Identity;
import com.dmdirc.config.IdentityManager;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses an IRC address. IRC addresses take the following form:
 * irc[s]://[[username][:password]@]<server>[:[+]port][/channel1[,channel2[,...]]]
 * 
 * @author Chris
 */
public class IrcAddress implements Serializable {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */    
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
        URI uri;
        String myAddress;
        
        // Check for +ports (SSL)
        myAddress = address.replaceFirst(":\\+([0-9]+)", ":$1");
        if (myAddress.length() < address.length()) {
            usesSSL = true;
        }
        
        try {
            uri = new URI(myAddress);
        } catch (URISyntaxException ex) {
            throw new InvalidAddressException("Unable to parse URI", ex);
        }
        
        if (uri.getScheme() != null && uri.getScheme().equalsIgnoreCase("ircs")) {
            usesSSL = true;
        } else if (uri.getScheme() == null || !uri.getScheme().equalsIgnoreCase("irc")) {
            throw new InvalidAddressException("Invalid protocol specified");
        }
        
        if (uri.getUserInfo() != null) {
            doPass(uri.getUserInfo());
        }
        
        doChannels(uri.getPath() + (uri.getQuery() == null ? "" :
            "?" + uri.getQuery()) + (uri.getFragment() == null ? "" :
            "#" + uri.getFragment()));

        if (uri.getPort() > -1) {
            doPort(uri.getPort());
        }

        if (uri.getHost() == null) {
            throw new InvalidAddressException("Invalid host or port specified");
        } else {
            doServer(uri.getHost());
        }
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
        if (channels == null || channels.length() == 0 || channels.charAt(0) != '/') {
            return;
        }
        
        for (String channel : channels.substring(1).split(",")) {
            if (!channel.equalsIgnoreCase("needpass") && 
                    !channel.equalsIgnoreCase("needkey") &&
                    !channel.equalsIgnoreCase("isnick") && !channel.isEmpty()) {
                this.channels.add(channel);
            }
        }
    }

    /**
     * Processes the port part of this address.
     *
     * @param port The port part of this address
     * @throws InvalidAddressException if the port is non-numeric
     */
    private void doPort(final int port) throws InvalidAddressException {
        this.port = port;
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
            for (String channel : new ArrayList<String>(getChannels())) {
                thisServer.join(channel);
            }
        }
    }
}
