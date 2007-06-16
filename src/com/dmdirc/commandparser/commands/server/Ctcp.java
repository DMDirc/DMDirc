/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.Config;
import com.dmdirc.Server;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.IntelligentCommand;
import com.dmdirc.commandparser.ServerCommand;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.interfaces.InputWindow;

import java.util.List;

/**
 * Allows the user to send CTCP messages.
 * @author chris
 */
public final class Ctcp extends ServerCommand implements IntelligentCommand {
    
    /**
     * Creates a new instance of Ctcp.
     */
    public Ctcp() {
        super();
        
        CommandManager.registerCommand(this);
    }
    
    /**
     * Executes this command.
     * @param origin The frame in which this command was issued
     * @param server The server object that this command is associated with
     * @param isSilent Whether this command is silenced or not
     * @param args The user supplied arguments
     */
    public void execute(final InputWindow origin, final Server server,
            final boolean isSilent, final String... args) {
        if (args.length < 2) {
            sendLine(origin, isSilent, "CommandUsage", Config.getCommandChar(),
                    "ctcp", "<target> <type> [arguments]");
        } else {
            server.getParser().sendLine("PRIVMSG " + args[0] + " :"
                    + ((char) 1) + implodeArgs(1, args) + ((char) 1));
            sendLine(origin, isSilent, "selfCTCP", args[0], implodeArgs(1, args));
        }
    }
    
    
    /** {@inheritDoc}. */
    public String getName() {
        return "ctcp";
    }
    
    /** {@inheritDoc}. */
    public boolean showInHelp() {
        return true;
    }
    
    /** {@inheritDoc}. */
    public boolean isPolyadic() {
        return true;
    }
    
    /** {@inheritDoc}. */
    public int getArity() {
        return 0;
    }
    
    /** {@inheritDoc}. */
    public String getHelp() {
        return "ctcp <target> <type> [arguments] - sends a CTCP message";
    }
    
    /** {@inheritDoc} */
    public AdditionalTabTargets getSuggestions(int arg, List<String> previousArgs) {
        final AdditionalTabTargets res = new AdditionalTabTargets();
        
        if (arg == 1) {
            res.add("VERSION");
            res.add("ACTION");
            res.add("USERINFO");
            res.add("CLIENTINFO");
            res.add("PING");
            res.add("FINGER");
            res.add("SOURCE");
            res.add("TIME");
            res.setIncludeNormal(false);
        }
        
        return res;
    }
    
}
