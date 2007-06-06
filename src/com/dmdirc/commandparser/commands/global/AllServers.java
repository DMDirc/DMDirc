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

package com.dmdirc.commandparser.commands.global;

import com.dmdirc.Server;
import com.dmdirc.ServerManager;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.CommandWindow;
import com.dmdirc.commandparser.GlobalCommand;

/**
 * The AllServers command allows users to issue commands to all servers.
 * @author chris
 */
public final class AllServers extends GlobalCommand {
    
    /** Creates a new instance of AllServers. */
    public AllServers() {
        super();
        
        CommandManager.registerCommand(this);
    }
    
    /** {@inheritDoc} */
    public void execute(final CommandWindow origin, final boolean isSilent,
            final String... args) {
        final String command = implodeArgs(args);
        CommandWindow window;
        
        for (Server target : ServerManager.getServerManager().getServers()) {
            window = target.getFrame();
            window.getCommandParser().parseCommand(window, command);
        }
    }
    
    
    /** {@inheritDoc}. */
    public String getName() {
        return "allservers";
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
        return "allservers <command> - executes the command as though it had been entered on all servers";
    }
    
}
