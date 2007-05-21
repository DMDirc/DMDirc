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

package com.dmdirc.commandparser.commands.channel;

import com.dmdirc.Channel;
import com.dmdirc.Server;
import com.dmdirc.commandparser.ChannelCommand;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.CommandWindow;
import com.dmdirc.parser.ChannelClientInfo;

/**
 * The kick command kicks a specified user from the channel.
 * This version allows the user to specify a reason.
 * @author chris
 */
public final class KickReason extends ChannelCommand {
    
    /** Creates a new instance of KickReason. */
    public KickReason() {
        super();
        
        CommandManager.registerCommand(this);
    }
    
    /**
     * Executes this command.
     * @param origin The frame in which this command was issued
     * @param server The server object that this command is associated with
     * @param channel The channel object that this command is associated with
     * @param isSilent Whether this command is silenced or not
     * @param args The user supplied arguments
     */
    public void execute(final CommandWindow origin, final Server server,
            final Channel channel, final boolean isSilent, final String... args) {
        
        final ChannelClientInfo victim = channel.getChannelInfo().getUser(args[0]);
        
        if (victim == null) {
            origin.addLine("commandError", "User not found: " + args[0]);
        } else {
            victim.kick(implodeArgs(1, args));
        }
    }
    
    /** {@inheritDoc}. */
    public String getName() {
        return "kick";
    }
    
    /** {@inheritDoc}. */
    public boolean showInHelp() {
        return true;
    }
    
    /** {@inheritDoc}. */
    public boolean isPolyadic() {
        return true;
    }
    
    /** {@inheritDoc}. */
    public int getArity() {
        return 0;
    }
    
    /** {@inheritDoc}. */
    public String getHelp() {
        return "kick <user> <reason> - kicks the specified user from the channel";
    }
    
}
