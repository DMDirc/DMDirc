/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.ui.input;

import com.dmdirc.commandparser.CommandType;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.config.provider.AggregateConfigProvider;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Factory for {@link TabCompleter}s which can inject the appropriate default tab completion
 * targets.
 */
@Singleton
public class TabCompleterFactory {

    /** The command controller to use to find commands. */
    private final Provider<CommandController> commandController;

    /**
     * Creates a new instance of {@link TabCompleterFactory}.
     *
     * @param commandController The command controller to use to find commands.
     */
    @Inject
    public TabCompleterFactory(
            final Provider<CommandController> commandController) {
        this.commandController = commandController;
    }

    /**
     * Gets a new root tab completer with the specified command types added as completion targets.
     *
     * @param configProvider The configuration provider to use for completion settings.
     * @param commandTypes   The types of command to be added
     *
     * @return A new tab completer with the appropriate configuration.
     */
    public TabCompleter getTabCompleter(
            final AggregateConfigProvider configProvider,
            final CommandType... commandTypes) {
        final TabCompleter tabCompleter = new TabCompleterImpl(configProvider);
        addCommands(tabCompleter, commandTypes);
        return tabCompleter;
    }

    /**
     * Gets a new child tab completer with the specified additional command types added as
     * completion targets.
     *
     * @param parent         The parent tab completer to inherit completions from.
     * @param configProvider The configuration provider to use for completion settings.
     * @param commandTypes   The types of command to be added
     *
     * @return A new tab completer with the appropriate configuration.
     */
    public TabCompleter getTabCompleter(
            final TabCompleter parent,
            final AggregateConfigProvider configProvider,
            final CommandType... commandTypes) {
        final TabCompleter tabCompleter = new TabCompleterImpl(configProvider, parent);
        addCommands(tabCompleter, commandTypes);
        return tabCompleter;
    }

    /**
     * Adds all commands of the specified type to the given completer.
     *
     * @param tabCompleter The completer to add commands to.
     * @param commandTypes The types of command that should be added to the completer.
     */
    private void addCommands(final TabCompleter tabCompleter, final CommandType... commandTypes) {
        for (CommandType commandType : commandTypes) {
            tabCompleter.addEntries(TabCompletionType.COMMAND,
                    commandController.get().getCommandNames(commandType));
        }
    }

}
