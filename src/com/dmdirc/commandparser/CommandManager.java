/*
 * Copyright (c) 2006-2014 DMDirc Developers
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
import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.config.ConfigBinding;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.ui.input.TabCompleter;
import com.dmdirc.ui.input.TabCompletionType;
import com.dmdirc.util.collections.MapList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The command manager creates and manages a single instance of all commands,
 * and provides methods to load each group of commands into a parser instance.
 */
public class CommandManager implements CommandController {

    /** A list of commands that have been instantiated. */
    private final Map<CommandInfo, Command> commands
            = new HashMap<>();

    /** A list of command parsers that have been instantiated. */
    private final MapList<CommandType, CommandParser> parsers
            = new MapList<>();

    /** The manager to use to iterate servers. */
    private final ServerManager serverManager;

    /** The command char we're using. */
    @ConfigBinding(domain="general", key="commandchar")
    private char commandChar;

    /** The silence char we're using. */
    @ConfigBinding(domain="general", key="silencechar")
    private char silenceChar;

    /**
     * Creates a new instance of the Command Manager.
     *
     * @param serverManager the manager to use to iterate servers.
     */
    public CommandManager(final ServerManager serverManager) {
        this.serverManager = serverManager;
    }

    public char getCommandChar() {
        return commandChar;
    }

    public char getSilenceChar() {
        return silenceChar;
    }

    /**
     * Initialises the command manager.
     *
     * @param configManager The configuration manager to read settings from.
     */
    public void initialise(final AggregateConfigProvider configManager) {
        configManager.getBinder().bind(this, CommandManager.class);
    }

    /** {@inheritDoc} */
    @Override
    public void registerCommand(final Command command, final CommandInfo info) {
        registerCommand(info, command, true);
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterCommand(final CommandInfo info) {
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
    private void registerCommand(final CommandInfo info, final Command command,
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
     * Registers or unregisters the specified command with all of the specified parsers.
     *
     * @param info The command information object
     * @param command The command to be registered
     * @param myParsers The parsers to register the command with
     * @param register Whether to register or unregister the commands
     * @since 0.6.3m1
     */
    private void registerCommand(final CommandInfo info, final Command command,
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
    private void registerCommandName(final CommandInfo command,
            final boolean register) {
        // Do tab completion
        final String commandName = getCommandChar() + command.getName();

        // TODO: This logic is probably in two places. Abstract it.
        for (Server server : serverManager.getServers()) {
            if (command.getType() == CommandType.TYPE_SERVER
                    || command.getType() == CommandType.TYPE_GLOBAL) {
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
    private void registerCommandName(final TabCompleter completer,
            final String name, final boolean register) {
        if (register) {
            completer.addEntry(TabCompletionType.COMMAND, name);
        }  else {
            completer.removeEntry(TabCompletionType.COMMAND, name);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void loadCommands(final CommandParser parser,
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

    /** {@inheritDoc} */
    @Override
    public Map.Entry<CommandInfo, Command> getCommand(final String name) {
        return getCommand(null, name);
    }

    /** {@inheritDoc} */
    @Override
    public Map.Entry<CommandInfo, Command> getCommand(final CommandType type,
            final String name) {
        final Map<CommandInfo, Command> res = getCommands(type, name);

        return res.isEmpty() ? null : res.entrySet().iterator().next();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isChannelCommand(final String command) {
        return getCommand(CommandType.TYPE_CHANNEL, command) != null
                || getCommand(CommandType.TYPE_CHAT, command) != null;
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getCommandNames(final CommandType type) {
        final List<String> res = new ArrayList<>();

        for (CommandInfo command : getCommands(type).keySet()) {
            res.add(getCommandChar() + command.getName());
        }

        return res;
    }

    /** {@inheritDoc} */
    @Override
    public Map<CommandInfo, Command> getCommands(final CommandType type) {
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
    private Map<CommandInfo, Command> getCommands(final CommandType type,
            final String name) {
        final Map<CommandInfo, Command> res = new HashMap<>();

        for (Map.Entry<CommandInfo, Command> entry : commands.entrySet()) {
            if ((type == null || type.equals(entry.getKey().getType()))
                    && (name == null || name.equals(entry.getKey().getName()))) {
                res.put(entry.getKey(), entry.getValue());
            }
        }

        return res;
    }

}
