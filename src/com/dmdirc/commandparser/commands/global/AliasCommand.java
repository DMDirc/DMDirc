/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.actions.Action;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.wrappers.Alias;
import com.dmdirc.actions.wrappers.AliasWrapper;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.commands.GlobalCommand;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.input.TabCompleter;
import com.dmdirc.ui.interfaces.InputWindow;

import java.util.List;

/**
 * The alias command allows users to create aliases on-the-fly.
 * 
 * @author chris
 */
public final class AliasCommand extends GlobalCommand implements
        IntelligentCommand {

    /**
     * Creates a new instance of Active.
     */
    public AliasCommand() {
        super();

        CommandManager.registerCommand(this);
    }

    /** {@inheritDoc} */
    @Override
    public void execute(final InputWindow origin, final boolean isSilent,
                        final CommandArguments args) {
        if (args.getArguments().length < 2) {
            showUsage(origin, isSilent, "alias", "[--remove] <name> [command]");
            return;
        }

        if (args.getArguments()[0].equalsIgnoreCase("--remove")) {
            final String name
                    = args.getArguments()[1].charAt(0) == CommandManager.getCommandChar()
                    ? args.getArguments()[1].substring(1) : args.getArguments()[1];

            if (doRemove(name)) {
                sendLine(origin, isSilent, FORMAT_OUTPUT, "Alias '" + name +
                         "' removed.");
            } else {
                sendLine(origin, isSilent, FORMAT_ERROR, "Alias '" + name +
                         "' not found.");
            }

            return;
        }

        final String name = args.getArguments()[0].charAt(0) == CommandManager.getCommandChar()
                ? args.getArguments()[0].substring(1) : args.getArguments()[0];

        for (Action alias : AliasWrapper.getAliasWrapper()) {
            if (AliasWrapper.getCommandName(alias).substring(1).equalsIgnoreCase(
                    name)) {
                sendLine(origin, isSilent, FORMAT_ERROR, "Alias '" + name +
                         "' already exists.");
                return;
            }
        }

        final Alias myAlias = new Alias(name);
        myAlias.setResponse(new String[]{args.getArgumentsAsString(1)});
        myAlias.createAction().save();

        sendLine(origin, isSilent, FORMAT_OUTPUT, "Alias '" + name +
                 "' created.");
    }

    /**
     * Removes the alias with the specified name.
     * 
     * @param name The name of the alias to remove
     * @return True if the alias was deleted, false otherwise
     */
    private boolean doRemove(final String name) {
        for (Action alias : AliasWrapper.getAliasWrapper()) {
            if (AliasWrapper.getCommandName(alias).substring(1).equalsIgnoreCase(
                    name)) {
                alias.delete();
                ActionManager.unregisterAction(alias);

                return true;
            }
        }

        return false;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "alias";
    }

    /** {@inheritDoc} */
    @Override
    public boolean showInHelp() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String getHelp() {
        return "alias [--remove] <name> [command] - creates or removes the specified alias";
    }

    /** {@inheritDoc} */
    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
                                               final List<String> previousArgs) {
        final AdditionalTabTargets res = new AdditionalTabTargets().excludeAll();

        if (arg == 0) {
            res.add("--remove");
        } else if (arg == 1 && previousArgs.get(0).equals("--remove")) {
            for (Action alias : AliasWrapper.getAliasWrapper()) {
                res.add(AliasWrapper.getCommandName(alias));
            }
        } else if (arg >= 1 && !previousArgs.get(0).equals("--remove")) {
            return TabCompleter.getIntelligentResults(arg, previousArgs, 1);
        }

        return res;
    }

}
