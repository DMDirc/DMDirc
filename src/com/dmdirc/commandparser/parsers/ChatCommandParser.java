/*
 * Copyright (c) 2006-2014 DMDirc Developers
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
import com.dmdirc.MessageTarget;
import com.dmdirc.Server;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.context.ChatCommandContext;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.interfaces.CommandController;

/**
 * A command parser which implements common functionality for chat windows (queries and channels).
 *
 * @since 0.6.4
 */
public class ChatCommandParser extends ServerCommandParser {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;
    /** The container that owns this parser. */
    private MessageTarget owner;

    /**
     * Creates a new chat command parser that belongs to a child of the specified server.
     *
     * @param server            The server which owns this parser's container
     * @param commandController The controller to load commands from.
     */
    public ChatCommandParser(final Server server, final CommandController commandController) {
        super(server.getConfigManager(), commandController);
        super.setOwner(server);
    }

    @Override
    public void setOwner(final FrameContainer owner) {
        if (this.owner == null) {
            this.owner = (MessageTarget) owner;
        }
    }

    @Override
    protected CommandContext getCommandContext(
            final FrameContainer origin,
            final CommandInfo commandInfo,
            final Command command,
            final CommandArguments args) {
        return new ChatCommandContext(origin, commandInfo, owner);
    }

    @Override
    protected void executeCommand(
            final FrameContainer origin,
            final CommandInfo commandInfo,
            final Command command,
            final CommandArguments args,
            final CommandContext context) {
        if (commandInfo.getType() == CommandType.TYPE_CHAT) {
            command.execute(origin, args, context);
        } else {
            super.executeCommand(origin, commandInfo, command, args, context);
        }
    }

    @Override
    protected void handleNonCommand(final FrameContainer origin,
            final String line) {
        owner.sendLine(line);
    }

}
