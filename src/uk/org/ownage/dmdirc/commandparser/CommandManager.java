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

import java.util.ArrayList;
import java.util.Vector;

import uk.org.ownage.dmdirc.Config;
import uk.org.ownage.dmdirc.commandparser.commands.Test;
import uk.org.ownage.dmdirc.commandparser.commands.channel.ChannelSettings;
import uk.org.ownage.dmdirc.commandparser.commands.channel.Cycle;
import uk.org.ownage.dmdirc.commandparser.commands.channel.Me;
import uk.org.ownage.dmdirc.commandparser.commands.channel.MeEmpty;
import uk.org.ownage.dmdirc.commandparser.commands.channel.Part;
import uk.org.ownage.dmdirc.commandparser.commands.channel.PartDefault;
import uk.org.ownage.dmdirc.commandparser.commands.query.QueryMe;
import uk.org.ownage.dmdirc.commandparser.commands.query.QueryMeEmpty;
import uk.org.ownage.dmdirc.commandparser.commands.server.Clear;
import uk.org.ownage.dmdirc.commandparser.commands.server.Help;
import uk.org.ownage.dmdirc.commandparser.commands.server.Join;
import uk.org.ownage.dmdirc.commandparser.commands.server.LoadFormatter;
import uk.org.ownage.dmdirc.commandparser.commands.server.Nick;
import uk.org.ownage.dmdirc.commandparser.commands.server.Quit;
import uk.org.ownage.dmdirc.commandparser.commands.server.QuitDefault;
import uk.org.ownage.dmdirc.commandparser.commands.server.Raw;
import uk.org.ownage.dmdirc.commandparser.commands.server.ReloadFormatter;
import uk.org.ownage.dmdirc.commandparser.commands.server.SaveFormatter;


/**
 * The command manager creates and manages a single instance of all commands,
 * and provides methods to load each group of commands into a parser instance.
 * @author chris
 */
public final class CommandManager {
    
    /**
     * The server commands that have been instansiated.
     */
    private static Vector<Command> serverCommands;
    /**
     * The channel commands that have been instansiated.
     */
    private static Vector<Command> channelCommands;
    /**
     * The query commands that have been instansiated.
     */
    private static Vector<Command> queryCommands;
    
    /**
     * Prevents creation of a new command manager.
     */
    private CommandManager() {
        //do nothing
    }
    
    /**
     * Initialises the command manager's various command lists.
     */
    private static void initLists() {
        channelCommands = new Vector<Command>(0, 1);
        
        channelCommands.add(new Cycle());
        channelCommands.add(new Me());
        channelCommands.add(new MeEmpty());
        channelCommands.add(new Part());
        channelCommands.add(new PartDefault());
        channelCommands.add(new ChannelSettings());
        
        serverCommands = new Vector<Command>(0, 1);
        
        serverCommands.add(new Help());
        serverCommands.add(new Join());
        serverCommands.add(new Nick());
        serverCommands.add(new ReloadFormatter());
        serverCommands.add(new SaveFormatter());
        serverCommands.add(new LoadFormatter());
        serverCommands.add(new Test());
        serverCommands.add(new Quit());
        serverCommands.add(new QuitDefault());
        serverCommands.add(new Raw());
        serverCommands.add(new Clear());
        
        queryCommands = new Vector<Command>(0, 1);
        
        queryCommands.add(new QueryMe());
        queryCommands.add(new QueryMeEmpty());
    }
    
    /**
     * Loads all channel commands into the specified parser.
     * @param parser The parser to load commands into
     */
    public static void loadChannelCommands(final CommandParser parser) {
        if (channelCommands == null) {
            CommandManager.initLists();
        }
        
        for (Command com : channelCommands) {
            parser.registerCommand(com);
        }
    }
    
    /**
     * Loads all server commands into the specified parser.
     * @param parser The parser to load commands into
     */
    public static void loadServerCommands(final CommandParser parser) {
        if (serverCommands == null) {
            CommandManager.initLists();
        }
        
        for (Command com : serverCommands) {
            parser.registerCommand(com);
        }
    }
    
    /**
     * Loads all query commands into the specified parser.
     * @param parser The parser to load commands into
     */
    static void loadQueryCommands(final QueryCommandParser parser) {
        if (queryCommands == null)    {
            CommandManager.initLists();
        }
        
        for (Command com : queryCommands) {
            parser.registerCommand(com);
        }
    }
    
    /**
     * Retrieves the server command identified by the specified signature.
     * @param signature The signature to look for
     * @return A server command with a matching signature, or null if none
     * were found.
     */
    public static ServerCommand getServerCommad(final String signature) {
        if (serverCommands == null) {
            CommandManager.initLists();
        }
        
        for (Command com : serverCommands) {
            if (com.getSignature().equals(signature)) {
                return (ServerCommand) com;
            }
        }
        
        return null;
    }
    
    /**
     * Retrieves the channel command identified by the specified signature.
     * @param signature The signature to look for
     * @return A channel command with a matching signature, or null if none
     * were found.
     */
    public static ChannelCommand getChannelCommand(final String signature) {
        if (channelCommands == null) {
            CommandManager.initLists();
        }
        
        for (Command com : channelCommands) {
            if (com.getSignature().equals(signature)) {
                return (ChannelCommand) com;
            }
        }
        
        return null;
    }
    
    /**
     * Returns a Vector containing the server commands that have been initialised
     * by this command manager.
     * @return A Vector of server commands, or null if none have been loaded
     */
    public static Vector<Command> getServerCommands() {
        return serverCommands;
    }
    
    /**
     * Returns a Vector containing the channel commands that have been initialised
     * by this command manager.
     * @return A Vector of channel commands, or null if none have been loaded
     */
    public static Vector<Command> getChannelCommands() {
        return channelCommands;
    }
    
    /**
     * Returns a Vector containing the query commands that have been initialised
     * by this command manager.
     * @return A Vector of query commands, or null if none have been loaded
     */
    public static Vector<Command> getQueryCommands() {
        return queryCommands;
    }
    
    /**
     * Returns the names (including command char) of all registered server
     * commands.
     * @return An ArrayList<String> containing all registered server command
     * names
     */
    public static ArrayList<String> getServerCommandNames() {
        if (serverCommands == null) {
            CommandManager.initLists();
        }
        
        return getCommandNames(serverCommands);
    }
    
    /**
     * Returns the names (including command char) of all registered channel
     * commands.
     * @return An ArrayList<String> containing all registered server command
     * names
     */
    public static ArrayList<String> getChannelCommandNames() {
        if (channelCommands == null) {
            CommandManager.initLists();
        }
        
        return getCommandNames(channelCommands);
    }
    
    /**
     * Returns the names (including command char) of all registered channel
     * commands.
     * @return An ArrayList<String> containing all registered server command
     * names
     */
    public static ArrayList<String> getQueryCommandNames() {
        if (queryCommands == null) {
            CommandManager.initLists();
        }
        
        return getCommandNames(queryCommands);
    }
    
    /**
     * Iterates through the specified source and returns a list of the names
     * of all commands found in it.
     * @param source The source vector to iterate over
     * @return A list of all commands in the source
     */
    private static ArrayList<String> getCommandNames(final Vector<Command> source) {
        if (source == null) {
            return null;
        }
        
        final ArrayList<String> res = new ArrayList<String>();
        final String commandChar = Config.getOption("general", "commandchar");
        
        for (Command command : source) {
            res.add(commandChar + command.getName());
        }
        
        return res;
    }
    
}
