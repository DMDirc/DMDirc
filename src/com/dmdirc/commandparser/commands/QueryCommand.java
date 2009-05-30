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

package com.dmdirc.commandparser.commands;

import com.dmdirc.Query;
import com.dmdirc.Server;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.ui.interfaces.InputWindow;

/**
 * Represents a command which can be performed only in the context of a query.
 *
 * @author chris
 */
public abstract class QueryCommand extends Command implements CommandInfo {
        
    /**
     * Executes this command.
     *
     * @param origin The window in which the command was typed
     * @param server The server instance that this command is being executed on
     * @param query The query object that the commadparser is associated with
     * @param isSilent Whether this command is silenced or not
     * @param args Arguments passed to this command
     * @since 0.6.3m1
     */
    public abstract void execute(InputWindow origin, Server server, Query query,
            boolean isSilent, CommandArguments args);

    /** {@inheritDoc} */
    @Override
    public CommandType getType() {
        return CommandType.TYPE_QUERY;
    }
}
