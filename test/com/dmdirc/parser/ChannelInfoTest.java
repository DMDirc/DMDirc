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
package com.dmdirc.parser;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

public class ChannelInfoTest extends junit.framework.TestCase {

    final ChannelInfo ci = new ChannelInfo(null, "name");
    
    @Test
    public void testGetName() {
        assertEquals("name", ci.getName());
    }
    
    @Test
    public void testAddingNames() {
        assertTrue(ci.getAddingNames());
        
        ci.setAddingNames(false);
        
        assertFalse(ci.getAddingNames());
    }
    
    @Test
    public void testMap() {
        final Map map = new HashMap();
        
        ci.setMap(map);
        
        assertEquals(map, ci.getMap());
    }
    
    @Test
    public void testCreateTime() {
        ci.setCreateTime(12345l);
        
        assertEquals(12345l, ci.getCreateTime());
    }
    
    @Test
    public void testTopicTime() {
        ci.setTopicTime(12345l);
        
        assertEquals(12345l, ci.getTopicTime());
    }
    
    @Test
    public void testTopic() {
        ci.setTopic("abcdef");
        
        assertEquals("abcdef", ci.getTopic());
    }

}