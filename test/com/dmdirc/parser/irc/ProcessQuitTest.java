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
import com.dmdirc.harness.parser.TestIQuit;
import com.dmdirc.parser.irc.callbacks.CallbackNotFoundException;

import com.dmdirc.parser.irc.callbacks.interfaces.IChannelQuit;
import com.dmdirc.parser.irc.callbacks.interfaces.IQuit;
import org.junit.Test;
import static org.junit.Assert.*;

public class ProcessQuitTest {
    
    @Test
    public void testChannelQuit() throws CallbackNotFoundException {
        final TestParser parser = new TestParser();

        parser.injectConnectionStrings();

        parser.injectLine(":nick JOIN #DMDirc_testing");
        parser.injectLine(":server 353 nick = #DMDirc_testing :@nick +luser");
        parser.injectLine(":server 366 nick #DMDirc_testing :End of /NAMES list.");
        parser.injectLine(":nick JOIN #DMDirc_testing2");
        parser.injectLine(":server 353 nick = #DMDirc_testing2 :@nick +luser2");
        parser.injectLine(":server 366 nick #DMDirc_testing2 :End of /NAMES list.");        
        
        final TestIQuit test = new TestIQuit();
        parser.getCallbackManager().addCallback("OnChannelQuit", test);
        
        assertEquals(2, parser.getChannelInfo("#DMDirc_testing").getChannelClients().size());
        
        parser.injectLine(":luser!foo@barsville QUIT :Bye bye, cruel world");
        
        assertEquals(1, parser.getChannelInfo("#DMDirc_testing").getChannelClients().size());
        assertEquals(2, parser.getChannelInfo("#DMDirc_testing2").getChannelClients().size());
        
        assertNotNull(test.channel);
        assertNotNull(test.cclient);
        assertNotNull(test.reason);
        
        assertEquals(1, test.count);
        assertEquals("#DMDirc_testing", test.channel.getName());
        assertEquals("luser", test.cclient.getClient().getNickname());
        assertEquals("Bye bye, cruel world", test.reason);
    }
    
    @Test
    public void testGlobalQuit() throws CallbackNotFoundException {
        final TestParser parser = new TestParser();

        parser.injectConnectionStrings();

        parser.injectLine(":nick JOIN #DMDirc_testing");
        parser.injectLine(":server 353 nick = #DMDirc_testing :@nick +luser");
        parser.injectLine(":server 366 nick #DMDirc_testing :End of /NAMES list.");
        parser.injectLine(":nick JOIN #DMDirc_testing2");
        parser.injectLine(":server 353 nick = #DMDirc_testing2 :@nick +luser2");
        parser.injectLine(":server 366 nick #DMDirc_testing2 :End of /NAMES list.");
        
        final TestIQuit test = new TestIQuit();
        parser.getCallbackManager().addCallback("OnQuit", test);
        
        assertEquals(2, parser.getChannelInfo("#DMDirc_testing").getChannelClients().size());
        
        parser.injectLine(":luser!foo@barsville QUIT :Bye bye, cruel world");
        
        assertEquals(1, parser.getChannelInfo("#DMDirc_testing").getChannelClients().size());
        assertEquals(2, parser.getChannelInfo("#DMDirc_testing2").getChannelClients().size());
        
        assertNotNull(test.client);
        assertNotNull(test.reason);
        
        assertEquals(1, test.count);
        assertEquals("luser", test.client.getNickname());
        assertEquals("Bye bye, cruel world", test.reason);
    }
    
    @Test
    public void testEmptyQuit() throws CallbackNotFoundException {
        final TestParser parser = new TestParser();

        parser.injectConnectionStrings();

        parser.injectLine(":nick JOIN #DMDirc_testing");
        parser.injectLine(":server 353 nick = #DMDirc_testing :@nick +luser");
        parser.injectLine(":server 366 nick #DMDirc_testing :End of /NAMES list.");
        
        final TestIQuit test = new TestIQuit();
        parser.getCallbackManager().addCallback("OnQuit", test);
        
        assertEquals(2, parser.getChannelInfo("#DMDirc_testing").getChannelClients().size());
        
        parser.injectLine(":luser!foo@barsville QUIT");
        
        assertEquals(1, parser.getChannelInfo("#DMDirc_testing").getChannelClients().size());
        
        assertNotNull(test.client);
        assertNotNull(test.reason);
        
        assertEquals(1, test.count);
        assertEquals("luser", test.client.getNickname());
        assertEquals("", test.reason);
    }    

}
