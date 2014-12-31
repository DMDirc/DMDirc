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

import com.dmdirc.FrameContainer;
import com.dmdirc.GlobalWindow;
import com.dmdirc.commandparser.BaseCommandInfo;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.commandparser.parsers.GlobalCommandParser;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.input.TabCompleterUtils;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;

/**
 * The if plugin command allows the user to execute commands based on whether or not a plugin is
 * loaded.
 */
public class Ifplugin extends Command implements IntelligentCommand {

    /** A command info object for this command. */
    public static final CommandInfo INFO = new BaseCommandInfo("ifplugin",
            "ifplugin <[!]plugin> <command> - executes a command if the "
            + "specified plugin is/isn't loaded",
            CommandType.TYPE_GLOBAL);
    /** The plugin manager to use to query plugins. */
    private final PluginManager pluginManager;
    /** Provider of global command parsers. */
    private final Provider<GlobalCommandParser> globalCommandParserProvider;
    /** Provider of global windows. */
    private final Provider<GlobalWindow> globalWindowProvider;
    /** Tab-completer utilities. */
    private final TabCompleterUtils tabCompleterUtils;

    /**
     * Creates a new instance of the {@link Ifplugin} command.
     *
     * @param controller                  The controller to use for command information.
     * @param pluginManager               The plugin manager to use to query plugins.
     * @param globalCommandParserProvider Provider to use to retrieve a global command parser.
     * @param globalWindowProvider        Provider to use to retrieve a global window.
     */
    @Inject
    public Ifplugin(
            final CommandController controller,
            final PluginManager pluginManager,
            final Provider<GlobalCommandParser> globalCommandParserProvider,
            final Provider<GlobalWindow> globalWindowProvider,
            final TabCompleterUtils tabCompleterUtils) {
        super(controller);
        this.pluginManager = pluginManager;
        this.globalCommandParserProvider = globalCommandParserProvider;
        this.globalWindowProvider = globalWindowProvider;
        this.tabCompleterUtils = tabCompleterUtils;
    }

    @Override
    public void execute(@Nonnull final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {
        if (args.getArguments().length <= 1) {
            showUsage(origin, args.isSilent(), "ifplugin", "<[!]plugin> <command>");
            return;
        }

        final boolean negative = args.getArguments()[0].charAt(0) == '!';

        final String pname = args.getArguments()[0].substring(negative ? 1 : 0);

        final PluginInfo pluginInfo = pluginManager.getPluginInfoByName(pname);

        boolean result = true;

        if (pluginInfo == null || !pluginInfo.isLoaded()) {
            result = false;
        }

        if (result != negative) {
            if (origin.isWritable()) {
                origin.getCommandParser().parseCommand(origin, args.getArgumentsAsString(1));
            } else {
                globalCommandParserProvider.get()
                        .parseCommand(globalWindowProvider.get(), args.getArgumentsAsString(1));
            }
        }
    }

    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final IntelligentCommandContext context) {
        final AdditionalTabTargets res;

        if (arg == 0) {
            res = new AdditionalTabTargets().excludeAll();

            for (PluginInfo possPlugin : pluginManager.getPluginInfos()) {
                res.add(possPlugin.getMetaData().getName());
                res.add('!' + possPlugin.getMetaData().getName());
            }
        } else {
            res = tabCompleterUtils.getIntelligentResults(arg, context, 1);
        }

        return res;
    }

}
