/*
 * Copyright (c) 2006-2012 DMDirc Developers
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

import com.dmdirc.commandparser.commands.Command;

/**
 * A combination of a {@link Command} and the {@link CommandInfo} which
 * triggered it.
 *
 * @since 0.6.4
 */
public class CommandInfoPair {

    /** The command info which caused the command to be triggered. */
    private final CommandInfo commandInfo;
    /** The command in question. */
    private final Command command;

    /**
     * Creates a new CommandInfoPair.
     *
     * @param commandInfo The command info associated with the command
     * @param command The command
     */
    public CommandInfoPair(final CommandInfo commandInfo, final Command command) {
        this.commandInfo = commandInfo;
        this.command = command;
    }

    /**
     * Retrieves the command part of this pair.
     *
     * @return This pair's command
     */
    public Command getCommand() {
        return command;
    }

    /**
     * Retrieves the commandinfo part of this pair.
     *
     * @return This pair's commandinfo
     */
    public CommandInfo getCommandInfo() {
        return commandInfo;
    }

}
