/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.ChannelCommand;
import com.dmdirc.commandparser.commands.ChatCommand;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.GlobalCommand;
import com.dmdirc.commandparser.commands.ServerCommand;

/**
 * A command parser that is tailored for use in a channel environment. Handles
 * both channel and server commands.
 * @author chris
 */
public final class ChannelCommandParser extends CommandParser {
    
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
     */
    public ChannelCommandParser() {
        super();
    }

    /** {@inheritDoc} */
    @Override
    public void setOwner(final FrameContainer owner) {
        channel = (Channel) owner;
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
            final boolean isSilent, final Command command, final CommandArguments args) {
        if (command instanceof ChannelCommand) {
            ((ChannelCommand) command).execute(origin, channel.getServer(), channel, isSilent, args);
        } else if (command instanceof ChatCommand) {
            ((ChatCommand) command).execute(origin, channel.getServer(), channel, isSilent, args);
        } else if (command instanceof ServerCommand) {
            ((ServerCommand) command).execute(origin, channel.getServer(), isSilent, args);
        } else {
            ((GlobalCommand) command).execute(origin, isSilent, args);
        }
    }
    
    /**
     * Called when the input was a line of text that was not a command. This normally
     * means it is sent to the server/channel/user as-is, with no further processing.
     *
     * @param origin The window in which the command was typed
     * @param line The line input by the user
     */
    @Override
    protected void handleNonCommand(final FrameContainer origin, final String line) {
        channel.sendLine(line);
    }
    
}
