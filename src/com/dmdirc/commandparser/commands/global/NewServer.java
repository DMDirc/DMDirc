/*
 * Copyright (c) 2006-2015 DMDirc Developers
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
import com.dmdirc.commandparser.BaseCommandInfo;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.config.profiles.ProfileManager;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.ConnectionFactory;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.util.InvalidURIException;
import com.dmdirc.util.URIParser;

import java.net.URI;
import java.util.stream.Collectors;

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
    private final ConnectionFactory connectionFactory;
    /** Plugin manager to query for available services. */
    private final PluginManager pluginManager;
    /** Manager to use to find profiles. */
    private final ProfileManager profileManager;
    /** The parser to use for user input. */
    private final URIParser uriParser;

    /**
     * Creates a new newserver command which will use the specified factory to construct servers.
     *
     * @param controller         The controller to use for command information.
     * @param connectionFactory      The factory to use to construct servers.
     * @param pluginManager      The plugin manager to use to query available services.
     * @param profileManager     Manager to use to find profiles.
     * @param uriParser          The parser to use for user input.
     */
    @Inject
    public NewServer(
            final CommandController controller,
            final ConnectionFactory connectionFactory,
            final PluginManager pluginManager,
            final ProfileManager profileManager,
            final URIParser uriParser) {
        super(controller);
        this.connectionFactory = connectionFactory;
        this.pluginManager = pluginManager;
        this.profileManager = profileManager;
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
            final Connection server = connectionFactory.createServer(address,
                    profileManager.getDefault());
            server.connect();
        } catch (InvalidURIException ex) {
            origin.addLine(FORMAT_ERROR, "Invalid URI: " + ex.getMessage()
                    + (ex.getCause() == null ? "" : ": " + ex.getCause().getMessage()));
        }
    }

    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final IntelligentCommandContext context) {
        final AdditionalTabTargets res = new AdditionalTabTargets();

        if (arg == 0) {
            res.addAll(pluginManager.getServicesByType("parser").stream()
                    .map(parserType -> parserType.getName() + "://").collect(Collectors.toList()));
        }
        res.excludeAll();
        return res;
    }

}
