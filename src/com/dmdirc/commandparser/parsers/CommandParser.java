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

package com.dmdirc.commandparser.parsers;

import com.dmdirc.Server;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.PreviousCommand;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.interfaces.InputWindow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Represents a generic command parser. A command parser takes a line of input
 * from the user, determines if it is an attempt at executing a command (based
 * on the character at the start of the string), and handles it appropriately.
 * @author chris
 */
public abstract class CommandParser implements Serializable {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /**
     * Commands that are associated with this parser.
     */
    private final Map<String, Command> commands;
    
    /**
     * A history of commands that have been entered into this parser.
     */
    private final List<PreviousCommand> history = new ArrayList<PreviousCommand>();
    
    /** Creates a new instance of CommandParser. */
    public CommandParser() {
        commands = new Hashtable<String, Command>();
        loadCommands();
    }
    
    /** Loads the relevant commands into the parser. */
    protected abstract void loadCommands();
    
    /**
     * Registers the specified command with this parser.
     * @param command Command to be registered
     */
    public final void registerCommand(final Command command) {
        commands.put(command.getSignature().toLowerCase(), command);
    }
    
    /**
     * Unregisters the specified command with this parser.
     * @param command Command to be unregistered
     */
    public final void unregisterCommand(final Command command) {
        commands.remove(command.getSignature().toLowerCase());
    }
    
    /**
     * Parses the specified string as a command.
     * @param origin The window in which the command was typed
     * @param line The line to be parsed
     * @param parseChannel Whether or not to try and parse the first argument
     * as a channel name
     */
    public final void parseCommand(final InputWindow origin,
            final String line, final boolean parseChannel) {
        if (line.isEmpty()) {
            return;
        }
        
        if (line.charAt(0) == CommandManager.getCommandChar()) {
            int offset = 1;
            boolean silent = false;
            
            if (line.length() > offset && line.charAt(offset) == CommandManager.getSilenceChar()) {
                silent = true;
                offset++;
            }
            
            final String[] args = line.split(" ");
            final String command = args[0].substring(offset);
            String[] comargs;
            
            if (args.length >= 2 && parseChannel && origin != null
                    && origin.getContainer() != null
                    && origin.getContainer().getServer() != null
                    && origin.getContainer().getServer().getParser() != null
                    && origin.getContainer().getServer().getParser().isValidChannelName(args[1])
                    && CommandManager.isChannelCommand(command)) {
                final Server server = origin.getContainer().getServer();
                
                if (server.hasChannel(args[1])) {
                    
                    final StringBuilder newLine = new StringBuilder();
                    for (int i = 0; i < args.length; i++) {
                        if (i == 1) { continue; }
                        newLine.append(" ").append(args[i]);
                    }
                    
                    server.getChannel(args[1]).getFrame().getCommandParser()
                            .parseCommand(origin, newLine.substring(1), false);
                    
                    return;
                } else {
                    // Do something haxy involving external commands here...
                }
            }
            
            comargs = new String[args.length - 1];
            
            System.arraycopy(args, 1, comargs, 0, args.length - 1);
            
            final String signature = command + "/" + (comargs.length);
            
            // Check the specific signature first, so that polyadic commands can
            // have error handlers if there are too few arguments (e.g., msg/0 and
            // msg/1 would return errors, so msg only gets called with 2+ args).
            if (commands.containsKey(signature.toLowerCase())) {
                addHistory(command, comargs);
                executeCommand(origin, silent, commands.get(signature.toLowerCase()), comargs);
            } else if (commands.containsKey(command.toLowerCase())) {
                addHistory(command, comargs);
                executeCommand(origin, silent, commands.get(command.toLowerCase()), comargs);
            } else {
                handleInvalidCommand(origin, command, comargs);
            }
        } else {
            handleNonCommand(origin, line);
        }
    }
    
    /**
     * Adds a command to this parser's history.
     *
     * @param command The command name that was used
     * @param args The arguments for the command, if any
     */
    private void addHistory(final String command, final String... args) {
        final StringBuilder builder = new StringBuilder(command);
        
        for (String arg : args) {
            builder.append(' ');
            builder.append(arg);
        }
        
        history.add(new PreviousCommand(builder.toString()));
        
        while (history.size() >
                IdentityManager.getGlobalConfig().getOptionInt("general", "commandhistory", 10)) {
            history.remove(0);
        }
    }
    
    /**
     * Retrieves the most recent time that the specified command was used.
     * Commands should not include command or silence chars.
     *
     * @param command The command to search for
     * @return The timestamp that the command was used, or 0 if it wasn't
     */
    public long getCommandTime(final String command) {
        long res = 0;
        
        for (PreviousCommand pc : history) {
            if (pc.getLine().matches("(?i)" + command)) {
                res = Math.max(res, pc.getTime());
            }
        }
        
        return res;
    }
    
    /**
     * Parses the specified string as a command.
     * @param origin The window in which the command was typed
     * @param line The line to be parsed
     */
    public final void parseCommand(final InputWindow origin,
            final String line) {
        parseCommand(origin, line, true);
    }
    
    /**
     * Handles the specified string as a non-command.
     * @param origin The window in which the command was typed
     * @param line The line to be parsed
     */
    public final void parseCommandCtrl(final InputWindow origin, final String line) {
        handleNonCommand(origin, line);
    }
    
    /**
     * Executes the specified command with the given arguments.
     * @param origin The window in which the command was typed
     * @param isSilent Whether the command is being silenced or not
     * @param command The command to be executed
     * @param args The arguments to the command
     */
    protected abstract void executeCommand(final InputWindow origin,
            final boolean isSilent, final Command command, final String... args);
    
    /**
     * Called when the user attempted to issue a command (i.e., used the command
     * character) that wasn't found. It could be that the command has a different
     * arity, or that it plain doesn't exist.
     * @param origin The window in which the command was typed
     * @param command The command the user tried to execute
     * @param args The arguments passed to the command
     */
    protected void handleInvalidCommand(final InputWindow origin,
            final String command, final String... args) {
        if (origin == null) {
            ActionManager.processEvent(CoreActionType.UNKNOWN_COMMAND, null,
                    null, command, args);
        } else {
            final StringBuffer buff = new StringBuffer("unknownCommand");
            
            ActionManager.processEvent(CoreActionType.UNKNOWN_COMMAND, buff,
                    origin.getContainer(), command, args);
            
            origin.addLine(buff, command + "/" + args.length);
        }
    }
    
    /**
     * Called when the input was a line of text that was not a command. This normally
     * means it is sent to the server/channel/user as-is, with no further processing.
     * @param origin The window in which the command was typed
     * @param line The line input by the user
     */
    protected abstract void handleNonCommand(final InputWindow origin,
            final String line);
}
