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

import com.dmdirc.Server;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.ServerCommand;
import com.dmdirc.config.Identity;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.interfaces.InputWindow;

/**
 * Allows the user to add/view/delete ignores.
 * @author chris
 */
public final class Ignore extends ServerCommand {
    
    /**
     * Creates a new instance of Ignore.
     */
    public Ignore() {
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
        
        final Identity identity = IdentityManager.getNetworkConfig(server.getNetwork());
        
        if (args.length == 0 || args[0].toLowerCase().equals("view")) {
            
            if (identity.hasOption("network", "ignorelist")) {
                final String list = identity.getOption("network", "ignorelist");
                
                if (!list.isEmpty()) {
                    sendLine(origin, isSilent, "commandOutput", "Ignore list:");
                    
                    int i = 0;
                    for (String line : list.split("\n")) {
                        if (!line.isEmpty()) {
                            i++;
                            sendLine(origin, isSilent, "commandOutput", i + ". " + line);
                        }
                    }
                    
                } else {
                    sendLine(origin, isSilent, "commandError", "No ignore list entries for this network.");
                }
                
            } else {
                sendLine(origin, isSilent, "commandError", "No ignore list entries for this network.");
            }
            
        } else if (args[0].toLowerCase().equals("add") && args.length > 1) {
            
            final String host = implodeArgs(1, args);
            String list = host;
            
            if (identity.hasOption("network", "ignorelist")) {
                list = identity.getOption("network", "ignorelist");
                list = list + "\n" + host;
            }
            
            identity.setOption("network", "ignorelist", list);
            
            sendLine(origin, isSilent, "commandOutput", "Added " + host + " to the ignore list.");
            
        } else if (args[0].toLowerCase().equals("remove") && args.length > 1) {
            
            final String host = server.getParser().toLowerCase(implodeArgs(1, args));
            
            final StringBuffer newlist = new StringBuffer();
            boolean found = false;
            
            if (identity.hasOption("network", "ignorelist")) {
                final String list = identity.getOption("network", "ignorelist");
                
                
                for (String entry : list.split("\n")) {
                    if (server.getParser().toLowerCase(entry).equals(host)) {
                        found = true;
                    } else {
                        if (newlist.length() > 0) {
                            newlist.append('\n');
                        }
                        newlist.append(entry);
                    }
                }
            }
            
            if (found) {
                identity.setOption("network", "ignorelist", newlist.toString());
                sendLine(origin, isSilent, "commandOutput", "Removed " + host + " from the ignore list.");
            } else {
                sendLine(origin, isSilent, "commandError", "Host '" + host + "' not found.");
            }
            
        } else {
            sendLine(origin, isSilent, "commandUsage", 
                     IdentityManager.getGlobalConfig().getOption("general", "commandchar"),
                     "ignore", "<add|remove|view> [host]");
        }
        
        server.updateIgnoreList();
        
    }
    
    /** {@inheritDoc}. */
    public String getName() {
        return "ignore";
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
        return "ignore <add|remove|view> [host] - manages the network's ignore list";
    }
    
}
