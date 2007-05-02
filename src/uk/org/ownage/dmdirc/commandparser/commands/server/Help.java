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

import java.util.Collections;
import java.util.List;

import uk.org.ownage.dmdirc.Server;
import uk.org.ownage.dmdirc.commandparser.Command;
import uk.org.ownage.dmdirc.commandparser.CommandManager;
import uk.org.ownage.dmdirc.commandparser.CommandWindow;
import uk.org.ownage.dmdirc.commandparser.ServerCommand;
import uk.org.ownage.dmdirc.ui.ChannelFrame;
import uk.org.ownage.dmdirc.ui.QueryFrame;

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
     * @param args The user supplied arguments
     */
    public void execute(final CommandWindow origin, final Server server,
            final String... args) {
        origin.addLine("-- Server commands ----------------------------------");
        showCommands(CommandManager.getServerCommands(), origin);
        if (origin instanceof ChannelFrame) {
            origin.addLine("-- Channel commands ---------------------------------");
            showCommands(CommandManager.getChannelCommands(), origin);
        }
        if (origin instanceof QueryFrame) {
            origin.addLine("-- Query commands -----------------------------------");
            showCommands(CommandManager.getQueryCommands(), origin);
            
        }
        origin.addLine("-----------------------------------------------------");
    }
    
    /**
     * Shows the user the commands from the specified list.
     * @param commands The commands to be displayed
     * @param origin The window to output to
     */
    private void showCommands(final List<Command> commands,
            final CommandWindow origin) {
        Collections.sort(commands);
        for (Command com : commands) {
            if (com.showInHelp()) {
                origin.addLine(com.getHelp());
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
