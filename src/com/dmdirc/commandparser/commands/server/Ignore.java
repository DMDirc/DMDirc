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
import com.dmdirc.Server;
import com.dmdirc.commandparser.BaseCommandInfo;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.commandparser.commands.context.ServerCommandContext;
import com.dmdirc.parser.common.IgnoreList;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.input.TabCompletionType;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Allows the user to add/view/delete ignores.
 */
public class Ignore extends Command implements IntelligentCommand {

    /** A command info object for this command. */
    public static final CommandInfo INFO = new BaseCommandInfo("ignore",
            "ignore [--remove|--regex] [host] - manages the network's ignore list",
            CommandType.TYPE_SERVER);

    /** {@inheritDoc} */
    @Override
    public void execute(final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {
        final Server server = ((ServerCommandContext) context).getServer();

        if (args.getArguments().length == 0) {
            executeView(origin, server, args.isSilent(), args, false);
        } else if ("--remove".equalsIgnoreCase(args.getArguments()[0])) {
            executeRemove(origin, server, args.isSilent(), args);
        } else if ("--regex".equalsIgnoreCase(args.getArguments()[0])) {
            executeRegex(origin, server, args.isSilent(), args);
        } else {
            executeAdd(origin, server, args.isSilent(), args);
        }
    }

    protected void executeView(final FrameContainer origin, final Server server,
            final boolean isSilent, final CommandArguments args, final boolean forceRegex) {
        final IgnoreList ignoreList = server.getIgnoreList();

        if (ignoreList.count() == 0) {
            sendLine(origin, args.isSilent(), FORMAT_ERROR, "No ignore list entries for this network.");
            return;
        }

        final List<String> entries;
        if (ignoreList.canConvert() && !forceRegex) {
            entries = ignoreList.getSimpleList();
        } else {
            if (!forceRegex) {
                sendLine(origin, args.isSilent(), FORMAT_ERROR,
                        "Unable to convert ignore list to simple format");
            }
            entries = ignoreList.getRegexList();
        }

        int i = 0;
        for (String line : entries) {
            i++;
            sendLine(origin, args.isSilent(), FORMAT_OUTPUT, i + ". " + line);
        }
    }

    protected void executeAdd(final FrameContainer origin, final Server server,
            final boolean isSilent, final CommandArguments args) {
        final IgnoreList ignoreList = server.getIgnoreList();
        final String target = args.getArgumentsAsString();

        ignoreList.addSimple(target);
        server.saveIgnoreList();
        sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "Added " + target + " to the ignore list.");
    }

    protected void executeRegex(final FrameContainer origin, final Server server,
            final boolean isSilent, final CommandArguments args) {
        if (args.getArguments().length == 1) {
            executeView(origin, server, args.isSilent(), args, true);
            return;
        }

        final IgnoreList ignoreList = server.getIgnoreList();
        final String target = args.getArgumentsAsString(1);

        try {
            Pattern.compile(target);
        } catch (PatternSyntaxException ex) {
            sendLine(origin, args.isSilent(), FORMAT_ERROR, "Unable to compile regex: "
                    + ex.getDescription());
            return;
        }

        ignoreList.add(target);
        server.saveIgnoreList();
        sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "Added " + target + " to the ignore list.");
    }

    protected void executeRemove(final FrameContainer origin, final Server server,
            final boolean isSilent, final CommandArguments args) {
        if (args.getArguments().length == 1) {
            showUsage(origin, args.isSilent(), "ignore", "--remove <host>");
            return;
        }

        final IgnoreList ignoreList = server.getIgnoreList();
        final String host = args.getArgumentsAsString(1);

        if (ignoreList.canConvert() && ignoreList.getSimpleList().contains(host)) {
            ignoreList.remove(ignoreList.getSimpleList().indexOf(host));
            server.saveIgnoreList();
            sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "Removed " + host + " from the ignore list.");
            return;
        }

        if (ignoreList.getRegexList().contains(host)) {
            ignoreList.remove(ignoreList.getRegexList().indexOf(host));
            server.saveIgnoreList();
            sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "Removed " + host + " from the ignore list.");
            return;
        }

        sendLine(origin, args.isSilent(), FORMAT_ERROR, "Ignore list doesn't contain '" + host + "'.");
    }

    /** {@inheritDoc} */
    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final IntelligentCommandContext context) {
        final AdditionalTabTargets targets = new AdditionalTabTargets();

        targets.excludeAll();
        if (arg == 0) {
            targets.add("--regex");
            targets.add("--remove");
            targets.include(TabCompletionType.CHANNEL_NICK);
            targets.include(TabCompletionType.QUERY_NICK);
        } else if (arg == 1 && context.getPreviousArgs().get(0).equals("--remove")) {
            final IgnoreList ignoreList = context.getWindow().getServer()
                .getIgnoreList();
            if (ignoreList.canConvert()) {
                for (String entry : ignoreList.getSimpleList()) {
                    targets.add(entry);
                }
            }
            for (String entry : ignoreList.getRegexList()) {
                targets.add(entry);
            }

        } else if (arg == 1 && context.getPreviousArgs().get(0).equals("--regex")) {
            targets.include(TabCompletionType.CHANNEL_NICK);
            targets.include(TabCompletionType.QUERY_NICK);
        }

        return targets;
    }

}
