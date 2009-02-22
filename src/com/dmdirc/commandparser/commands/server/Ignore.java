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
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.ServerCommand;
import com.dmdirc.config.Identity;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.input.TabCompletionType;
import com.dmdirc.ui.interfaces.InputWindow;

import java.util.List;

/**
 * Allows the user to add/view/delete ignores.
 * @author chris
 */
public final class Ignore extends ServerCommand implements IntelligentCommand {
    
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
    @Override
    public void execute(final InputWindow origin, final Server server,
            final boolean isSilent, final CommandArguments args) {
        
        final Identity identity = server.getNetworkIdentity();
        
        if (args.getArguments().length == 0
                || args.getArguments()[0].toLowerCase().equals("view")) {
            
            if (identity.hasOptionString("network", "ignorelist")) {
                final List<String> list = identity.getOptionList("network", "ignorelist");
                
                if (list.isEmpty()) {
                    sendLine(origin, isSilent, FORMAT_ERROR,
                            "No ignore list entries for this network.");

                } else {
                    sendLine(origin, isSilent, FORMAT_OUTPUT, "Ignore list:");
                    
                    int i = 0;
                    for (String line : list) {
                        if (!line.isEmpty()) {
                            i++;
                            sendLine(origin, isSilent, FORMAT_OUTPUT, i + ". " + line);
                        }
                    }                    
                }
                
            } else {
                sendLine(origin, isSilent, FORMAT_ERROR, "No ignore list entries for this network.");
            }
            
        } else if (args.getArguments()[0].toLowerCase().equals("add")
                && args.getArguments().length > 1) {
            
            final String host = args.getArgumentsAsString(1);
            String list = host;
            
            if (identity.hasOptionString("network", "ignorelist")) {
                list = identity.getOption("network", "ignorelist");
                list = list + "\n" + host;
            }
            
            identity.setOption("network", "ignorelist", list);
            
            sendLine(origin, isSilent, FORMAT_OUTPUT, "Added " + host + " to the ignore list.");
            
        } else if (args.getArguments()[0].toLowerCase().equals("remove")
                && args.getArguments().length > 1) {
            
            final String host = server.getParser().getIRCStringConverter()
                    .toLowerCase(args.getArgumentsAsString(1));
            
            final StringBuffer newlist = new StringBuffer();
            boolean found = false;
            
            if (identity.hasOptionString("network", "ignorelist")) {
                final String list = identity.getOption("network", "ignorelist");
                
                
                for (String entry : list.split("\n")) {
                    if (server.getParser().getIRCStringConverter()
                            .toLowerCase(entry).equals(host)) {
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
                sendLine(origin, isSilent, FORMAT_OUTPUT, "Removed " + host
                        + " from the ignore list.");
            } else {
                sendLine(origin, isSilent, FORMAT_ERROR, "Host '" + host + "' not found.");
            }
            
        } else {
            showUsage(origin, isSilent, "ignore", "<add|remove|view> [host]");
        }
        
        server.updateIgnoreList();
        
    }
    
    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "ignore";
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean showInHelp() {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getHelp() {
        return "ignore <add|remove|view> [host] - manages the network's ignore list";
    }

    /** {@inheritDoc} */
    @Override
    public AdditionalTabTargets getSuggestions(final int arg, final List<String> previousArgs) {
        final AdditionalTabTargets targets = new AdditionalTabTargets();
        targets.excludeAll();
        
        if (arg == 0) {
            targets.add("add");
            targets.add("remove");
            targets.add("view");
        } else if (arg == 1) {
            targets.include(TabCompletionType.CHANNEL_NICK);
            targets.include(TabCompletionType.QUERY_NICK);
        }
        
        return targets;
    }
    
}
