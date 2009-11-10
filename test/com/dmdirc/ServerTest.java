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

package com.dmdirc;

import com.dmdirc.config.IdentityManager;
import com.dmdirc.addons.ui_dummy.DummyController;

import java.net.URI;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class ServerTest {

    private static Server server;

    @BeforeClass
    public static void setUp() throws Exception {
        Main.setUI(new DummyController());
        IdentityManager.load();
        server = new Server(new URI("irc-test://255.255.255.255"),
                IdentityManager.getProfiles().get(0));
        server.connect();
    }

    @Test
    public void testGetNetworkFromServerName() {
        final String[][] tests = {
            {"foo.com", "foo.com"},
            {"bar.foo.com", "foo.com"},
            {"irc.us.foo.com", "foo.com"},
            {"irc.foo.co.uk", "foo.co.uk"},
            {"com", "com"},
            {"localhost", "localhost"},
            {"foo.de", "foo.de"}
        };

        for (String[] test : tests) {
            assertEquals(test[1], Server.getNetworkFromServerName(test[0]));
        }
    }

    @Test
    public void testDuplicateInviteRemoval() {
        server.disconnect();
        
        server.addInvite(new Invite(server, "#chan1", "a!b@c"));
        server.addInvite(new Invite(server, "#chan1", "d!e@f"));

        assertEquals(1, server.getInvites().size());
        //assertEquals("d", server.getInvites().get(0).getSource()[0]);
        server.removeInvites("#chan1");
    }

    @Test
    public void testRemoveInvites() {
        server.disconnect();
        
        server.addInvite(new Invite(server, "#chan1", "a!b@c"));
        server.addInvite(new Invite(server, "#chan2", "d!e@f"));

        server.removeInvites("#chan1");
        assertEquals(1, server.getInvites().size());

        server.removeInvites("#chan2");
        assertEquals(0, server.getInvites().size());
    }

    @Test
    public void testRemoveInvitesOnSocketClosed() {
        server.reconnect();
        server.addInvite(new Invite(server, "#chan1", "a!b@c"));
        server.onSocketClosed();
        assertEquals(0, server.getInvites().size());
    }
    
   /* @Test
    public void testNumericActions() throws InterruptedException {
        final TestActionListener tal = new TestActionListener();
        
        server.disconnect();
        
        ActionManager.init();
        ActionManager.addListener(tal, CoreActionType.SERVER_NUMERIC);
        
        server.reconnect();
        
        Thread.sleep(1000); // Give the parser thread time to run + inject
        
        assertEquals(1, tal.events.keySet().size());
        assertTrue(tal.events.containsKey(CoreActionType.SERVER_NUMERIC));
        
        final int[] counts = new int[6];
        
        for (Object[] args : tal.events.values(CoreActionType.SERVER_NUMERIC)) {
            counts[(Integer) args[1]]++;
            assertSame("Server arg for numeric " + args[1] + " should be same. "
                    + "Actual: " + args[0].hashCode() + " Expected: " + server.hashCode(),
                    server, args[0]);
        }
        
        assertEquals(1, counts[1]);
        assertEquals(1, counts[2]);
        assertEquals(1, counts[3]);
        assertEquals(1, counts[4]);
        assertEquals(2, counts[5]);
        
        server.disconnect();
    }*/

}
