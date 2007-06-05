/*
 * CoreActionMetaTypeTest.java
 * JUnit 4.x based test
 *
 * Created on 05 June 2007, 22:55
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
public class CoreActionMetaTypeTest {
    
    public CoreActionMetaTypeTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void checkArgTypeSize() {
        for (CoreActionMetaType type : CoreActionMetaType.values()) {
            assertEquals(type.getArity(), type.getArgTypes().length);
        }
    }
    
    @Test
    public void checkArgNameSize() {
        for (CoreActionMetaType type : CoreActionMetaType.values()) {
            assertEquals(type.getArity(), type.getArgNames().length);
        }
    }    
    
}
