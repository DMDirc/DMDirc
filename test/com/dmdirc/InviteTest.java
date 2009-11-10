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
import java.util.Date;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class InviteTest {
    
    private static Server server;
    private static Invite test;
    private static long ts;
    
    @BeforeClass
    public static void setUp() throws Exception {
        Main.setUI(new DummyController());
        IdentityManager.load();
        
        server = new Server(new URI("irc-test://255.255.255.255"),
                IdentityManager.getProfiles().get(0));
        server.connect();
        
        test = new Invite(server, "#channel", "nick!ident@host");
        server.addInvite(test);
        ts = new Date().getTime();
    }    

    @Test
    public void testGetServer() {
        assertEquals(server, test.getServer());
    }

    @Test
    public void testGetChannel() {
        assertEquals("#channel", test.getChannel());
    }

    @Test
    public void testGetTimestamp() {
        assertTrue(test.getTimestamp() - ts < 10000);
        assertTrue(test.getTimestamp() - ts > -10000);
    }

    @Test
    public void testGetSource() {
        assertEquals(3, test.getSource().length);
        assertEquals("nick", test.getSource()[0]);
        assertEquals("ident", test.getSource()[1]);
        assertEquals("host", test.getSource()[2]);
    }

    @Test
    public void testAccept() {
        test.accept();
        assertEquals(0, server.getInvites().size());
    }

}