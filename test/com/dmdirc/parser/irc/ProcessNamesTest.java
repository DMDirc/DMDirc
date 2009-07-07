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

package com.dmdirc.parser.irc;

import com.dmdirc.harness.parser.TestParser;
import com.dmdirc.harness.parser.TestIErrorInfo;
import com.dmdirc.parser.irc.callbacks.CallbackNotFoundException;
import com.dmdirc.parser.interfaces.callbacks.ErrorInfoListener;
import org.junit.Test;
import static org.junit.Assert.*;

public class ProcessNamesTest {
    
    @Test
    public void testExternalNames() throws CallbackNotFoundException {
        final TestParser parser = new TestParser();
        final TestIErrorInfo test = new TestIErrorInfo();
        parser.injectConnectionStrings();
        parser.getCallbackManager().addCallback(ErrorInfoListener.class, test);
        
        parser.injectLine(":server 366 nick #nonexistant :End of /NAMES list.");
        
        assertFalse("Should not error on unknown NAMES replies", test.error);
    }
    
    @Test
    public void testChannelUmodes() {
        final TestParser parser = new TestParser();

        parser.injectConnectionStrings();
        parser.injectLine(":nick JOIN #DMDirc_testing");
        parser.injectLine(":server 353 nick = #DMDirc_testing :@nick +luser @+nick2 nick3");
        parser.injectLine(":server 366 nick #DMDirc_testing :End of /NAMES list");

        assertEquals(1, parser.getChannels().size());
        assertNotNull(parser.getChannel("#DMDirc_testing"));
        assertEquals(4, parser.getChannel("#DMDirc_testing").getChannelClients().size());
        assertNotNull(parser.getClient("luser"));
        assertEquals(1, parser.getClient("luser").getChannelClients().size());

        IRCChannelClientInfo cci = parser.getClient("luser").getChannelClients().get(0);
        assertEquals(parser.getChannel("#DMDirc_testing"), cci.getChannel());
        assertEquals("+", cci.getChanModeStr(true));
        
        cci = parser.getChannel("#DMDirc_testing").getChannelClient("nick2");
        assertNotNull(cci);
        assertEquals("@+", cci.getChanModeStr(true));

        cci = parser.getChannel("#DMDirc_testing").getChannelClient("nick3");
        assertNotNull(cci);
        assertEquals("", cci.getChanModeStr(true));
    }

}
