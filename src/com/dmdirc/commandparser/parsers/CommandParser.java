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

package com.dmdirc.commandparser.parsers;

import com.dmdirc.Server;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.CommandOptions;
import com.dmdirc.commandparser.commands.ExternalCommand;
import com.dmdirc.commandparser.commands.PreviousCommand;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.interfaces.InputWindow;
import com.dmdirc.util.RollingList;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Represents a generic command parser. A command parser takes a line of input
 * from the user, determines if it is an attempt at executing a command (based
 * on the character at the start of the string), and handles it appropriately.
 *
 * @author chris
 */
public abstract class CommandParser implements Serializable {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;

    /**
     * Commands that are associated with this parser.
     */
    private final Map<String, Command> commands;

    /**
     * A history of commands that have been entered into this parser.
     */
    private final RollingList<PreviousCommand> history;

    /** Creates a new instance of CommandParser. */
    public CommandParser() {
        commands = new Hashtable<String, Command>();
        history = new RollingList<PreviousCommand>(
                IdentityManager.getGlobalConfig().getOptionInt("general",
                    "commandhistory"));
        loadCommands();
    }

    /** Loads the relevant commands into the parser. */
    protected abstract void loadCommands();

    /**
     * Registers the specified command with this parser.
     *
     * @since 0.6.3m1
     * @param command Command to be registered
     * @param info The information the command should be registered with
     */
    public final void registerCommand(final Command command, final CommandInfo info) {
        commands.put(info.getName().toLowerCase(), command);
    }

    /**
     * Unregisters the specified command with this parser.
     *
     * @param info Command information to be unregistered
     * @since 0.6.3m1
     */
    public final void unregisterCommand(final CommandInfo info) {
        commands.remove(info.getName().toLowerCase());
    }

    /**
     * Retrieves a map of commands known by this command parser.
     *
     * @since 0.6.3m1
     * @return A map of commands known to this parser
     */
    public Map<String, Command> getCommands() {
        return new HashMap<String, Command>(commands);
    }

    /**
     * Parses the specified string as a command.
     *
     * @param origin The window in which the command was typed
     * @param line The line to be parsed
     * @param parseChannel Whether or not to try and parse the first argument
     * as a channel name
     */
    public final void parseCommand(final InputWindow origin,
            final String line, final boolean parseChannel) {
        final CommandArguments args = new CommandArguments(line);

        if (args.isCommand()) {
            final boolean silent = args.isSilent();
            final String command = args.getCommandName();
            final String[] cargs = args.getArguments();

            if (args.getArguments().length > 0 && parseChannel && origin != null
                    && origin.getContainer() != null
                    && origin.getContainer().getServer() != null
                    && origin.getContainer().getServer().isValidChannelName(cargs[0])
                    && CommandManager.isChannelCommand(command)) {
                final Server server = origin.getContainer().getServer();

                if (server.hasChannel(cargs[0])) {
                    server.getChannel(cargs[0]).getFrame().getCommandParser()
                            .parseCommand(origin, CommandManager.getCommandChar()
                            + args.getCommandName() + " " + args.getWordsAsString(2), false);
                    return;
                } else {
                    final Map.Entry<CommandInfo, Command> actCommand = CommandManager.getCommand(
                            CommandType.TYPE_CHANNEL, command);

                    if (actCommand != null && actCommand.getValue() instanceof ExternalCommand) {
                        ((ExternalCommand) actCommand.getValue()).execute(
                                origin, server, cargs[0], silent,
                                new CommandArguments(args.getCommandName()
                                + " " + args.getWordsAsString(2)));
                        return;
                    }
                }
            }

            if (commands.containsKey(command.toLowerCase())) {
                addHistory(args.getStrippedLine());
                executeCommand(origin, silent, commands.get(command.toLowerCase()), args);
           } else {
                handleInvalidCommand(origin, args);
            }
        } else {
            handleNonCommand(origin, line);
        }
    }

    /**
     * Adds a command to this parser's history.
     *
     * @param command The command name and arguments that were used
     */
    private void addHistory(final String command) {
        synchronized(history) {
            final PreviousCommand pc = new PreviousCommand(command);
            history.remove(pc);
            history.add(pc);
        }
    }

    /**
     * Retrieves the most recent time that the specified command was used.
     * Commands should not include command or silence chars.
     *
     * @param command The command to search for
     * @return The timestamp that the command was used, or 0 if it wasn't
     */
    public long getCommandTime(final String command) {
        long res = 0;

        synchronized(history) {
            for (PreviousCommand pc : history.getList()) {
                if (pc.getLine().matches("(?i)" + command)) {
                    res = Math.max(res, pc.getTime());
                }
            }
        }

        return res;
    }

    /**
     * Parses the specified string as a command.
     *
     * @param origin The window in which the command was typed
     * @param line The line to be parsed
     */
    public final void parseCommand(final InputWindow origin,
            final String line) {
        parseCommand(origin, line, true);
    }

    /**
     * Handles the specified string as a non-command.
     *
     * @param origin The window in which the command was typed
     * @param line The line to be parsed
     */
    public final void parseCommandCtrl(final InputWindow origin, final String line) {
        handleNonCommand(origin, line);
    }

    /**
     * Executes the specified command with the given arguments.
     *
     * @param origin The window in which the command was typed
     * @param isSilent Whether the command is being silenced or not
     * @param command The command to be executed
     * @param args The arguments to the command
     * @since 0.6.3m1
     */
    protected abstract void executeCommand(final InputWindow origin,
            final boolean isSilent, final Command command, final CommandArguments args);

    /**
     * Called when the user attempted to issue a command (i.e., used the command
     * character) that wasn't found. It could be that the command has a different
     * arity, or that it plain doesn't exist.
     *
     * @param origin The window in which the command was typed
     * @param args The arguments passed to the command
     * @since 0.6.3m1
     */
    protected void handleInvalidCommand(final InputWindow origin,
            final CommandArguments args) {
        if (origin == null) {
            ActionManager.processEvent(CoreActionType.UNKNOWN_COMMAND, null,
                    null, args.getCommandName(), args.getArguments());
        } else {
            final StringBuffer buff = new StringBuffer("unknownCommand");

            ActionManager.processEvent(CoreActionType.UNKNOWN_COMMAND, buff,
                    origin.getContainer(), args.getCommandName(), args.getArguments());

            origin.addLine(buff, args.getCommandName());
        }
    }

    /**
     * Called when the input was a line of text that was not a command. This normally
     * means it is sent to the server/channel/user as-is, with no further processing.
     *
     * @param origin The window in which the command was typed
     * @param line The line input by the user
     */
    protected abstract void handleNonCommand(final InputWindow origin,
            final String line);

    /**
     * Determines if the specified command has defined any command options.
     *
     * @param command The command to investigate
     * @return True if the command defines options, false otherwise
     */
    protected boolean hasCommandOptions(final Command command) {
        return command.getClass().isAnnotationPresent(CommandOptions.class);
    }

    /**
     * Retrieves the command options for the specified command. If the command
     * does not define options, this method will return null.
     *
     * @param command The command whose options should be retrieved
     * @return The command's options, or null if not available
     */
    protected CommandOptions getCommandOptions(final Command command) {
        return command.getClass().getAnnotation(CommandOptions.class);
    }
}
