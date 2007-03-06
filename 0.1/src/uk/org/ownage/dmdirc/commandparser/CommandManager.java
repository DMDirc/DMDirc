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

import java.util.Vector;
import uk.org.ownage.dmdirc.commandparser.commands.*;
import uk.org.ownage.dmdirc.commandparser.commands.server.*;
import uk.org.ownage.dmdirc.commandparser.commands.channel.*;

/**
 * The command manager creates and manages a single instance of all commands,
 * and provides methods to load each group of commands into a parser instance
 * @author chris
 */
public class CommandManager {
    
    /**
     * The server commands that have been instansiated
     */
    private static Vector<Command> serverCommands;
    /**
     * The channel commands that have been instansiated
     */
    private static Vector<Command> channelCommands;
    
    /**
     * Loads all channel commands into the specified parser
     * @param parser The parser to load commands into
     */
    public static void loadChannelCommands(CommandParser parser) {
        if (channelCommands == null) {
            channelCommands = new Vector<Command>(0,1);
            
            channelCommands.add(new Cycle());
            channelCommands.add(new Me());
            channelCommands.add(new MeEmpty());
            channelCommands.add(new Part());
            channelCommands.add(new PartDefault());
        }
        
        for (Command com : channelCommands) {
            parser.registerCommand(com);
        }
    }
    
    /**
     * Loads all server commands into the specified parser
     * @param parser The parser to load commands into
     */
    public static void loadServerCommands(CommandParser parser) {
        if (serverCommands == null) {
            serverCommands = new Vector<Command>(0,1);
            
            serverCommands.add(new Help());
            serverCommands.add(new Join());
            serverCommands.add(new Test());
            serverCommands.add(new Quit());
            serverCommands.add(new QuitDefault());
        }
        
        for (Command com : serverCommands) {
            parser.registerCommand(com);
        }
    }
    
    /**
     * Retrieves the server command identified by the specified signature
     * @param signature The signature to look for
     * @return A server command with a matching signature, or null if none
     * were found.
     */
    public static ServerCommand getServerCommad(String signature) {
        if (serverCommands == null) {
            return null;
        }
        
        for (Command com : serverCommands) {
            if (com.getSignature().equals(signature)) {
                return (ServerCommand) com;
            }
        }
        
        return null;
    }
    
    /**
     * Retrieves the channel command identified by the specified signature
     * @param signature The signature to look for
     * @return A channel command with a matching signature, or null if none
     * were found.
     */
    public static ChannelCommand getChannelCommand(String signature) {
        if (channelCommands == null) {
            return null;
        }
        
        for (Command com : channelCommands) {
            if (com.getSignature().equals(signature)) {
                return (ChannelCommand) com;
            }
        }
        
        return null;
    }
    
    public static Vector<Command> getServerCommands() {
        if (serverCommands == null) {
            return null;
        }
        
        return serverCommands;
    }
    
    public static Vector<Command> getChannelCommands() {
        if (channelCommands == null) {
            return null;
        }
        
        return channelCommands;
    }
    
}
