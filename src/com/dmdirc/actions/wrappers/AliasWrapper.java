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

package com.dmdirc.actions.wrappers;

import com.dmdirc.GlobalWindow;
import com.dmdirc.Server;
import com.dmdirc.ServerManager;
import com.dmdirc.actions.Action;
import com.dmdirc.actions.ActionCondition;
import com.dmdirc.actions.ActionGroup;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.input.TabCompletionType;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates alias actions.
 */
public class AliasWrapper extends ActionGroup {

    /** Singleton instance of the alias wrapper. */
    private static AliasWrapper me;

    /** A list of registered alias names. */
    private final List<String> aliases = new ArrayList<>();

    /** Command controller to get command info from. */
    private final CommandController commandController;

    /** Server Manager. */
    private final ServerManager serverManager;

    /**
     * Creates a new instance of AliasWrapper.
     *
     * @param commandController Command controller to get command info from.
     */
    public AliasWrapper(final CommandController commandController, final ServerManager serverManager) {
        super("aliases");

        this.commandController = commandController;
        this.serverManager = serverManager;
    }

    /**
     * Retrieves a singleton instance of this alias wrapper.
     *
     * @return A singleton instance of AliasWrapper
     */
    @Deprecated
    public static AliasWrapper getAliasWrapper() {
        return me;
    }

    /**
     * Sets the alias wrapper that will be used as a singleton instance.
     *
     * @param wrapper The wrapper to use as a singleton.
     */
    @Deprecated
    public static void setAliasWrapper(final AliasWrapper wrapper) {
        me = wrapper;
    }

    /**
     * Retrieves a list of alias names registered with this wrapper.
     *
     * @return A list of alias names
     */
    public List<String> getAliases() {
        return new ArrayList<>(aliases);
    }

    /** {@inheritDoc} */
    @Override
    public void add(final Action action) {
        if (action.getTriggers()[0].equals(CoreActionType.UNKNOWN_COMMAND)) {

            final String commandName = getCommandName(action);

            if (commandName != null) {
                super.add(action);
                aliases.add(commandName);

                if (GlobalWindow.getGlobalWindow() != null) {
                    GlobalWindow.getGlobalWindow().getTabCompleter()
                            .addEntry(TabCompletionType.COMMAND, commandName);
                }

                for (Server server : serverManager.getServers()) {
                    server.getTabCompleter().addEntry(TabCompletionType.COMMAND, commandName);
                }
            } else {
                Logger.userError(ErrorLevel.MEDIUM, "Invalid alias action (no name): "
                        + action.getName());
            }
        } else {
            Logger.userError(ErrorLevel.MEDIUM, "Invalid alias action (wrong trigger): "
                    + action.getName());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void remove(final Action action) {
        if (action.getTriggers()[0].equals(CoreActionType.UNKNOWN_COMMAND)) {
            super.remove(action);

            final String commandName = getCommandName(action);

            aliases.remove(commandName);

            for (Server server : serverManager.getServers()) {
                server.getTabCompleter().removeEntry(TabCompletionType.COMMAND, commandName);
            }
        }
    }

    /**
     * Retrieves the command name of the specified alias action.
     *
     * @param action The action whose name is to be determined
     * @return The command name for the specified alias, or null if it has
     *         no appropriate conditions.
     */
    public String getCommandName(final Action action) {
        for (ActionCondition condition : action.getConditions()) {
            if (condition.getArg() == 1) {
                return commandController.getCommandChar()
                        + condition.getTarget();
            }
        }

        // How can we have an alias without a command name?
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isDelible() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "Aliases allow you to create new commands that invoke one or "
                + "more other commands. You can manage aliases using the \""
                + "Alias Manager\", located in the Settings menu.";
    }

}
