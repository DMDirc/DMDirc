
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

package com.dmdirc.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ConfigFileTest {
    
    private ConfigFile cf;
    
    @Before
    public void setUp() throws Exception {
        cf = new ConfigFile(getClass().getClassLoader().
                    getResourceAsStream("com/dmdirc/util/test2.txt"));
    }    

    @Test
    public void testRead() {
        boolean err = false;
        
        try {
            cf.read(); 
        } catch (FileNotFoundException ex) {
            err = true;
        } catch (IOException ex) {
            err = true;
        } catch (InvalidConfigFileException ex) {
            err = true;
        }
        
        assertFalse(err);
    }
    
    @Test
    public void testDomains() {
        testRead();
        assertTrue(cf.hasDomain("keysections"));
        assertTrue(cf.hasDomain("section alpha"));
        assertTrue(cf.hasDomain("section one point one"));
        assertTrue(cf.hasDomain("section one"));
        assertFalse(cf.hasDomain("random domain"));
    }
    
    @Test
    public void testKeyDomains() {
        testRead();
        assertTrue(cf.isKeyDomain("section one"));
        assertFalse(cf.isKeyDomain("section one point one"));
        assertFalse(cf.isKeyDomain("section two"));
    }
    
    @Test
    public void testFlatDomains() {
        testRead();
        assertTrue(cf.isFlatDomain("keysections"));
        assertTrue(cf.isFlatDomain("section alpha"));
        assertTrue(cf.isFlatDomain("section one point one"));
        assertFalse(cf.isFlatDomain("section one"));
        assertFalse(cf.hasDomain("random domain"));        
    }
    
    @Test
    public void testFlatDomainContents() {
        testRead();
        assertEquals(2, cf.getFlatDomain("section alpha").size());
        assertEquals("line 1", cf.getFlatDomain("section alpha").get(0));
        assertEquals("line 2", cf.getFlatDomain("section alpha").get(1));
    }
    
    @Test
    public void testKeyDomainContents() {
        testRead();
        assertEquals(3, cf.getKeyDomain("section one").size());
        assertEquals("one", cf.getKeyDomain("section one").get("1"));
        assertEquals("two", cf.getKeyDomain("section one").get("2"));
        assertEquals("three", cf.getKeyDomain("section one").get("3"));
    }
    
    @Test
    public void testColons() throws IOException, InvalidConfigFileException {
        final File file = File.createTempFile("DMDirc.unittest", null);
        ConfigFile config = new ConfigFile(file);
        Map<String, String> data = new HashMap<String, String>();
        data.put("test1", "hello");
        data.put("test:2", "hello");
        data.put("test3", "hello:");
        config.addDomain("test", data);
        config.write();
        
        config = new ConfigFile(file);
        config.read();
        
        assertTrue(config.isKeyDomain("test"));
        data = config.getKeyDomain("test");
        assertEquals("hello", data.get("test1"));
        assertEquals("hello", data.get("test:2"));
        assertEquals("hello:", data.get("test3"));
    }
    
    @Test
    public void testEquals() throws IOException, InvalidConfigFileException {
        final File file = File.createTempFile("DMDirc.unittest", null);
        ConfigFile config = new ConfigFile(file);
        Map<String, String> data = new HashMap<String, String>();
        data.put("test1", "hello");
        data.put("test=2", "hello");
        data.put("test3", "hello=");
        config.addDomain("test", data);
        config.write();
        
        config = new ConfigFile(file);
        config.read();
        
        assertTrue(config.isKeyDomain("test"));
        data = config.getKeyDomain("test");
        assertEquals("hello", data.get("test1"));
        assertEquals("hello", data.get("test=2"));
        assertEquals("hello=", data.get("test3"));
    }
    
    @Test
    public void testNewlines() throws IOException, InvalidConfigFileException {
        final File file = File.createTempFile("DMDirc.unittest", null);
        ConfigFile config = new ConfigFile(file);
        Map<String, String> data = new HashMap<String, String>();
        data.put("test1", "hello");
        data.put("test2", "hello\ngoodbye");
        data.put("test3", "hello\n");
        data.put("test4", "hello\r\ngoodbye");
        config.addDomain("test", data);
        config.write();
        
        config = new ConfigFile(file);
        config.read();
        
        assertTrue(config.isKeyDomain("test"));
        data = config.getKeyDomain("test");
        assertEquals("hello", data.get("test1"));
        assertEquals("hello\ngoodbye", data.get("test2"));
        assertEquals("hello\n", data.get("test3"));
        assertEquals("hello\r\ngoodbye", data.get("test4"));
    }
    
    @Test
    public void testBackslash() throws IOException, InvalidConfigFileException {
        final File file = File.createTempFile("DMDirc.unittest", null);
        ConfigFile config = new ConfigFile(file);
        Map<String, String> data = new HashMap<String, String>();
        data.put("test1", "hello\\");
        data.put("test2", "\\nhello");
        data.put("test3\\", "hello");
        config.addDomain("test", data);
        config.write();
        
        config = new ConfigFile(file);
        config.read();
        
        assertTrue(config.isKeyDomain("test"));
        data = config.getKeyDomain("test");
        assertEquals("hello\\", data.get("test1"));
        assertEquals("\\nhello", data.get("test2"));
        assertEquals("hello", data.get("test3\\"));
    }
    
    @Test
    public void testHash() throws IOException, InvalidConfigFileException {
        final File file = File.createTempFile("DMDirc.unittest", null);
        ConfigFile config = new ConfigFile(file);
        Map<String, String> data = new HashMap<String, String>();
        data.put("test1#", "hello");
        data.put("#test2", "hello");
        data.put("test3", "#hello");
        config.addDomain("test", data);
        config.write();
        
        config = new ConfigFile(file);
        config.read();
        
        assertTrue(config.isKeyDomain("test"));
        data = config.getKeyDomain("test");
        assertEquals("hello", data.get("test1#"));
        assertEquals("hello", data.get("#test2"));
        assertEquals("#hello", data.get("test3"));
    }    
    
    @Test
    public void testEscape() {
        final String input = "blah blah\\foo\r\nbar=:";
        final String output = "blah blah\\\\foo\\r\\nbar\\=\\:";
        assertEquals(output, ConfigFile.escape(input));
    }
    
    @Test
    public void testUnescape() {
        final String input = "blah blah\\foo\r\nbar=:";
        assertEquals(input, ConfigFile.unescape(ConfigFile.escape(input)));
    }
    
    @Test
    public void testDelete() throws IOException {
        final File file = File.createTempFile("DMDirc_unittest", null);
        ConfigFile config = new ConfigFile(file);
        config.write();
        assertTrue(file.exists());
        config.delete();
        assertFalse(file.exists());
    }
    
    @Test
    public void testDuplicateKeys() throws IOException, InvalidConfigFileException {
        final ConfigFile file = new ConfigFile(getClass().getResourceAsStream("test2.txt"));
        file.read();
        
        assertTrue(file.isKeyDomain("section one"));
        assertEquals(3, file.getKeyDomain("section one").size());
        assertTrue(file.isFlatDomain("section one point one"));
    }

}