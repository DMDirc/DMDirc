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

import com.dmdirc.Query;
import com.dmdirc.Server;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.ChatCommand;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.GlobalCommand;
import com.dmdirc.commandparser.commands.QueryCommand;
import com.dmdirc.commandparser.commands.ServerCommand;
import com.dmdirc.ui.interfaces.InputWindow;

/**
 * A command parser that is tailored for use in a query environment. Handles
 * both query and server commands.
 * @author chris
 */
public final class QueryCommandParser extends CommandParser {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /**
     * The server instance that this parser is attached to.
     */
    private final Server server;
    /**
     * The query instance that this parser is attached to.
     */
    private final Query query;
    
    /**
     * Creates a new instance of QueryCommandParser.
     * @param newServer The server instance that this parser is attached to
     * @param newQuery The query instance that this parser is attached to
     */
    public QueryCommandParser(final Server newServer, final Query newQuery) {
        super();
        
        server = newServer;
        query = newQuery;
    }
    
    /** Loads the relevant commands into the parser. */
    @Override
    protected void loadCommands() {
        CommandManager.loadCommands(this, CommandType.TYPE_GLOBAL,
                CommandType.TYPE_SERVER, CommandType.TYPE_QUERY);
    }
    
    /** {@inheritDoc} */
    @Override
    protected void executeCommand(final InputWindow origin,
            final boolean isSilent, final Command command, final CommandArguments args) {
        if (command instanceof QueryCommand) {
            ((QueryCommand) command).execute(origin, server, query, isSilent, args);
        } else if (command instanceof ChatCommand) {
            ((ChatCommand) command).execute(origin, server, query, isSilent, args);
        } else if (command instanceof ServerCommand) {
            ((ServerCommand) command).execute(origin, server, isSilent, args);
        } else {
            ((GlobalCommand) command).execute(origin, isSilent, args);
        }
    }
    
    /**
     * Called when the input was a line of text that was not a command. This normally
     * means it is sent to the server/channel/user as-is, with no further processing.
     * @param origin The window in which the command was typed
     * @param line The line input by the user
     */
    @Override
    protected void handleNonCommand(final InputWindow origin, final String line) {
        query.sendLine(line);
    }
    
}
