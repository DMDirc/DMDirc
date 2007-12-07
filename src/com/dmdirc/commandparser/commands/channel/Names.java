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
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.commands.ChannelCommand;
import com.dmdirc.ui.interfaces.InputWindow;

/**
 * Sends a names request.
 * 
 * @author chris
 */
public class Names extends ChannelCommand {
    
    /**
     * Creates a new instance of Names.
     */
    public Names() {
        super();
        
        CommandManager.registerCommand(this);
    }    

    /** {@inheritDoc} */
    @Override
    public void execute(final InputWindow origin, final Server server, 
            final Channel channel, final boolean isSilent, final String... args) {
        server.getParser().sendLine("NAMES " + channel.getChannelInfo().getName());
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "names";
    }

    /** {@inheritDoc} */
    @Override
    public boolean showInHelp() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String getHelp() {
        return "names - Requests a list of users that are in the channel";
    }

}
