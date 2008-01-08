/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.commandparser.commands.ChannelCommand;
import com.dmdirc.commandparser.commands.ChatCommand;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.GlobalCommand;
import com.dmdirc.commandparser.commands.QueryCommand;
import com.dmdirc.commandparser.commands.ServerCommand;

/**
 * Defines the possible targets for commands.
 * 
 * @author chris
 */
public enum CommandType {
    
    /** A global command, which may be executed anywhere. */
    TYPE_GLOBAL,
    /** A server command, which only makes sense in the context of a connection. */
    TYPE_SERVER,
    /** A chat command, which needs a MessageTarget to make sense. */
    TYPE_CHAT,
    /** A channel command. */
    TYPE_CHANNEL,
    /** A query command. */
    TYPE_QUERY;
   
    /**
     * Looks up the command type for the specified command, by inspecting its
     * class.
     * 
     * @param command The command to look up
     * @return The type of the specified command
     */
    public static CommandType fromCommand(final Command command) {
        if (command instanceof GlobalCommand) {
            return TYPE_GLOBAL;
        } else if (command instanceof ServerCommand) {
            return TYPE_SERVER;
        } else if (command instanceof ChatCommand) {
            return TYPE_CHAT;
        } else if (command instanceof ChannelCommand) {
            return TYPE_CHANNEL;
        } else if (command instanceof QueryCommand) {
            return TYPE_QUERY;
        } else {
            return null;
        }
    }

}
