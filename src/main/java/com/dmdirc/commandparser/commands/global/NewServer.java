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

import com.dmdirc.commandparser.BaseCommandInfo;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.BaseCommand;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.config.profiles.ProfileManager;
import com.dmdirc.events.CommandErrorEvent;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.ConnectionFactory;
import com.dmdirc.interfaces.WindowModel;
import com.dmdirc.plugins.ServiceManager;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.util.InvalidURIException;
import com.dmdirc.util.URIParser;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.net.URI;
import java.util.stream.Collectors;

/**
 * The new server command allows users to open a new server window.
 */
public class NewServer extends BaseCommand implements IntelligentCommand {

    /** A command info object for this command. */
    public static final CommandInfo INFO = new BaseCommandInfo("newserver",
            "newserver <host[:[+]port]> [password] - connect to a new server",
            CommandType.TYPE_GLOBAL);
    /** The factory to use to construct servers. */
    private final ConnectionFactory connectionFactory;
    /** Service manager to query for available services. */
    private final ServiceManager serviceManager;
    /** Manager to use to find profiles. */
    private final ProfileManager profileManager;
    /** The parser to use for user input. */
    private final URIParser uriParser;

    /**
     * Creates a new newserver command which will use the specified factory to construct servers.
     */
    @Inject
    public NewServer(
            final CommandController controller,
            final ConnectionFactory connectionFactory,
            final ServiceManager serviceManager,
            final ProfileManager profileManager,
            final URIParser uriParser) {
        super(controller);
        this.connectionFactory = connectionFactory;
        this.serviceManager = serviceManager;
        this.profileManager = profileManager;
        this.uriParser = uriParser;
    }

    @Override
    public void execute(@Nonnull final WindowModel origin,
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
            origin.getEventBus().publishAsync(new CommandErrorEvent(origin,
                    "Invalid URI: " + ex.getMessage() +
                            (ex.getCause() == null ? "" : ": " + ex.getCause().getMessage())));
        }
    }

    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final IntelligentCommandContext context) {
        final AdditionalTabTargets res = new AdditionalTabTargets();

        if (arg == 0) {
            res.addAll(serviceManager.getServicesByType("parser").stream()
                    .map(parserType -> parserType.getName() + "://").collect(Collectors.toList()));
        }
        res.excludeAll();
        return res;
    }

}
