/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class IdentityTest extends junit.framework.TestCase {
    
    private Identity myIdent;
    private ConfigTarget target;
    
    @Before
    public void setUp() throws Exception {
        target = new ConfigTarget();
        target.setChannel("#unittest@unittest");
        
        myIdent = Identity.buildIdentity(target);
    }
    
    @After
    public void tearDown() throws Exception {
        myIdent = null;
    }    
    
    @Test
    public void testGetProperties() {
        myIdent.setOption("domain", "option", "value");
        final Properties props = myIdent.getProperties();
        
        assertEquals(props.getProperty("domain.option"), "value");
        
        myIdent.unsetOption("domain", "option");
    }

    @Test
    public void testGetName() {
        final Properties props = myIdent.getProperties();
        
        assertEquals(props.getProperty("identity.name"), myIdent.getName());
    }

    @Test
    public void testIsProfile() {
        assertFalse(myIdent.isProfile());
        
        myIdent.setOption("profile", "nickname", "foo");
        myIdent.setOption("profile", "realname", "foo");
        
        assertTrue(myIdent.isProfile());
        
        myIdent.unsetOption("profile", "nickname");
        myIdent.unsetOption("profile", "realname");
    }

    @Test
    public void testHasOption() {
        assertFalse(myIdent.hasOption("has", "option"));
        
        myIdent.setOption("has", "option", "");
        
        assertTrue(myIdent.hasOption("has", "option"));
        
        myIdent.unsetOption("has", "option");
    }

    @Test
    public void testGetOption() {
        myIdent.setOption("domain", "option", "value");
        final Properties props = myIdent.getProperties();
        
        assertEquals(props.getProperty("domain.option"), myIdent.getOption("domain", "option"));
        
        myIdent.unsetOption("domain", "option");
    }

    @Test
    public void testSetOption() {
        final int count = myIdent.getProperties().size();
        
        myIdent.setOption("foo", "bar", "baz");
        
        assertEquals(count + 1, myIdent.getProperties().size());
        
        myIdent.unsetOption("foo", "bar");
    }

    @Test
    public void testRemoveOption() {
        final Properties props = myIdent.getProperties();
        final int count = props.size();
        
        myIdent.setOption("foo", "bar", "baz");
        myIdent.unsetOption("foo", "bar");
        
        assertEquals(count, props.size());
    }

    @Test
    public void testSave() {
        myIdent.setOption("foo", "bar", "baz!");
        
        myIdent.save();
        myIdent = null;
        
        myIdent = Identity.buildIdentity(target);
        
        assertEquals("baz!", myIdent.getOption("foo", "bar"));
        
        myIdent.unsetOption("foo", "bar");
        myIdent.save();
    }

    @Test
    public void testGetTarget() {
        assertEquals(target.getData(), myIdent.getTarget().getData());
        assertEquals(target.getType(), myIdent.getTarget().getType());
    }

    @Test
    public void testToString() {
        assertEquals(myIdent.getOption("identity", "name"), myIdent.getName());
    }
    
}
