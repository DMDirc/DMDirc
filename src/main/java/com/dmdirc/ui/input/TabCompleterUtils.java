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

import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.WindowModel;

import java.util.Arrays;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Utilities relating to {@link TabCompleter}s.
 */
@Singleton
public class TabCompleterUtils {

    private final CommandController commandController;

    @Inject
    public TabCompleterUtils(final CommandController commandController) {
        this.commandController = commandController;
    }

    /**
     * Retrieves intelligent results for a deferred command.
     *
     * @param arg     The argument number that is being requested
     * @param context Intelligent tab completion context
     * @param offset  The number of arguments our command used before deferring to this method
     *
     * @return Additional tab targets for the text, or null if none are available
     */
    @Nullable
    public AdditionalTabTargets getIntelligentResults(final int arg,
            final IntelligentCommand.IntelligentCommandContext context, final int offset) {
        if (arg == offset) {
            final AdditionalTabTargets targets = new AdditionalTabTargets().excludeAll();
            targets.include(TabCompletionType.COMMAND);
            return targets;
        } else {
            return getIntelligentResults(context.getWindow(),
                    new CommandArguments(commandController,
                            context.getPreviousArgs().subList(offset,
                                    context.getPreviousArgs().size())), context.getPartial());
        }
    }

    /**
     * Retrieves the intelligent results for the command and its arguments formed from args.
     *
     * @param window  The input window the results are required for
     * @param args    The input arguments
     * @param partial The partially-typed word being completed (if any)
     *
     * @return Additional tab targets for the text, or null if none are available
     *
     * @since 0.6.4
     */
    @Nullable
    private AdditionalTabTargets getIntelligentResults(
            final WindowModel window,
            final CommandArguments args, final String partial) {
        if (!args.isCommand()) {
            return null;
        }

        final Map.Entry<CommandInfo, Command> command =
                commandController.getCommand(args.getCommandName());

        AdditionalTabTargets targets = null;

        if (command != null) {
            if (command.getValue() instanceof IntelligentCommand) {
                targets = ((IntelligentCommand) command.getValue())
                        .getSuggestions(args.getArguments().length,
                                new IntelligentCommand.IntelligentCommandContext(window,
                                        Arrays.asList(args.getArguments()), partial));
            }

            if (command.getKey().getType() == CommandType.TYPE_CHANNEL) {
                if (targets == null) {
                    targets = new AdditionalTabTargets();
                }

                targets.include(TabCompletionType.CHANNEL);
            }
        }

        return targets;
    }

    /**
     * Handles potentially intelligent tab completion.
     *
     * @param window  The input window the results are required for
     * @param text    The text that is being completed
     * @param partial The partially-typed word being completed (if any)
     *
     * @return Additional tab targets for the text, or null if none are available
     *
     * @since 0.6.4
     */
    @Nullable
    public AdditionalTabTargets getIntelligentResults(
            final WindowModel window, final CommandController commandController,
            final String text, final String partial) {
        return getIntelligentResults(window,
                new CommandArguments(commandController, text), partial);
    }
}
