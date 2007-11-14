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

import com.dmdirc.Server;
import com.dmdirc.ServerManager;
import com.dmdirc.commandparser.commands.ChannelCommand;
import com.dmdirc.commandparser.commands.ChatCommand;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.GlobalCommand;
import com.dmdirc.commandparser.commands.QueryCommand;
import com.dmdirc.commandparser.commands.ServerCommand;
import com.dmdirc.commandparser.commands.channel.*;
import com.dmdirc.commandparser.commands.chat.*;
import com.dmdirc.commandparser.commands.global.*;
//import com.dmdirc.commandparser.commands.query.*;
import com.dmdirc.commandparser.commands.server.*;
import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.interfaces.ConfigChangeListener;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.input.TabCompleter;
import com.dmdirc.util.MapList;
import com.dmdirc.util.WeakList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The command manager creates and manages a single instance of all commands,
 * and provides methods to load each group of commands into a parser instance.
 * 
 * @author chris
 */
public final class CommandManager {
    
    /** A list of commands that have been instansiated. */
    private static final Map<CommandInfo, Command> commands
            = new HashMap<CommandInfo, Command>();
    
    /** A list of command parsers that have been instansiated. */
    private static final MapList<CommandType, CommandParser> parsers
            = new MapList<CommandType, CommandParser>();
           
    /**
     * Channel commands that have been registered to appear in the nicklist
     * popup.
     */
    private static List<Command> channelPopupCommands = new WeakList<Command>();
    
    /** The command char we're using. */
    private static char commandChar = IdentityManager.getGlobalConfig()
            .getOption("general", "commandchar", "/").charAt(0);
    
    /** The silence char we're using. */
    private static char silenceChar = IdentityManager.getGlobalConfig()
            .getOption("general", "silencechar", ".").charAt(0);
    
    /**
     * Prevents creation of a new command manager.
     */
    private CommandManager() {
        //do nothing
    }
    
    /**
     * Returns the current command character.
     *
     * @return the current command char
     */
    public static char getCommandChar() {
        return commandChar;
    }
    
    /**
     * Returns the current silence character.
     *
     * @return the current silence char
     */
    public static char getSilenceChar() {
        return silenceChar;
    }
    
    /**
     * Registers a command with the command manager.
     * 
     * @param command The command to be registered
     */
    public static void registerCommand(final Command command) {
        registerCommand(command, true);
    }
    
    /**
     * Unregisters a command with the command manager.
     * 
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
            registerCommand(command, parsers.get(CommandType.TYPE_CHANNEL), register);
        } else if (command instanceof ServerCommand) {
            registerCommand(command, parsers.get(CommandType.TYPE_SERVER), register);
        } else if (command instanceof QueryCommand) {
            registerCommand(command, parsers.get(CommandType.TYPE_QUERY), register);
        } else if (command instanceof GlobalCommand) {
            registerCommand(command, parsers.get(CommandType.TYPE_GLOBAL), register);
        } else if (command instanceof ChatCommand) {
            registerCommand(command, parsers.get(CommandType.TYPE_QUERY), register);
            registerCommand(command, parsers.get(CommandType.TYPE_CHANNEL), register);
        } else {
            canContinue = false;
            
            Logger.userError(ErrorLevel.HIGH, "Attempted to (un)register an invalid command: "
                    + command.getClass().getName());
        }
        
        if (canContinue) {
            commands.put(command, command);
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
            final List<? extends CommandParser> parsers, final boolean register) {
        if (parsers == null) {
            return;
        }
        
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
        final String commandName = getCommandChar() + command.getName();
        
        for (Server server : ServerManager.getServerManager().getServers()) {
            if (command instanceof ServerCommand || command instanceof GlobalCommand) {
                registerCommandName(server.getTabCompleter(), commandName, register);
            }
            
            if (command instanceof ChannelCommand || command instanceof ChatCommand) {
                for (String channelName : server.getChannels()) {
                    registerCommandName(server.getChannel(channelName).getTabCompleter(),
                            commandName, register);
                }
            }
            
            if (command instanceof QueryCommand || command instanceof ChatCommand) {
                for (String queryName : server.getQueries()) {
                    registerCommandName(server.getQuery(queryName).getTabCompleter(),
                            commandName, register);
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
     * 
     * @param command The command to be registered
     */
    public static void registerPopupCommand(final Command command) {
        channelPopupCommands.add(command);
    }
    
    /**
     * Retrieves the commands for use in the nicklist popup.
     * 
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
        new ChannelSettings();
        new Cycle();
        new KickReason();
        new Mode();
        new Part();
        new SetNickColour();
        new SetTopic();
        new ShowTopic();
        
        // Server commands
        new AllChannels();
        new Away();
        new Back();
        new ChangeServer();
        new Ctcp();
        new Disconnect();
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
        new RawServerCommand("map");
        new RawServerCommand("motd");
        registerPopupCommand(new RawServerCommand("whois"));
        new RawServerCommand("who");
        
        // Query commands
        
        // Global commands
        new Active();
        new AllServers();
        new Clear();
        new Debug();
        new Echo();
        new Exit();
        new Ifplugin();
        new NewServer();
        new Notify();
        new LoadPlugin();
        new OpenWindow();
        new ReloadActions();
        new ReloadIdentities();
        new ReloadPlugin();
        new SaveConfig();
        new Set();
        
        // Set up a listener for config changes
        final ConfigChangeListener listener = new ConfigChangeListener() {
            @Override
            public void configChanged(String domain, String key) {
                commandChar = IdentityManager.getGlobalConfig()
                        .getOption("general", "commandchar").charAt(0);
                silenceChar = IdentityManager.getGlobalConfig()
                        .getOption("general", "silencechar").charAt(0);
            }
        };
        
        IdentityManager.getGlobalConfig().addChangeListener("general", "commandchar", listener);
        IdentityManager.getGlobalConfig().addChangeListener("general", "silencechar", listener);
    }
    
    /**
     * Loads all channel commands into the specified parser.
     * 
     * @param parser The parser to load commands into
     */
    public static void loadChannelCommands(final CommandParser parser) {
        for (Command com : getCommands(CommandType.TYPE_CHANNEL, null, null)) {
            parser.registerCommand(com);
        }
        
        for (Command com : getCommands(CommandType.TYPE_CHAT, null, null)) {
            parser.registerCommand(com);
        }
        
        parsers.add(CommandType.TYPE_CHANNEL, parser);
    }
    
    /**
     * Loads all server commands into the specified parser.
     * 
     * @param parser The parser to load commands into
     */
    public static void loadServerCommands(final CommandParser parser) {
        for (Command command : getCommands(CommandType.TYPE_SERVER, null, null)) {
            parser.registerCommand(command);
        }
        
        parsers.add(CommandType.TYPE_SERVER, parser);
    }
    
    /**
     * Loads all global commands into the specified parser.
     * 
     * @param parser The parser to load commands into
     */
    public static void loadGlobalCommands(final CommandParser parser) {
        for (Command com : getCommands(CommandType.TYPE_GLOBAL, null, null)) {
            parser.registerCommand(com);
        }
        
        parsers.add(CommandType.TYPE_GLOBAL, parser);
    }
    
    /**
     * Loads all query commands into the specified parser.
     * 
     * @param parser The parser to load commands into
     */
    public static void loadQueryCommands(final CommandParser parser) {
        for (Command com : getCommands(CommandType.TYPE_QUERY, null, null)) {
            parser.registerCommand(com);
        }
        
        for (Command com : getCommands(CommandType.TYPE_CHAT, null, null)) {
            parser.registerCommand(com);
        }
        
        parsers.add(CommandType.TYPE_QUERY, parser);
    }
    
    /**
     * Retrieves the command identified by the specified name, regardless of
     * type.
     *
     * @param name The name to look for
     * @return A command with a matching signature, or null if none were found
     */
    public static Command getCommand(final String name) {
        return getCommand(null, name, null);
    }
    
    public static Command getCommand(final CommandType type, final String name,
            final String signature) {
        final List<Command> res = getCommands(type, name, signature);
        
        return res.isEmpty() ? null : res.get(0);
    }    
     
    /**
     * Determines if the specified command is a valid channel command.
     * 
     * @param command The name of the command to test
     * @return True iff the command is a channel command, false otherwise
     */
    public static boolean isChannelCommand(final String command) {
        return getCommand(CommandType.TYPE_CHANNEL, command, null) != null
                || getCommand(CommandType.TYPE_CHAT, command, null) != null;
    }
       
    public static List<String> getCommandNames(final CommandType type) {
        final List<String> res = new ArrayList<String>();
        
        for (Command command : getCommands(type, null, null)) {
            res.add(getCommandChar() + command.getName());
        }
        
        return res;
    }
    
    public static List<Command> getCommands(final CommandType type) {    
        return getCommands(type, null, null);
    }
    
    private static List<Command> getCommands(final CommandType type,
            final String name, final String signature) {
        final List<Command> res = new ArrayList<Command>();
        
        for (Map.Entry<CommandInfo, Command> entry : commands.entrySet()) {
            if ((type == null || type.equals(entry.getKey().getType()))
                    && (name == null || name.equals(entry.getKey().getName()))
                    && (signature == null || signature.equals(entry.getKey().getSignature()))) {
                res.add(entry.getValue());
            }
        }
        
        return res;
    }
    
}
