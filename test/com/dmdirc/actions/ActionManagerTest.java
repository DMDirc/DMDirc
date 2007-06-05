/*
 * ActionManagerTest.java
 * JUnit 4.x based test
 *
 * Created on 05 June 2007, 22:47
 */

package com.dmdirc.actions;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author chris
 */
public class ActionManagerTest {
    
    public ActionManagerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        ActionManager.init();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void substituteVars() {
        final Object[] args = new Object[]{
            "foo", "someCommand", new String[]{"a", "b", "c", "d"}
        };
        
        final String subject = "Blah blah ${1.STRING_STRING} $1 $2 $3-$4- $1-";
        final String expected = "Blah blah someCommand a b c dd a b c d";
        
        assertEquals(expected, ActionManager.substituteVars(subject, args));
    }
    
}
