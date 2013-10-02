/*
 * Copyright (c) 2006-2013 DMDirc Developers
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

import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.parser.common.MyInfo;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.interfaces.ProtocolDescription;
import com.dmdirc.plugins.NoSuchProviderException;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.plugins.Service;
import com.dmdirc.plugins.ServiceProvider;

import java.net.URI;

import javax.inject.Inject;

/**
 * Provides a method to retrieve a parser.
 *
 * @since 0.6
 */
public class ParserFactory {

    /** PluginManager used by this ParserFactory */
    private final PluginManager pluginManager;

    /**
     * Creates a new instance of {@link ParserFactory}.
     *
     * @param pluginManager The plugin manager used by this factory.
     */
    @Inject
    public ParserFactory(final PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    /**
     * Retrieves a parser instance.
     *
     * @param myInfo The client information to use
     * @param address The address of the server to connect to
     * @return An appropriately configured parser
     * @since 0.6.3
     */
    public Parser getParser(final MyInfo myInfo, final URI address) {
        final Object obj = getExportResult(address, "getParser", myInfo, address);

        if (obj instanceof Parser) {
            return (Parser) obj;
        }
        return null;
    }

    /**
     * Retrieves a protocol description.
     *
     * @param address The address to retrieve a description for
     * @return A corresponding protocol description
     * @since 0.6.4
     */
    public ProtocolDescription getDescription(final URI address) {
        final Object obj = getExportResult(address, "getProtocolDescription");

        if (obj instanceof ProtocolDescription) {
            return (ProtocolDescription) obj;
        }
        return null;
    }

    /**
     * Retrieves and executes an exported service with the specified name from a
     * {@link ServiceProvider} which can provide a parser for the specified
     * address. If no such provider or service is found, null is returned.
     *
     * @param address The address a provider is required for
     * @param serviceName The name of the service to be retrieved
     * @param args The arguments to be passed to the exported service
     * @return The result from a relevant exported service, or null
     * @since 0.6.4
     */
    protected Object getExportResult(final URI address,
            final String serviceName, final Object ... args) {
        // TODO: Move default scheme to a setting
        final String scheme = address.getScheme() == null ? "irc" : address.getScheme();

        try {
            final Service service = pluginManager.getService("parser", scheme);

            if (service != null && !service.getProviders().isEmpty()) {
                final ServiceProvider provider = service.getProviders().get(0);

                if (provider != null) {
                    provider.activateServices();

                    return provider.getExportedService(serviceName)
                            .execute(args);
                }
            }
        } catch (NoSuchProviderException nspe) {
            Logger.userError(ErrorLevel.MEDIUM,
                    "No parser found for: " + address.getScheme(), nspe);
        }

        return null;
    }

}
