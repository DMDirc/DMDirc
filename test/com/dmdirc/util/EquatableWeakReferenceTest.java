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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import org.junit.Test;
import static org.junit.Assert.*;

public class EquatableWeakReferenceTest {

    @Test
    public void testEquals() {
        final Object myObject = "moo";
        final Reference<Object> myRef = new WeakReference<Object>(myObject);
        final EquatableWeakReference<Object> ewf = new EquatableWeakReference<Object>(myObject);

        assertTrue(ewf.equals(myObject));
        assertTrue(ewf.equals(myRef));
        assertFalse(ewf.equals("bar"));
        assertFalse(ewf.equals(new WeakReference<Object>("bar")));
    }

    @Test
    public void testHashCode() {
        final Object myObject = "moo";
        final EquatableWeakReference<Object> ewf = new EquatableWeakReference<Object>(myObject);
        
        assertEquals(myObject.hashCode(), ewf.hashCode());
    }

}