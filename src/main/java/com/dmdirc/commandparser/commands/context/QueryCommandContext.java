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

package com.dmdirc.commandparser.commands.context;

import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.interfaces.PrivateChat;
import com.dmdirc.interfaces.WindowModel;

/**
 * A query-specific command context.
 *
 * @since 0.6.4
 */
public class QueryCommandContext extends ChatCommandContext {

    /** The query the command was executed for. */
    private final PrivateChat query;

    /**
     * Creates a new query command context.
     *
     * @param source      The source of the command
     * @param commandInfo The command info object which associated the command with the input
     * @param query       The query associated with the command
     */
    public QueryCommandContext(final WindowModel source,
            final CommandInfo commandInfo, final PrivateChat query) {
        super(source, commandInfo, query);
        this.query = query;
    }

    /**
     * Retrieves the query associated with this context.
     *
     * @return This context's query
     */
    public PrivateChat getQuery() {
        return query;
    }

}
