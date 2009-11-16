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

package com.dmdirc;

import com.dmdirc.config.Identity;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.interfaces.Window;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * The ServerManager maintains a list of all servers, and provides methods to
 * search or iterate over them.
 *
 * @author chris
 */
public final class ServerManager {

    /** Singleton instance of ServerManager. */
    private static ServerManager me;

    /** All servers that currently exist. */
    private final List<Server> servers = new ArrayList<Server>();

    /**
     * Creates a new instance of ServerManager.
     */
    private ServerManager() {
    }

    /**
     * Returns the singleton instance of ServerManager.
     *
     * @return Instance of ServerManager
     */
    public static synchronized ServerManager getServerManager() {
        if (me == null) {
            me = new ServerManager();
        }
        return me;
    }

    /**
     * Registers a new server with the manager.
     *
     * @param server The server to be registered
     */
    public void registerServer(final Server server) {
        synchronized (servers) {
            servers.add(server);
        }
    }

    /**
     * Unregisters a server from the manager. The request is ignored if the
     * ServerManager is in the process of closing all servers.
     *
     * @param server The server to be unregistered
     */
    public void unregisterServer(final Server server) {
        synchronized (servers) {
            servers.remove(server);
        }
    }

    /**
     * Returns a list of all servers.
     *
     * @return A list of all servers
     */
    public List<Server> getServers() {
        return new ArrayList<Server>(servers);
    }

    /**
     * Makes all servers disconnected with the specified quit message.
     *
     * @param message The quit message to send to the IRC servers
     */
    public void disconnectAll(final String message) {
        synchronized (servers) {
            for (Server server : servers) {
                server.disconnect(message);
            }
        }
    }

    /**
     * Closes all servers with a default quit message.
     */
    public void closeAll() {
        synchronized (servers) {
            for (Server server : servers) {
                server.disconnect();
                server.close();
            }
        }
    }

    /**
     * Closes all servers with the specified quit message.
     *
     * @param message The quit message to send to the IRC servers
     */
    public void closeAll(final String message) {
        synchronized (servers) {
            for (Server server : servers) {
                server.disconnect(message);
                server.close();
            }
        }
    }

    /**
     * Returns the number of servers that are registered with the manager.
     *
     * @return number of registered servers
     */
    public int numServers() {
        return servers.size();
    }

    /**
     * Returns the server instance that owns the specified internal frame.
     *
     * @param active The internal frame to check
     * @return The server associated with the internal frame
     */
    public Server getServerFromFrame(final Window active) {
        synchronized (servers) {
            for (Server server : servers) {
                if (server.ownsFrame(active)) {
                    return server;
                }
            }
        }

        return null;
    }

    /**
     * Retrieves a list of servers connected to the specified network.
     *
     * @param network The network to search for
     * @return A list of servers connected to the network
     */
    public List<Server> getServersByNetwork(final String network) {
        final List<Server> res = new ArrayList<Server>();

        synchronized (servers) {
            for (Server server : servers) {
                if (server.isNetwork(network)) {
                    res.add(server);
                }
            }
        }

        return res;
    }

    /**
     * Retrieves a list of servers connected to the specified address.
     *
     * @param address The address to search for
     * @return A list of servers connected to the network
     */
    public List<Server> getServersByAddress(final String address) {
        final List<Server> res = new ArrayList<Server>();

        synchronized (servers) {
            for (Server server : servers) {
                if (server.getName().equalsIgnoreCase(address)) {
                    res.add(server);
                }
            }
        }

        return res;
    }

    /**
     * Creates a new server which will connect to the specified URI with the
     * default profile.
     *
     * @param uri The URI to connect to
     * @return The server which will be connecting
     * @since 0.6.4
     */
    public Server connectToAddress(final URI uri) {
        return connectToAddress(uri, IdentityManager.getProfiles().get(0));
    }

    /**
     * Creates a new server which will connect to the specified URI with the
     * specified profile.
     *
     * @param uri The URI to connect to
     * @param profile The profile to use
     * @return The server which will be connecting
     * @since 0.6.4
     */
    public Server connectToAddress(final URI uri, final Identity profile) {
        Logger.assertTrue(profile.isProfile());

        final Server server = new Server(uri, profile);
        server.connect();

        return server;
    }

    /**
     * Connects the user to Quakenet if neccessary and joins #DMDirc.
     */
    public void joinDevChat() {
        final List<Server> qnetServers = getServersByNetwork("Quakenet");

        Server connectedServer = null;

        for (Server server : qnetServers) {
            if (server.getState() == ServerState.CONNECTED) {
                connectedServer = server;

                if (server.hasChannel("#DMDirc")) {
                    server.join("#DMDirc");
                    return;
                }
            }
        }

        if (connectedServer == null) {
            try {
                final Server server = new Server(new URI("irc://irc.quakenet.org/DMDirc"),
                        IdentityManager.getProfiles().get(0));
                server.connect();
            } catch (URISyntaxException ex) {
                Logger.appError(ErrorLevel.MEDIUM, "Unable to construct new server", ex);
            }
        } else {
            connectedServer.join("#DMDirc");
        }
    }

}
