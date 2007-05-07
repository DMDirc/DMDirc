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
import java.util.List;

import uk.org.ownage.dmdirc.Config;
import uk.org.ownage.dmdirc.Server;
import uk.org.ownage.dmdirc.ServerManager;
import uk.org.ownage.dmdirc.commandparser.commands.channel.*;
import uk.org.ownage.dmdirc.commandparser.commands.query.*;
import uk.org.ownage.dmdirc.commandparser.commands.server.*;
import uk.org.ownage.dmdirc.logger.ErrorLevel;
import uk.org.ownage.dmdirc.logger.Logger;

/**
 * The command manager creates and manages a single instance of all commands,
 * and provides methods to load each group of commands into a parser instance.
 * @author chris
 */
public final class CommandManager {
    
    /**
     * The server commands that have been instansiated.
     */
    private static List<Command> serverCommands;
    /**
     * The channel commands that have been instansiated.
     */
    private static List<Command> channelCommands;
    /**
     * The query commands that have been instansiated.
     */
    private static List<Command> queryCommands;
    /**
     * The parsers that have requested server commands.
     */
    private static List<CommandParser> serverParsers;
    /**
     * The parsers that have requested channel commands.
     */
    private static List<CommandParser> channelParsers;
    /**
     * The parsers that have requested query commands.
     */
    private static List<CommandParser> queryParsers;
    
    /**
     * Channel commands that have been registered to appear in the nicklist
     * popup.
     */
    private static List<Command> channelPopupCommands;
    
    /**
     * Prevents creation of a new command manager.
     */
    private CommandManager() {
        //do nothing
    }
    
    /**
     * Registers a command with the command manager.
     * @param command The command to be registered
     */
    public static void registerCommand(final Command command) {
        if (channelCommands == null) {
            initLists();
        }
        
        List<CommandParser> target = null;
        
        if (command instanceof ChannelCommand) {
            target = channelParsers;
            channelCommands.add(command);
        } else if (command instanceof ServerCommand) {
            target = serverParsers;
            serverCommands.add(command);
        } else if (command instanceof QueryCommand) {
            target = queryParsers;
            queryCommands.add(command);
        } else {
            Logger.error(ErrorLevel.ERROR, "Attempted to register an invalid command: "
                    + command.getClass().getName());
        }
        
        // FIXME: There's no way to kill old/dead entries in the *Parsers lists.
        //        Ideally, they'd unregister themselves (or so) when unloaded.
        if (target != null) {
            for (CommandParser parser : target) {
                if (parser != null) {
                    parser.registerCommand(command);
                }
            }
            
            final String commandName = Config.getOption("general", "commandchar") + command.getName();
            
            for (Server server : ServerManager.getServerManager().getServers()) {
                if (command instanceof ServerCommand) {
                    server.getTabCompleter().addEntry(commandName);
                } else if (command instanceof ChannelCommand) {
                    for (String channelName : server.getChannels()) {
                        server.getChannel(channelName).getTabCompleter().addEntry(commandName);
                    }
                } else if (command instanceof QueryCommand) {
                    for (String queryName : server.getQueries()) {
                        server.getQuery(queryName).getTabCompleter().addEntry(commandName);
                    }
                }
            }
        }
    }
    
    /**
     * Unregisters a command with the command manager.
     * @param command The command to be unregistered
     */
    public static void unregisterCommand(final Command command) {
        if (channelCommands == null) {
            return;
        }
        
        List<CommandParser> target = null;
        
        if (command instanceof ChannelCommand) {
            target = channelParsers;
            channelCommands.remove(command);
        } else if (command instanceof ServerCommand) {
            target = serverParsers;
            serverCommands.remove(command);
        } else if (command instanceof QueryCommand) {
            target = queryParsers;
            queryCommands.remove(command);
        } else {
            Logger.error(ErrorLevel.ERROR, "Attempted to unregister an invalid command: "
                    + command.getClass().getName());
        }
        
        // FIXME: There's no way to kill old/dead entries in the *Parsers lists.
        //        Ideally, they'd unregister themselves (or so) when unloaded.
        if (target != null) {
            for (CommandParser parser : target) {
                if (parser != null) {
                    parser.unregisterCommand(command);
                }
            }
            
            final String commandName = Config.getOption("general", "commandchar") + command.getName();
            
            for (Server server : ServerManager.getServerManager().getServers()) {
                if (command instanceof ServerCommand) {
                    server.getTabCompleter().removeEntry(commandName);
                } else if (command instanceof ChannelCommand) {
                    for (String channelName : server.getChannels()) {
                        server.getChannel(channelName).getTabCompleter().removeEntry(commandName);
                    }
                } else if (command instanceof QueryCommand) {
                    for (String queryName : server.getQueries()) {
                        server.getQuery(queryName).getTabCompleter().removeEntry(commandName);
                    }
                }
            }
        }
    }
    
    /**
     * Registers a command for use in the nicklist popup.
     * @param command The command to be registered
     */
    public static void registerPopupCommand(final Command command) {
        if (channelPopupCommands == null) {
            initLists();
        }
        
        channelPopupCommands.add(command);
    }
    
    /**
     * Retrieves the commands for use in the nicklist popup.
     * @return A list of commands suitable for use in the nicklist popup
     */
    public static List<Command> getNicklistCommands() {
        if (channelPopupCommands == null) {
            initLists();
        }
        
        return channelPopupCommands;
    }
    
    /**
     * Initialises the command manager's various command lists.
     */
    private static void initLists() {
        channelCommands = new ArrayList<Command>();
        serverCommands = new ArrayList<Command>();
        queryCommands = new ArrayList<Command>();
        
        channelParsers = new ArrayList<CommandParser>();
        serverParsers = new ArrayList<CommandParser>();
        queryParsers = new ArrayList<CommandParser>();
        
        channelPopupCommands = new ArrayList<Command>();
        
        initCommands();
    }
    
    /**
     * Instansiates the default commands.
     */
    private static void initCommands() {
        // Channel commands
        new Ban();
        new Benchmark();
        new ChannelSettings();
        new Cycle();
        new Kick();
        new KickEmpty();
        new KickReason();
        new Me();
        new MeEmpty();
        new Mode();
        new Notify();
        new Part();
        new PartDefault();
        new SetNickColour();
        new SetTopic();
        new ShowTopic();
        
        // Server commands
        new AllChannels();
        new AllServers();
        new Away();
        new Back();
        new Clear();
        new ConfigInfo();
        new Ctcp();
        new Disconnect();
        new Echo();
        new Help();
        new Ignore();
        new Join();
        new LoadFormatter();
        new LoadPlugin();
        new Message();
        new Motd();
        new Nick();
        new Notice();
        new Query();
        new Quit();
        new QuitDefault();
        new Raw();
        new Reconnect();
        new ReloadActions();
        new ReloadFormatter();
        new SaveFormatter();
        new Set();
        new Whois();
        
        // Query commands
        new QueryMe();
        new QueryMeEmpty();
    }
    
    /**
     * Loads all channel commands into the specified parser.
     * @param parser The parser to load commands into
     */
    public static void loadChannelCommands(final CommandParser parser) {
        if (channelCommands == null) {
            initLists();
        }
        
        for (Command com : channelCommands) {
            parser.registerCommand(com);
        }
        
        channelParsers.add(parser);
    }
    
    /**
     * Loads all server commands into the specified parser.
     * @param parser The parser to load commands into
     */
    public static void loadServerCommands(final CommandParser parser) {
        if (serverCommands == null) {
            initLists();
        }
        
        for (Command com : serverCommands) {
            parser.registerCommand(com);
        }
        
        serverParsers.add(parser);
    }
    
    /**
     * Loads all query commands into the specified parser.
     * @param parser The parser to load commands into
     */
    protected static void loadQueryCommands(final QueryCommandParser parser) {
        if (queryCommands == null)    {
            initLists();
        }
        
        for (Command com : queryCommands) {
            parser.registerCommand(com);
        }
        
        queryParsers.add(parser);
    }
    
    /**
     * Retrieves the server command identified by the specified signature.
     * @param signature The signature to look for
     * @return A server command with a matching signature, or null if none
     * were found.
     */
    public static ServerCommand getServerCommand(final String signature) {
        if (serverCommands == null) {
            initLists();
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
            initLists();
        }
        
        for (Command com : channelCommands) {
            if (com.getSignature().equals(signature)) {
                return (ChannelCommand) com;
            }
        }
        
        return null;
    }
    
    /**
     * Returns a list containing the server commands that have been initialised
     * by this command manager.
     * @return An ArrayList of server commands, or null if none have been loaded
     */
    public static List<Command> getServerCommands() {
        return serverCommands;
    }
    
    /**
     * Returns a list containing the channel commands that have been initialised
     * by this command manager.
     * @return An ArrayList of channel commands, or null if none have been loaded
     */
    public static List<Command> getChannelCommands() {
        return channelCommands;
    }
    
    /**
     * Returns a list containing the query commands that have been initialised
     * by this command manager.
     * @return An ArrayList of query commands, or null if none have been loaded
     */
    public static List<Command> getQueryCommands() {
        return queryCommands;
    }
    
    /**
     * Determines if the specified command is a valid channel command.
     * @param command The name of the command to test
     * @return True iff the command is a channel command, false otherwise
     */
    public static boolean isChannelCommand(final String command) {
        if (channelCommands == null) {
            CommandManager.initLists();
        }
        
        for (Command chanCommand : channelCommands) {
            if (chanCommand.getName().equalsIgnoreCase(command)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Returns the names (including command char) of all registered server
     * commands.
     * @return An ArrayList&lt;String&gt; containing all registered server command
     * names
     */
    public static List<String> getServerCommandNames() {
        if (serverCommands == null) {
            CommandManager.initLists();
        }
        
        return getCommandNames(serverCommands);
    }
    
    /**
     * Returns the names (including command char) of all registered channel
     * commands.
     * @return An ArrayList&lt;String&gt; containing all registered server command
     * names
     */
    public static List<String> getChannelCommandNames() {
        if (channelCommands == null) {
            CommandManager.initLists();
        }
        
        return getCommandNames(channelCommands);
    }
    
    /**
     * Returns the names (including command char) of all registered channel
     * commands.
     * @return An ArrayList&lt;String&gt; containing all registered server command
     * names
     */
    public static List<String> getQueryCommandNames() {
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
    private static List<String> getCommandNames(final List<Command> source) {
        if (source == null) {
            return null;
        }
        
        final List<String> res = new ArrayList<String>();
        
        for (Command command : source) {
            res.add(Config.getOption("general", "commandchar") + command.getName());
        }
        
        return res;
    }
    
}
