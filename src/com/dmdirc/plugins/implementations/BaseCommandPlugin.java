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

package com.dmdirc.plugins.implementations;

import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.plugins.PluginInfo;

import java.util.HashMap;
import java.util.Map;

import dagger.ObjectGraph;

/**
 * Implementation of {@link BasePlugin} that maintains commands.
 */
public abstract class BaseCommandPlugin extends BasePlugin {

    /**
     * List of commands to load and unload.
     */
    private final Map<CommandInfo, Command> commands = new HashMap<>();
    /**
     * Command controller to register commands.
     */
    private CommandController commandController;

    @Override
    public void onLoad() {
        super.onLoad();
        loadCommands();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        unloadCommands();
    }

    @Override
    public void load(final PluginInfo pluginInfo, final ObjectGraph graph) {
        super.load(pluginInfo, graph);

        commandController = graph.plus(new CommandHelper.CommandHelperModule())
                .get(CommandHelper.class).getCommandController();
    }

    /**
     * Registers a command from this plugin.
     *
     * @param command     Command to register
     * @param commandInfo Command info to register
     */
    protected void registerCommand(final Command command, final CommandInfo commandInfo) {
        commands.put(commandInfo, command);
    }

    /**
     * Registers a command from this plugin.
     *
     * <p>
     * This method will create a new instance of the specified command class using the
     * dependency-injection framework. It must only be called after
     * {@link #setObjectGraph(dagger.ObjectGraph)}, and any command must be injectable using that
     * object graph.
     *
     * @param <T>         The type of the command that will be registered.
     * @param command     The class of the command to register.
     * @param commandInfo Command info to register.
     */
    protected <T extends Command> void registerCommand(
            final Class<T> command,
            final CommandInfo commandInfo) {
        commands.put(commandInfo, getObjectGraph().get(command));
    }

    /**
     * Unregisters a command from this plugin.
     *
     * @param commandInfo Command info to register
     */
    protected void unregisterCommand(final CommandInfo commandInfo) {
        commands.remove(commandInfo);
        commandController.unregisterCommand(commandInfo);
    }

    /**
     * Loads the commands provided by this plugin.
     */
    private void loadCommands() {
        for (Map.Entry<CommandInfo, Command> command : commands.entrySet()) {
            commandController.registerCommand(command.getValue(), command.getKey());
        }
    }

    /**
     * Unloads the commands loaded by this plugin.
     */
    private void unloadCommands() {
        commands.keySet().forEach(commandController::unregisterCommand);
    }

    /**
     * Gets the command controller used.
     *
     * @return A command controller.
     *
     * @deprecated Plugins should inject their own controllers.
     */
    @Deprecated
    protected CommandController getCommandController() {
        return commandController;
    }

}
