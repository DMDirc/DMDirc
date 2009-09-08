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

package com.dmdirc.actions;

import com.dmdirc.config.IdentityManager;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class ActionConditionTest {

    @BeforeClass
    public static void setUp() throws Exception {
        IdentityManager.load();
        ActionManager.init();
    }

    @Test
    public void testConstructor1() {
        final ActionCondition ac = new ActionCondition(1, CoreActionComponent.STRING_STRING,
            CoreActionComparison.STRING_STARTSWITH, "foo");
        assertEquals(1, ac.getArg());
        assertEquals(CoreActionComponent.STRING_STRING, ac.getComponent());
        assertEquals(CoreActionComparison.STRING_STARTSWITH, ac.getComparison());
        assertEquals("foo", ac.getTarget());
    }

    @Test
    public void testConstructor2() {
        final ActionCondition ac = new ActionCondition("foobarbaz",
            CoreActionComparison.STRING_STARTSWITH, "foo");
        assertEquals("foobarbaz", ac.getStarget());
        assertEquals(CoreActionComparison.STRING_STARTSWITH, ac.getComparison());
        assertEquals("foo", ac.getTarget());
    }

    @Test
    public void testTest1() {
        final ActionCondition ac = new ActionCondition(1, CoreActionComponent.STRING_STRING,
            CoreActionComparison.STRING_STARTSWITH, "foo");
        assertTrue(ac.test(new ActionSubstitutor(CoreActionType.CLIENT_USER_INPUT), null, "foo bar"));
    }

    @Test
    public void testTest2() {
        final ActionCondition ac = new ActionCondition("foobarbaz",
            CoreActionComparison.STRING_STARTSWITH, "foo");
        assertTrue(ac.test(new ActionSubstitutor(CoreActionType.CLIENT_CLOSED)));
    }

    @Test
    public void testSetters() {
        final ActionCondition ac = new ActionCondition("foobarbaz",
            CoreActionComparison.STRING_STARTSWITH, "foo");
        assertEquals("foo", ac.getTarget());
        ac.setTarget("bar");
        assertEquals("bar", ac.getTarget());

        assertEquals("foobarbaz", ac.getStarget());
        ac.setStarget("bar");
        assertEquals("bar", ac.getStarget());

        assertEquals(CoreActionComparison.STRING_STARTSWITH, ac.getComparison());
        ac.setComparison(CoreActionComparison.STRING_EQUALS);
        assertEquals(CoreActionComparison.STRING_EQUALS, ac.getComparison());

        ac.setComponent(CoreActionComponent.STRING_STRING);
        assertEquals(CoreActionComponent.STRING_STRING, ac.getComponent());

        ac.setArg(0);
        assertEquals(0, ac.getArg());
    }
    
    @Test
    public void testToString() {
        final ActionCondition ac1 = new ActionCondition("foobarbaz",
            CoreActionComparison.STRING_STARTSWITH, "foo");
        assertTrue(ac1.toString().indexOf("foo") > -1);
        assertTrue(ac1.toString().indexOf("foobarbaz") > -1);
        assertTrue(ac1.toString().indexOf(CoreActionComparison.STRING_STARTSWITH.toString()) > -1);
    }

    @Test
    public void testEquals() {
        final ActionCondition ac1 = new ActionCondition("foobarbaz",
            CoreActionComparison.STRING_STARTSWITH, "foo");
        final ActionCondition ac2 = new ActionCondition("foobarbaz",
            CoreActionComparison.STRING_STARTSWITH, "foo");

        assertTrue(ac1.equals(ac2));
        assertTrue(ac2.equals(ac1));
        assertFalse(ac1.equals(null));
        assertFalse(ac1.equals("foo"));
        assertEquals(ac1.hashCode(), ac2.hashCode());
        
        ac2.setStarget("bar");
        assertFalse(ac2.equals(ac1));
        assertFalse(ac1.equals(ac2));
        
        ac2.setStarget("foobarbaz");
        ac2.setComponent(CoreActionComponent.STRING_STRING);
        ac2.setArg(1);
        assertFalse(ac2.equals(ac1));
        assertFalse(ac1.equals(ac2));

        ac1.setComponent(CoreActionComponent.STRING_STRING);
        ac1.setArg(1);
        assertTrue(ac1.equals(ac2));
        assertTrue(ac2.equals(ac1));
        assertEquals(ac1.hashCode(), ac2.hashCode());

        ac1.setComponent(CoreActionComponent.STRING_LENGTH);
        assertFalse(ac2.equals(ac1));
        assertFalse(ac1.equals(ac2));

        ac1.setComponent(CoreActionComponent.STRING_STRING);
        ac1.setComparison(CoreActionComparison.STRING_NCONTAINS);
        assertFalse(ac2.equals(ac1));
        assertFalse(ac1.equals(ac2));

        ac1.setComparison(CoreActionComparison.STRING_STARTSWITH);
        ac1.setTarget("flub");
        assertFalse(ac2.equals(ac1));
        assertFalse(ac1.equals(ac2));
    }

}