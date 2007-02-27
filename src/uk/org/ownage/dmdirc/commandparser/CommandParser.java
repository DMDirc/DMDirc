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

package uk.org.ownage.dmdirc.commandparser;

import java.util.Hashtable;
import uk.org.ownage.dmdirc.Config;

/**
 * Represents a generic command parser. A command parser takes a line of input
 * from the user, determines if it is an attempt at executing a command (based
 * on the character at the start of the string), and handles it appropriately.
 * @author chris
 */
abstract public class CommandParser {
    
    /**
     * Commands that are associated with this parser
     */
    private Hashtable<String,Command> commands;
    
    /** Creates a new instance of CommandParser */
    public CommandParser() {
        commands = new Hashtable<String,Command>();
        loadCommands();
    }
    
    /** Loads the relevant commands into the parser */
    protected abstract void loadCommands();
    
    /**
     * Registers the specified command with this parser
     * @param command Command to be registered
     */
    public void registerCommand(Command command) {
        commands.put(command.getSignature(), command);
    }
    
    /**
     * Parses the specified string as a command
     * @param line The line to be parsed
     */
    public void parseCommand(String line) {
        if (line.charAt(0) == Config.getOption("general","commandchar").charAt(0)) {
            String[] args = line.split(" ");
            
            assert(args.length > 0);
            
            String command = args[0].substring(1);
            
            String signature = command+"/"+(args.length-1);
            
            // Check the specific signature first, so that polyadic commands can
            // have error handlers if there are too few arguments (e.g., msg/0 and
            // msg/1 would return errors, so msg only gets called with 2+ args).
            if (commands.containsKey(signature)) {
                executeCommand(commands.get(signature), args);
            } else if (commands.containsKey(command)) {
                executeCommand(commands.get(command), args);
            } else {
                handleInvalidCommand(command, args);
            }
        } else {
            handleNonCommand(line);
        }
    }
    
    /**
     * Executes the specified command with the given arguments.
     * @param command The command to be executed
     * @param args The arguments to the command
     */
    abstract protected void executeCommand(Command command, String... args);
    
    /**
     * Called when the user attempted to issue a command (i.e., used the command
     * character) that wasn't found. It could be that the command has a different
     * arity, or that it plain doesn't exist.
     * @param command The command the user tried to execute
     * @param args The arguments passed to the command
     */
    abstract protected void handleInvalidCommand(String command, String... args);

    /**
     * Called when the input was a line of text that was not a command. This normally
     * means it is sent to the server/channel/user as-is, with no further processing.
     * @param line The line input by the user
     */
    abstract protected void handleNonCommand(String line);
}
