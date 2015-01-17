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
import com.dmdirc.commandparser.aliases.Alias;
import com.dmdirc.commandparser.aliases.AliasFactory;
import com.dmdirc.commandparser.aliases.AliasManager;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.WindowModel;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.input.TabCompleterUtils;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * The alias command allows users to create aliases on-the-fly.
 */
public class AliasCommand extends Command implements IntelligentCommand {

    /** A command info object for this command. */
    public static final CommandInfo INFO = new BaseCommandInfo("alias",
            "alias [--remove] <name> [command] - creates or removes the specified alias",
            CommandType.TYPE_GLOBAL);
    /** Factory to use when creating aliases. */
    private final AliasFactory aliasFactory;
    /** Manager to use to modify aliases. */
    private final AliasManager aliasManager;
    /** Tab-completer utilities. */
    private final TabCompleterUtils tabCompleterUtils;

    /**
     * Creates a new instance of {@link AliasCommand}.
     *
     * @param controller    The controller that owns this command.
     * @param aliasFactory The factory to use when creating new aliases.
     * @param aliasManager The manager to use to modify aliases.
     */
    @Inject
    public AliasCommand(
            final CommandController controller,
            final AliasFactory aliasFactory,
            final AliasManager aliasManager,
            final TabCompleterUtils tabCompleterUtils) {
        super(controller);
        this.aliasFactory = aliasFactory;
        this.aliasManager = aliasManager;
        this.tabCompleterUtils = tabCompleterUtils;
    }

    @Override
    public void execute(@Nonnull final WindowModel origin,
            final CommandArguments args, final CommandContext context) {
        if (args.getArguments().length < 2) {
            showUsage(origin, args.isSilent(), "alias", "[--remove] <name> [command]");
            return;
        }

        if ("--remove".equalsIgnoreCase(args.getArguments()[0])) {
            final String name = removeCommandChar(args.getArguments()[1]);
            if (doRemove(name)) {
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "Alias '"
                        + name + "' removed.");
            } else {
                sendLine(origin, args.isSilent(), FORMAT_ERROR, "Alias '"
                        + name + "' not found.");
            }

            return;
        }

        final String name = removeCommandChar(args.getArguments()[0]);

        if (aliasManager.getAlias(name).isPresent()) {
            sendLine(origin, args.isSilent(), FORMAT_ERROR, "Alias '" + name
                    + "' already exists.");
            return;
        }

        final Alias myAlias = aliasFactory.createAlias(name, 0,
                removeCommandChar(args.getArgumentsAsString(1)));
        aliasManager.addAlias(myAlias);

        sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "Alias '" + name + "' created.");
    }

    /**
     * Removes the alias with the specified name.
     *
     * @param name The name of the alias to remove
     *
     * @return True if the alias was deleted, false otherwise
     */
    private boolean doRemove(final String name) {
        final Optional<Alias> alias = aliasManager.getAlias(name);
        alias.ifPresent(aliasManager::removeAlias);
        return alias.isPresent();
    }

    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final IntelligentCommandContext context) {
        final AdditionalTabTargets res = new AdditionalTabTargets().excludeAll();

        if (arg == 0) {
            res.add("--remove");
        } else if (arg == 1 && "--remove".equals(context.getPreviousArgs().get(0))) {
            res.addAll(aliasManager.getAliasNames());
        } else if (arg >= 1 && !"--remove".equals(context.getPreviousArgs().get(0))) {
            return tabCompleterUtils.getIntelligentResults(arg, context, 1);
        }

        return res;
    }

    private String removeCommandChar(final String input) {
        return input.charAt(0) == getController().getCommandChar()
                ? input.substring(1) : input;
    }

}
