/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.ServerCommand;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.input.TabCompletionType;
import com.dmdirc.ui.interfaces.InputWindow;

import java.util.List;

/**
 * Allows the user to send privmsgs.
 * @author chris
 */
public final class Message extends ServerCommand implements IntelligentCommand {
    
    /**
     * Creates a new instance of Message.
     */
    public Message() {
        super();
        
        CommandManager.registerCommand(this);
    }
    
    /** {@inheritDoc} */
    @Override
    public void execute(final InputWindow origin, final Server server,
            final boolean isSilent, final String... args) {
        if (args.length < 2) {
            showUsage(origin, isSilent, "msg", "<target> <message>");
        } else {
            server.getParser().sendLine("PRIVMSG " + args[0] + " :"
                    + implodeArgs(1, args));
            sendLine(origin, isSilent, "selfMessage", args[0], implodeArgs(1, args));
        }
    }
    
    
    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "msg";
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean showInHelp() {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getHelp() {
        return "msg <target> <message> - sends a private message";
    }

    /** {@inheritDoc} */
    @Override
    public AdditionalTabTargets getSuggestions(final int arg, final List<String> previousArgs) {
        final AdditionalTabTargets res = new AdditionalTabTargets();
        
        if (arg == 0) {
            res.excludeAll();
            res.include(TabCompletionType.CHANNEL_NICK);
            res.include(TabCompletionType.CHANNEL);
            res.include(TabCompletionType.QUERY_NICK);
        }
        
        return res;
    } 
    
}
