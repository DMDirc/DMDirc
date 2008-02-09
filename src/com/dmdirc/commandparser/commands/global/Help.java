/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.ServerCommand;
import com.dmdirc.ui.interfaces.ChannelWindow;
import com.dmdirc.ui.interfaces.InputWindow;
import com.dmdirc.ui.interfaces.QueryWindow;
import com.dmdirc.ui.interfaces.ServerWindow;

import com.dmdirc.ui.messages.Styliser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The help command shows the user a list of available commands, along with
 * their arguments, and a description. It is context-aware, so channel commands
 * are only displayed when in a channel window, for example.
 * @author chris
 */
public final class Help extends ServerCommand {
    
    /**
     * Creates a new instance of Help.
     */
    public Help() {
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
        if (args.length == 0) {
            showAllCommands(origin, isSilent);
        } else {
            showCommand(origin, isSilent, args[0]);
        }
    }
    
    private void showAllCommands(final InputWindow origin, final boolean isSilent) {
        final List<Command> commands = new ArrayList<Command>();

        commands.addAll(CommandManager.getCommands(CommandType.TYPE_GLOBAL));
        
        if (origin instanceof ServerWindow) {
            commands.addAll(CommandManager.getCommands(CommandType.TYPE_SERVER));
        } else if (origin instanceof ChannelWindow) {
            commands.addAll(CommandManager.getCommands(CommandType.TYPE_CHANNEL));
            commands.addAll(CommandManager.getCommands(CommandType.TYPE_CHAT));
            commands.addAll(CommandManager.getCommands(CommandType.TYPE_SERVER));
        } else if (origin instanceof QueryWindow) {
            commands.addAll(CommandManager.getCommands(CommandType.TYPE_QUERY));
            commands.addAll(CommandManager.getCommands(CommandType.TYPE_CHAT));
            commands.addAll(CommandManager.getCommands(CommandType.TYPE_SERVER));
        }
        
        Collections.sort(commands);
        
        sendLine(origin, isSilent, FORMAT_OUTPUT, Styliser.CODE_FIXED
                + "----------------------- Available commands -------");
        
        final StringBuilder builder = new StringBuilder();
        
        for (Command command : commands) {
            if (builder.length() + command.getName().length() + 1 > 50) {
                sendLine(origin, isSilent, FORMAT_OUTPUT, Styliser.CODE_FIXED + builder.toString());
                builder.delete(0, builder.length());
            } else if (builder.length() > 0) {
                builder.append(' ');
            }
            
            builder.append(command.getName());
        }
        
        if (builder.length() > 0) {
            sendLine(origin, isSilent, FORMAT_OUTPUT, Styliser.CODE_FIXED + builder.toString());
        }
        
        sendLine(origin, isSilent, FORMAT_OUTPUT, Styliser.CODE_FIXED
                + "--------------------------------------------------");
    }
    
    private void showCommand(final InputWindow origin, final boolean isSilent,
            final String name) {
        final Command command = CommandManager.getCommand(name);
        
        if (command == null) {
            sendLine(origin, isSilent, FORMAT_ERROR, "Command '" + name + "' not found.");
        } else {
            sendLine(origin, isSilent, FORMAT_OUTPUT, Styliser.CODE_FIXED
                    + "---------------------- Command information -------");            
            sendLine(origin, isSilent, FORMAT_OUTPUT, Styliser.CODE_FIXED
                    + " Name: " + name);
            sendLine(origin, isSilent, FORMAT_OUTPUT, Styliser.CODE_FIXED
                    + " Type: " + command.getType());
            sendLine(origin, isSilent, FORMAT_OUTPUT, Styliser.CODE_FIXED
                    + "Usage: " + command.getHelp());
            sendLine(origin, isSilent, FORMAT_OUTPUT, Styliser.CODE_FIXED
                    + "--------------------------------------------------");            
        }
    }
    
    /** {@inheritDoc}. */
    public String getName() {
        return "help";
    }
    
    /** {@inheritDoc}. */
    public boolean showInHelp() {
        return true;
    }
    
    /** {@inheritDoc}. */
    public String getHelp() {
        return "help [command] - shows client command help";
    }
    
}
