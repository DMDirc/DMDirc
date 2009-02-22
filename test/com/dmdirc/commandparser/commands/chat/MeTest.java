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
package com.dmdirc.commandparser.commands.chat;

import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.harness.TestMessageTarget;
import com.dmdirc.harness.TestInputWindow;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class MeTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        IdentityManager.load();
    }
    
    private final Me command = new Me();

    @Test
    public void testUsage() {
        final TestInputWindow tiw = new TestInputWindow();
        command.execute(tiw, null, null, false, new CommandArguments("/foo"));
        
        assertTrue(tiw.lines.containsKey("commandUsage"));
    }
    
    @Test
    public void testSend() {
        final TestMessageTarget mtt = new TestMessageTarget();
        command.execute(null, null, mtt, false, new CommandArguments("/foo hello meep moop"));
        assertEquals("hello meep moop", mtt.action);
    }
}