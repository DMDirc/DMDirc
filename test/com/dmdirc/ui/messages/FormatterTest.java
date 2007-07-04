/*
 * FormatterTest.java
 * JUnit 4.x based test
 *
 * Created on 16 June 2007, 22:54
 */

package com.dmdirc.ui.messages;

import com.dmdirc.Config;
import com.dmdirc.config.IdentityManager;
import java.io.File;
import java.util.Set;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author chris
 */
public class FormatterTest {
    
    public FormatterTest() {
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
    public void formatMessage() {
        Formatter.registerDefault("unitTest", "abc %2$s %1$s def");
        
        // Standard format test
        assertEquals(Formatter.formatMessage("unitTest", "123", "456"), "abc 456 123 def");
        
        // Check unknown formats
        assertTrue(Formatter.formatMessage("unitTest123", "m").indexOf("No format string") > -1);
        
        Formatter.registerDefault("unitTest2", "abc %2$$$ZAS %1$s def");
        
        // And invalid formats
        assertTrue(Formatter.formatMessage("unitTest2", "m").indexOf("Invalid format string") > -1);
    }

    @Test
    public void getFormats() {
        final Set<String> s1 = Formatter.getFormats();
        Formatter.registerDefault("unitTest3", "abc");
        final Set<String> s2 = Formatter.getFormats();
        final Set<String> s3 = Formatter.getFormats();
        
        assertEquals(s2, s3);
        assertTrue(s1.size() + 1 == s2.size());
    }

    @Test
    public void hasFormat() {
        final String[] targets = new String[]{"unknown", "abc", "def", "unittestfail"};
        
        for (String target : targets) {
            assertFalse(Formatter.hasFormat(target));
        }
        
        for (String target : Formatter.getFormats()) {
            assertTrue(Formatter.hasFormat(target));
        }
    }

    @Test
    public void registerDefault() {
    }

    @Test
    public void saveAndLoad() {
        Formatter.registerDefault("unitTest_saveLoad", "");
        
        final String fileName = "unittest_formatter";
        final File file = new File(Config.getConfigDir() + fileName);
        
        if (file.exists()) {
            file.delete();
        }
        
        Formatter.saveAs(fileName);
        
        assertTrue(file.exists());
        
        Formatter.reload();
        
        Formatter.loadFile(fileName);
        
        assertTrue(Formatter.hasFormat("unitTest_saveLoad"));
        
        file.delete();
    }

    @Test
    public void reload() {
        Formatter.reload();
        
        final Set<String> s1 = Formatter.getFormats();
        
        Formatter.registerDefault("UnitTestABCDEF", "");
        
        Formatter.reload();
        
        final Set<String> s2 = Formatter.getFormats();
        
        assertEquals(s1.size(), s2.size());
    }
    
}
