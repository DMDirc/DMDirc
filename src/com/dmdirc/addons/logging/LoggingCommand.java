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

package com.dmdirc.addons.logging;

import com.dmdirc.Server;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.ServerCommand;
import com.dmdirc.plugins.Plugin;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.interfaces.InputWindow;
import java.util.List;

/**
 * The dcop command retrieves information from a dcop application.
 *
 * @author Shane "Dataforce" Mc Cormack
 */
public final class LoggingCommand extends ServerCommand implements IntelligentCommand {

    /**
     * Creates a new instance of LoggingCommand.
     */
    public LoggingCommand() {
        super();
        CommandManager.registerCommand(this);
    }

    /** {@inheritDoc} */
    @Override
    public void execute(final InputWindow origin, final Server server,
                        final boolean isSilent, final CommandArguments args) {
        final PluginInfo pluginInfo = PluginManager.getPluginManager().getPluginInfoByName("logging");
        if (pluginInfo == null) {
            sendLine(origin, isSilent, FORMAT_ERROR, "Logging Plugin is not loaded.");
            return;
        }
        final Plugin gotPlugin = pluginInfo.getPlugin();

        if (!(gotPlugin instanceof LoggingPlugin)) {
            sendLine(origin, isSilent, FORMAT_ERROR, "Logging Plugin is not loaded.");
            return;
        }

        final LoggingPlugin plugin = (LoggingPlugin) gotPlugin;

        if (args.getArguments().length > 0) {
            if (args.getArguments()[0].equalsIgnoreCase("reload")) {
                if (PluginManager.getPluginManager().reloadPlugin(pluginInfo.getFilename())) {
                    sendLine(origin, isSilent, FORMAT_OUTPUT, "Plugin reloaded.");
                } else {
                    sendLine(origin, isSilent, FORMAT_ERROR, "Plugin failed to reload.");
                }
            } else if (args.getArguments()[0].equalsIgnoreCase("history")) {
                if (!plugin.showHistory(origin)) {
                    sendLine(origin, isSilent, FORMAT_ERROR, "Unable to open history for this window.");
                }
            } else if (args.getArguments()[0].equalsIgnoreCase("help")) {
                sendLine(origin, isSilent, FORMAT_OUTPUT, getName() + " reload           - Reload the logging plugin.");
                sendLine(origin, isSilent, FORMAT_OUTPUT, getName() + " history          - Open the history of this window, if available.");
                sendLine(origin, isSilent, FORMAT_OUTPUT, getName() + " help             - Show this help.");
            } else {
                sendLine(origin, isSilent, FORMAT_ERROR, "Unknown command '" + args.getArguments()[0] + "'. Use " + getName() + " help for a list of commands.");
            }
        } else {
            sendLine(origin, isSilent, FORMAT_ERROR, "Use " + getName() + " help for a list of commands.");
        }
    }

    /**
     * Returns a list of suggestions for the specified argument, given the list
     * of previous arguments.
     *
     * @param arg The argument that is being completed
     * @param previousArgs The contents of the previous arguments, if any
     * @return A list of suggestions for the argument
     */
    @Override
    public AdditionalTabTargets getSuggestions(final int arg, final List<String> previousArgs) {
        final AdditionalTabTargets res = new AdditionalTabTargets();
        if (arg == 0) {
            res.add("reload");
            res.add("history");
            res.add("help");
            res.excludeAll();
        }
        return res;
    }

    /**
     * Returns this command's name.
     *
     * @return The name of this command
     */
    @Override
    public String getName() {
        return "logging";
    }

    /**
     * Returns whether or not this command should be shown in help messages.
     *
     * @return True iff the command should be shown, false otherwise
     */
    @Override
    public boolean showInHelp() {
        return true;
    }

    /**
     * Returns a string representing the help message for this command.
     *
     * @return the help message for this command
     */
    @Override
    public String getHelp() {
        return this.getName() + " <set|help> [parameters]";
    }

}

