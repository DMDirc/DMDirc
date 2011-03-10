/*
 * Copyright (c) 2006-2011 DMDirc Developers
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

package com.dmdirc.plugins;

import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.config.prefs.PreferencesDialogModel;
import com.dmdirc.util.validators.ValidationResponse;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Base implementation of the Plugin interface.
 */
public abstract class BasePlugin implements Plugin {

    /** Domain name for the settings in this plugin. */
    private String myDomain = "plugin-unknown";
    /** Has the domain been set? */
    private boolean domainSet;
    /** Associated Plugin info. */
    private PluginInfo pluginInfo;
    /** Files directory for this plugin. */
    private File filesDir;
    /** List of commands to load and unload. */
    private final Map<CommandInfo, Command> commands =
            new HashMap<CommandInfo, Command>();

    /** {@inheritDoc} */
    @Override
    public void setDomain(final String newDomain) {
        if (!domainSet) {
            domainSet = true;
            myDomain = newDomain;
            domainUpdated();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onLoad() {
        loadCommands();
    }

    /** {@inheritDoc} */
    @Override
    public void onUnload() {
        unloadCommands();
    }

    /**
     * Registers a command from this plugin.
     *
     * @param command Command to register
     * @param commandInfo Command info to register
     */
    protected void registerCommand(final Command command,
            final CommandInfo commandInfo) {
        commands.put(commandInfo, command);
    }

    /**
     * Unregisters a command from this plugin.
     *
     * @param commandInfo Command info to register
     */
    protected void unregisterCommand(final CommandInfo commandInfo) {
        commands.remove(commandInfo);
        CommandManager.getCommandManager().unregisterCommand(commandInfo);
    }

    /** Loads the commands provided by this plugin. */
    private void loadCommands() {
        for (Entry<CommandInfo, Command> command : commands.entrySet()) {
            CommandManager.getCommandManager().registerCommand(
                    command.getValue(), command.getKey());
        }
    }

    /** Unloads the commands loaded by this plugin. */
    private void unloadCommands() {
        for (CommandInfo command : commands.keySet()) {
            CommandManager.getCommandManager().unregisterCommand(command);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated PluginInfo should be obtained using a constructor parameter,
     * if required
     */
    @Override
    @Deprecated
    public void setPluginInfo(final PluginInfo pluginInfo) {
        this.pluginInfo = pluginInfo;
    }

    /** {@inheritDoc} */
    @Override
    public PluginInfo getPluginInfo() {
        return pluginInfo;
    }

    /** {@inheritDoc} */
    @Override
    public String getDomain() {
        return myDomain;
    }

    /**
     * Called when the domain for plugin settings has been set.
     * This will only be called once (either when the plugin is loading, or when
     * its config is being shown).
     */
    protected void domainUpdated() {
        //Define this here so only implementations that care have to override
    }

    /**
     * Get the files directory for this plugin.
     * This will attempt to create the directory if it doesn't exist the first
     * time the directory name is requested.
     *
     * @return Files directory for this plugin.
     */
    protected File getFilesDir() {
        if (filesDir == null) {
            final String fs = System.getProperty("file.separator");
            final String dir = PluginManager.getPluginManager()
                    .getFilesDirectory();
            filesDir = new File(dir + pluginInfo.getMetaData().getName() + fs);
            if (!filesDir.exists()) {
                filesDir.mkdirs();
            }
        }

        return filesDir;
    }

    /**
     * Convenience Method.
     *
     * @return Filesdir as a string with trailing path separator
     */
    protected String getFilesDirString() {
        return getFilesDir().getAbsolutePath()
                + System.getProperty("file.separator");
    }

    /** {@inheritDoc} */
    @Override
    public ValidationResponse checkPrerequisites() {
        return new ValidationResponse();
    }

    /** {@inheritDoc} */
    @Override
    public void showConfig(final PreferencesDialogModel manager) {
        //Define this here so only implementations that care have to override
    }
}
