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
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.GlobalCommand;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.interfaces.InputWindow;

/**
 * The new server command allows users to open a new server window.
 * @author chris
 */
public final class NewServer extends GlobalCommand {
    
    /**
     * Creates a new instance of NewServer.
     */
    public NewServer() {
        super();
        
        CommandManager.registerCommand(this);
    }
    
    /** {@inheritDoc} */
    public void execute(final InputWindow origin, final boolean isSilent,
            final String... args) {
        if (args.length == 0) {
            origin.addLine("commandUsage",
                    IdentityManager.getGlobalConfig().getOption("general", "commandchar"),
                    "newserver", "[--ssl] <host[:port]> [password]");
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
                origin.addLine("commandError", "Invalid port specified");
                return;
            }
        } else {
            host = args[offset];
        }
        
        // Check for password
        if (args.length > ++offset) {
            pass = implodeArgs(offset, args);
        }
        
        new Server(host, port, pass, ssl, IdentityManager.getProfiles().get(0));
    }
    
    
    
    /** {@inheritDoc}. */
    public String getName() {
        return "newserver";
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
        return "newserver [--ssl] <host[:port]> [password] - connect to a new server";
    }
    
}
