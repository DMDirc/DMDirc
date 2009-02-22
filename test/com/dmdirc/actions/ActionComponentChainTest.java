/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.actions;

import com.dmdirc.config.IdentityManager;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ActionComponentChainTest {

    @Before
    public void setUp() throws Exception {
        IdentityManager.load();
        ActionManager.init();
    }

    @Test
    public void testSingle() {
        final ActionComponentChain chain = new ActionComponentChain(String.class, "STRING_STRING");
        assertEquals(chain.get("foo bar baz"), "foo bar baz");
        assertEquals("STRING_STRING", chain.toString());
    }

    @Test
    public void testDouble() {
        final ActionComponentChain chain = new ActionComponentChain(String.class,
                "STRING_STRING.STRING_STRING");
        assertEquals(chain.get("foo bar baz"), "foo bar baz");
        assertEquals("STRING_STRING.STRING_STRING", chain.toString());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidName() {
        final ActionComponentChain chain = new ActionComponentChain(String.class,
                "STRONG_STRING");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidType() {
        final ActionComponentChain chain = new ActionComponentChain(String.class,
                "USER_MODES.STRING_STRING");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidLink() {
        final ActionComponentChain chain = new ActionComponentChain(String.class,
                "STRING_STRING.USER_MODES");
    }

    @Test
    public void testAppliesTo() {
        final ActionComponentChain chain = new ActionComponentChain(String.class,
                "STRING_STRING.STRING_STRING.STRING_LENGTH");
        assertEquals(String.class, chain.appliesTo());
    }
    
    @Test
    public void testGetType() {
        final ActionComponentChain chain = new ActionComponentChain(String.class,
                "STRING_STRING.STRING_STRING.STRING_LENGTH");
        assertEquals(Integer.class, chain.getType());
    }    
    
    @Test
    public void testGetName() {
        final ActionComponentChain chain = new ActionComponentChain(String.class,
                "STRING_STRING.STRING_STRING.STRING_LENGTH");
        
        assertTrue(chain.getName().indexOf(CoreActionComponent.STRING_STRING.getName()) > -1);
        assertTrue(chain.getName().indexOf(CoreActionComponent.STRING_LENGTH.getName()) > -1);
    }  
    
    @Test(expected=AssertionError.class)
    public void testEmptyAppliesTo() {
        final ActionComponentChain chain = new ActionComponentChain(String.class, "");
        chain.appliesTo();
    }
    
    @Test(expected=AssertionError.class)
    public void testEmptyGetType() {
        final ActionComponentChain chain = new ActionComponentChain(String.class, "");
        chain.getType();
    }
    
    @Test(expected=AssertionError.class)
    public void testEmptyGetName() {
        final ActionComponentChain chain = new ActionComponentChain(String.class, "");
        chain.getName();
    }
    
    @Test(expected=AssertionError.class)
    public void testEmptyToString() {
        final ActionComponentChain chain = new ActionComponentChain(String.class, "");
        chain.toString();
    }      

}