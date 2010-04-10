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

package com.dmdirc.serverlists;

import com.dmdirc.Server;
import com.dmdirc.config.Identity;
import com.dmdirc.config.IdentityManager;
import java.net.URI;

/**
 * Describes an entry for a server within a {@link ServerGroup}.
 *
 * @since 0.6.4
 * @author chris
 */
public class ServerEntry implements ServerGroupItem {

    /** The address of the server in question. */
    private URI address;
    /** The user-friendly name of the server. */
    private String name;
    /** The name of the profile to use. */
    private String profile;

    /**
     * Creates a new server entry.
     *
     * @param name The name of this server
     * @param address The address of this server
     * @param profile The name of the profile to be used by this server
     */
    public ServerEntry(final String name, final URI address, final String profile) {
        this.name = name;
        this.address = address;
        this.profile = profile;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Retrieves the address used by this server.
     *
     * @return This server's address
     */
    @Override
    public URI getAddress() {
        return address;
    }

    /**
     * Retrieves the name of the profile which should be used when connecting
     * to this server.
     *
     * @return The profile name used by this entry
     */
    public String getProfile() {
        return profile;
    }

    /** {@inheritDoc} */
    @Override
    public void connect() {
        final Server server = new Server(address, getProfileIdentity());
        server.connect();
    }

    /**
     * Returns the {@link Identity} which corresponds to this server's desired
     * profile.
     *
     * @return This server's profile identity
     */
    protected Identity getProfileIdentity() {
        for (Identity identity : IdentityManager.getProfiles()) {
            if (profile.equals(identity.getName())) {
                return identity;
            }
        }

        return IdentityManager.getProfiles().get(0);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "[" + getName() + ": address: " + getAddress() + "]";
    }

}
