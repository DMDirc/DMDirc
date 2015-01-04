/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

import com.dmdirc.interfaces.InviteManager;
import com.dmdirc.interfaces.User;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class InviteTest {

    @Mock
    private InviteManager inviteManager;
    @Mock
    private User user;
    private Invite invite;
    private long ts;

    @Before
    public void setUp() {
        invite = new Invite(inviteManager, "#channel", user);
        ts = new Date().getTime();
    }

    @Test
    public void testGetChannel() {
        assertEquals("#channel", invite.getChannel());
    }

    @Test
    public void testGetTimestamp() {
        assertTrue(invite.getTimestamp() - ts < 10000);
        assertTrue(invite.getTimestamp() - ts > -10000);
    }

    @Test
    public void testGetSource() {
        assertEquals(user, invite.getSource());
    }

    @Test
    public void testAccept() {
        invite.accept();
        verify(inviteManager).acceptInvites(invite);
    }

    @Test
    public void testDecline() {
        invite.decline();
        verify(inviteManager).removeInvite(invite);
    }

}
