/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.config;

import org.junit.Test;
import static org.junit.Assert.*;

public class ConfigTargetTest {

    @Test
    public void testDefault() {
        final ConfigTarget target = new ConfigTarget();

        assertEquals(target.getType(), ConfigTarget.TYPE.GLOBAL);
        assertTrue(target.toString().toLowerCase().matches("global.*config"));
    }

    @Test
    public void testSetGlobal() {
        final ConfigTarget target = new ConfigTarget();
        target.setGlobal();

        assertEquals(target.getType(), ConfigTarget.TYPE.GLOBAL);
        assertEquals(target.getTypeName(), "global");
        assertTrue(target.toString().toLowerCase().matches("global.*config"));
    }

    @Test
    public void testSetGlobalDefault() {
        final ConfigTarget target = new ConfigTarget();
        target.setGlobalDefault();

        assertEquals(target.getType(), ConfigTarget.TYPE.GLOBALDEFAULT);
        assertEquals(target.getTypeName(), "globaldefault");
        assertTrue(target.toString().toLowerCase().matches("global.*defaults"));
    }

    @Test
    public void testSetTheme() {
        final ConfigTarget target = new ConfigTarget();
        target.setTheme();

        assertEquals(target.getType(), ConfigTarget.TYPE.THEME);
        assertEquals(target.getTypeName(), "theme");
        assertTrue(target.toString().toLowerCase().matches("theme"));
    }

    @Test
    public void testSetProfile() {
        final ConfigTarget target = new ConfigTarget();
        target.setProfile();

        assertEquals(target.getType(), ConfigTarget.TYPE.PROFILE);
        assertEquals(target.getTypeName(), "profile");
        assertTrue(target.toString().toLowerCase().matches("profile"));
    }

    @Test
    public void testSetIrcd() {
        final ConfigTarget target = new ConfigTarget();
        target.setIrcd("ircd_name");

        assertEquals(target.getType(), ConfigTarget.TYPE.IRCD);
        assertEquals(target.getTypeName(), "ircd");
        assertEquals(target.getData(), "ircd_name");
        assertTrue(target.toString().toLowerCase().matches("ircd.*: ircd_name"));
    }

    @Test
    public void testSetNetwork() {
        final ConfigTarget target = new ConfigTarget();
        target.setNetwork("net_name");

        assertEquals(target.getType(), ConfigTarget.TYPE.NETWORK);
        assertEquals(target.getTypeName(), "network");
        assertEquals(target.getData(), "net_name");
        assertTrue(target.toString().toLowerCase().matches("network.*: net_name"));
    }

    @Test
    public void testSetServer() {
        final ConfigTarget target = new ConfigTarget();
        target.setServer("server_name");

        assertEquals(target.getType(), ConfigTarget.TYPE.SERVER);
        assertEquals(target.getTypeName(), "server");
        assertEquals(target.getData(), "server_name");
        assertTrue(target.toString().toLowerCase().matches("server.*: server_name"));
    }

    @Test
    public void testSetChannel() {
        final ConfigTarget target = new ConfigTarget();
        target.setChannel("channel_name");

        assertEquals(target.getType(), ConfigTarget.TYPE.CHANNEL);
        assertEquals(target.getTypeName(), "channel");
        assertEquals(target.getData(), "channel_name");
        assertTrue(target.toString().toLowerCase().matches("channel.*: channel_name"));
    }

    @Test
    public void testEquals() {
        final ConfigTarget t1 = new ConfigTarget();
        final ConfigTarget t2 = new ConfigTarget();
        final ConfigTarget t3 = new ConfigTarget();
        final ConfigTarget t4 = new ConfigTarget();
        final ConfigTarget t5 = new ConfigTarget();

        t1.setChannel("abc");
        t2.setChannel("def");
        t3.setServer("alpha");
        t4.setServer("abc");
        t5.setChannel("abc");

        assertTrue(t1.equals(t1));
        assertFalse(t1.equals(t2));
        assertFalse(t1.equals(t3));
        assertFalse(t1.equals(t4));
        assertTrue(t1.equals(t5));

        assertFalse(t2.equals(t1));
        assertTrue(t2.equals(t2));
        assertFalse(t2.equals(t3));
        assertFalse(t2.equals(t4));
        assertFalse(t2.equals(t5));

        assertFalse(t3.equals(t1));
        assertFalse(t3.equals(t2));
        assertTrue(t3.equals(t3));
        assertFalse(t3.equals(t4));
        assertFalse(t3.equals(t5));

        assertFalse(t4.equals(t1));
        assertFalse(t4.equals(t2));
        assertFalse(t4.equals(t3));
        assertTrue(t4.equals(t4));
        assertFalse(t4.equals(t5));

        assertTrue(t5.equals(t1));
        assertFalse(t5.equals(t2));
        assertFalse(t5.equals(t3));
        assertFalse(t5.equals(t4));
        assertTrue(t5.equals(t5));
        
        assertEquals(t1.hashCode(), t5.hashCode());
    }

}
