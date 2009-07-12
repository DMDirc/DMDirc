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
 * irc[s]://[password@]&lt;server&gt;[:[+]port][/channel1[,channel2[,...]]]
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
    /** The protocol for this address. */
    private String protocol;
    /** The server name for this address. */
    private String server;
    /** The port number for this address. */
    private int port;
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

        if (uri.getScheme() == null) {
            throw new InvalidAddressException("Invalid protocol specified");
        }

        protocol = uri.getScheme();

        if (protocol.endsWith("s")) {
            protocol = protocol.substring(0, protocol.length() - 1);
            usesSSL = true;
        }
        
        if (uri.getUserInfo() != null) {
            doPass(uri.getUserInfo());
        }
        
        doChannels(uri.getPath() + (uri.getQuery() == null ? "" :
            "?" + uri.getQuery()) + (uri.getFragment() == null ? "" :
            "#" + uri.getFragment()));

        doPort(uri.getPort());

        if (uri.getHost() == null) {
            throw new InvalidAddressException("Invalid host or port specified");
        } else {
            doServer(uri.getHost());
        }
    }

    /**
     * Constructs a new IrcAddress object representing the specified details.
     *
     * @param host The hostname or IP of the server
     * @param port The port of the server
     * @param pass The password to use for the server
     * @param ssl Whether or not to use SSL
     * @since 0.6.3m2
     */
    public IrcAddress(final String host, final int port, final String pass,
            final boolean ssl) {
        this.server = host;
        this.port = port;
        this.pass = pass;
        this.usesSSL = ssl;
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
     */
    private void doPort(final int port) {
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
     * Retrieves the protocol from this address.
     *
     * @since 0.6.3m2
     * @return This address's protocol
     */
    public String getProtocol() {
        return protocol;
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
     * @return This address's port, or -1 if none specified.
     */
    public int getPort() {
        return port;
    }

    /**
     * Retrieves the port used for this address.
     *
     * @since 0.6.3m2
     * @param fallback The port to fall back to if none was specified
     * @return This address's port, or the fallback value if none is specified
     */
    public int getPort(final int fallback) {
        return port == -1 ? fallback : port;
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
            final Server newServer = new Server(this, profile);
            newServer.connect();
        } else {
            final Server thisServer = servers.get(0);
            for (String channel : new ArrayList<String>(getChannels())) {
                thisServer.join(channel);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        final IrcAddress other = (IrcAddress) obj;

        if (this.usesSSL != other.usesSSL) {
            return false;
        }
        
        if ((this.protocol == null) ? (other.protocol != null)
                : !this.protocol.equals(other.protocol)) {
            return false;
        }

        if ((this.server == null) ? (other.server != null)
                : !this.server.equals(other.server)) {
            return false;
        }

        if (this.port != other.port) {
            return false;
        }

        if (this.channels != other.channels && (this.channels == null
                || !this.channels.equals(other.channels))) {
            return false;
        }
        
        if ((this.pass == null) ? (other.pass != null) : !this.pass.equals(other.pass)) {
            return false;
        }
        
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + (this.usesSSL ? 1 : 0);
        hash = 67 * hash + (this.protocol != null ? this.protocol.hashCode() : 0);
        hash = 67 * hash + (this.server != null ? this.server.hashCode() : 0);
        hash = 67 * hash + this.port;
        hash = 67 * hash + (this.channels != null ? this.channels.hashCode() : 0);
        hash = 67 * hash + (this.pass != null ? this.pass.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return protocol + "://" + pass + "@" + server + ":" + (isSSL() ? "+" : "")
                + port + "/" + channels;
    }
}
