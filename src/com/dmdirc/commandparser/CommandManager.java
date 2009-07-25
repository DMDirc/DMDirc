/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.Query;
import com.dmdirc.Server;
import com.dmdirc.ServerManager;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.channel.*;
import com.dmdirc.commandparser.commands.chat.*;
import com.dmdirc.commandparser.commands.global.*;
//import com.dmdirc.commandparser.commands.query.*;
import com.dmdirc.commandparser.commands.server.*;
import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.interfaces.ConfigChangeListener;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.input.TabCompleter;
import com.dmdirc.ui.input.TabCompletionType;
import com.dmdirc.util.MapList;

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
    
    /** The command char we're using. */
    private static char commandChar = IdentityManager.getGlobalConfig()
            .getOptionChar("general", "commandchar");
    
    /** The silence char we're using. */
    private static char silenceChar = IdentityManager.getGlobalConfig()
            .getOptionChar("general", "silencechar");
    
    /**
     * Prevents creation of a new command manager.
     */
    private CommandManager() {
        // Do nothing
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
     * @param info The information about the command
     * @since 0.6.3m1
     */
    public static void registerCommand(final Command command, final CommandInfo info) {
        registerCommand(info, command, true);
    }

    /**
     * Registers a {@link Command} which also implements the {@link CommandInfo}
     * interface with the command manager.
     *
     * @param <T> The type of object that's being registered
     * @param command An object that extends {@link Command} and implements
     * {@link CommandInfo} to be registered.
     * @since 0.6.3m1
     */
    public static <T extends Command & CommandInfo> void registerCommand(final T command) {
        registerCommand(command, command);
    }
    
    /**
     * Unregisters a command with the command manager.
     * 
     * @param info The information object for the command that should be unregistered
     * @since 0.6.3m1
     */
    public static void unregisterCommand(final CommandInfo info) {
        registerCommand(info, commands.get(info), false);
    }
    
    /**
     * Registers or unregisters a command.
     *
     * @param info The information about the command
     * @param command The command to be (un)registered
     * @param register True if the command should be registered, false if it
     * should be unregistered.
     * @since 0.6.3m1
     */
    private static void registerCommand(final CommandInfo info, final Command command,
            final boolean register) {
        if (parsers.containsKey(info.getType())) {
            registerCommand(info, command, parsers.get(info.getType()), register);
        }
        
        if (register) {
            commands.put(info, command);
        } else {
            commands.remove(info);
        }

        registerCommandName(info, register);
    }
    
    /**
     * Registers the specified command with all of the specified parsers.
     *
     * @param info The command information object
     * @param command The command to be reigstered
     * @param parsers The parsers to register the command with
     * @since 0.6.3m1
     */
    private static void registerCommand(final CommandInfo info, final Command command,
            final List<? extends CommandParser> myParsers, final boolean register) {
        for (CommandParser parser : myParsers) {
            if (register) {
                parser.registerCommand(command, info);
            } else {
                parser.unregisterCommand(info);
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
     * @since 0.6.3m1
     */
    private static void registerCommandName(final CommandInfo command,
            final boolean register) {
        // Do tab completion
        final String commandName = getCommandChar() + command.getName();

        // TODO: This logic is probably in two places. Abstract it.
        for (Server server : ServerManager.getServerManager().getServers()) {
            if (command.getType() == CommandType.TYPE_SERVER ||
                    command.getType() == CommandType.TYPE_GLOBAL) {
                registerCommandName(server.getTabCompleter(), commandName, register);
            }
            
            if (command.getType() == CommandType.TYPE_CHANNEL
                    || command.getType() == CommandType.TYPE_CHAT) {
                for (String channelName : server.getChannels()) {
                    registerCommandName(server.getChannel(channelName).getTabCompleter(),
                            commandName, register);
                }
            }
            
            if (command.getType() == CommandType.TYPE_QUERY
                    || command.getType() == CommandType.TYPE_CHAT) {
                for (Query query : server.getQueries()) {
                    registerCommandName(query.getTabCompleter(),
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
            completer.addEntry(TabCompletionType.COMMAND, name);
        }  else {
            completer.removeEntry(TabCompletionType.COMMAND, name);
        }
    }
        
    /**
     * Instansiates the default commands.
     */
    public static void initCommands() {
        // Chat commands
        new Me();
        
        // Channel commands
        new Ban();
        new ChannelSettings();
        new Cycle();
        new KickReason();
        new Mode();
        new Names();
        new Part();
        new SetNickColour();
        new ShowTopic();
        
        // Server commands
        new AllChannels();
        new Away();
        new Back();
        new ChangeServer();
        new Ctcp();
        new Disconnect();
        new Ignore();
        new JoinChannelCommand();
        new Message();
        new Nick();
        new Notice();
        new OpenQuery();
        new Raw();
        new Reconnect();
        new Umode();
        
        new RawServerCommand("invite");
        new RawServerCommand("lusers");
        new RawServerCommand("map");
        new RawServerCommand("motd");
        new RawServerCommand("oper");
        new RawServerCommand("whois");
        new RawServerCommand("who");
        
        // Query commands
        
        // Global commands
        new Active();
        new AliasCommand();
        new AllServers();
        new Clear();
        new Debug();
        new Echo();
        new Exit();
        new Help();
        new Ifplugin();
        new NewServer();
        new Notify();
        new LoadPlugin();
        new UnloadPlugin();
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
                        .getOptionChar("general", "commandchar");
                silenceChar = IdentityManager.getGlobalConfig()
                        .getOptionChar("general", "silencechar");
            }
        };
        
        IdentityManager.getGlobalConfig().addChangeListener("general", "commandchar", listener);
        IdentityManager.getGlobalConfig().addChangeListener("general", "silencechar", listener);
    }

    /**
     * Loads all commands of the specified types into the specified parser.
     *
     * @see CommandType#getComponentTypes()
     * @since 0.6.3m1
     * @param parser The {@link CommandParser} to load commands in to
     * @param supertypes The types of commands that should be loaded
     */
    public static void loadCommands(final CommandParser parser,
            final CommandType ... supertypes) {
        for (CommandType supertype : supertypes) {
            for (CommandType type : supertype.getComponentTypes()) {
                for (Map.Entry<CommandInfo, Command> pair : getCommands(type, null).entrySet()) {
                    parser.registerCommand(pair.getValue(), pair.getKey());
                }

                parsers.add(type, parser);
            }
        }
    }
    
    /**
     * Retrieves the command identified by the specified name, regardless of
     * type.
     *
     * @param name The name to look for
     * @return A command with a matching signature, or null if none were found
     */
    public static Map.Entry<CommandInfo, Command> getCommand(final String name) {
        return getCommand(null, name);
    }
    
    /**
     * Retrieves a command of the specified type with the specified name.
     * 
     * @param type The type of the command to look for
     * @param name The name to look for
     * @return A command with a matching signature, or null if none were found
     */
    public static Map.Entry<CommandInfo, Command> getCommand(final CommandType type,
            final String name) {
        final Map<CommandInfo, Command> res = getCommands(type, name);
        
        return res.isEmpty() ? null : res.entrySet().iterator().next();
    }    
     
    /**
     * Determines if the specified command is a valid channel command.
     * 
     * @param command The name of the command to test
     * @return True iff the command is a channel command, false otherwise
     */
    public static boolean isChannelCommand(final String command) {
        return getCommand(CommandType.TYPE_CHANNEL, command) != null
                || getCommand(CommandType.TYPE_CHAT, command) != null;
    }
       
    /**
     * Retrieves a list of the names of all commands of the specified type.
     * 
     * @param type The type of command to list
     * @return A list of command names
     */
    public static List<String> getCommandNames(final CommandType type) {
        final List<String> res = new ArrayList<String>();
        
        for (CommandInfo command : getCommands(type).keySet()) {
            res.add(getCommandChar() + command.getName());
        }
        
        return res;
    }
    
    /**
     * Retrieves a map of all {@link CommandInfo}s and their associated
     * {@link Command}s of the specified type.
     * 
     * @param type The type of command to list
     * @return A map of commands
     * @since 0.6.3m1
     */    
    public static Map<CommandInfo, Command> getCommands(final CommandType type) {
        return getCommands(type, null);
    }
    
    /**
     * Retrieves a map of all commands of the specified type, with the
     * specified name.
     * 
     * @param type The type of command to list, or null for all types
     * @param name The name of the command to look for, or null for any name
     * @return A map of {@link CommandInfo}s and their associated {@link Command}.
     * @since 0.6.3m1
     */    
    private static Map<CommandInfo, Command> getCommands(final CommandType type,
            final String name) {
        final Map<CommandInfo, Command> res = new HashMap<CommandInfo, Command>();
        
        for (Map.Entry<CommandInfo, Command> entry : commands.entrySet()) {
            if ((type == null || type.equals(entry.getKey().getType()))
                    && (name == null || name.equals(entry.getKey().getName()))) {
                res.put(entry.getKey(), entry.getValue());
            }
        }
        
        return res;
    }
    
}
