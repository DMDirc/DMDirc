/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

package com.dmdirc.interfaces;

import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.parsers.CommandParser;

import java.util.List;
import java.util.Map;

/**
 * Manages a list of known commands, and facilitates loading default commands.
 */
public interface CommandController {

    /**
     * Retrieves the command identified by the specified name, regardless of type.
     *
     * @param name The name to look for
     *
     * @return A command with a matching signature, or null if none were found
     */
    Map.Entry<CommandInfo, Command> getCommand(final String name);

    /**
     * Retrieves a command of the specified type with the specified name.
     *
     * @param type The type of the command to look for
     * @param name The name to look for
     *
     * @return A command with a matching signature, or null if none were found
     */
    Map.Entry<CommandInfo, Command> getCommand(final CommandType type, final String name);

    /**
     * Returns the current command character.
     *
     * @return the current command char
     */
    char getCommandChar();

    /**
     * Retrieves a list of the names of all commands of the specified type.
     *
     * @param type The type of command to list
     *
     * @return A list of command names
     */
    List<String> getCommandNames(final CommandType type);

    /**
     * Retrieves a map of all {@link CommandInfo}s and their associated {@link Command}s of the
     * specified type.
     *
     * @param type The type of command to list
     *
     * @return A map of commands
     *
     * @since 0.6.3m1
     */
    Map<CommandInfo, Command> getCommands(final CommandType type);

    /**
     * Returns the current silence character.
     *
     * @return the current silence char
     */
    char getSilenceChar();

    /**
     * Determines if the specified command is a valid channel command.
     *
     * @param command The name of the command to test
     *
     * @return True iff the command is a channel command, false otherwise
     */
    boolean isChannelCommand(final String command);

    /**
     * Loads all commands of the specified types into the specified parser.
     *
     * @see CommandType#getComponentTypes()
     * @since 0.6.3m1
     * @param parser     The {@link CommandParser} to load commands in to
     * @param supertypes The types of commands that should be loaded
     */
    void loadCommands(final CommandParser parser, final CommandType... supertypes);

    /**
     * Registers a command with the command manager.
     *
     * @param command The command to be registered
     * @param info    The information about the command
     *
     * @since 0.6.3m1
     */
    void registerCommand(final Command command, final CommandInfo info);

    /**
     * Unregisters a command with the command manager.
     *
     * @param info The information object for the command that should be unregistered
     *
     * @since 0.6.3m1
     */
    void unregisterCommand(final CommandInfo info);

    /**
     * Describes a command and its associated {@link CommandInfo} object.
     */
    interface CommandDetails {

        /**
         * Gets the command.
         *
         * @return The command.
         */
        Command getCommand();

        /**
         * Gets the command's information.
         *
         * @return The command information.
         */
        CommandInfo getInfo();

    }

}
