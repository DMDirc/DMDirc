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

package com.dmdirc.commandparser.commands.server;

import com.dmdirc.FrameContainer;
import com.dmdirc.Query;
import com.dmdirc.Server;
import com.dmdirc.WritableFrameContainer;
import com.dmdirc.commandparser.BaseCommandInfo;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.WrappableCommand;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.commandparser.commands.context.ServerCommandContext;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.input.TabCompletionType;
import com.dmdirc.ui.messages.Styliser;

import javax.inject.Inject;

/**
 * Allows the user to open a query dialog with another user.
 */
public class OpenQuery extends Command implements IntelligentCommand,
        WrappableCommand {

    /** A command info object for this command. */
    public static final CommandInfo INFO = new BaseCommandInfo("query",
            "query <user> [message] - opens a query with the specified user",
            CommandType.TYPE_SERVER);

    /** Creates a new instance of this command. */
    @Inject
    public OpenQuery() {
    }

    /** {@inheritDoc} */
    @Override
    public void execute(final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {
        if (args.getArguments().length == 0) {
            showUsage(origin, args.isSilent(), "query", "<target> <message>");
            return;
        }

        final Server server = ((ServerCommandContext) context).getServer();
        if (server.getParser().isValidChannelName(args.getArguments()[0])) {
            sendLine(origin, args.isSilent(), FORMAT_ERROR, "You can't open a query "
                    + "with a channel; maybe you meant " + Styliser.CODE_FIXED
                    + Styliser.CODE_BOLD
                    + getController().getCommandChar()
                    + (args.getArguments().length > 1 ? "msg" : "join") + " "
                    + args.getArgumentsAsString()
                    + Styliser.CODE_BOLD + Styliser.CODE_FIXED + "?");
            return;
        }

        final Query query = server.getQuery(args.getArguments()[0], !args.isSilent());

        if (args.getArguments().length > 1) {
            query.sendLine(args.getArgumentsAsString(1), args.getArguments()[0]);
        }

        if (!args.isSilent()) {
            //TODO how are we going to handle focusing an existing query?
            //context.getSource().getController().requestWindowFocus(query);
            return; //NOPMD
        }
    }

    /** {@inheritDoc} */
    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final IntelligentCommandContext context) {
        final AdditionalTabTargets targets = new AdditionalTabTargets();

        if (arg == 0) {
            targets.excludeAll();
            targets.include(TabCompletionType.CHANNEL_NICK);
        }

        return targets;
    }

    /** {@inheritDoc} */
    @Override
    public int getLineCount(final WritableFrameContainer origin,
            final CommandArguments arguments) {
        if (arguments.getArguments().length >= 2) {
            final String target = arguments.getArguments()[0];
            return origin.getServer().getNumLines("PRIVMSG "
                    + target + " :" + arguments.getArgumentsAsString(1));
        } else {
            return 1;
        }
    }

}
