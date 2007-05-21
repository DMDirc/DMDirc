/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.commandparser;

import com.dmdirc.Channel;
import com.dmdirc.Server;

/**
 * A command parser that is tailored for use in a channel environment. Handles
 * both channel and server commands.
 * @author chris
 */
public final class ChannelCommandParser extends CommandParser {
    
    /**
     * The server instance that this parser is attached to.
     */
    private final Server server;
    /**
     * The channel instance that this parser is attached to.
     */
    private final Channel channel;
    
    /**
     * Creates a new instance of ChannelCommandParser.
     * @param newServer The server instance that this parser is attached to
     * @param newChannel The channel instance that this parser is attached to
     */
    public ChannelCommandParser(final Server newServer, final Channel newChannel) {
        super();
        
        this.server = newServer;
        this.channel = newChannel;
    }
    
    /** Loads the relevant commands into the parser. */
    protected void loadCommands() {
        CommandManager.loadGlobalCommands(this);
        CommandManager.loadServerCommands(this);
        CommandManager.loadChannelCommands(this);
    }
    
    /** {@inheritDoc} */
    protected void executeCommand(final CommandWindow origin,
            final boolean isSilent, final Command command, final String... args) {
        if (command instanceof ChannelCommand) {
            ((ChannelCommand) command).execute(origin, server, channel, isSilent, args);
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
    protected void handleNonCommand(final CommandWindow origin, final String line) {
        channel.sendLine(line);
    }
    
}
