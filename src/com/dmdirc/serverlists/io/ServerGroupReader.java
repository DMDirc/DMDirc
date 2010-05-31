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
import com.dmdirc.serverlists.ServerGroup;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * Facilitates loading of a {@link ServerGroup} from a DMDirc {@link Identity}.
 *
 * @since 0.6.4
 * @author chris
 */
public class ServerGroupReader {

    /** The identity this reader should read from. */
    private final Identity identity;

    /** The reader we'll use for individual servers. */
    private final ServerEntryReader entryReader;

    /**
     * Creates a new ServerGroupReader that will read from the specified
     * identity.
     *
     * @param identity The identity describing the server group
     */
    public ServerGroupReader(final Identity identity) {
        this.identity = identity;
        this.entryReader = new ServerEntryReader(identity);
    }

    /**
     * Reads the default server group from this reader's identity.
     *
     * @see #read(java.lang.String)
     * @return A ServerGroup corresponding to the identity's default group
     * @throws IllegalArgumentException If the identity doesn't define a group
     */
    public ServerGroup read() {
        if (identity.hasOptionString("identity", "name")) {
            return read(null, identity.getOption("identity", "name"));
        }

        throw new IllegalArgumentException("Identity has no name");
    }

    /**
     * Reads a named server group from this reader's identity.
     *
     * @param parent The parent of the group being read
     * @param name The name of the server group to read
     * @return A corresponding ServerGroup
     * @throws IllegalArgumentException If the server group doesn't define a name
     */
    public ServerGroup read(final ServerGroup parent, final String name)
            throws IllegalArgumentException {
        if (!identity.hasOptionString(name, "name")) {
            throw new IllegalArgumentException("ServerGroup '" + name + "' not defined");
        }

        final ServerGroup group = new ServerGroup(parent,
                identity.getOption(name, "name"));

        if (identity.hasOptionString(name, "description")) {
            group.setDescription(identity.getOption(name, "description"));
        }

        if (identity.hasOptionString(name, "links")) {
            readLinks(group, identity.getOption(name, "links"));
        }

        for (String item : identity.getOptionList(name, "contents", true)) {
            if (item.endsWith(" servergroup")) {
                try {
                    group.addItem(read(group, item));
                } catch (IllegalArgumentException ex) {
                    // TODO: Raise an error about malformed group
                }
            } else if (item.endsWith(" server")) {
                try {
                    group.addItem(entryReader.read(group, item));
                } catch (URISyntaxException ex) {
                    // TODO: Raise an error about malformed server
                } catch (IllegalArgumentException ex) {
                    // TODO: Raise an error about malformed server
                }
            }

            // TODO: Raise an error about unknown content?
        }

        return group;
    }

    /**
     * Reads a set of links from the named domain and adds them to the specified
     * group.
     *
     * @param group The group to add links to
     * @param domain The domain in the identity containing links
     */
    private void readLinks(final ServerGroup group, final String domain) {
        for (Map.Entry<String, String> entry : identity.getOptions(domain).entrySet()) {
            try {
                group.addLink(entry.getKey(), new URI(entry.getValue()));
            } catch (URISyntaxException ex) {
                // TODO: Raise an error about illegal URI?
            }
        }
    }

    /**
     * Returns a writer which may be used to write updated data.
     *
     * @return An appropriately configured {@link ServerGroupWriter}
     */
    public ServerGroupWriter getWriter() {
        return new ServerGroupWriter(identity);
    }

}
