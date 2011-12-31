/*
 * Copyright (c) 2006-2011 DMDirc Developers
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

import java.util.Date;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class InviteTest {

    private static Server server;
    private static Invite test;
    private static long ts;

    @BeforeClass
    public static void setUp() throws Exception {
        server = mock(Server.class);

        when(server.parseHostmask("nick!ident@host"))
                .thenReturn(new String[] {"nick", "ident", "host"});

        test = new Invite(server, "#channel", "nick!ident@host");
        ts = new Date().getTime();
    }

    @Test
    public void testGetServer() {
        assertSame(server, test.getServer());
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
        verify(server).acceptInvites(test);
    }

    @Test
    public void testDecline() {
        test.decline();
        verify(server).removeInvite(test);
    }

}