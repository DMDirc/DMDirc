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

package uk.org.ownage.dmdirc.commandparser.commands.server;

import uk.org.ownage.dmdirc.Config;
import uk.org.ownage.dmdirc.Server;
import uk.org.ownage.dmdirc.commandparser.CommandManager;
import uk.org.ownage.dmdirc.commandparser.CommandWindow;
import uk.org.ownage.dmdirc.commandparser.ServerCommand;
import uk.org.ownage.dmdirc.identities.Identity;
import uk.org.ownage.dmdirc.identities.IdentityManager;

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
     * @param args The user supplied arguments
     */
    public void execute(final CommandWindow origin, final Server server,
            final String... args) {
        
        final Identity identity = IdentityManager.getNetworkConfig(server.getNetwork());
        
        if (args.length == 0 || args[0].toLowerCase().equals("view")) {
            
            if (identity.hasOption("network", "ignorelist")) {
                final String list = identity.getOption("network", "ignorelist");
                
                if (list.length() > 0) {
                    origin.addLine("Ignore list:");
                    
                    int i = 0;
                    for (String line : list.split("\n")) {
                        i++;
                        origin.addLine(i + ". " + list);
                    }
                    
                } else {
                    origin.addLine("No ignore list entries for this network.");
                }
                
            } else {
                origin.addLine("No ignore list entries for this network.");
            }
            
        } else if (args[0].toLowerCase().equals("add") && args.length > 1) {
            
            final String host = implodeArgs(1, args);
            String list = host;
            
            if (identity.hasOption("network", "ignorelist")) {
                list = identity.getOption("network", "ignorelist");
                list = list + "\n" + host;
            }
            
            identity.setOption("network", "ignorelist", list);
            
            origin.addLine("Added " + host + " to the ignore list.");
            
        } else if (args[0].toLowerCase().equals("remove") && args.length > 1) {
            
            final String host = implodeArgs(1, args);
            
            final StringBuffer newlist = new StringBuffer();
            boolean found = false;
            
            if (identity.hasOption("network", "ignorelist")) {
                final String list = identity.getOption("network", "ignorelist");
                
                
                for (String entry : list.split("\n")) {
                    if (entry.toLowerCase().equals(host)) {
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
                origin.addLine("Removed " + host + " from the ignore list.");
            } else {
                origin.addLine("Host '" + host + "' not found.");
            }
            
        } else {
            origin.addLine("Usage: "
                    + Config.getOption("general", "commandchar")
                    + "ignore <add|remove|view> [host]");
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
