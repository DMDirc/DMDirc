/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
import com.dmdirc.MessageTarget;
import com.dmdirc.Query;
import com.dmdirc.Server;
import com.dmdirc.commandparser.commands.ChannelCommand;
import com.dmdirc.commandparser.commands.ChatCommand;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.GlobalCommand;
import com.dmdirc.commandparser.commands.QueryCommand;
import com.dmdirc.commandparser.commands.ServerCommand;
import com.dmdirc.ui.interfaces.InputWindow;
import org.junit.Test;
import static org.junit.Assert.*;

public class CommandTypeTest {

    @Test
    public void testGlobal() {
        final Command command = new GlobalCommand() {

            @Override
            public void execute(InputWindow origin, boolean isSilent, CommandArguments args) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public String getName() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean showInHelp() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public String getHelp() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        
        assertEquals(CommandType.TYPE_GLOBAL, CommandType.fromCommand(command));
    }
    
    @Test
    public void testServer() {
        final Command command = new ServerCommand() {

            @Override
            public void execute(InputWindow origin, Server server, boolean isSilent, CommandArguments args) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public String getName() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean showInHelp() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public String getHelp() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        
        assertEquals(CommandType.TYPE_SERVER, CommandType.fromCommand(command));
    }
    
    @Test
    public void testChat() {
        final Command command = new ChatCommand() {

            @Override
            public String getName() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean showInHelp() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public String getHelp() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void execute(InputWindow origin, Server server,
                    MessageTarget target, boolean isSilent, CommandArguments args) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        
        assertEquals(CommandType.TYPE_CHAT, CommandType.fromCommand(command));
    }    
    
    @Test
    public void testChannel() {
        final Command command = new ChannelCommand() {
            @Override
            public String getName() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean showInHelp() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public String getHelp() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void execute(InputWindow origin, Server server,
                    Channel channel, boolean isSilent, CommandArguments args) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        
        assertEquals(CommandType.TYPE_CHANNEL, CommandType.fromCommand(command));
    }
    
    @Test
    public void testQuery() {
        final Command command = new QueryCommand() {

            @Override
            public String getName() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean showInHelp() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public String getHelp() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void execute(InputWindow origin, Server server, Query query,
                    boolean isSilent, CommandArguments args) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        
        assertEquals(CommandType.TYPE_QUERY, CommandType.fromCommand(command));
    }
    
    @Test
    public void testOther() {
        assertNull(CommandType.fromCommand(null));
    }

}