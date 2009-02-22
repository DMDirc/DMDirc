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
import com.dmdirc.harness.parser.TestINickChanged;
import com.dmdirc.parser.irc.callbacks.CallbackNotFoundException;
import org.junit.Test;
import static org.junit.Assert.*;

public class ProcessNickTest {
    
    @Test
    public void testNickSameName() {
        final TestParser parser = new TestParser();
        final TestINickChanged tinc = new TestINickChanged();

        parser.getCallbackManager().addCallback("OnNickChanged", tinc);
        
        parser.injectConnectionStrings();
        parser.injectLine(":nick JOIN #DMDirc_testing");
        parser.injectLine(":nick JOIN #DMDirc_testing2");
        parser.injectLine(":server 353 nick = #DMDirc_testing :@nick +luser @+nick2 nick3");
        parser.injectLine(":server 366 nick #DMDirc_testing :End of /NAMES list");
        parser.injectLine(":luser!lu@ser.com NICK LUSER");

        assertNotNull(parser.getClientInfo("LUSER"));
        assertEquals(1, parser.getClientInfo("LUSER").getChannelClients().size());

        ChannelClientInfo cci = parser.getClientInfo("LUSER").getChannelClients().get(0);
        assertEquals(parser.getChannelInfo("#DMDirc_testing"), cci.getChannel());
        assertEquals("+", cci.getChanModeStr(true));
        
        assertSame(cci.getClient(), tinc.client);
        assertEquals("luser", tinc.oldNick);
    }
    
    @Test
    public void testNickDifferent() {
        final TestParser parser = new TestParser();

        parser.injectConnectionStrings();
        parser.injectLine(":nick JOIN #DMDirc_testing");
        parser.injectLine(":server 353 nick = #DMDirc_testing :@nick +luser @+nick2 nick3");
        parser.injectLine(":server 366 nick #DMDirc_testing :End of /NAMES list");
        parser.injectLine(":luser!lu@ser.com NICK foobar");

        assertNotNull(parser.getClientInfo("foobar"));
        assertNull(parser.getClientInfo("luser"));
        assertEquals(1, parser.getClientInfo("foobar").getChannelClients().size());

        ChannelClientInfo cci = parser.getClientInfo("foobar").getChannelClients().get(0);
        assertEquals(parser.getChannelInfo("#DMDirc_testing"), cci.getChannel());
        assertEquals("+", cci.getChanModeStr(true));
    }    
    
    @Test
    public void testOverrideNick() throws CallbackNotFoundException {
        final TestParser parser = new TestParser();
        final TestIErrorInfo info = new TestIErrorInfo();
        
        parser.getCallbackManager().addCallback("OnErrorInfo", info);
        parser.injectConnectionStrings();
        parser.injectLine(":nick JOIN #DMDirc_testing");
        parser.injectLine(":server 353 nick = #DMDirc_testing :@nick +luser @+nick2 nick3");
        parser.injectLine(":server 366 nick #DMDirc_testing :End of /NAMES list");
        parser.injectLine(":luser!lu@ser.com NICK nick3");

        assertTrue("Parser should raise an error if a nick change overrides an "
                + "existing client", info.error);
    }
    
    @Test
    public void testUnknownNick() {
        final TestParser parser = new TestParser();
        final TestINickChanged tinc = new TestINickChanged();
        
        parser.getCallbackManager().addCallback("OnNickChanged", tinc);
        
        parser.injectConnectionStrings();
        parser.injectLine(":random!lu@ser NICK rand");
        
        assertNull(tinc.client);
        assertNull(tinc.oldNick);
    }

}
