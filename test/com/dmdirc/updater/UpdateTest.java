/*
 * UpdateTest.java
 * JUnit 4.x based test
 *
 * Created on 05 June 2007, 22:59
 */

package com.dmdirc.updater;

import junit.framework.TestCase;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author chris
 */
public class UpdateTest extends TestCase {
    
    private final String subject = "outofdate component rversion lversion url";
    
    private Update update;
    
    public UpdateTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        update = new Update(subject);
    }

    @Test
    public void testGetComponent() {
        assertEquals("component", update.getComponent());
    }

    @Test
    public void testGetLocalVersion() {
        assertEquals("lversion", update.getLocalVersion());
    }

    @Test
    public void testGetRemoteVersion() {
        assertEquals("rversion", update.getRemoteVersion());
    }

    @Test
    public void testGetUrl() {
        assertEquals("url", update.getUrl());
    }
    
}
