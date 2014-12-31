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

package com.dmdirc.actions;

import com.dmdirc.Server;
import com.dmdirc.interfaces.actions.ActionComponent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ActionComponentChainTest {

    @Mock private ActionManager actionManager;

    @Before
    public void setUp() throws Exception {
        when(actionManager.getComponent("SERVER_NAME"))
                .thenReturn(CoreActionComponent.SERVER_NAME);
        when(actionManager.getComponent("SERVER_NETWORK"))
                .thenReturn(CoreActionComponent.SERVER_NETWORK);
        when(actionManager.getComponent("STRING_LENGTH"))
                .thenReturn(CoreActionComponent.STRING_LENGTH);
        when(actionManager.getComponent("STRING_STRING"))
                .thenReturn(CoreActionComponent.STRING_STRING);
        when(actionManager.getComponent("USER_MODES"))
                .thenReturn(CoreActionComponent.USER_MODES);

        // TODO: Testing for this behaviour here is odd - ActionComponentChain
        //       should probably do its own validation and throw a more
        //       appropriate exception.
        when(actionManager.getComponent(""))
                .thenThrow(new AssertionError());
    }

    @Test
    public void testSingle() {
        final ActionComponentChain chain = new ActionComponentChain(String.class, "STRING_STRING", actionManager);
        assertEquals("foo bar baz", chain.get("foo bar baz"));
        assertEquals("STRING_STRING", chain.toString());
    }

    @Test
    public void testDouble() {
        final ActionComponentChain chain = new ActionComponentChain(String.class,
                "STRING_STRING.STRING_STRING", actionManager);
        assertEquals("foo bar baz", chain.get("foo bar baz"));
        assertEquals("STRING_STRING.STRING_STRING", chain.toString());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidName() {
        new ActionComponentChain(String.class, "STRONG_STRING", actionManager);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidType() {
        new ActionComponentChain(String.class, "USER_MODES.STRING_STRING", actionManager);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidLink() {
        new ActionComponentChain(String.class, "STRING_STRING.USER_MODES", actionManager);
    }

    @Test
    public void testAppliesTo() {
        final ActionComponent chain = new ActionComponentChain(String.class,
                "STRING_STRING.STRING_STRING.STRING_LENGTH", actionManager);
        assertSame(String.class, chain.appliesTo());
    }

    @Test
    public void testGetType() {
        final ActionComponent chain = new ActionComponentChain(String.class,
                "STRING_STRING.STRING_STRING.STRING_LENGTH", actionManager);
        assertSame(Integer.class, chain.getType());
    }

    @Test
    public void testGetName() {
        final ActionComponent chain = new ActionComponentChain(String.class,
                "STRING_STRING.STRING_STRING.STRING_LENGTH", actionManager);

        assertTrue(chain.getName().contains(CoreActionComponent.STRING_STRING.getName()));
        assertTrue(chain.getName().contains(CoreActionComponent.STRING_LENGTH.getName()));
    }

    @Test(expected=AssertionError.class)
    public void testEmptyAppliesTo() {
        final ActionComponent chain = new ActionComponentChain(String.class, "", actionManager);
        chain.appliesTo();
    }

    @Test(expected=AssertionError.class)
    public void testEmptyGetType() {
        final ActionComponent chain = new ActionComponentChain(String.class, "", actionManager);
        chain.getType();
    }

    @Test(expected=AssertionError.class)
    public void testEmptyGetName() {
        final ActionComponent chain = new ActionComponentChain(String.class, "", actionManager);
        chain.getName();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test(expected=AssertionError.class)
    public void testEmptyToString() {
        final ActionComponentChain chain = new ActionComponentChain(String.class, "", actionManager);
        chain.toString();
    }

    @Test
    public void testRequiresConnection1() {
        final ActionComponentChain chain = new ActionComponentChain(Server.class,
                "SERVER_NETWORK", actionManager);
        assertTrue(chain.requiresConnection());
    }

    @Test
    public void testRequiresConnection2() {
        final ActionComponentChain chain = new ActionComponentChain(Server.class,
                "SERVER_NETWORK.STRING_LENGTH", actionManager);
        assertTrue(chain.requiresConnection());
    }

    @Test
    public void testRequiresConnection3() {
        final ActionComponentChain chain = new ActionComponentChain(Server.class,
                "SERVER_NAME.STRING_LENGTH", actionManager);
        assertFalse(chain.requiresConnection());
    }

}