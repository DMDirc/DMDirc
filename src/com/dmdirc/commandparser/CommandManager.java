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

package com.dmdirc.commandparser;

import com.dmdirc.Config;
import com.dmdirc.Server;
import com.dmdirc.ServerManager;
import com.dmdirc.commandparser.commands.channel.*;
import com.dmdirc.commandparser.commands.chat.*;
import com.dmdirc.commandparser.commands.global.*;
//import com.dmdirc.commandparser.commands.query.*;
import com.dmdirc.commandparser.commands.server.*;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.input.TabCompleter;

import java.util.ArrayList;
import java.util.List;

/**
 * The command manager creates and manages a single instance of all commands,
 * and provides methods to load each group of commands into a parser instance.
 * @author chris
 */
public final class CommandManager {
    
    /**
     * The global commands that have been instansiated.
     */
    private static List<Command> globalCommands = new ArrayList<Command>();
    /**
     * The server commands that have been instansiated.
     */
    private static List<Command> serverCommands = new ArrayList<Command>();
    /**
     * The channel commands that have been instansiated.
     */
    private static List<Command> channelCommands = new ArrayList<Command>();
    /**
     * The query commands that have been instansiated.
     */
    private static List<Command> queryCommands = new ArrayList<Command>();
    /**
     * The "chat" commands that have been instansiated.
     */
    private static List<Command> chatCommands = new ArrayList<Command>();
    
    /**
     * The parsers that have requested global commands.
     */
    private static List<CommandParser> globalParsers = new ArrayList<CommandParser>();
    /**
     * The parsers that have requested server commands.
     */
    private static List<CommandParser> serverParsers = new ArrayList<CommandParser>();
    /**
     * The parsers that have requested channel commands.
     */
    private static List<CommandParser> channelParsers = new ArrayList<CommandParser>();
    /**
     * The parsers that have requested query commands.
     */
    private static List<CommandParser> queryParsers = new ArrayList<CommandParser>();
    
    /**
     * Channel commands that have been registered to appear in the nicklist
     * popup.
     */
    private static List<Command> channelPopupCommands = new ArrayList<Command>();
    
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
        registerCommand(command, true);
    }
    
    /**
     * Unregisters a command with the command manager.
     * @param command The command to be unregistered
     */
    public static void unregisterCommand(final Command command) {
        registerCommand(command, false);
    }
    
    /**
     * Registers or unregisters a command.
     *
     * @param command The command to be (un)registered
     * @param register True if the command should be registered, false if it
     * should be unregistered.
     */
    private static void registerCommand(final Command command, final boolean register) {
        boolean canContinue = true;
        
        if (command instanceof ChannelCommand) {
            registerCommand(command, channelParsers, register);
            channelCommands.add(command);
        } else if (command instanceof ServerCommand) {
            registerCommand(command, serverParsers, register);
            serverCommands.add(command);
        } else if (command instanceof QueryCommand) {
            registerCommand(command, queryParsers, register);
            queryCommands.add(command);
        } else if (command instanceof GlobalCommand) {
            registerCommand(command, globalParsers, register);
            globalCommands.add(command);
        } else if (command instanceof ChatCommand) {
            registerCommand(command, queryParsers, register);
            registerCommand(command, channelParsers, register);
            chatCommands.add(command);
        } else {
            canContinue = false;
            
            Logger.userError(ErrorLevel.HIGH, "Attempted to (un)register an invalid command: "
                    + command.getClass().getName());
        }
        
        if (canContinue) {
            registerCommandName(command, register);
        }
    }
    
    /**
     * Registers the specified command with all of the specified parsers.
     *
     * @param command The command to be reigstered
     * @param parsers The parsers to register the command with
     */
    private static void registerCommand(final Command command,
            final List<CommandParser> parsers, final boolean register) {
        // FIXME: There's no way to kill old/dead entries in the *Parsers lists.
        //        Ideally, they'd unregister themselves (or so) when unloaded.
        for (CommandParser parser : parsers) {
            if (register) {
                parser.registerCommand(command);
            } else {
                parser.unregisterCommand(command);
            }
        }
    }
    
    /**
     * Registers or unregisters the specified command's name with the relevant
     * tab completers.
     *
     * @param command The command to be registered
     * @param register True if the command should be registered, false if it
     * should be unregistered.
     */
    private static void registerCommandName(final Command command,
            final boolean register) {
        // Do tab completion
        final String commandName = Config.getCommandChar() + command.getName();
        
        for (Server server : ServerManager.getServerManager().getServers()) {
            if (command instanceof ServerCommand || command instanceof GlobalCommand) {
                registerCommandName(server.getTabCompleter(), commandName, register);
            }
            
            if (command instanceof ChannelCommand || command instanceof ChatCommand) {
                for (String channelName : server.getChannels()) {
                    registerCommandName(server.getChannel(channelName).getTabCompleter(), commandName, register);
                }
            } 
            
            if (command instanceof QueryCommand || command instanceof ChatCommand) {
                for (String queryName : server.getQueries()) {
                    registerCommandName(server.getQuery(queryName).getTabCompleter(), commandName, register);
                }
            }
        }
    }
    
    /**
     * Registers or unregisters the specified command with the specified tab-
     * completer.
     *
     * @param completer The tab completer to be used
     * @param name The command name to be registered
     * @param register True if the command should be registered, false if it
     * should be unregistered.
     */
    private static void registerCommandName(final TabCompleter completer,
            final String name, final boolean register) {
        if (register) {
            completer.addEntry(name);
        }  else {
            completer.removeEntry(name);
        }
    }
    
    /**
     * Registers a command for use in the nicklist popup.
     * @param command The command to be registered
     */
    public static void registerPopupCommand(final Command command) {
        channelPopupCommands.add(command);
    }
    
    /**
     * Retrieves the commands for use in the nicklist popup.
     * @return A list of commands suitable for use in the nicklist popup
     */
    public static List<Command> getNicklistCommands() {
        return channelPopupCommands;
    }
    
    /**
     * Instansiates the default commands.
     */
    public static void initCommands() {
        // Chat commands
        new Me();
        new MeEmpty();
        
        // Channel commands
        new Ban();
        new Benchmark();
        new ChannelSettings();
        new Cycle();
        new Kick();
        new KickEmpty();
        new KickReason();
        new Mode();
        new Part();
        new PartDefault();
        new SetNickColour();
        new SetTopic();
        new ShowTopic();
        
        // Server commands
        new AllChannels();
        new Away();
        new Back();
        new ChangeServer();
        new Clear();
        new ConfigInfo();
        new Ctcp();
        new Disconnect();
        new Echo();
        new Help();
        new Ignore();
        new Message();
        new Nick();
        new Notice();
        new Query();
        new Raw();
        new Reconnect();
        
        new RawServerCommand("invite");
        new RawServerCommand("join");
        new RawServerCommand("lusers");
        new RawServerCommand("motd");
        new RawServerCommand("whois");
        new RawServerCommand("who");
        
        // Query commands
        
        // Global commands
        new Active();
        new AllServers();
        new Exit();
        new ExitDefault();
        new Ifplugin();
        new NewServer();
        new Notify();
        new LoadFormatter();
        new LoadPlugin();
        new ReloadActions();
        new ReloadFormatter();
        new ReloadPlugin();
        new SaveFormatter();
        new Set();
    }
    
    /**
     * Loads all channel commands into the specified parser.
     * @param parser The parser to load commands into
     */
    public static void loadChannelCommands(final CommandParser parser) {
        for (Command com : channelCommands) {
            parser.registerCommand(com);
        }
        
        for (Command com : chatCommands) {
            parser.registerCommand(com);
        }
        
        channelParsers.add(parser);
    }
    
    /**
     * Loads all server commands into the specified parser.
     * @param parser The parser to load commands into
     */
    public static void loadServerCommands(final CommandParser parser) {
        for (Command command : serverCommands) {
            parser.registerCommand(command);
        }
        
        serverParsers.add(parser);
    }
    
    /**
     * Loads all global commands into the specified parser.
     * @param parser The parser to load commands into
     */
    public static void loadGlobalCommands(final CommandParser parser) {
        for (Command com : globalCommands) {
            parser.registerCommand(com);
        }
        
        globalParsers.add(parser);
    }
    
    /**
     * Loads all query commands into the specified parser.
     * @param parser The parser to load commands into
     */
    protected static void loadQueryCommands(final QueryCommandParser parser) {
        for (Command com : queryCommands) {
            parser.registerCommand(com);
        }
        
        for (Command com : chatCommands) {
            parser.registerCommand(com);
        }
        
        queryParsers.add(parser);
    }
    
    /**
     * Retrieves the command identified by the specified name, regardless of
     * type.
     *
     * @param name The name to look for
     * @return A command with a matching signature, or null if none were found
     */
    public static Command getCommand(final String name) {
        if (getGlobalCommandByName(name) != null) {
            return (Command) getGlobalCommandByName(name);
        } else if (getServerCommandByName(name) != null) {
            return (Command) getServerCommandByName(name);
        } else if (getChannelCommandByName(name) != null) {
            return (Command) getChannelCommandByName(name);
        } else if (getChatCommandByName(name) != null) {
            return (Command) getChatCommandByName(name);
        } else {
            return (Command) getChannelCommandByName(name);
        }
    }
    
    /**
     * Retrieves the server command identified by the specified signature.
     * @param signature The signature to look for
     * @return A server command with a matching signature, or null if none
     * were found.
     */
    public static ServerCommand getServerCommand(final String signature) {
        for (Command com : serverCommands) {
            if (com.getSignature().equalsIgnoreCase(signature)) {
                return (ServerCommand) com;
            }
        }
        
        return null;
    }
    
    /**
     * Retrieves the server command identified by the specified name.
     * @param name The name to look for
     * @return A server command with a matching name, or null if none were found
     */
    public static ServerCommand getServerCommandByName(final String name) {
        return (ServerCommand) getCommandByName(name, serverCommands);
    }
    
    /**
     * Retrieves the global command identified by the specified signature.
     * @param signature The signature to look for
     * @return A global command with a matching signature, or null if none
     * were found.
     */
    public static GlobalCommand getGlobalCommand(final String signature) {
        for (Command com : globalCommands) {
            if (com.getSignature().equalsIgnoreCase(signature)) {
                return (GlobalCommand) com;
            }
        }
        
        return null;
    }
    
    /**
     * Retrieves the global command identified by the specified name.
     * @param name The name to look for
     * @return A global command with a matching name, or null if none were found
     */
    public static GlobalCommand getGlobalCommandByName(final String name) {
        return (GlobalCommand) getCommandByName(name, globalCommands);
    }
    
    /**
     * Retrieves the channel command identified by the specified signature.
     * @param signature The signature to look for
     * @return A channel command with a matching signature, or null if none
     * were found.
     */
    public static ChannelCommand getChannelCommand(final String signature) {
        for (Command com : channelCommands) {
            if (com.getSignature().equalsIgnoreCase(signature)) {
                return (ChannelCommand) com;
            }
        }
        
        return null;
    }
    
    /**
     * Retrieves the channel command identified by the specified name.
     * @param name The name to look for
     * @return A channel command with a matching name, or null if none were found
     */
    public static ChannelCommand getChannelCommandByName(final String name) {
        return (ChannelCommand) getCommandByName(name, channelCommands);
    }
    
    /**
     * Retrieves the chat command identified by the specified signature.
     * @param signature The signature to look for
     * @return A chat command with a matching signature, or null if none
     * were found.
     */
    public static ChatCommand getChatCommand(final String signature) {
        for (Command com : chatCommands) {
            if (com.getSignature().equalsIgnoreCase(signature)) {
                return (ChatCommand) com;
            }
        }
        
        return null;
    }
    
    /**
     * Retrieves the chat command identified by the specified name.
     * @param name The name to look for
     * @return A chat command with a matching name, or null if none were found
     */
    public static ChatCommand getChatCommandByName(final String name) {
        return (ChatCommand) getCommandByName(name, chatCommands);
    }
    
    /**
     * Retrieves the command identified by the specified name.
     * @param name The name to look for
     * @param list The list to look in
     * @return A command with a matching name, or null if none were found
     */
    private static Command getCommandByName(final String name,
            final List<Command> list) {
        for (Command com : list) {
            if (com.getName().equalsIgnoreCase(name)) {
                return com;
            }
        }
        
        return null;
    }
    
    /**
     * Returns a list containing the global commands that have been initialised
     * by this command manager.
     * @return An ArrayList of global commands, or null if none have been loaded
     */
    public static List<Command> getGlobalCommands() {
        return globalCommands;
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
     * Returns a list containing the chat commands that have been initialised
     * by this command manager.
     * @return An ArrayList of chat commands, or null if none have been loaded
     */
    public static List<Command> getChatCommands() {
        return chatCommands;
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
        for (Command chanCommand : channelCommands) {
            if (chanCommand.getName().equalsIgnoreCase(command)) {
                return true;
            }
        }
        
        for (Command chanCommand : chatCommands) {
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
        return getCommandNames(serverCommands);
    }
    
    /**
     * Returns the names (including command char) of all registered global
     * commands.
     * @return An ArrayList&lt;String&gt; containing all registered global command
     * names
     */
    public static List<String> getGlobalCommandNames() {
        return getCommandNames(globalCommands);
    }
    
    /**
     * Returns the names (including command char) of all registered channel
     * commands.
     * @return A list containing all registered channel command names
     */
    public static List<String> getChannelCommandNames() {
        return getCommandNames(channelCommands);
    }
    
    /**
     * Returns the names (including command char) of all registered query
     * commands.
     * @return A list containing all registered query command names
     */
    public static List<String> getQueryCommandNames() {
        return getCommandNames(queryCommands);
    }
    
    /**
     * Returns the names (including command char) of all registered chat
     * commands.
     * @return An ArrayList&lt;String&gt; containing all registered chat command
     * names
     */
    public static List<String> getChatCommandNames() {
        return getCommandNames(chatCommands);
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
            res.add(Config.getCommandChar() + command.getName());
        }
        
        return res;
    }
    
}
