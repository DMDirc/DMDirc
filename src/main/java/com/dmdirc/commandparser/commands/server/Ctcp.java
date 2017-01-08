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

package com.dmdirc.commandparser.commands.server;

import com.dmdirc.commandparser.BaseCommandInfo;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.BaseCommand;
import com.dmdirc.commandparser.commands.CommandOptions;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.commandparser.commands.context.ServerCommandContext;
import com.dmdirc.events.ServerCtcpSentEvent;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.WindowModel;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.input.TabCompletionType;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * Allows the user to send CTCP messages.
 */
@CommandOptions(allowOffline = false)
public class Ctcp extends BaseCommand implements IntelligentCommand {

    /** A command info object for this command. */
    public static final CommandInfo INFO = new BaseCommandInfo("ctcp",
            "ctcp <target> <type> [arguments] - sends a CTCP message",
            CommandType.TYPE_SERVER);

    /**
     * Creates a new instance of this command.
     *
     * @param controller The controller to use for command information.
     */
    @Inject
    public Ctcp(final CommandController controller) {
        super(controller);
    }

    @Override
    public void execute(@Nonnull final WindowModel origin,
            final CommandArguments args, final CommandContext context) {
        final Connection connection = ((ServerCommandContext) context).getConnection();
        if (args.getArguments().length < 2) {
            showUsage(origin, args.isSilent(), "ctcp", "<target> <type> [arguments]");
        } else {
            connection.getParser().get().sendCTCP(args.getArguments()[0],
                    args.getArguments()[1], args.getArgumentsAsString(2));
            if (!args.isSilent()) {
                origin.getEventBus().publishAsync(new ServerCtcpSentEvent(
                        connection, args.getArguments()[0], args.getArgumentsAsString(1)));
            }
        }
    }

    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final IntelligentCommandContext context) {
        final AdditionalTabTargets res = new AdditionalTabTargets();

        if (arg == 0) {
            res.exclude(TabCompletionType.COMMAND);
        } else if (arg == 1) {
            res.add("VERSION");
            res.add("ACTION");
            res.add("USERINFO");
            res.add("CLIENTINFO");
            res.add("PING");
            res.add("FINGER");
            res.add("SOURCE");
            res.add("TIME");
            res.excludeAll();
        }

        return res;
    }

}
