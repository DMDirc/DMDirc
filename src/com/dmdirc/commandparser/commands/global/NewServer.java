/*
 * Copyright (c) 2006-2014 DMDirc Developers
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

package com.dmdirc.commandparser.commands.global;

import com.dmdirc.FrameContainer;
import com.dmdirc.Server;
import com.dmdirc.commandparser.BaseCommandInfo;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.ServerFactory;
import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.plugins.Service;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.util.InvalidURIException;
import com.dmdirc.util.URIParser;

import java.net.URI;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * The new server command allows users to open a new server window.
 */
public class NewServer extends Command implements IntelligentCommand {

    /** A command info object for this command. */
    public static final CommandInfo INFO = new BaseCommandInfo("newserver",
            "newserver <host[:[+]port]> [password] - connect to a new server",
            CommandType.TYPE_GLOBAL);
    /** The factory to use to construct servers. */
    private final ServerFactory serverFactory;
    /** Plugin manager to query for available services. */
    private final PluginManager pluginManager;
    /** Identity controller to use to find profiles. */
    private final IdentityController identityController;
    /** The parser to use for user input. */
    private final URIParser uriParser;

    /**
     * Creates a new newserver command which will use the specified factory to construct servers.
     *
     * @param controller         The controller to use for command information.
     * @param serverFactory      The factory to use to construct servers.
     * @param pluginManager      The plugin manager to use to query available services.
     * @param identityController Identity controller to use to find profiles.
     * @param uriParser          The parser to use for user input.
     */
    @Inject
    public NewServer(
            final CommandController controller,
            final ServerFactory serverFactory,
            final PluginManager pluginManager,
            final IdentityController identityController,
            final URIParser uriParser) {
        super(controller);
        this.serverFactory = serverFactory;
        this.pluginManager = pluginManager;
        this.identityController = identityController;
        this.uriParser = uriParser;
    }

    @Override
    public void execute(@Nonnull final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {
        if (args.getArguments().length == 0) {
            showUsage(origin, args.isSilent(), "newserver", "<host[:[+]port]> [password]");
            return;
        }

        try {
            final URI address = uriParser.parseFromText(args.getArgumentsAsString());
            final Server server = serverFactory.createServer(address,
                    identityController.getProvidersByType("profile").get(0));
            server.connect();
        } catch (InvalidURIException ex) {
            if (origin == null) {
                Logger.userError(ErrorLevel.LOW, "Invalid URI given to /newserver", ex);
            } else {
                origin.addLine(FORMAT_ERROR, "Invalid URI: " + ex.getMessage()
                        + (ex.getCause() == null ? "" : ": " + ex.getCause().getMessage()));
            }
        }
    }

    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final IntelligentCommandContext context) {
        final AdditionalTabTargets res = new AdditionalTabTargets();

        if (arg == 0) {
            for (Service parserType : pluginManager.getServicesByType("parser")) {
                res.add(parserType.getName() + "://");
            }
        }
        res.excludeAll();
        return res;
    }

}
