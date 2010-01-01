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

package com.dmdirc.addons.dcc;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class IpToLongTest {
    
    private String stringip;
    private long longip;

    public IpToLongTest(String stringip, Long longip) {
        this.stringip = stringip;
        this.longip = longip;
    }
    
    @Test
    public void testToLong() {
        assertEquals(longip, DCC.ipToLong(stringip));
    }
    
    @Test
    public void testToString() {
        assertEquals(stringip, DCC.longToIP(longip));
    }
    
    @Parameterized.Parameters
    public static List<Object[]> data() {
        final Object[][] tests = {
            {"0.0.0.0", 0L},
            {"0.0.0.255", 255L},
            {"127.0.0.1", 2130706433L},
            {"255.0.0.0", 4278190080L},
            {"255.255.255.255", 4294967295L},
        };

        return Arrays.asList(tests);
    }

}
