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

package com.dmdirc.plugins.plugins.dcop;

import java.io.IOException;
import java.util.List;

import com.dmdirc.Channel;
import com.dmdirc.Server;
import com.dmdirc.commandparser.ChannelCommand;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.CommandWindow;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;

/**
 * The now playing command retrieves the currently playing song from a
 * variety of media players.
 * @author chris
 */
public final class NowPlayingCommand extends ChannelCommand {
    
    /**
     * Creates a new instance of DcopCommand.
     */
    public NowPlayingCommand() {
        super();
        
        CommandManager.registerCommand(this);
    }
    
    /**
     * Executes this command.
     * @param origin The frame in which this command was issued
     * @param server The server object that this command is associated with
     * @param channel The channel object this command is associated with
     * @param args The user supplied arguments
     */    
    public void execute(final CommandWindow origin, final Server server, 
            final Channel channel, final String... args) {
        try {
            
            final List<String> res = DcopPlugin.getDcopResult("dcop amarok default nowPlaying");
            
            channel.sendAction("is listening to " + res.get(0));
            
        } catch (IOException ex) {
            Logger.error(ErrorLevel.ERROR, "Unable to execute dcop", ex);
        }        
    }
        
    
    
    /** {@inheritDoc}. */
    public String getName() {
        return "nowplaying";
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
        return 0;
    }
    
    /** {@inheritDoc}. */
    public String getHelp() {
        return "nowplaying - tells the channel the song you're currently playing";
    }

}
