/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.commandparser.parsers;

import com.dmdirc.ServerState;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.commandparser.commands.context.ServerCommandContext;
import com.dmdirc.events.CommandErrorEvent;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.events.eventbus.EventBus;
import com.dmdirc.interfaces.WindowModel;
import com.dmdirc.interfaces.config.AggregateConfigProvider;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A command parser used in the context of a server.
 */
public class ServerCommandParser extends GlobalCommandParser {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;

    /**
     * The server instance that this parser is attached to.
     */
    private final Connection server;

    /**
     * Creates a new command parser for server commands.
     *
     * @param configManager     Config manager to read settings from
     * @param commandController The controller to load commands from.
     * @param eventBus          Event bus to post events on
     */
    public ServerCommandParser(
            final AggregateConfigProvider configManager,
            final CommandController commandController,
            final EventBus eventBus,
            final Connection connection) {
        super(configManager, commandController, eventBus);
        this.server = checkNotNull(connection);
    }

    /** Loads the relevant commands into the parser. */
    @Override
    protected void loadCommands() {
        commandManager.loadCommands(this, CommandType.TYPE_GLOBAL, CommandType.TYPE_SERVER);
    }

    @Override
    protected CommandContext getCommandContext(
            final WindowModel origin,
            final CommandInfo commandInfo,
            final Command command,
            final CommandArguments args) {
        return new ServerCommandContext(origin, commandInfo, server);
    }

    @Override
    protected void executeCommand(
            @Nonnull final WindowModel origin,
            final CommandInfo commandInfo,
            final Command command,
            final CommandArguments args,
            final CommandContext context) {
        if (commandInfo.getType() == CommandType.TYPE_SERVER) {
            if (hasCommandOptions(command) && !getCommandOptions(command).allowOffline()
                    && (server == null || (server.getState() != ServerState.CONNECTED
                    && server.getState() != ServerState.CONNECTING)
                    || !server.getParser().isPresent())) {
                if (!args.isSilent()) {
                    origin.getEventBus().publishAsync(new CommandErrorEvent(origin,
                            "You must be connected to use this command"));
                }
            } else {
                command.execute(origin, args, context);
            }
        } else {
            super.executeCommand(origin, commandInfo, command, args, context);
        }
    }

    /**
     * Called when the input was a line of text that was not a command. This normally means it is
     * sent to the server/channel/user as-is, with no further processing.
     *
     * @param origin The window in which the command was typed
     * @param line   The line input by the user
     */
    @Override
    protected void handleNonCommand(final WindowModel origin, final String line) {
        server.sendLine(line);
    }

}
