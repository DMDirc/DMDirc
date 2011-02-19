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

package com.dmdirc.commandparser.commands.global;

import com.dmdirc.FrameContainer;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.ui.input.AdditionalTabTargets;

/**
 * Allows the user to load a plugin.
 */
public class LoadPlugin extends Command implements IntelligentCommand,
        CommandInfo {

    /** {@inheritDoc} */
    @Override
    public void execute(final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {
        if (args.getArguments().length == 0) {
            showUsage(origin, args.isSilent(), "loadplugin", "<plugin>");
            return;
        }

        // Add previously unknown plugin to plugin manager
        PluginManager.getPluginManager().addPlugin(
                args.getArgumentsAsString());
        final PluginInfo plugin = PluginManager.getPluginManager()
                .getPluginInfo(args.getArgumentsAsString());

        if (plugin == null) {
            sendLine(origin, args.isSilent(), FORMAT_ERROR,
                    "Plugin loading failed");
        } else if (plugin.isLoaded()) {
            sendLine(origin, args.isSilent(), FORMAT_OUTPUT,
                    "Plugin already loaded.");
        } else {
            plugin.loadPlugin();
            if (plugin.isLoaded()) {
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT,
                        "Plugin loaded.");
                PluginManager.getPluginManager().updateAutoLoad(plugin);
            } else {
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT,
                        "Loading plugin failed. ("
                        + plugin.getLastError() + ")");
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "loadplugin";
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
        return "loadplugin <plugin> - loads the specified class as a plugin";
    }

    /** {@inheritDoc} */
    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final IntelligentCommandContext context) {
        final AdditionalTabTargets res = new AdditionalTabTargets();

        res.excludeAll();

        if (arg == 0) {
            for (PluginInfo possPlugin
                    : PluginManager.getPluginManager()
                            .getPossiblePluginInfos(false)) {
                res.add(possPlugin.getRelativeFilename());
            }
        }

        return res;
    }

}
