/*
 * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
import com.dmdirc.Server;
import com.dmdirc.commandparser.BaseCommandInfo;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.CommandOptions;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.WrappableCommand;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.commandparser.commands.context.ServerCommandContext;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.input.TabCompletionType;
import com.dmdirc.ui.interfaces.InputWindow;

/**
 * Allows the user to send privmsgs.
 */
@CommandOptions(allowOffline = false)
public class Message extends Command implements IntelligentCommand,
        WrappableCommand {

    /** A command info object for this command. */
    public static final CommandInfo INFO = new BaseCommandInfo("msg",
            "msg <target> <message> - sends a private message",
            CommandType.TYPE_SERVER);

    /** {@inheritDoc} */
    @Override
    public void execute(final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {
        final Server server = ((ServerCommandContext) context).getServer();
        if (args.getArguments().length < 2) {
            showUsage(origin, args.isSilent(), "msg", "<target> <message>");
        } else {
            final String target = args.getArguments()[0];
            final String message = args.getArgumentsAsString(1);
            sendLine(origin, args.isSilent(), "selfMessage", target, message);

            // If this is a known server or channel, and this is not a silent
            // invokation, use sendLine, else send it raw to the parser.
            if (!args.isSilent() && server.hasChannel(target)) {
                server.getChannel(target).sendLine(message);
            } else if (!args.isSilent() && server.hasQuery(target)) {
                server.getQuery(target).sendLine(message, target);
            } else {
                final Parser parser = server.getParser();

                if (parser == null) {
                    // This can happen if the server gets disconnected after
                    // the command manager has checked the @CommandOptions
                    // annotation. Yay for concurrency.
                    sendLine(origin, args.isSilent(), FORMAT_ERROR,
                            "You must be connected to use this command");
                } else {
                    parser.sendMessage(target, message);
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final IntelligentCommandContext context) {
        final AdditionalTabTargets res = new AdditionalTabTargets();

        if (arg == 0) {
            res.excludeAll();
            res.include(TabCompletionType.CHANNEL_NICK);
            res.include(TabCompletionType.CHANNEL);
            res.include(TabCompletionType.QUERY_NICK);
        }

        return res;
    }

    /** {@inheritDoc} */
    @Override
    public int getLineCount(final InputWindow origin, final CommandArguments arguments) {
        if (arguments.getArguments().length >= 2) {
            final String target = arguments.getArguments()[0];
            return origin.getContainer().getServer().getNumLines("PRIVMSG "
                    + target + " :" + arguments.getArgumentsAsString(1));
        } else {
            return 1;
        }
    }

}
