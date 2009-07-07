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

import com.dmdirc.harness.parser.TestIChannelSelfJoin;
import com.dmdirc.harness.parser.TestParser;
import com.dmdirc.parser.irc.callbacks.CallbackNotFoundException;
import com.dmdirc.parser.interfaces.callbacks.ChannelJoinListener;

import com.dmdirc.parser.interfaces.callbacks.ChannelSelfJoinListener;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ProcessJoinTest {
    
    @Test
    public void testSelfJoinChannel() throws CallbackNotFoundException {
        final TestParser parser = new TestParser();
        final TestIChannelSelfJoin test = new TestIChannelSelfJoin();

        parser.injectConnectionStrings();
        parser.getCallbackManager().addCallback(ChannelSelfJoinListener.class, test);
        parser.injectLine(":nick JOIN #DMDirc_testing");

        assertNotNull(test.channel);
        assertEquals("#DMDirc_testing", test.channel.getName());
        assertEquals("#DMDirc_testing", test.channel.toString());
        assertSame(parser, test.channel.getParser());
        assertEquals(1, parser.getChannels().size());
        assertTrue(parser.getChannels().contains(test.channel));
        assertEquals(test.channel, parser.getChannel("#DMDirc_testing"));
    }
    
    @Test
    public void testOtherJoinChannel() throws CallbackNotFoundException {
        final TestParser parser = new TestParser();
        final ChannelJoinListener test = mock(ChannelJoinListener.class);

        parser.injectConnectionStrings();
        parser.getCallbackManager().addCallback(ChannelJoinListener.class, test);
        
        parser.injectLine(":nick JOIN #DMDirc_testing");
        parser.injectLine(":foo!bar@baz JOIN #DMDirc_testing");

        verify(test).onChannelJoin(parser, parser.getChannel("#DMDirc_testing"),
                parser.getClientInfo("foo!bar@baz").getChannelClients().get(0));
    }    

}
