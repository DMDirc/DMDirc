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

import com.dmdirc.Channel;
import com.dmdirc.Server;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.actions.interfaces.ActionType;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.ServerCommand;
import com.dmdirc.interfaces.ActionListener;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.interfaces.InputWindow;
import com.dmdirc.ui.messages.Styliser;

import java.util.List;

/**
 * Allows the user to join channels.
 *
 * @since 0.6.3m1
 * @author chris
 */
public final class JoinChannelCommand extends ServerCommand implements
        ActionListener, IntelligentCommand {
    
    /**
     * Creates a new instance of the join channel command.
     */
    public JoinChannelCommand() {
        super();
        
        CommandManager.registerCommand(this);
        ActionManager.addListener(this, CoreActionType.CHANNEL_MESSAGE,
                CoreActionType.CHANNEL_ACTION);
    }
    
    /** {@inheritDoc} */
    @Override
    public void execute(final InputWindow origin, final Server server,
            final boolean isSilent, final CommandArguments args) {
        if (args.getArguments().length == 0) {
            showUsage(origin, isSilent, "join", "join <channel [key]>[,channel [key]...]");
            return;
        }

        for (String pair : args.getArgumentsAsString().split(",")) {
            if (pair.trim().indexOf(' ') == -1) {
                server.join(pair);
            } else {

            }
        }

        if (server.getParser().isValidChannelName(args.getArguments()[0])) {
            sendLine(origin, isSilent, FORMAT_ERROR, "You can't open a query "
                    + "with a channel; maybe you meant " + Styliser.CODE_FIXED
                    + Styliser.CODE_BOLD + CommandManager.getCommandChar()
                    + (args.getArguments().length > 1 ? "msg" : "join")
                    + " " + args.getArgumentsAsString()
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
        return "join";
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean showInHelp() {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getHelp() {
        return "join <channel [key]>[,channel [key]...] - joins the specified channel(s)";
    }

    /** {@inheritDoc} */
    @Override
    public void processEvent(final ActionType type, final StringBuffer format,
            final Object... arguments) {
        final Channel chan = (Channel) arguments[0];
        final String message = (String) arguments[2];
    }

    /** {@inheritDoc} */
    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final List<String> previousArgs) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
