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

package com.dmdirc.commandparser.commands.global;

import com.dmdirc.FrameContainer;
import com.dmdirc.WritableFrameContainer;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.commandparser.parsers.GlobalCommandParser;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.input.TabCompleter;

/**
 * The if plugin command allows the user to execute commands based on whether
 * or not a plugin is loaded.
 *
 * @author chris
 */
public final class Ifplugin extends Command implements IntelligentCommand,
        CommandInfo {

    /**
     * Creates a new instance of Ifplugin.
     */
    public Ifplugin() {
        super();
    }

    /** {@inheritDoc} */
    @Override
    public void execute(final FrameContainer<?> origin,
            final CommandArguments args, final CommandContext context) {
        if (args.getArguments().length <= 1) {
            showUsage(origin, args.isSilent(), "ifplugin", "<[!]plugin> <command>");
            return;
        }

        final boolean negative = args.getArguments()[0].charAt(0) == '!';

        final String pname = args.getArguments()[0].substring(negative ? 1 : 0);

        final PluginInfo pluginInfo = PluginManager.getPluginManager().getPluginInfoByName(pname);

        boolean result = true;

        if (pluginInfo == null || !pluginInfo.isLoaded()) {
            result = false;
        }

        if (result != negative) {
            if (origin == null) {
                GlobalCommandParser.getGlobalCommandParser().parseCommand(null,
                        context.getSource(), args.getArgumentsAsString(1));
            } else {
                ((WritableFrameContainer<?>) origin).getCommandParser()
                        .parseCommand(origin, context.getSource(), args.getArgumentsAsString(1));
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "ifplugin";
    }

    /** {@inheritDoc} */
    @Override
    public boolean showInHelp() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public CommandType getType() {
        return CommandType.TYPE_GLOBAL;
    }

    /** {@inheritDoc} */
    @Override
    public String getHelp() {
        return "ifplugin <[!]plugin> <command> - executes a command if the "
                + "specified plugin is/isn't loaded";
    }

    /** {@inheritDoc} */
    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final IntelligentCommandContext context) {
        final AdditionalTabTargets res;

        if (arg == 0) {
            res = new AdditionalTabTargets().excludeAll();

            for (PluginInfo possPlugin
                    : PluginManager.getPluginManager().getPluginInfos()) {
                res.add(possPlugin.getName());
                res.add("!" + possPlugin.getName());
            }
        } else {
            res = TabCompleter.getIntelligentResults(arg, context, 1);
        }

        return res;
    }

}
