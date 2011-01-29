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

package com.dmdirc.commandparser.parsers;

import com.dmdirc.Channel;
import com.dmdirc.FrameContainer;
import com.dmdirc.Server;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.context.ChannelCommandContext;
import com.dmdirc.ui.interfaces.Window;

/**
 * A command parser that is tailored for use in a channel environment.
 *
 * @author chris
 */
public class ChannelCommandParser extends ChatCommandParser {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;

    /**
     * The channel instance that this parser is attached to.
     */
    private Channel channel;

    /**
     * Creates a new instance of ChannelCommandParser.
     *
     * @param server The server this parser's query belongs to
     */
    public ChannelCommandParser(final Server server) {
        super(server);
    }

    /** {@inheritDoc} */
    @Override
    public void setOwner(final FrameContainer owner) {
        if (channel == null) {
            channel = (Channel) owner;
        }

        super.setOwner(owner);
    }

    /** {@inheritDoc} */
    @Override
    protected void loadCommands() {
        CommandManager.loadCommands(this, CommandType.TYPE_GLOBAL,
                CommandType.TYPE_SERVER, CommandType.TYPE_CHANNEL);
    }

    /** {@inheritDoc} */
    @Override
    protected void executeCommand(final FrameContainer origin,
            final Window window, final CommandInfo commandInfo,
            final Command command, final CommandArguments args) {
        if (commandInfo.getType() == CommandType.TYPE_CHANNEL) {
            command.execute(origin, args, new ChannelCommandContext(window, commandInfo, channel));
        } else {
            super.executeCommand(origin, window, commandInfo, command, args);
        }
    }

}
