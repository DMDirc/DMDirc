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

package com.dmdirc.config;

import org.junit.Test;

import static org.junit.Assert.*;

public class ConfigTargetTest {

    @Test
    public void testDefault() {
        final ConfigTarget target = new ConfigTarget();

        assertEquals(ConfigTarget.TYPE.GLOBAL, target.getType());
        assertTrue(target.toString().toLowerCase().matches("global.*config"));
    }

    @Test
    public void testSetGlobal() {
        final ConfigTarget target = new ConfigTarget();
        target.setGlobal();

        assertEquals(ConfigTarget.TYPE.GLOBAL, target.getType());
        assertEquals("global", target.getTypeName());
        assertTrue(target.toString().toLowerCase().matches("global.*config"));
    }

    @Test
    public void testSetGlobalDefault() {
        final ConfigTarget target = new ConfigTarget();
        target.setGlobalDefault();

        assertEquals(ConfigTarget.TYPE.GLOBALDEFAULT, target.getType());
        assertEquals("globaldefault", target.getTypeName());
        assertTrue(target.toString().toLowerCase().matches("global.*defaults"));
    }

    @Test
    public void testSetTheme() {
        final ConfigTarget target = new ConfigTarget();
        target.setTheme();

        assertEquals(ConfigTarget.TYPE.THEME, target.getType());
        assertEquals("theme", target.getTypeName());
        assertTrue(target.toString().toLowerCase().matches("theme"));
    }

    @Test
    public void testSetProfile() {
        final ConfigTarget target = new ConfigTarget();
        target.setCustom("profile");

        assertEquals(ConfigTarget.TYPE.CUSTOM, target.getType());
        assertEquals("custom", target.getTypeName());
        assertEquals("profile", target.getData());
        assertEquals("custom: profile", target.toString().toLowerCase());
    }

    @Test
    public void testSetProtocol() {
        final ConfigTarget target = new ConfigTarget();
        target.setProtocol("irc");

        assertEquals(ConfigTarget.TYPE.PROTOCOL, target.getType());
        assertEquals("protocol", target.getTypeName());
        assertEquals("irc", target.getData());
        assertTrue(target.toString().toLowerCase().matches("protocol.*: irc"));
    }

    @Test
    public void testSetIrcd() {
        final ConfigTarget target = new ConfigTarget();
        target.setIrcd("ircd_name");

        assertEquals(ConfigTarget.TYPE.IRCD, target.getType());
        assertEquals("ircd", target.getTypeName());
        assertEquals("ircd_name", target.getData());
        assertTrue(target.toString().toLowerCase().matches("ircd.*: ircd_name"));
    }

    @Test
    public void testSetNetwork() {
        final ConfigTarget target = new ConfigTarget();
        target.setNetwork("net_name");

        assertEquals(ConfigTarget.TYPE.NETWORK, target.getType());
        assertEquals("network", target.getTypeName());
        assertEquals("net_name", target.getData());
        assertTrue(target.toString().toLowerCase().matches("network.*: net_name"));
    }

    @Test
    public void testSetServer() {
        final ConfigTarget target = new ConfigTarget();
        target.setServer("server_name");

        assertEquals(ConfigTarget.TYPE.SERVER, target.getType());
        assertEquals("server", target.getTypeName());
        assertEquals("server_name", target.getData());
        assertTrue(target.toString().toLowerCase().matches("server.*: server_name"));
    }

    @Test
    public void testSetChannel() {
        final ConfigTarget target = new ConfigTarget();
        target.setChannel("channel_name");

        assertEquals(ConfigTarget.TYPE.CHANNEL, target.getType());
        assertEquals("channel", target.getTypeName());
        assertEquals("channel_name", target.getData());
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

        assertEquals(t1, t1);
        assertFalse(t1.equals(t2));
        assertFalse(t1.equals(t3));
        assertFalse(t1.equals(t4));
        assertEquals(t1, t5);

        assertFalse(t2.equals(t1));
        assertEquals(t2, t2);
        assertFalse(t2.equals(t3));
        assertFalse(t2.equals(t4));
        assertFalse(t2.equals(t5));

        assertFalse(t3.equals(t1));
        assertFalse(t3.equals(t2));
        assertEquals(t3, t3);
        assertFalse(t3.equals(t4));
        assertFalse(t3.equals(t5));

        assertFalse(t4.equals(t1));
        assertFalse(t4.equals(t2));
        assertFalse(t4.equals(t3));
        assertEquals(t4, t4);
        assertFalse(t4.equals(t5));

        assertEquals(t5, t1);
        assertFalse(t5.equals(t2));
        assertFalse(t5.equals(t3));
        assertFalse(t5.equals(t4));
        assertEquals(t5, t5);

        assertEquals(t1.hashCode(), t5.hashCode());
    }

}
