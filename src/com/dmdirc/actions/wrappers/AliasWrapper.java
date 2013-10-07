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

import com.dmdirc.FrameContainer;
import com.dmdirc.WritableFrameContainer;
import com.dmdirc.actions.Action;
import com.dmdirc.actions.ActionCondition;
import com.dmdirc.actions.ActionGroup;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.input.TabCompletionType;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Encapsulates alias actions.
 */
@Singleton
public class AliasWrapper extends ActionGroup {

    /** The name of the actions group we wrap. */
    protected static final String GROUP_NAME = "aliases";

    /** A list of registered alias names. */
    private final List<String> aliases = new ArrayList<>();

    /** Command controller to get command info from. */
    private final CommandController commandController;

    /** Server Manager. */
    private final WindowManager windowManager;

    /**
     * Creates a new instance of AliasWrapper.
     *
     * @param commandController Command controller to get command info from.
     * @param windowManager The window manager to use to find root windows.
     */
    @Inject
    public AliasWrapper(
            final CommandController commandController,
            final WindowManager windowManager) {
        super(GROUP_NAME);

        this.commandController = commandController;
        this.windowManager = windowManager;
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

                for (FrameContainer root : windowManager.getRootWindows()) {
                    if (root instanceof WritableFrameContainer) {
                        ((WritableFrameContainer) root).getTabCompleter()
                                .addEntry(TabCompletionType.COMMAND, commandName);
                    }
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

            for (FrameContainer root : windowManager.getRootWindows()) {
                if (root instanceof WritableFrameContainer) {
                    ((WritableFrameContainer) root).getTabCompleter()
                            .removeEntry(TabCompletionType.COMMAND, commandName);
                }
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
