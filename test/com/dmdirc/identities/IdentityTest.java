/*
 * IdentityTest.java
 * JUnit based test
 *
 * Created on 19 April 2007, 17:33
 */

package com.dmdirc.identities;

import java.util.Properties;
import junit.framework.*;

/**
 *
 * @author chris
 */
public class IdentityTest extends TestCase {
    
    private Identity myIdent;
    private ConfigTarget target;
    
    public IdentityTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        target = new ConfigTarget();
        target.setChannel("#unittest@unittest");
        
        myIdent = Identity.buildIdentity(target);
    }
    
    protected void tearDown() throws Exception {
        myIdent = null;
    }    
    
    public void testGetProperties() {
        myIdent.setOption("domain", "option", "value");
        final Properties props = myIdent.getProperties();
        
        assertEquals(props.getProperty("domain.option"), "value");
        
        myIdent.removeOption("domain", "option");
    }

    public void testGetName() {
        final Properties props = myIdent.getProperties();
        
        assertEquals(props.getProperty("identity.name"), myIdent.getName());
    }

    public void testIsProfile() {
        assertFalse(myIdent.isProfile());
        
        myIdent.setOption("profile", "nickname", "foo");
        myIdent.setOption("profile", "realname", "foo");
        
        assertTrue(myIdent.isProfile());
        
        myIdent.removeOption("profile", "nickname");
        myIdent.removeOption("profile", "realname");
    }

    public void testHasOption() {
        assertFalse(myIdent.hasOption("has", "option"));
        
        myIdent.setOption("has", "option", "");
        
        assertTrue(myIdent.hasOption("has", "option"));
        
        myIdent.removeOption("has", "option");
    }

    public void testGetOption() {
        myIdent.setOption("domain", "option", "value");
        final Properties props = myIdent.getProperties();
        
        assertEquals(props.getProperty("domain.option"), myIdent.getOption("domain", "option"));
        
        myIdent.removeOption("domain", "option");
    }

    public void testSetOption() {
        final int count = myIdent.getProperties().size();
        
        myIdent.setOption("foo", "bar", "baz");
        
        assertEquals(count + 1, myIdent.getProperties().size());
        
        myIdent.removeOption("foo", "bar");
    }

    public void testRemoveOption() {
        final Properties props = myIdent.getProperties();
        final int count = props.size();
        
        myIdent.setOption("foo", "bar", "baz");
        myIdent.removeOption("foo", "bar");
        
        assertEquals(count, props.size());
    }

    public void testSave() {
        myIdent.setOption("foo", "bar", "baz!");
        
        myIdent.save();
        myIdent = null;
        
        myIdent = Identity.buildIdentity(target);
        
        assertEquals("baz!", myIdent.getOption("foo", "bar"));
        
        myIdent.removeOption("foo", "bar");
        myIdent.save();
    }

    public void testGetTarget() {
        assertEquals(target.getData(), myIdent.getTarget().getData());
        assertEquals(target.getType(), myIdent.getTarget().getType());
    }

    public void testToString() {
        assertEquals(myIdent.getOption("identity", "name"), myIdent.getName());
    }

    public void testCompareTo() {
        // TODO add your test code.
    }

    public void testBuildIdentity() {
        // TODO add your test code.
    }
    
}
