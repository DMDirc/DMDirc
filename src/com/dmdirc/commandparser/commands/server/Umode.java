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
import com.dmdirc.ServerState;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.commands.CommandOptions;
import com.dmdirc.commandparser.commands.ServerCommand;
import com.dmdirc.ui.interfaces.InputWindow;

/**
 * Allows the user to change user modes.
 * 
 * @author chris
 */
@CommandOptions(allowOffline=false)
public class Umode extends ServerCommand {
    
    /**
     * Creates a new instance of Umode.
     */
    public Umode() {
        super();
        
        CommandManager.registerCommand(this);
    }    

    /** {@inheritDoc} */
    @Override
    public void execute(final InputWindow origin, final Server server, 
            final boolean isSilent, final CommandArguments args) {
        if (server.getState() != ServerState.CONNECTED) {
            sendLine(origin, isSilent, FORMAT_ERROR, "Not connected");
            return;
        }

        server.getParser().sendRawMessage("MODE "
                + server.getParser().getLocalClient().getNickname()
                + " " + args.getArgumentsAsString());
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "umode";
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean showInHelp() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String getHelp() {
        return "umode [modes] - sets or displays your user modes";
    }

}
