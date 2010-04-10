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

import com.dmdirc.serverlists.io.ServerGroupReader;
import com.dmdirc.config.Identity;
import com.dmdirc.config.IdentityListener;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.serverlists.service.ServerListServiceProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Maintains a list of top level {@link ServerGroup}s and handles reading and
 * writing of the lists to disk.
 * 
 * @since 0.6.4
 * @author chris
 */
public class ServerList implements IdentityListener {

    /** A list of all known groups. */
    private final List<ServerGroup> groups = new ArrayList<ServerGroup>();

    /**
     * Creates a new ServerList and loads groups and servers.
     */
    public ServerList() {
        IdentityManager.addIdentityListener("servergroup", this);

        for (Identity identity : IdentityManager.getCustomIdentities("servergroup")) {
            identityAdded(identity);
        }

        new ServerListServiceProvider(this).register();
    }

    /**
     * Adds a server group to the master server list.
     *
     * @param group The group to be added
     */
    public void addServerGroup(final ServerGroup group) {
        groups.add(group);
    }

    /**
     * Retrieves a list of all known server groups.
     *
     * @return An immutable list of server groups.
     */
    public List<ServerGroup> getServerGroups() {
        return Collections.unmodifiableList(groups);
    }

    /**
     * Retrieves a ServerGroup with the specified name, if one exists. This
     * method ignores the case of group's name when comparing.
     *
     * @param name The name of the group to be retrieved
     * @return A correspondingly named server group, or null if none exists
     */
    public ServerGroup getGroupByName(final String name) {
        for (ServerGroup group : getServerGroups()) {
            if (group.getName().equalsIgnoreCase(name)) {
                return group;
            }
        }

        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void identityAdded(final Identity identity) {
        addServerGroup(new ServerGroupReader(identity).read());
    }

    /** {@inheritDoc} */
    @Override
    public void identityRemoved(final Identity identity) {
        // TODO: Remove server group
    }

}
