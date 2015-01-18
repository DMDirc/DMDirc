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
import com.dmdirc.FrameContainer;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.context.ChatCommandContext;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.interfaces.Chat;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.WindowModel;

import javax.annotation.Nonnull;

/**
 * A command parser which implements common functionality for chat windows (queries and channels).
 *
 * @since 0.6.4
 */
public class ChatCommandParser extends ServerCommandParser {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;
    /** The container that owns this parser. */
    private Chat owner;

    /**
     * Creates a new chat command parser that belongs to a child of the specified server.
     *
     * @param owner             The container which owns this parser's container
     * @param commandController The controller to load commands from.
     * @param eventBus          Event but to post events on
     */
    public ChatCommandParser(final FrameContainer owner, final CommandController commandController,
            final DMDircMBassador eventBus) {
        super(owner.getConfigManager(), commandController, eventBus);
        super.setOwner(owner);
    }

    @Override
    public void setOwner(final WindowModel owner) {
        if (this.owner == null && owner instanceof Chat) {
            this.owner = (Chat) owner;
        }
    }

    @Override
    protected CommandContext getCommandContext(
            final WindowModel origin,
            final CommandInfo commandInfo,
            final Command command,
            final CommandArguments args) {
        return new ChatCommandContext(origin, commandInfo, owner);
    }

    @Override
    protected void executeCommand(
            @Nonnull final WindowModel origin,
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
    protected void handleNonCommand(final WindowModel origin,
            final String line) {
        owner.sendLine(line);
    }

}
