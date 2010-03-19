/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.ServerCommand;
import com.dmdirc.parser.common.IgnoreList;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.input.TabCompletionType;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Allows the user to add/view/delete ignores.
 * @author chris
 */
public final class Ignore extends ServerCommand implements IntelligentCommand {
    
    /**
     * Creates a new instance of Ignore.
     */
    public Ignore() {
        super();
        
        CommandManager.registerCommand(this);
    }
    
    /**
     * Executes this command.
     * @param origin The frame in which this command was issued
     * @param server The server object that this command is associated with
     * @param isSilent Whether this command is silenced or not
     * @param args The user supplied arguments
     */
    @Override
    public void execute(final FrameContainer origin, final Server server,
            final boolean isSilent, final CommandArguments args) {

        if (args.getArguments().length == 0) {
            executeView(origin, server, isSilent, args, false);
        } else if ("--remove".equalsIgnoreCase(args.getArguments()[0])) {
            executeRemove(origin, server, isSilent, args);
        } else if ("--regex".equalsIgnoreCase(args.getArguments()[0])) {
            executeRegex(origin, server, isSilent, args);
        } else {
            executeAdd(origin, server, isSilent, args);
        }
    }

    protected void executeView(final FrameContainer origin, final Server server,
            final boolean isSilent, final CommandArguments args, final boolean forceRegex) {
        final IgnoreList ignoreList = server.getIgnoreList();

        if (ignoreList.count() == 0) {
            sendLine(origin, isSilent, FORMAT_ERROR, "No ignore list entries for this network.");
            return;
        }

        final List<String> entries;
        if (ignoreList.canConvert() && !forceRegex) {
            entries = ignoreList.getSimpleList();
        } else {
            if (!forceRegex) {
                sendLine(origin, isSilent, FORMAT_ERROR,
                        "Unable to convert ignore list to simple format");
            }
            entries = ignoreList.getRegexList();
        }

        int i = 0;
        for (String line : entries) {
            i++;
            sendLine(origin, isSilent, FORMAT_OUTPUT, i + ". " + line);
        }
    }

    protected void executeAdd(final FrameContainer origin, final Server server,
            final boolean isSilent, final CommandArguments args) {
        final IgnoreList ignoreList = server.getIgnoreList();
        final String target = args.getArgumentsAsString();

        ignoreList.addSimple(target);
        server.saveIgnoreList();
        sendLine(origin, isSilent, FORMAT_OUTPUT, "Added " + target + " to the ignore list.");
    }

    protected void executeRegex(final FrameContainer origin, final Server server,
            final boolean isSilent, final CommandArguments args) {
        if (args.getArguments().length == 1) {
            executeView(origin, server, isSilent, args, true);
            return;
        }

        final IgnoreList ignoreList = server.getIgnoreList();
        final String target = args.getArgumentsAsString(1);

        try {
            Pattern.compile(target);
        } catch (PatternSyntaxException ex) {
            sendLine(origin, isSilent, FORMAT_ERROR, "Unable to compile regex: "
                    + ex.getDescription());
            return;
        }

        ignoreList.add(target);
        server.saveIgnoreList();
        sendLine(origin, isSilent, FORMAT_OUTPUT, "Added " + target + " to the ignore list.");
    }

    protected void executeRemove(final FrameContainer origin, final Server server,
            final boolean isSilent, final CommandArguments args) {
        if (args.getArguments().length == 1) {
            showUsage(origin, isSilent, "ignore", "--remove <host>");
            return;
        }

        final IgnoreList ignoreList = server.getIgnoreList();
        final String host = args.getArgumentsAsString(1);

        if (ignoreList.canConvert() && ignoreList.getSimpleList().contains(host)) {
            ignoreList.remove(ignoreList.getSimpleList().indexOf(host));
            server.saveIgnoreList();
            sendLine(origin, isSilent, FORMAT_OUTPUT, "Removed " + host + " from the ignore list.");
            return;
        }

        if (ignoreList.getRegexList().contains(host)) {
            ignoreList.remove(ignoreList.getRegexList().indexOf(host));
            server.saveIgnoreList();
            sendLine(origin, isSilent, FORMAT_OUTPUT, "Removed " + host + " from the ignore list.");
            return;
        }

        sendLine(origin, isSilent, FORMAT_ERROR, "Ignore list doesn't contain '" + host + "'.");
    }
    
    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "ignore";
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean showInHelp() {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getHelp() {
        return "ignore [--remove|--regex] [host] - manages the network's ignore list";
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
        } else if (arg == 1 && context.getPreviousArgs().get(0).equals("--regex")) {
            targets.include(TabCompletionType.CHANNEL_NICK);
            targets.include(TabCompletionType.QUERY_NICK);
        } else if (arg == 1 && context.getPreviousArgs().get(0).equals("--remove")) {
            // TODO: If/when passed a server, include known ignore list entries
        }
        
        return targets;
    }
    
}
