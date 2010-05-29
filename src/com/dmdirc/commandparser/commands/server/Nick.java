/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.commandparser.commands.server;

import com.dmdirc.FrameContainer;
import com.dmdirc.Server;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.CommandOptions;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.commandparser.commands.context.ServerCommandContext;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.input.TabCompletionType;

/**
 * Allows the user to change nickname.
 * @author chris
 */
@CommandOptions(allowOffline=false)
public final class Nick extends Command implements IntelligentCommand,
        CommandInfo {

    /**
     * Creates a new instance of Nick.
     */
    public Nick() {
        super();

        CommandManager.registerCommand(this);
    }

    /** {@inheritDoc} */
    @Override
    public void execute(final FrameContainer<?> origin,
            final CommandArguments args, final CommandContext context) {
        final Server server = ((ServerCommandContext) context).getServer();
        if (args.getArguments().length == 0) {
            showUsage(origin, args.isSilent(), "nick", "<new nickname>");
            return;
        }

        server.getParser().getLocalClient().setNickname(args.getArguments()[0]);
    }


    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "nick";
    }

    /** {@inheritDoc} */
    @Override
    public boolean showInHelp() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public CommandType getType() {
        return CommandType.TYPE_SERVER;
    }

    /** {@inheritDoc} */
    @Override
    public String getHelp() {
        return "nick <new nickname> - attempts to change your nickname to the one specified";
    }

    /** {@inheritDoc} */
    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final IntelligentCommandContext context) {
        final AdditionalTabTargets res = new AdditionalTabTargets();

        res.exclude(TabCompletionType.COMMAND);

        if (arg > 0) {
            res.excludeAll();
        }

        return res;
    }

}
