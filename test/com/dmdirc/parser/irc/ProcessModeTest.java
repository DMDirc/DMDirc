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
import com.dmdirc.parser.irc.callbacks.CallbackNotFoundException;

import org.junit.Test;
import static org.junit.Assert.*;

public class ProcessModeTest {
    
    @Test
    public void testBasicUmodes() throws CallbackNotFoundException {
        final TestParser parser = new TestParser();

        parser.injectConnectionStrings();

        parser.injectLine(":server 221 nick iw");

        assertTrue(parser.getMyself().getUserModeStr().indexOf('i') > -1);
        assertTrue(parser.getMyself().getUserModeStr().indexOf('w') > -1);
    }
    
    @Test
    public void testAlteringUmodes() throws CallbackNotFoundException {
        final TestParser parser = new TestParser();

        parser.injectConnectionStrings();

        parser.injectLine(":server 221 nick iw");
        parser.injectLine(":server MODE nick :-iw+ox");

        assertTrue(parser.getMyself().getUserModeStr().indexOf('o') > -1);
        assertTrue(parser.getMyself().getUserModeStr().indexOf('x') > -1);
        assertEquals(-1, parser.getMyself().getUserModeStr().indexOf('i'));
        assertEquals(-1, parser.getMyself().getUserModeStr().indexOf('w'));
    }
    
    @Test
    public void testChannelUmodes() {
        final TestParser parser = new TestParser();

        parser.injectConnectionStrings();
        parser.injectLine(":nick JOIN #DMDirc_testing");
        parser.injectLine(":server 353 nick = #DMDirc_testing :@nick +luser");
        parser.injectLine(":server 366 nick #DMDirc_testing :End of /NAMES list");

        final ChannelClientInfo cci = parser.getClientInfo("luser").getChannelClients().get(0);

        parser.injectLine(":server MODE #DMDirc_testing +v luser");
        assertEquals("+", cci.getChanModeStr(true));

        parser.injectLine(":server MODE #DMDirc_testing +o luser");
        assertEquals("ov", cci.getChanModeStr(false));
        assertEquals("@+", cci.getChanModeStr(true));

        parser.injectLine(":server MODE #DMDirc_testing +bov moo luser luser");
        assertEquals("ov", cci.getChanModeStr(false));

        parser.injectLine(":server MODE #DMDirc_testing -bov moo luser luser");
        assertEquals("", cci.getChanModeStr(false));
        assertEquals("", cci.getChanModeStr(true));
    }
    
    @Test
    public void testUnknownUser1() {
        final TestParser parser = new TestParser();

        parser.injectConnectionStrings();
        parser.injectLine(":nick JOIN #DMDirc_testing");
        parser.injectLine(":server 353 nick = #DMDirc_testing :@nick +luser");
        parser.injectLine(":server 366 nick #DMDirc_testing :End of /NAMES list");

        parser.injectLine(":luser!me@my MODE #DMDirc_testing +v :moo");
        
        assertNotNull(parser.getClientInfo("moo"));
        assertEquals(1, parser.getClientInfo("moo").channelCount());
        
        final ChannelClientInfo cci = parser.getClientInfo("moo").getChannelClients().get(0);
        
        assertEquals("+", cci.getChanModeStr(true));        
        assertEquals("Parser should update ident when it sees a MODE line",
                "me", parser.getClientInfo("luser").getIdent());
        assertEquals("Parser should update host when it sees a MODE line",
                "my", parser.getClientInfo("luser").getHost());
    }
    
    @Test
    public void testUnknownUser2() {
        final TestParser parser = new TestParser();

        parser.injectConnectionStrings();
        parser.injectLine(":nick JOIN #DMDirc_testing");
        parser.injectLine(":server 353 nick = #DMDirc_testing :@nick +luser");
        parser.injectLine(":server 366 nick #DMDirc_testing :End of /NAMES list");
        parser.injectLine(":nick JOIN #DMDirc_testing2");
        parser.injectLine(":server 353 nick = #DMDirc_testing2 :@nick +moo");
        parser.injectLine(":server 366 nick #DMDirc_testing2 :End of /NAMES list");        

        parser.injectLine(":server MODE #DMDirc_testing +v moo");
        
        assertNotNull(parser.getClientInfo("moo"));
        assertEquals(2, parser.getClientInfo("moo").channelCount());
        
        final ChannelClientInfo cci = parser.getClientInfo("moo").getChannelClients().get(0);
        
        assertEquals("+", cci.getChanModeStr(true));        
    }   
    
    @Test
    public void testChannelModes() {
        final TestParser parser = new TestParser();

        parser.injectConnectionStrings();
        parser.injectLine(":nick JOIN #DMDirc_testing");
        parser.injectLine(":server 353 nick = #DMDirc_testing :@nick +luser");
        parser.injectLine(":server 366 nick #DMDirc_testing :End of /NAMES list");
        parser.injectLine(":server 324 nick #DMDirc_testing +Zstnl 1234");

        assertEquals("1234", parser.getChannelInfo("#DMDirc_testing").getModeParam('l'));
        
        String modes = parser.getChannelInfo("#DMDirc_testing").getModeStr().split(" ")[0];
        assertEquals(6, modes.length());
        assertEquals('+', modes.charAt(0));
        assertTrue(modes.indexOf('Z') > -1);
        assertTrue(modes.indexOf('s') > -1);
        assertTrue(modes.indexOf('t') > -1);
        assertTrue(modes.indexOf('n') > -1);
        assertTrue(modes.indexOf('l') > -1);
        
        parser.injectLine(":server MODE #DMDirc_testing :-Z");
        
        modes = parser.getChannelInfo("#DMDirc_testing").getModeStr().split(" ")[0];
        assertEquals(5, modes.length());
        assertEquals('+', modes.charAt(0));
        assertTrue(modes.indexOf('s') > -1);
        assertTrue(modes.indexOf('t') > -1);
        assertTrue(modes.indexOf('n') > -1);
        assertTrue(modes.indexOf('l') > -1);        
    }

}
