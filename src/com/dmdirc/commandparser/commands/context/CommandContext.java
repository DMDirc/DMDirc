/*
 * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.commandparser.commands.context;

import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.ui.interfaces.Window;

/**
 * Provides information relating to the context that a command was executed in.
 *
 * @since 0.6.4
 * @author chris
 */
public class CommandContext {

    /** The command info object which associated the command with the input. */
    protected final CommandInfo commandInfo;

    /** The source of this command. */
    protected final Window source;

    /**
     * Creates a new command context.
     *
     * @param source The source of the command
     * @param commandInfo The command info object which associated the command with the input
     */
    public CommandContext(final Window source, final CommandInfo commandInfo) {
        this.source = source;
        this.commandInfo = commandInfo;
    }

    /**
     * Retrieves the command information object which caused the command to
     * be selected in response to the user's input.
     *
     * @return The relevant command info object
     */
    public CommandInfo getCommandInfo() {
        return commandInfo;
    }

    /**
     * Retrieves the window, if any, where the command was entered.
     *
     * @return The window the command came from, or null
     */
    public Window getSource() {
        return source;
    }

}
