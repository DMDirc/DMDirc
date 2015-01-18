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
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.GroupChat;
import com.dmdirc.interfaces.WindowModel;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.input.TabCompletionType;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.inject.Inject;

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

    /**
     * Creates a new instance of this command.
     *
     * @param controller The controller to use for command information.
     */
    @Inject
    public Message(final CommandController controller) {
        super(controller);
    }

    @Override
    public void execute(@Nonnull final WindowModel origin,
            final CommandArguments args, final CommandContext context) {
        final Connection connection = ((ServerCommandContext) context).getConnection();
        if (args.getArguments().length < 2) {
            showUsage(origin, args.isSilent(), "msg", "<target> <message>");
        } else {
            final String target = args.getArguments()[0];
            final String message = args.getArgumentsAsString(1);
            sendLine(origin, args.isSilent(), "selfMessage", target, message);

            // If this is a known server or channel, and this is not a silent
            // invocation, use sendLine, else send it raw to the parser.
            final Optional<GroupChat> channel = connection.getGroupChatManager().getChannel(target);
            if (!args.isSilent() && channel.isPresent()) {
                channel.get().sendLine(message);
            } else if (!args.isSilent() && connection.hasQuery(target)) {
                connection.getQuery(target).sendLine(message, target);
            } else {
                final Optional<Parser> parser = connection.getParser();

                if (parser.isPresent()) {
                    parser.get().sendMessage(target, message);
                } else {
                    // This can happen if the server gets disconnected after
                    // the command manager has checked the @CommandOptions
                    // annotation. Yay for concurrency.
                    sendLine(origin, args.isSilent(), FORMAT_ERROR,
                            "You must be connected to use this command");
                }
            }
        }
    }

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

    @Override
    public int getLineCount(final WindowModel origin, final CommandArguments arguments) {
        if (arguments.getArguments().length >= 2) {
            final String target = arguments.getArguments()[0];
            return origin.getConnection().get().getWindowModel().getNumLines(
                    "PRIVMSG " + target + " :" + arguments.getArgumentsAsString(1));
        } else {
            return 1;
        }
    }

}
