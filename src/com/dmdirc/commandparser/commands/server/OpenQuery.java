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

package com.dmdirc.commandparser.commands.server;

import com.dmdirc.Server;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.ServerCommand;
import com.dmdirc.commandparser.commands.WrappableCommand;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.input.TabCompletionType;
import com.dmdirc.ui.interfaces.InputWindow;
import com.dmdirc.ui.messages.Styliser;

import java.util.List;

/**
 * Allows the user to open a query dialog with another user.
 * @author chris
 */
public final class OpenQuery extends ServerCommand implements
        IntelligentCommand, WrappableCommand {
    
    /**
     * Creates a new instance of Query.
     */
    public OpenQuery() {
        super();
        
        CommandManager.registerCommand(this);
    }
    
    /** {@inheritDoc} */
    @Override
    public void execute(final InputWindow origin, final Server server,
            final boolean isSilent, final CommandArguments args) {
        if (args.getArguments().length == 0) {
            showUsage(origin, isSilent, "query", "<target> <message>");
            return;
        }
            
        if (server.getParser().isValidChannelName(args.getArguments()[0])) {
            sendLine(origin, isSilent, FORMAT_ERROR, "You can't open a query "
                    + "with a channel; maybe you meant " + Styliser.CODE_FIXED
                    + Styliser.CODE_BOLD + CommandManager.getCommandChar()
                    + (args.getArguments().length > 1 ? "msg" : "join") + " "
                    + args.getArgumentsAsString()
                    + Styliser.CODE_BOLD + Styliser.CODE_FIXED + "?");
            return;
        }

        if (server.hasQuery(args.getArguments()[0])) {
            server.getQuery(args.getArguments()[0]).activateFrame();
        } else {
            server.addQuery(args.getArguments()[0]);
            server.getQuery(args.getArguments()[0]).show();
        }

        if (args.getArguments().length > 1) {
            server.getQuery(args.getArguments()[0]).sendLine(args.getArgumentsAsString(1));
        }
    }
    
    
    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "query";
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean showInHelp() {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getHelp() {
        return "query <user> [message] - opens a query with the specified user";
    }
    
    /** {@inheritDoc} */
    @Override
    public AdditionalTabTargets getSuggestions(final int arg, final List<String> previousArgs) {
        final AdditionalTabTargets targets = new AdditionalTabTargets();
        
        if (arg == 0) {
            targets.excludeAll();
            targets.include(TabCompletionType.CHANNEL_NICK);
        }
        
        return targets;
    }
    
    /** {@inheritDoc} */
    @Override
    public int getLineCount(final InputWindow origin, final CommandArguments arguments) {
        if (arguments.getArguments().length >= 2) {
            final String target = arguments.getArguments()[0];
            return origin.getContainer().getServer().getNumLines("PRIVMSG "
                    + target + " :" + arguments.getArgumentsAsString(1));
        } else {
            return 1;
        }
    }
    
}
