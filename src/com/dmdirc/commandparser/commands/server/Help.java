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
import com.dmdirc.commandparser.Command;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.ServerCommand;
import com.dmdirc.ui.ChannelFrame;
import com.dmdirc.ui.interfaces.InputWindow;
import com.dmdirc.ui.QueryFrame;

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
        sendLine(origin, isSilent, "commandOutput", "-- Server commands ----------------------------------");
        showCommands(CommandManager.getServerCommands(), origin, isSilent);
        if (origin instanceof ChannelFrame) {
            sendLine(origin, isSilent, "commandOutput", "-- Channel commands ---------------------------------");
            showCommands(CommandManager.getChannelCommands(), origin, isSilent);
        }
        if (origin instanceof QueryFrame) {
            sendLine(origin, isSilent, "commandOutput", "-- Query commands -----------------------------------");
            showCommands(CommandManager.getQueryCommands(), origin, isSilent);
            
        }
        sendLine(origin, isSilent, "commandOutput", "-----------------------------------------------------");
    }
    
    /**
     * Shows the user the commands from the specified list.
     * @param commands The commands to be displayed
     * @param origin The window to output to
     * @param isSilent Whether this command is silent or not
     */
    private void showCommands(final List<Command> commands,
            final InputWindow origin, final boolean isSilent) {
        Collections.sort(commands);
        for (Command com : commands) {
            if (com.showInHelp()) {
                sendLine(origin, isSilent, "commandOutput", com.getHelp());
            }
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
    public boolean isPolyadic() {
        return false;
    }
    
    /** {@inheritDoc}. */
    public int getArity() {
        return 0;
    }
    
    /** {@inheritDoc}. */
    public String getHelp() {
        return "help - shows all available client commands";
    }
    
}
