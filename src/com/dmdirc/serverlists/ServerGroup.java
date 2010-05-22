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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A server group contains an ordered collection of server entries or other
 * server groups.
 *
 * @since 0.6.4
 * @author chris
 */
public class ServerGroup implements ServerGroupItem {

    /** The name of the group. */
    private String name;

    /** The name of the network for the group. */
    private String network;

    /** A description of this group. */
    private String description = "";

    /** A set of links relevant to this group (e.g. homepages). */
    private Map<String, URI> links = new HashMap<String, URI>();
    
    /** The items contained within the group. */
    private final List<ServerGroupItem> entries = new ArrayList<ServerGroupItem>();

    /**
     * Creates a new server group with the specified name.
     *
     * @param name The name to be used for this group
     */
    public ServerGroup(final String name) {
        this.name = name;
    }

    /** {@inheritDoc} */
    @Override
    public ServerGroup getGroup() {
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this group.
     *
     * @param name The new name for the group
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Sets the network name of this group.
     *
     * @param network The new network name for the group
     */
    public void setNetwork(final String network) {
        this.network = network;
    }

    /**
     * Adds a new item to this server group.
     *
     * @param item The item to be added
     */
    public void addItem(final ServerGroupItem item) {
        entries.add(item);
    }

    /**
     * Retrieves a list of items belonging to this group.
     *
     * @return An immutable list of items contained within this group
     */
    public List<ServerGroupItem> getItems() {
        return Collections.unmodifiableList(entries);
    }

    /**
     * Retrieves a ServerGroupItem with the specified name, if one exists. This
     * method ignores the case of item's name when comparing.
     *
     * @param name The name of the item to be retrieved
     * @return A correspondingly named item, or null if none exists
     */
    public ServerGroupItem getItemByName(final String name) {
        for (ServerGroupItem item : getItems()) {
            if (item.getName().equalsIgnoreCase(name)) {
                return item;
            }
        }

        return null;
    }

    /**
     * Retrieves the description of this group.
     * 
     * @return This group's description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Retrieves the network that this group represents, if any.
     *
     * @return This group's network name, or null if the group does not
     * correspond to a known network.
     */
    public String getNetwork() {
        return network;
    }

    /**
     * Sets the description of this group.
     *
     * @param description The new description for this group.
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Retrieves a map of link titles to {@link URI}s which are associated
     * with this server group. Links will typically include network homepages,
     * forums, or support channels.
     * 
     * @return An immutable map of links
     */
    public Map<String, URI> getLinks() {
        return Collections.unmodifiableMap(links);
    }

    /**
     * Adds a new link with the specified title and address. If a link with the
     * same title existed previously, it will be replaced.
     *
     * @param title The title of the new link
     * @param address The address of the link
     */
    public void addLink(final String title, final URI address) {
        links.put(title, address);
    }

    /**
     * {@inheritDoc}
     *
     * Current implementation just selects the first item in this group and
     * asks for its URI.
     */
    @Override
    public URI getAddress() {
        if (!entries.isEmpty()) {
            return entries.get(0).getAddress();
        }

        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * Current implementation just selects the first item in this group and
     * asks it to connect.
     */
    @Override
    public void connect() {
        if (!entries.isEmpty()) {
            entries.get(0).connect();
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "[" + getName() + ": links: " + getLinks() + "; desc: "
                + getDescription() + "; content: " + getItems() + "]";
    }
}
