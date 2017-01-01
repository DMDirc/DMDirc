/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc;

import com.dmdirc.config.profiles.Profile;
import com.dmdirc.interfaces.config.ReadOnlyConfigProvider;
import com.dmdirc.parser.common.MyInfo;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.interfaces.ProtocolDescription;
import com.dmdirc.plugins.NoSuchProviderException;
import com.dmdirc.plugins.Service;
import com.dmdirc.plugins.ServiceManager;
import com.dmdirc.plugins.ServiceProvider;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.dmdirc.util.LogUtils.APP_ERROR;
import static com.dmdirc.util.LogUtils.USER_ERROR;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provides a method to retrieve a parser.
 *
 * @since 0.6
 */
public class ParserFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ParserFactory.class);

    /** The name of the general domain. */
    private static final String DOMAIN_GENERAL = "general";
    /** The name of the server domain. */
    private static final String DOMAIN_SERVER = "server";

    /** ServiceManager used by this ParserFactory */
    private final ServiceManager serviceManager;

    /**
     * Creates a new instance of {@link ParserFactory}.
     */
    @Inject
    public ParserFactory(final ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }

    /**
     * Creates a new parser instance.
     *
     * @param profile The profile to use
     * @param address The address of the server to connect to
     * @param configManager The config manager to read settings from
     *
     * @return An appropriately configured parser
     *
     * @since 0.6.3
     */
    public Optional<Parser> getParser(final Profile profile, final URI address,
            final ReadOnlyConfigProvider configManager) {
        final Object obj = getExportResult(address, "getParser", buildMyInfo(profile), address);

        if (obj instanceof Parser) {
            final Parser parser = (Parser) obj;
            parser.setPingTimerInterval(configManager.getOptionInt(DOMAIN_SERVER, "pingtimer"));
            parser.setPingTimerFraction((int) (configManager.getOptionInt(DOMAIN_SERVER,
                    "pingfrequency") / parser.getPingTimerInterval()));

            if (configManager.hasOptionString(DOMAIN_GENERAL, "bindip")) {
                parser.setBindIP(configManager.getOption(DOMAIN_GENERAL, "bindip"));
            }

            parser.setProxy(buildProxyURI(configManager));

            return Optional.of(parser);
        }

        return Optional.empty();
    }

    /**
     * Retrieves the MyInfo object used for the Parser.
     *
     * @return The MyInfo object for our profile
     */
    private MyInfo buildMyInfo(final Profile profile) {
        checkNotNull(profile);
        checkArgument(!profile.getNicknames().isEmpty());

        final MyInfo myInfo = new MyInfo();
        myInfo.setNickname(profile.getNicknames().get(0));
        myInfo.setRealname(profile.getRealname());
        profile.getIdent().ifPresent(myInfo::setUsername);

        return myInfo;
    }

    /**
     * Constructs a URI for the configured proxy for this server, if any.
     *
     * @return An appropriate URI or null if no proxy is configured
     */
    @Nullable
    private URI buildProxyURI(final ReadOnlyConfigProvider configManager) {
        if (configManager.hasOptionString(DOMAIN_SERVER, "proxy.address")) {
            final String type;

            if (configManager.hasOptionString(DOMAIN_SERVER, "proxy.type")) {
                type = configManager.getOption(DOMAIN_SERVER, "proxy.type");
            } else {
                type = "socks";
            }

            final int port;
            if (configManager.hasOptionInt(DOMAIN_SERVER, "proxy.port")) {
                port = configManager.getOptionInt(DOMAIN_SERVER, "proxy.port");
            } else {
                port = 8080;
            }

            final String host = configManager.getOptionString(DOMAIN_SERVER, "proxy.address");

            final String userInfo;
            if (configManager.hasOptionString(DOMAIN_SERVER, "proxy.username")
                    && configManager.hasOptionString(DOMAIN_SERVER, "proxy.password")) {
                userInfo = configManager.getOption(DOMAIN_SERVER, "proxy.username")
                        + configManager.getOption(DOMAIN_SERVER, "proxy.password");
            } else {
                userInfo = "";
            }

            try {
                return new URI(type, userInfo, host, port, "", "", "");
            } catch (URISyntaxException ex) {
                LOG.warn(APP_ERROR, "Unable to create proxy URI", ex);
            }
        }

        return null;
    }

    /**
     * Retrieves a protocol description.
     *
     * @param address The address to retrieve a description for
     *
     * @return A corresponding protocol description
     *
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
     * {@link ServiceProvider} which can provide a parser for the specified address. If no such
     * provider or service is found, null is returned.
     *
     * @param address     The address a provider is required for
     * @param serviceName The name of the service to be retrieved
     * @param args        The arguments to be passed to the exported service
     *
     * @return The result from a relevant exported service, or null
     *
     * @since 0.6.4
     */
    protected Object getExportResult(final URI address,
            final String serviceName, final Object... args) {
        // TODO: Move default scheme to a setting
        final String scheme = address.getScheme() == null ? "irc" : address.getScheme();

        try {
            final Service service = serviceManager.getService("parser", scheme);

            if (service != null && !service.getProviders().isEmpty()) {
                final ServiceProvider provider = service.getProviders().get(0);

                if (provider != null) {
                    provider.activateServices();

                    return provider.getExportedService(serviceName)
                            .execute(args);
                }
            }
        } catch (NoSuchProviderException nspe) {
            LOG.warn(USER_ERROR, "No parser found for {}", address.getScheme(), nspe);
        }

        return null;
    }

}
