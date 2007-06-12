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

/**
 * The benchmark command allows us to fake a stream of channel messages for
 * benchmarking purposes.
 * @author chris
 */
public final class Benchmark extends ChannelCommand {
    
    /** Creates a new instance of Benchmark. */
    public Benchmark() {
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
        final String[] strings = {
            "this is a test",
            "test number two",
            "this is a line that is significantly longer than the other lines",
            "this is a line containing bold, underline, and other stuff",
            "here are some 4colours FFFF00for you!",
            "some more test data",
        };
        
        for (int i = 0; i < 200; i++) {            
            for (int j = 0; j < strings.length; j++) {
                channel.onChannelMessage(server.getParser(),
                        channel.getChannelInfo(),
                        channel.getChannelInfo().getUser(server.getParser().getMyself()),
                        strings[j], "benchmarker!dmdirc@dmdirc.com");
            }
        }
    }
    
    /** {@inheritDoc}. */
    public String getName() {
        return "benchmark";
    }
    
    /** {@inheritDoc}. */
    public boolean showInHelp() {
        return false;
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
        return null;
    }
}
