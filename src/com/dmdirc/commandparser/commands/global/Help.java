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

package com.dmdirc.commandparser.commands.global;

import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.GlobalCommand;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.input.TabCompletionType;
import com.dmdirc.ui.interfaces.InputWindow;
import com.dmdirc.ui.messages.Styliser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The help command shows the user a list of available commands, along with
 * their arguments, and a description. It is context-aware, so channel commands
 * are only displayed when in a channel window, for example.
 * @author chris
 */
public final class Help extends GlobalCommand implements IntelligentCommand {
    
    /**
     * Creates a new instance of Help.
     */
    public Help() {
        super();
        
        CommandManager.registerCommand(this);
    }
    
    /** {@inheritDoc} */
    @Override
    public void execute(final InputWindow origin, final boolean isSilent,
            final CommandArguments args) {
        if (args.getArguments().length == 0) {
            showAllCommands(origin, isSilent);
        } else {
            showCommand(origin, isSilent, args.getArguments()[0]);
        }
    }
    
    /**
     * Shows a list of all commands valid for the current window.
     * 
     * @param origin The window the command was executed in
     * @param isSilent Whether this command has been silenced or not
     */
    private void showAllCommands(final InputWindow origin, final boolean isSilent) {
        final List<String> commands = new ArrayList<String>(origin.getCommandParser()
                .getCommands().keySet());

        Collections.sort(commands);
        
        sendLine(origin, isSilent, FORMAT_OUTPUT, Styliser.CODE_FIXED
                + "----------------------- Available commands -------");
        
        final StringBuilder builder = new StringBuilder();
        
        for (String command : commands) {
            if (builder.length() + command.length() + 1 > 50) {
                sendLine(origin, isSilent, FORMAT_OUTPUT, Styliser.CODE_FIXED + builder.toString());
                builder.delete(0, builder.length());
            } else if (builder.length() > 0) {
                builder.append(' ');
            }
            
            builder.append(command);
        }
        
        if (builder.length() > 0) {
            sendLine(origin, isSilent, FORMAT_OUTPUT, Styliser.CODE_FIXED + builder.toString());
        }
        
        sendLine(origin, isSilent, FORMAT_OUTPUT, Styliser.CODE_FIXED
                + "--------------------------------------------------");
    }
    
    /**
     * Shows information about the specified command.
     * 
     * @param origin The window the command was executed in
     * @param isSilent Whether this command has been silenced or not
     * @param name The name of the command to display info for
     */
    private void showCommand(final InputWindow origin, final boolean isSilent,
            final String name) {
        Map.Entry<CommandInfo, Command> command = null;

        if (name.length() > 0 && name.charAt(0) == CommandManager.getCommandChar()) {
            command = CommandManager.getCommand(name.substring(1));
        } else {
            command = CommandManager.getCommand(name);
        }
        
        if (command == null) {
            sendLine(origin, isSilent, FORMAT_ERROR, "Command '" + name + "' not found.");
        } else {
            sendLine(origin, isSilent, FORMAT_OUTPUT, Styliser.CODE_FIXED
                    + "---------------------- Command information -------");            
            sendLine(origin, isSilent, FORMAT_OUTPUT, Styliser.CODE_FIXED
                    + " Name: " + name);
            sendLine(origin, isSilent, FORMAT_OUTPUT, Styliser.CODE_FIXED
                    + " Type: " + command.getKey().getType());
            sendLine(origin, isSilent, FORMAT_OUTPUT, Styliser.CODE_FIXED
                    + "Usage: " + command.getKey().getHelp());
            sendLine(origin, isSilent, FORMAT_OUTPUT, Styliser.CODE_FIXED
                    + "--------------------------------------------------");            
        }
    }
    
    /** {@inheritDoc}. */
    @Override
    public String getName() {
        return "help";
    }
    
    /** {@inheritDoc}. */
    @Override
    public boolean showInHelp() {
        return true;
    }
    
    /** {@inheritDoc}. */
    @Override
    public String getHelp() {
        return "help [command] - shows client command help";
    }

    /** {@inheritDoc} */
    @Override
    public AdditionalTabTargets getSuggestions(final int arg, final List<String> previousArgs) {
        final AdditionalTabTargets res = new AdditionalTabTargets().excludeAll();

        if (arg == 0) {
            res.include(TabCompletionType.COMMAND);
        }
        
        return res;
    } 
    
}
