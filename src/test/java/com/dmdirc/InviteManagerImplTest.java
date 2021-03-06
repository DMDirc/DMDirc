/*
 * Copyright (c) 2006-2017 DMDirc Developers
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

import com.dmdirc.interfaces.Connection;
import com.dmdirc.events.eventbus.EventBus;
import com.dmdirc.interfaces.User;
import com.dmdirc.interfaces.WindowModel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InviteManagerImplTest {

    @Mock private Connection connection;
    @Mock private WindowModel frameContainer;
    @Mock private EventBus eventBus;
    @Mock private User user;

    private InviteManagerImpl inviteManager;

    @Before
    public void setup() {
        inviteManager = new InviteManagerImpl(connection);

        when(connection.getWindowModel()).thenReturn(frameContainer);
        when(frameContainer.getEventBus()).thenReturn(eventBus);
    }

    @Test
    public void testDuplicateInviteRemoval() {
        inviteManager.addInvite(new Invite(inviteManager, "#chan1", user));
        inviteManager.addInvite(new Invite(inviteManager, "#chan1", user));

        assertEquals(1, inviteManager.getInvites().size());
    }

    @Test
    public void testRemoveInvites() {
        inviteManager.addInvite(new Invite(inviteManager, "#chan1", user));
        inviteManager.addInvite(new Invite(inviteManager, "#chan2", user));

        inviteManager.removeInvites("#chan1");
        assertEquals(1, inviteManager.getInvites().size());

        inviteManager.removeInvites("#chan2");
        assertEquals(0, inviteManager.getInvites().size());
    }

}