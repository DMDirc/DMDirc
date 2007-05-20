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

package uk.org.ownage.dmdirc.commandparser.commands.channel;

import uk.org.ownage.dmdirc.Channel;
import uk.org.ownage.dmdirc.Server;
import uk.org.ownage.dmdirc.commandparser.ChannelCommand;
import uk.org.ownage.dmdirc.commandparser.CommandManager;
import uk.org.ownage.dmdirc.commandparser.CommandWindow;

/**
 * The kick command kicks a specified user from the channel.
 * @author chris
 */
public final class Kick extends ChannelCommand {
    
    /** Creates a new instance of Kick. */
    public Kick() {
        super();
        
        CommandManager.registerCommand(this);
        CommandManager.registerPopupCommand(this);
    }
    
    /**
     * Executes this command.
     * @param origin The frame in which this command was issued
     * @param server The server object that this command is associated with
     * @param channel The channel object that this command is associated with
     * @param args The user supplied arguments
     */
    public void execute(final CommandWindow origin, final Server server,
            final Channel channel, final String... args) {
        final String[] newArgs = new String[2];
        newArgs[0] = args[0]; // Nickname
        newArgs[1] = origin.getConfigManager().getOption("general", "kickmessage");
        
        CommandManager.getChannelCommand("kick").execute(origin, server, channel, newArgs);
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
        return false;
    }
    
    /** {@inheritDoc}. */
    public int getArity() {
        return 1;
    }
    
    /** {@inheritDoc}. */
    public String getHelp() {
        return "kick <user> - kicks the specified user from the channel";
    }
    
}
