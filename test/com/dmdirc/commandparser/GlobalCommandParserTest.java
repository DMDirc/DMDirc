/*
 * GlobalCommandParserTest.java
 * JUnit 4.x based test
 *
 * Created on 16 June 2007, 22:50
 */

package com.dmdirc.commandparser;

import com.dmdirc.Config;
import com.dmdirc.config.IdentityManager;
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
public class GlobalCommandParserTest extends TestCase {
    
    public GlobalCommandParserTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        IdentityManager.load();
        Config.init();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testGetGlobalCommandParser() {
        final GlobalCommandParser p1 = GlobalCommandParser.getGlobalCommandParser();
        
        final GlobalCommandParser p2 = GlobalCommandParser.getGlobalCommandParser();
        
        assertSame(p1, p2);
    }
    
}
