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

package dmdirc.commandparser;

import java.util.Hashtable;

/**
 *
 * @author chris
 */
abstract public class CommandParser {
    
    private Hashtable<String,Command> commands;
    
    /** Creates a new instance of CommandParser */
    public CommandParser() {
        commands = new Hashtable<String,Command>();
    }
    
    /** Loads the relevant commands into the parser */
    protected abstract void LoadCommands();
    
    public void registerCommand(Command command) {
        commands.put(command.getSignature(), command);
    }
    
    /** Parses the specified string as a command */
    public void parseCommand(String line) {
        String[] args = line.split(" ");
        
        assert(args.length > 0);
        
        String command = args[0];
        String signature = command+"/"+(args.length-1);
        
        // Check the specific signature first, so that polyadic commands can
        // have error handlers if there are too few arguments (e.g., msg/0 and
        // msg/1 would return errors, so msg only gets called with 2+ args).
        if (commands.containsKey(signature)) {
            
        } else if (commands.containsKey(command)) {
            
        } else {
            
        }
    }
    
}
