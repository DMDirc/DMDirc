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

package com.dmdirc.commandparser.parsers;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.Query;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.commandparser.commands.context.QueryCommandContext;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.WindowModel;

import javax.annotation.Nonnull;

/**
 * A command parser that is tailored for use in a query environment. Handles both query and server
 * commands.
 */
public class QueryCommandParser extends ChatCommandParser {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;
    /**
     * The query instance that this parser is attached to.
     */
    private final Query query;

    /**
     * Creates a new instance of QueryCommandParser.
     *
     * @param owner            The container this parser's query belongs to
     * @param commandController The controller to load commands from.
     * @param eventBus          Event bus to post events on
     */
    public QueryCommandParser(final WindowModel owner, final CommandController commandController,
            final DMDircMBassador eventBus, final Query query) {
        super(owner, commandController, eventBus, query);
        this.query = query;
    }

    /** Loads the relevant commands into the parser. */
    @Override
    protected void loadCommands() {
        commandManager.loadCommands(this, CommandType.TYPE_GLOBAL,
                CommandType.TYPE_SERVER, CommandType.TYPE_QUERY);
    }

    @Override
    protected CommandContext getCommandContext(
            final WindowModel origin,
            final CommandInfo commandInfo,
            final Command command,
            final CommandArguments args) {
        return new QueryCommandContext(origin, commandInfo, query);
    }

    @Override
    protected void executeCommand(
            @Nonnull final WindowModel origin,
            final CommandInfo commandInfo,
            final Command command,
            final CommandArguments args,
            final CommandContext context) {
        if (commandInfo.getType() == CommandType.TYPE_QUERY) {
            command.execute(origin, args, context);
        } else {
            super.executeCommand(origin, commandInfo, command, args, context);
        }
    }

}
