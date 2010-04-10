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

package com.dmdirc.serverlists.service;

import com.dmdirc.ParserFactory;
import com.dmdirc.parser.common.MyInfo;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.plugins.ExportedService;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.plugins.Service;
import com.dmdirc.plugins.ServiceProvider;

import com.dmdirc.serverlists.ServerGroup;
import com.dmdirc.serverlists.ServerGroupItem;
import com.dmdirc.serverlists.ServerList;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

/**
 * Provides a fake parser service which handles <code>serverlist://</code> URIs,
 * and returns a suitable parser for the corresponding server list.
 *
 * @author chris
 * @since 0.6.4
 */
public class ServerListServiceProvider implements ServiceProvider {

    /** The server list that we're providing for. */
    private final ServerList serverList;

    /** The services that this provider providers. */
    private final List<Service> services;

    /**
     * Creates a new server list service provider.
     *
     * @param serverList The {@link ServerList} to retrieve items from
     */
    public ServerListServiceProvider(final ServerList serverList) {
        this.serverList = serverList;
        this.services = Arrays.asList(new Service[] {
           PluginManager.getPluginManager().getService("parser", "serverlist", true)
        });
    }

    /**
     * Registers this service provider.
     */
    public void register() {
        for (Service service : services) {
            service.addProvider(this);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isActive() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void activateServices() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public List<Service> getServices() {
        return services;
    }

    /** {@inheritDoc} */
    @Override
    public String getProviderName() {
        return "Serverlist service provider";
    }

    /** {@inheritDoc} */
    @Override
    public ExportedService getExportedService(final String name) {
        if ("getParser".equals(name)) {
            return new ExportedService(ServerListServiceProvider.class, "getParser", this);
        } else {
            return new ExportedService(null, null);
        }
    }

    /**
     * Retrieves a parser for the specified details.
     *
     * @param myInfo The user-supplied client identification information
     * @param address The address to connect to (a serverlist:// URI)
     * @return A corresponding Parser instance, or null if none applicable
     */
    public Parser getParser(final MyInfo myInfo, final URI address) {
        ServerGroup group = serverList.getGroupByName(address.getHost());

        if (address.getPath() != null && !address.getPath().isEmpty()) {
            for (String part : address.getPath().split("/")) {
                if (part.isEmpty()) {
                    continue;
                }

                final ServerGroupItem item = group.getItemByName(part);

                if (item == null) {
                    return null;
                } else if (item instanceof ServerGroup) {
                    group = (ServerGroup) item;
                } else {
                    return getParserForItem(myInfo, item);
                }
            }
        }

        return getParserForItem(myInfo, group);
    }

    /**
     * Retrieves a parser for the specified item.
     *
     * @param myInfo The user-supplied client identification information
     * @param item The item to retrieve a URI from
     * @return A corresponding parser instance
     */
    protected Parser getParserForItem(final MyInfo myInfo, final ServerGroupItem item) {
        return new ParserFactory().getParser(myInfo, item.getAddress());
    }

}
