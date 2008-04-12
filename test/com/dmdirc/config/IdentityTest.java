/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.interfaces.ConfigChangeListener;

import java.io.IOException;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class IdentityTest {
    
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
    public void testToString() {
        assertEquals(myIdent.getName(), myIdent.toString());
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
    public void testSetOptionInt() {
        myIdent.setOption("foo", "baz", 123);
        assertEquals("123", myIdent.getOption("foo", "baz"));
    }
    
    @Test
    public void testSetOptionBool() {
        myIdent.setOption("foo", "baz", false);
        assertEquals("false", myIdent.getOption("foo", "baz"));
        myIdent.setOption("foo", "baz", true);
        assertEquals("true", myIdent.getOption("foo", "baz"));        
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
    
    @Test(expected=InvalidIdentityFileException.class)
    public void testNoName() throws IOException, InvalidIdentityFileException {
        new Identity(getClass().getResourceAsStream("identity1"), null, false);
    }
    
    @Test(expected=InvalidIdentityFileException.class)
    public void testNoTarget() throws IOException, InvalidIdentityFileException {
        new Identity(getClass().getResourceAsStream("identity2"), null, false);
    }
    
    @Test
    public void testMigrate() throws IOException, InvalidIdentityFileException {
        final Identity id = new Identity(getClass().getResourceAsStream("identity3"), null, false);
        
        assertTrue(id.getFile().isKeyDomain("identity"));
        assertTrue(id.getFile().isKeyDomain("meep"));
        assertTrue(id.getFile().isKeyDomain("unit"));
        
        assertEquals("unit test", id.getFile().getKeyDomain("identity").get("name"));
        assertEquals("true", id.getFile().getKeyDomain("unit").get("test"));
        assertEquals("2", id.getFile().getKeyDomain("meep").get("moop"));
    }
    
    @Test
    public void testSetListener() {
        final TestConfigListener listener = new TestConfigListener();
        myIdent.addListener(listener);
        assertEquals(0, listener.count);
        
        myIdent.setOption("unit", "test", "meep");        

        assertEquals(1, listener.count);
        assertEquals("unit", listener.domain);
        assertEquals("test", listener.key);
    }    
    
    @Test
    public void testUnsetListener() {
        final TestConfigListener listener = new TestConfigListener();
        myIdent.setOption("unit", "test", "meep");
        myIdent.addListener(listener);
        
        assertEquals(0, listener.count);
        myIdent.unsetOption("unit", "test");
        assertEquals(1, listener.count);
        assertEquals("unit", listener.domain);
        assertEquals("test", listener.key);
    }
    
    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(IdentityTest.class);
    }    
    
    private class TestConfigListener implements ConfigChangeListener {
        
        public int count = 0;
        public String domain, key;

        public void configChanged(String domain, String key) {
            count++;
            this.domain = domain;
            this.key = key;
        }
        
    }    
    
}
