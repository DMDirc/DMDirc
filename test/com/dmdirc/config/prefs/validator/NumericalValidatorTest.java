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
package com.dmdirc.config.prefs.validator;

import org.junit.Test;
import static org.junit.Assert.*;

public class NumericalValidatorTest {

    @Test
    public void testGetMax() {
        assertEquals(10, new NumericalValidator(1, 10).getMax());
        assertEquals(Integer.MAX_VALUE, new NumericalValidator(1, -1).getMax());
    }

    @Test
    public void testGetMin() {
        assertEquals(1, new NumericalValidator(1, 10).getMin());
        assertEquals(Integer.MIN_VALUE, new NumericalValidator(-1, 10).getMin());
    }

    @Test
    public void testNAN() {
        final ValidationResponse vr = new NumericalValidator(3, 5).validate("foo");
        
        assertTrue(vr.isFailure());
        assertTrue(vr.getFailureReason().indexOf("number") > -1);
    }
    
    @Test
    public void testMin() {
        final NumericalValidator nv1 = new NumericalValidator(-1, -1);
        final NumericalValidator nv2 = new NumericalValidator(-5, -1);
        
        assertFalse(nv1.validate("-5").isFailure());
        assertFalse(nv2.validate("-5").isFailure());
        assertFalse(nv2.validate("-4").isFailure());
        assertFalse(nv2.validate("10").isFailure());
        assertTrue(nv2.validate("-6").isFailure());
    }
    
    @Test
    public void testMax() {
        final NumericalValidator nv1 = new NumericalValidator(-1, -1);
        final NumericalValidator nv2 = new NumericalValidator(-1, 10);
        
        assertFalse(nv1.validate("-5").isFailure());
        assertFalse(nv1.validate("50").isFailure());
        assertFalse(nv2.validate("-5").isFailure());
        assertFalse(nv2.validate("-4").isFailure());
        assertFalse(nv2.validate("10").isFailure());
        assertTrue(nv2.validate("11").isFailure());
    }    

}