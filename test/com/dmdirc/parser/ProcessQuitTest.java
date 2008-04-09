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

package com.dmdirc.parser;

import com.dmdirc.parser.callbacks.CallbackNotFoundException;

import com.dmdirc.parser.callbacks.interfaces.IChannelQuit;
import com.dmdirc.parser.callbacks.interfaces.IQuit;
import org.junit.Test;
import static org.junit.Assert.*;

public class ProcessQuitTest extends junit.framework.TestCase {
    
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
        
        final ICQTest test = new ICQTest();
        parser.getCallbackManager().addCallback("OnChannelQuit", test);
        
        parser.injectLine(":luser!foo@barsville QUIT :Bye bye, cruel world");
        
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
        
        final ICQTest test = new ICQTest();
        parser.getCallbackManager().addCallback("OnQuit", test);
        
        parser.injectLine(":luser!foo@barsville QUIT :Bye bye, cruel world");
        
        assertNotNull(test.client);
        assertNotNull(test.reason);
        
        assertEquals(1, test.count);
        assertEquals("luser", test.client.getNickname());
        assertEquals("Bye bye, cruel world", test.reason);
    }    
    
    private class ICQTest implements IChannelQuit, IQuit {
        
        public ChannelInfo channel;
        public ChannelClientInfo cclient;
        public ClientInfo client;
        public String reason;
        public int count = 0;

        public void onChannelQuit(IRCParser tParser, ChannelInfo cChannel,
                                  ChannelClientInfo cChannelClient,
                                  String sReason) {
            this.channel = cChannel;
            this.cclient = cChannelClient;
            this.reason = sReason;
            this.count++;
        }

        public void onQuit(IRCParser tParser, ClientInfo cClient, String sReason) {
            this.client = cClient;
            this.reason = sReason;
            this.count++;
        }
        
    }

}
