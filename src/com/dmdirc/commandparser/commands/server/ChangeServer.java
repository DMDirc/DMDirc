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
import com.dmdirc.commandparser.ServerCommand;
import com.dmdirc.ui.InputWindow;

/**
 * The /server command allows the user to connect to a new server.
 * @author chris
 */
public final class ChangeServer extends ServerCommand {
    
    /**
     * Creates a new instance of ChangeServer.
     */
    public ChangeServer() {
        super();
        
        CommandManager.registerCommand(this);
    }
    
    /** {@inheritDoc} */
    public void execute(final InputWindow origin, final Server server,
            final boolean isSilent, final String... args) {
        if (args.length == 0) {
            sendLine(origin, isSilent, "commandUsage", Config.getCommandChar(), "server",
                    "[--ssl] <host[:port]> [password]");
            return;
        }
        
        boolean ssl = false;
        String host = "";
        String pass = "";
        int port = 6667;
        int offset = 0;
        
        // Check for SSL
        if (args[offset].equalsIgnoreCase("--ssl")) {
            ssl = true;
            offset++;
        }
        
        // Check for port
        if (args[offset].indexOf(':') > -1) {
            final String[] parts = args[offset].split(":");
            host = parts[0];
            try {
                port = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ex) {
                sendLine(origin, isSilent, "commandError", "Invalid port specified");
                return;
            }
        } else {
            host = args[offset];
        }
        
        // Check for password
        if (args.length > ++offset) {
            pass = implodeArgs(offset, args);
        }
        
        server.connect(host, port, pass, ssl, server.getProfile());
    }
    
    
    /** {@inheritDoc}. */
    public String getName() {
        return "server";
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
        return "server [--ssl] <host[:port]> [password] - connect to a different server";
    }
    
}
