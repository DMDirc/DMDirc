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

package uk.org.ownage.dmdirc.commandparser.commands;

import uk.org.ownage.dmdirc.Server;
import uk.org.ownage.dmdirc.commandparser.CommandWindow;
import uk.org.ownage.dmdirc.commandparser.ServerCommand;

/**
 * A sample command. Takes no arguments, and merely prints the SVN ID string of 
 * the IRC parser.
 * @author chris
 */
public class Test extends ServerCommand {
    
    /** Creates a new instance of Test. */
    public Test() {
        description = "a test command";
        arguments = "";
        polyadic = false;
        arity = 0;            
        name = "test";        
        show = true;
    }

    /**
     * Executes this command.
     * @param origin The window in which the command was typed
     * @param server The server instance that this command is being executed on
     * @param args Arguments passed to this command
     */
    public void execute(final CommandWindow origin, final Server server, 
            final String... args) {
        origin.addLine(server.getParser().getSvnInfo());
    }
    
}
