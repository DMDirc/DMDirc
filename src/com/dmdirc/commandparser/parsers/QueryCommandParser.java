/*
 * Copyright (c) 2006-2013 DMDirc Developers
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

import com.dmdirc.FrameContainer;
import com.dmdirc.Query;
import com.dmdirc.Server;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.context.QueryCommandContext;
import com.dmdirc.interfaces.CommandController;

/**
 * A command parser that is tailored for use in a query environment. Handles
 * both query and server commands.
 */
public class QueryCommandParser extends ChatCommandParser {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;

    /**
     * The query instance that this parser is attached to.
     */
    private Query query;

    /**
     * Creates a new instance of QueryCommandParser.
     *
     * @param server The server this parser's query belongs to
     * @param commandController The controller to load commands from.
     */
    public QueryCommandParser(final Server server, final CommandController commandController) {
        super(server, commandController);
    }

    /** {@inheritDoc} */
    @Override
    public void setOwner(final FrameContainer owner) {
        if (query == null) {
            query = (Query) owner;
        }

        super.setOwner(query);
    }

    /** Loads the relevant commands into the parser. */
    @Override
    protected void loadCommands() {
        commandManager.loadCommands(this, CommandType.TYPE_GLOBAL,
                CommandType.TYPE_SERVER, CommandType.TYPE_QUERY);
    }

    /** {@inheritDoc} */
    @Override
    protected void executeCommand(final FrameContainer origin,
            final CommandInfo commandInfo, final Command command,
            final CommandArguments args) {
        if (commandInfo.getType() == CommandType.TYPE_QUERY) {
            command.execute(origin, args, new QueryCommandContext(origin,
                    commandInfo, query));
        } else {
            super.executeCommand(origin, commandInfo, command, args);
        }
    }

}
