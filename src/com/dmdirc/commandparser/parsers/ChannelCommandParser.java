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

import com.dmdirc.Channel;
import com.dmdirc.DMDircMBassador;
import com.dmdirc.FrameContainer;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.context.ChannelCommandContext;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.interfaces.CommandController;

import javax.annotation.Nonnull;

/**
 * A command parser that is tailored for use in a channel environment.
 */
public class ChannelCommandParser extends ChatCommandParser {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;
    /**
     * The channel instance that this parser is attached to.
     */
    private Channel channel;

    /**
     * Creates a new instance of ChannelCommandParser.
     *
     * @param owner            The container this parser's query belongs to
     * @param commandController The controller to load commands from.
     * @param eventBus          Event bus to post events on
     */
    public ChannelCommandParser(final FrameContainer owner, final CommandController commandController,
            final DMDircMBassador eventBus) {
        super(owner, commandController, eventBus);
    }

    @Override
    public void setOwner(final FrameContainer owner) {
        if (channel == null) {
            channel = (Channel) owner;
        }

        super.setOwner(owner);
    }

    @Override
    protected void loadCommands() {
        commandManager.loadCommands(this, CommandType.TYPE_GLOBAL,
                CommandType.TYPE_SERVER, CommandType.TYPE_CHANNEL);
    }

    @Override
    protected CommandContext getCommandContext(
            final FrameContainer origin,
            final CommandInfo commandInfo,
            final Command command,
            final CommandArguments args) {
        return new ChannelCommandContext(origin, commandInfo, channel);
    }

    @Override
    protected void executeCommand(
            @Nonnull final FrameContainer origin,
            final CommandInfo commandInfo,
            final Command command,
            final CommandArguments args,
            final CommandContext context) {
        if (commandInfo.getType() == CommandType.TYPE_CHANNEL) {
            command.execute(origin, args, context);
        } else {
            super.executeCommand(origin, commandInfo, command, args, context);
        }
    }

}
