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

import com.dmdirc.commandparser.parsers.ServerCommandParser;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.ServerFactory;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.interfaces.config.ConfigProviderMigrator;
import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.interfaces.config.IdentityFactory;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.parser.common.ChannelJoinRequest;
import com.dmdirc.ui.WindowManager;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * The ServerManager maintains a list of all servers, and provides methods to
 * search or iterate over them.
 */
@Singleton
public class ServerManager implements ServerFactory {

    /** All servers that currently exist. */
    private final Set<Server> servers = new CopyOnWriteArraySet<>();

    /** The identity controller to use to find profiles. */
    private final IdentityController identityController;

    /** A provider of {@link CommandController}s to pass to servers. */
    private final Provider<CommandController> commandController;

    /** The identity factory to give to servers. */
    private final IdentityFactory identityFactory;

    /** Window manager to add new servers to. */
    private final WindowManager windowManager;

    /** Concrete server factory to use. */
    private final ServerFactoryImpl serverFactoryImpl;

    /**
     * Creates a new instance of ServerManager.
     *
     * @param identityController The identity controller to use to find profiles.
     * @param identityFactory The factory to use to create new identities.
     * @param commandController A provider of {@link CommandController}s to pass to servers.
     * @param windowManager Window manager to add new servers to.
     * @param serverFactory The factory to use to create servers.
     */
    @Inject
    public ServerManager(
            final IdentityController identityController,
            final IdentityFactory identityFactory,
            final Provider<CommandController> commandController,
            final WindowManager windowManager,
            final ServerFactoryImpl serverFactory) {
        this.identityController = identityController;
        this.identityFactory = identityFactory;
        this.commandController = commandController;
        this.windowManager = windowManager;
        this.serverFactoryImpl = serverFactory;
    }

    /** {@inheritDoc} */
    @Override
    public Server createServer(final URI uri, final ConfigProvider profile) {
        final ConfigProviderMigrator configProvider =
                identityFactory.createMigratableConfig(uri.getScheme(), "", "", uri.getHost());

        final Server server = serverFactoryImpl.getServer(
                configProvider,
                new ServerCommandParser(configProvider.getConfigProvider(), commandController.get()),
                uri,
                profile);
        registerServer(server);
        windowManager.addWindow(server);

        if (configProvider.getConfigProvider().getOptionBool("general", "showrawwindow")) {
            server.addRaw();
        }

        return server;
    }

    /**
     * Registers a new server with the manager.
     *
     * @param server The server to be registered
     */
    public void registerServer(final Server server) {
        servers.add(server);
    }

    /**
     * Unregisters a server from the manager. The request is ignored if the
     * ServerManager is in the process of closing all servers.
     *
     * @param server The server to be unregistered
     */
    public void unregisterServer(final Server server) {
        servers.remove(server);
    }

    /**
     * Returns a list of all servers.
     *
     * @return A list of all servers
     */
    public List<Server> getServers() {
        return new ArrayList<>(servers);
    }

    /**
     * Makes all servers disconnected with the specified quit message.
     *
     * @param message The quit message to send to the IRC servers
     */
    public void disconnectAll(final String message) {
        for (Server server : servers) {
            server.disconnect(message);
        }
    }

    /**
     * Closes all servers with the specified quit message.
     *
     * @param message The quit message to send to the IRC servers
     */
    public void closeAll(final String message) {
        for (Server server : servers) {
            server.disconnect(message);
            server.close();
        }
    }

    /**
     * Returns the number of servers that are registered with the manager.
     *
     * @return number of registered servers
     */
    public int numServers() {
        return servers.size();
    }

    /**
     * Retrieves a list of servers connected to the specified network.
     *
     * @param network The network to search for
     * @return A list of servers connected to the network
     */
    public List<Server> getServersByNetwork(final String network) {
        final List<Server> res = new ArrayList<>();

        for (Server server : servers) {
            if (server.isNetwork(network)) {
                res.add(server);
            }
        }

        return res;
    }

    /**
     * Creates a new server which will connect to the specified URI with the
     * default profile.
     *
     * @param uri The URI to connect to
     * @return The server which will be connecting
     * @since 0.6.3
     */
    public Server connectToAddress(final URI uri) {
        return connectToAddress(uri,
                identityController.getProvidersByType("profile").get(0));
    }

    /**
     * Creates a new server which will connect to the specified URI with the
     * specified profile.
     *
     * @param uri The URI to connect to
     * @param profile The profile to use
     * @return The server which will be connecting
     * @since 0.6.3
     */
    public Server connectToAddress(final URI uri, final ConfigProvider profile) {
        checkArgument(profile.isProfile());
        Server server = null;

        for (Server loopServer : servers) {
            if (loopServer.compareURI(uri)) {
                server = loopServer;
                break;
            }
        }

        if (server == null) {
            server = createServer(uri, profile);
            server.connect();
            return server;
        }

        if (server.getState().isDisconnected()) {
            server.connect(uri, profile);
        } else {
            server.join(server.getParser().extractChannels(uri)
                    .toArray(new ChannelJoinRequest[0]));
        }

        return server;
    }

    /**
     * Connects the user to Quakenet if neccessary and joins #DMDirc.
     */
    public void joinDevChat() {
        final List<Server> qnetServers = getServersByNetwork("Quakenet");

        Server connectedServer = null;

        for (Server server : qnetServers) {
            if (server.getState() == ServerState.CONNECTED) {
                connectedServer = server;

                if (server.hasChannel("#DMDirc")) {
                    server.join(new ChannelJoinRequest("#DMDirc"));
                    return;
                }
            }
        }

        if (connectedServer == null) {
            try {
                connectToAddress(new URI("irc://irc.quakenet.org/DMDirc"));
            } catch (URISyntaxException ex) {
                Logger.appError(ErrorLevel.MEDIUM, "Unable to construct new server", ex);
            }
        } else {
            connectedServer.join(new ChannelJoinRequest("#DMDirc"));
        }
    }

}
