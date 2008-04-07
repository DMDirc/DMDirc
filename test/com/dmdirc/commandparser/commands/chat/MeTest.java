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
package com.dmdirc.commandparser.commands.chat;

import com.dmdirc.MessageTarget;
import com.dmdirc.Server;
import com.dmdirc.commandparser.commands.TestInputWindow;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.interfaces.InputWindow;
import org.junit.Test;
import static org.junit.Assert.*;

public class MeTest extends junit.framework.TestCase {
    
    private final Me command = new Me();

    @Test
    public void testUsage() {
        final TestInputWindow tiw = new TestInputWindow();
        command.execute(tiw, null, null, false, new String[0]);
        
        assertTrue(tiw.lines.containsKey("commandUsage"));
    }
    
    @Test
    public void testSend() {
        final MessageTargetTest mtt = new MessageTargetTest();
        command.execute(null, null, mtt, false, new String[]{"hello", "meep", "moop"});
        assertEquals("hello meep moop", mtt.action);
    }
    
    private class MessageTargetTest extends MessageTarget {
        
        public String action;

        public MessageTargetTest() {
            super("", IdentityManager.getGlobalConfig());
        }
        
        @Override
        public void sendAction(String action) {
            this.action = action;
        }

        @Override
        public void sendLine(String line) {
            // Do nothing
        }

        @Override
        public InputWindow getFrame() {
            return null;
        }

        @Override
        public int getMaxLineLength() {
            return 0;
        }

        @Override
        public String toString() {
            return null;
        }

        @Override
        public Server getServer() {
            return null;
        }

        @Override
        public void windowClosing() {
            // Do nothing
        }
        
    }
}