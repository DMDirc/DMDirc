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

package com.dmdirc.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ConfigFileTest extends junit.framework.TestCase {
    
    private ConfigFile cf;
    
    @Before
    public void setUp() throws Exception {
        cf = new ConfigFile(getClass().getClassLoader().
                    getResource("com/dmdirc/util/test2.txt").toURI());
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

}