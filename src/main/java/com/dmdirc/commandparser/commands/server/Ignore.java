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
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.commandparser.commands.context.ServerCommandContext;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.WindowModel;
import com.dmdirc.parser.common.IgnoreList;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.input.TabCompletionType;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

/**
 * Allows the user to add/view/delete ignores.
 */
public class Ignore extends BaseCommand implements IntelligentCommand {

    /** A command info object for this command. */
    public static final CommandInfo INFO = new BaseCommandInfo("ignore",
            "ignore [--remove|--regex] [host] - manages the network's ignore list",
            CommandType.TYPE_SERVER);

    /**
     * Creates a new instance of this command.
     *
     * @param controller The controller to use for command information.
     */
    @Inject
    public Ignore(final CommandController controller) {
        super(controller);
    }

    @Override
    public void execute(@Nonnull final WindowModel origin,
            final CommandArguments args, final CommandContext context) {
        final Connection connection = ((ServerCommandContext) context).getConnection();

        if (args.getArguments().length == 0) {
            executeView(origin, connection, args, false);
        } else if ("--remove".equalsIgnoreCase(args.getArguments()[0])) {
            executeRemove(origin, connection, args);
        } else if ("--regex".equalsIgnoreCase(args.getArguments()[0])) {
            executeRegex(origin, connection, args);
        } else {
            executeAdd(origin, connection, args);
        }
    }

    protected void executeView(final WindowModel origin, final Connection connection,
            final CommandArguments args, final boolean forceRegex) {
        final IgnoreList ignoreList = connection.getIgnoreList();

        if (ignoreList.count() == 0) {
            showError(origin, args.isSilent(), "No ignore list entries for this network.");
            return;
        }

        final List<String> entries;
        if (ignoreList.canConvert() && !forceRegex) {
            entries = ignoreList.getSimpleList();
        } else {
            if (!forceRegex) {
                showError(origin, args.isSilent(),
                        "Unable to convert ignore list to simple format");
            }
            entries = ignoreList.getRegexList();
        }

        int i = 0;
        for (String line : entries) {
            i++;
            showOutput(origin, args.isSilent(), i + ". " + line);
        }
    }

    protected void executeAdd(final WindowModel origin, final Connection connection,
            final CommandArguments args) {
        final IgnoreList ignoreList = connection.getIgnoreList();
        final String target = args.getArgumentsAsString();

        ignoreList.addSimple(target);
        connection.saveIgnoreList();
        showOutput(origin, args.isSilent(), "Added " + target + " to the ignore list.");
    }

    protected void executeRegex(final WindowModel origin, final Connection connection,
            final CommandArguments args) {
        if (args.getArguments().length == 1) {
            executeView(origin, connection, args, true);
            return;
        }

        final IgnoreList ignoreList = connection.getIgnoreList();
        final String target = args.getArgumentsAsString(1);

        try {
            //noinspection ResultOfMethodCallIgnored
            Pattern.compile(target);
        } catch (PatternSyntaxException ex) {
            showError(origin, args.isSilent(), "Unable to compile regex: " + ex.getDescription());
            return;
        }

        ignoreList.add(target);
        connection.saveIgnoreList();
        showOutput(origin, args.isSilent(), "Added " + target + " to the ignore list.");
    }

    protected void executeRemove(final WindowModel origin, final Connection connection,
            final CommandArguments args) {
        if (args.getArguments().length == 1) {
            showUsage(origin, args.isSilent(), "ignore", "--remove <host>");
            return;
        }

        final IgnoreList ignoreList = connection.getIgnoreList();
        final String host = args.getArgumentsAsString(1);

        if (ignoreList.canConvert() && ignoreList.getSimpleList().contains(host)) {
            ignoreList.remove(ignoreList.getSimpleList().indexOf(host));
            connection.saveIgnoreList();
            showOutput(origin, args.isSilent(), "Removed " + host
                    + " from the ignore list.");
            return;
        }

        if (ignoreList.getRegexList().contains(host)) {
            ignoreList.remove(ignoreList.getRegexList().indexOf(host));
            connection.saveIgnoreList();
            showOutput(origin, args.isSilent(), "Removed " + host
                    + " from the ignore list.");
            return;
        }

        showError(origin, args.isSilent(), "Ignore list doesn't contain '" + host + "'.");
    }

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
        } else if (arg == 1 && "--remove".equals(context.getPreviousArgs().get(0))) {
            final IgnoreList ignoreList = context.getWindow().getConnection()
                    .get().getIgnoreList();
            if (ignoreList.canConvert()) {
                targets.addAll(ignoreList.getSimpleList().stream().collect(Collectors.toList()));
            }
            targets.addAll(ignoreList.getRegexList().stream().collect(Collectors.toList()));

        } else if (arg == 1 && "--regex".equals(context.getPreviousArgs().get(0))) {
            targets.include(TabCompletionType.CHANNEL_NICK);
            targets.include(TabCompletionType.QUERY_NICK);
        }

        return targets;
    }

}
