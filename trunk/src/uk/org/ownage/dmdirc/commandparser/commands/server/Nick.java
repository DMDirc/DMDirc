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
import uk.org.ownage.dmdirc.commandparser.CommandWindow;
import uk.org.ownage.dmdirc.commandparser.ServerCommand;

/**
 * Allows the user to change nickname
 * @author chris
 */
public class Nick extends ServerCommand {
    
    /**
     * Creates a new instance of Join
     */
    public Nick () {
        description = "Changes your nickname";
        arguments = "<new nickname>";
        polyadic = false;
        arity = 1;
        name = "nick";
        show = true;
    }

    /**
     * Executes this command
     * @param origin The frame in which this command was issued
     * @param server The server object that this command is associated with
     * @param args The user supplied arguments
     */    
    public void execute(CommandWindow origin, Server server, String... args) {
        server.getParser().setNickname(args[0]);
    }
    
}