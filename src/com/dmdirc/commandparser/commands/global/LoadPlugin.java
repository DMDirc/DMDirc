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

import com.dmdirc.commandparser.BaseCommandInfo;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.WindowModel;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.plugins.PluginMetaData;
import com.dmdirc.ui.input.AdditionalTabTargets;

import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * Allows the user to load a plugin.
 */
public class LoadPlugin extends Command implements IntelligentCommand {

    /** A command info object for this command. */
    public static final CommandInfo INFO = new BaseCommandInfo("loadplugin",
            "loadplugin <plugin> - loads the specified class as a plugin",
            CommandType.TYPE_GLOBAL);
    /** The plugin manager to use to load plugins. */
    private final PluginManager pluginManager;

    /**
     * Creates a new instance of the {@link LoadPlugin} command.
     *
     * @param controller    The controller to use for command information.
     * @param pluginManager The plugin manager to load plugins with.
     */
    @Inject
    public LoadPlugin(final CommandController controller, final PluginManager pluginManager) {
        super(controller);
        this.pluginManager = pluginManager;
    }

    @Override
    public void execute(@Nonnull final WindowModel origin,
            final CommandArguments args, final CommandContext context) {
        if (args.getArguments().length == 0) {
            showUsage(origin, args.isSilent(), "loadplugin", "<plugin>");
            return;
        }

        // Add previously unknown plugin to plugin manager
        pluginManager.addPlugin(args.getArgumentsAsString());
        final PluginInfo plugin = pluginManager.getPluginInfo(args.getArgumentsAsString());

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
                pluginManager.updateAutoLoad(plugin);
            } else {
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT,
                        "Loading plugin failed. ("
                        + plugin.getLastError() + ")");
            }
        }
    }

    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final IntelligentCommandContext context) {
        final AdditionalTabTargets res = new AdditionalTabTargets();

        res.excludeAll();

        if (arg == 0) {
            res.addAll(
                    pluginManager.getAllPlugins().stream().map(PluginMetaData::getRelativeFilename)
                            .collect(Collectors.toList()));
        }

        return res;
    }

}
