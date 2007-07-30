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

package com.dmdirc.addons.dns;

import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.GlobalCommand;
import com.dmdirc.ui.interfaces.InputWindow;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Performs DNS lookups for nicknames, hostnames or IPs.
 */
public final class DNSCommand extends GlobalCommand {
    
    /** Creates a new instance of DNSCommand. */
    public DNSCommand() {
        super();
        
        CommandManager.registerCommand(this);
    }
    
    /** {@inheritDoc} */
    public void execute(final InputWindow origin, final boolean isSilent,
            final String... args) {
        sendLine(origin, isSilent, "commandOutput", "Resolving: " + args[0]);
        new Timer("DNS Command Timer").schedule(new TimerTask() {
            public void run() {
                if (args[0].matches("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b")) {
                    sendLine(origin, isSilent, "commandOutput", "Resolved: " + args[0] + ": " + DNSPlugin.getHostname(args[0]));
                } else {
                    sendLine(origin, isSilent, "commandOutput", "Resolved: " + args[0] + ": " + DNSPlugin.getIPs(args[0]));
                }
            }
        }, 0);
    }
    
    /** {@inheritDoc} */
    public String getName() {
        return "dns";
    }
    
    /** {@inheritDoc} */
    public boolean showInHelp() {
        return true;
    }
    
    /** {@inheritDoc} */
    public boolean isPolyadic() {
        return false;
    }
    
    /** {@inheritDoc} */
    public int getArity() {
        return 1;
    }
    
    /** {@inheritDoc} */
    public String getHelp() {
        return "dns <IP|hostname> - Performs DNS lookup of the specified ip/hostname/nickname";
    }
    
}
