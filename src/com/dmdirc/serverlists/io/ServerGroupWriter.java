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
import com.dmdirc.serverlists.ServerGroupItem;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides methods to instantiate a writer for a server group or entry.
 *
 * @since 0.6.4
 * @author chris
 */
public class ServerGroupWriter {

    /** The identity which will be written to. */
    private final Identity identity;

    /**
     * Creates a new writer which will write to the specified identity.
     *
     * @param identity The identity to write the server group to
     */
    public ServerGroupWriter(final Identity identity) {
        this.identity = identity;
    }

    /**
     * Writes the specified server group and all of its children to this
     * writer's identity.
     *
     * @param group The group to be written
     */
    public void write(final ServerGroup group) {
        final Set<String> sections = identity.getDomains();

        identity.setOption("identity", "name", group.getName() + " servergroup");

        writeGroup(group, sections);

        for (String missing : sections) {
            for (String setting : identity.getOptions(missing).keySet()) {
                identity.unsetOption(missing, setting);
            }
        }
    }

    /**
     * Appends the specified group to this writer's identity. Any keysections
     * that are used will be removed from the sections set, so callers can
     * track which sections in the identity have not been used.
     *
     * @param group The group to be written
     * @param sections A set of sections from which any new keysections
     * should be removed
     */
    protected void writeGroup(final ServerGroup group, final Set<String> sections) {
        final String domain = group.getName() + " servergroup";

        if (!group.getLinks().isEmpty()) {
            writeLinks(group.getName(), group.getLinks());
            sections.remove(group + " links");
        }

        final List<String> children = new ArrayList<String>();
        
        for (ServerGroupItem item : group.getItems()) {
            if (item instanceof ServerGroup) {
                writeGroup((ServerGroup) item, sections);
                children.add(item.getName() + " servergroup");
            } else if (item instanceof ServerEntry) {
                writeEntry((ServerEntry) item, sections);
                children.add(item.getName() + " server");
            }
        }

        if (group.getNetwork() == null) {
            identity.unsetOption(domain, "network");
        } else {
            identity.setOption(domain, "network", group.getNetwork());
        }
        
        identity.setOption(domain, "description", group.getDescription());
        identity.setOption(domain, "contents", children);
        identity.setOption(domain, "name", group.getName());

        sections.remove(domain);
    }

    /**
     * Writes a map of links for the specified entry.
     *
     * @param name The name of the entry for which to write the links
     * @param links The links to be written
     */
    protected void writeLinks(final String name, final Map<String, URI> links) {
        final String domain = name + " links";
        final Set<String> existing = identity.getOptions(domain).keySet();
        existing.removeAll(links.keySet());

        for (String deleted : existing) {
            identity.unsetOption(domain, deleted);
        }

        for (Map.Entry<String, URI> link : links.entrySet()) {
            identity.setOption(domain, link.getKey(), link.getValue().toString());
        }
    }

    /**
     * Writes the specified entry to this writer's identity. Any keysections
     * that are used will be removed from the sections set, so callers can
     * track which sections in the identity have not been used.
     *
     * @param entry The entry to be written
     * @param sections A set of sections from which any new keysections
     * should be removed
     */
    protected void writeEntry(final ServerEntry entry, final Set<String> sections) {
        final String domain = entry.getName() + " server";

        identity.setOption(domain, "name", entry.getName());
        identity.setOption(domain, "address", entry.getAddress().toString());
        
        sections.remove(domain);
    }

}
