/*
 * Copyright (c) 2006-2017 DMDirc Developers
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

import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandInfoPair;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.interfaces.WindowModel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * A command parser takes a line of input from the user, determines if it is an attempt at executing a command (based
 * on the character at the start of the string), and handles it appropriately.
 */
public interface CommandParser {

    /**
     * Registers the specified command with this parser.
     *
     * @param command Command to be registered
     * @param info    The information the command should be registered with
     */
    void registerCommand(Command command, CommandInfo info);

    /**
     * Unregisters the specified command with this parser.
     *
     * @param info Command information to be unregistered
     */
    void unregisterCommand(CommandInfo info);

    /**
     * Retrieves a map of commands known by this command parser.
     *
     * @return A map of commands known to this parser
     */
    Map<String, CommandInfoPair> getCommands();

    /**
     * Parses the specified string as a command.
     *
     * @param origin       The container which received the command
     * @param line         The line to be parsed
     * @param parseChannel Whether or not to try and parse the first argument as a channel name
     */
    void parseCommand(@Nonnull WindowModel origin, String line, boolean parseChannel);

    /**
     * Parses the specified string as a command.
     *
     * @param origin The container which received the command
     * @param line   The line to be parsed
     */
    void parseCommand(@Nonnull WindowModel origin, String line);

    /**
     * Handles the specified string as a non-command.
     *
     * @param origin The window in which the command was typed
     * @param line   The line to be parsed
     */
    void parseCommandCtrl(WindowModel origin, String line);

    /**
     * Gets the command with the given name that was previously registered with this parser.
     *
     * @param commandName The name of the command to retrieve.
     * @return The command info pair, or {@code null} if the command does not exist.
     */
    @Nullable
    CommandInfoPair getCommand(String commandName);

}
