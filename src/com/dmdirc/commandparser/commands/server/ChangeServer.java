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

package com.dmdirc.commandparser.commands.server;

import com.dmdirc.FrameContainer;
import com.dmdirc.commandparser.BaseCommandInfo;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.commandparser.commands.context.ServerCommandContext;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.util.InvalidURIException;
import com.dmdirc.util.URIParser;

import java.net.URI;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * The /server command allows the user to connect to a new server.
 */
public class ChangeServer extends Command {

    /** A command info object for this command. */
    public static final CommandInfo INFO = new BaseCommandInfo("server",
            "server <host[:[+]port]> [password] - connect to a different server",
            CommandType.TYPE_SERVER);
    /** The parser to use for user input. */
    private final URIParser uriParser;

    /**
     * Creates a new instance of this command.
     *
     * @param controller The controller to use for command information.
     * @param uriParser  The parser to use for user input.
     */
    @Inject
    public ChangeServer(final CommandController controller, final URIParser uriParser) {
        super(controller);

        this.uriParser = uriParser;
    }

    @Override
    public void execute(@Nonnull final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {
        if (args.getArguments().length == 0) {
            showUsage(origin, args.isSilent(), "server", "<host[:[+]port]> [password]");
            return;
        }

        try {
            final Connection connection = ((ServerCommandContext) context).getConnection();
            final URI address = uriParser.parseFromText(args.getArgumentsAsString());
            connection.connect(address, connection.getProfile());
        } catch (InvalidURIException ex) {
            origin.addLine(FORMAT_ERROR, "Invalid URI: " + ex.getMessage()
                    + (ex.getCause() == null ? "" : ": " + ex.getCause().getMessage()));
        }
    }

}
