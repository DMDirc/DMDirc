/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.serverlists.io;

import com.dmdirc.config.Identity;
import com.dmdirc.serverlists.ServerEntry;
import com.dmdirc.serverlists.ServerGroup;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Facilitates loading of a {@link ServerEntry} from a DMDirc {@link Identity}.
 *
 * @since 0.6.4
 * @author chris
 */
public class ServerEntryReader {

    /** The identity to read entries from. */
    private final Identity identity;

    /**
     * Creates a new Server Entry Reader which will read from the specified
     * identity.
     *
     * @param identity The identity which defines our server entries
     */
    public ServerEntryReader(final Identity identity) {
        this.identity = identity;
    }

    /**
     * Attempts to read the details of the specified server from this reader's
     * identity.
     *
     * @param group The group that owns this server
     * @param name The name of the server to be read
     * @return A corresponding ServerEntry
     * @throws URISyntaxException If the server doesn't specify a valid URI
     * @throws IllegalArgumentException If the server doesn't define a name or address
     */
    public ServerEntry read(final ServerGroup group, final String name) throws URISyntaxException,
            IllegalArgumentException {
        if (!identity.hasOptionString(name, "name")
                || !identity.hasOptionString(name, "address")) {
            throw new IllegalArgumentException("Server does not specify name or address: "
                    + name);
        }

        final String serverName = identity.getOption(name, "name");
        final URI serverURI = new URI(identity.getOption(name, "address"));

        return new ServerEntry(group, serverName, serverURI, null);
    }

}
