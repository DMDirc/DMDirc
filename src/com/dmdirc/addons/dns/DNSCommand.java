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

package com.dmdirc.addons.dns;

import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.commands.GlobalCommand;
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
    @Override
    public void execute(final InputWindow origin, final boolean isSilent,
            final CommandArguments args) {
        if (args.getArguments().length == 0) {
            showUsage(origin, isSilent, "dns", "<IP|hostname>");
            return;
        }
        
        sendLine(origin, isSilent, FORMAT_OUTPUT, "Resolving: " + args.getArguments()[0]);
        new Timer("DNS Command Timer").schedule(new TimerTask() {
            /** {@inheritDoc} */
            @Override
            public void run() {
                if (args.getArguments()[0].matches("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b")) {
                    sendLine(origin, isSilent, FORMAT_OUTPUT, "Resolved: "
                            + args.getArguments()[0] + ": "
                            + DNSPlugin.getHostname(args.getArguments()[0]));
                } else {
                    sendLine(origin, isSilent, FORMAT_OUTPUT, "Resolved: "
                            + args.getArguments()[0] + ": "
                            + DNSPlugin.getIPs(args.getArguments()[0]));
                }
            }
        }, 0);
    }
    
    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "dns";
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean showInHelp() {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getHelp() {
        return "dns <IP|hostname> - Performs DNS lookup of the specified ip/hostname/nickname";
    }
    
}
