/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

package com.dmdirc.interfaces;

import com.dmdirc.config.profiles.Profile;

import java.net.URI;
import java.util.List;

/**
 * Manager of {@link Connection}s.
 */
public interface ConnectionManager extends ConnectionFactory {

    /**
     * Returns a list of all connections.
     *
     * @return A list of all connections
     */
    List<Connection> getConnections();

    /**
     * Makes all connections disconnect with the specified quit message.
     *
     * @param message The quit message to send to the connections
     */
    void disconnectAll(String message);

    /**
     * Closes all connections with the specified quit message.
     *
     * @param message The quit message to send to the IRC servers
     */
    void closeAll(String message);

    /**
     * Returns the number of connections that are registered with the manager.
     *
     * @return number of registered connections
     */
    int getConnectionCount();

    /**
     * Retrieves a list of connections connected to the specified network.
     *
     * @param network The network to search for
     *
     * @return A list of servers connected to the network
     */
    List<Connection> getConnectionsByNetwork(String network);

    /**
     * Creates a new connection which will connect to the specified URI with the default profile.
     *
     * @param uri The URI to connect to
     *
     * @return The new connection.
     *
     * @since 0.6.3
     */
    Connection connectToAddress(URI uri);

    /**
     * Creates a new connection which will connect to the specified URI with the specified profile.
     *
     * @param uri     The URI to connect to
     * @param profile The profile to use
     *
     * @return The server which will be connecting
     *
     * @since 0.6.3
     */
    Connection connectToAddress(URI uri, Profile profile);

    /**
     * Connects the user to Quakenet if necessary and joins #DMDirc.
     */
    void joinDevChat();
}
