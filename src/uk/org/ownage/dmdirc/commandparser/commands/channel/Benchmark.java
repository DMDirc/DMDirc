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
import uk.org.ownage.dmdirc.commandparser.CommandWindow;

/**
 * The benchmark command allows us to fake a stream of channel messages for
 * benchmarking purposes. 
 * @author chris
 */
public final class Benchmark extends ChannelCommand {
    
    /** Creates a new instance of Benchmark. */
    public Benchmark() {
        description = "simulates a stream of channel messages";
        arguments = "";
        polyadic = false;
        arity = 0;
        name = "benchmark";
        show = false;
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
        String strings[] = {
            "this is a test",
            "test number two",
            "this is a line that is significantly longer than the other lines",
            "this is a line containing bold, underline, and other stuff",
            "here are some 4colours FFFF00for you!",
            "some more test data"
        };
        
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < strings.length; j++) {
                channel.onChannelMessage(server.getParser(), channel.getChannelInfo(), null, strings[j], "benchmarker!dmdirc@dmdirc.com");
            }
            
            for (int j = 0; j < strings.length; j++) {
                channel.onChannelMessage(server.getParser(), channel.getChannelInfo(), channel.getChannelInfo().getUser(server.getParser().getMyself()), strings[j], "benchmarker!dmdirc@dmdirc.com");
            }            
        }
    }
}
