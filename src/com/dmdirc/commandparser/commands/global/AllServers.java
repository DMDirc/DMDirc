/*
 * Copyright (c) 2006-2014 DMDirc Developers
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
import com.dmdirc.Server;
import com.dmdirc.ServerManager;
import com.dmdirc.commandparser.BaseCommandInfo;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.input.TabCompleter;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * The AllServers command allows users to issue commands to all servers.
 */
public class AllServers extends Command implements IntelligentCommand {

    /** A command info object for this command. */
    public static final CommandInfo INFO = new BaseCommandInfo("allservers",
            "allservers <command> - executes the command as though it had"
            + " been entered on all servers", CommandType.TYPE_GLOBAL);
    /** Server manager to use to iterate servers. */
    private final ServerManager serverManager;

    /**
     * Creates a new instance of the {@link AllServers} command.
     *
     * @param controller    The controller that owns this command.
     * @param serverManager The server manager to use to iterate servers.
     */
    @Inject
    public AllServers(final CommandController controller, final ServerManager serverManager) {
        super(controller);
        this.serverManager = serverManager;
    }

    @Override
    public void execute(@Nonnull final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {
        final String command = args.getArgumentsAsString();

        for (Server target : serverManager.getServers()) {
            target.getCommandParser().parseCommand(target, command);
        }
    }

    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final IntelligentCommandContext context) {
        return TabCompleter.getIntelligentResults(arg, context, 0);
    }

}
