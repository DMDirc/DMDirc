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

package com.dmdirc.parser.irc;

import com.dmdirc.harness.parser.TestIErrorInfo;
import com.dmdirc.harness.parser.TestParser;
import org.junit.Test;
import static org.junit.Assert.*;

public class Process004005Test {
    
    private TestParser doCaseMappingTest(final String target, final int expected) {
        final TestParser parser = new TestParser();
        parser.injectConnectionStrings();
        parser.injectLine(":server 005 nick CASEMAPPING=" + target
                    + " :are supported by this server");
        
        assertEquals(expected, parser.getIRCStringConverter().getLimit());
        
        return parser;
    }
    
    @Test
    public void testCaseMappingASCII() {
        doCaseMappingTest("ascii", 0);
        doCaseMappingTest("ASCII", 0);
    }
    
    @Test
    public void testCaseMappingRFC() {
        doCaseMappingTest("rfc1459", 4);
        doCaseMappingTest("RFC1459", 4);
    }
    
    @Test
    public void testCaseMappingStrict() {
        doCaseMappingTest("strict-rfc1459", 3);
        doCaseMappingTest("strict-RFC1459", 3);        
    }
    
    @Test
    public void testCaseMappingUnknown() {
        final TestParser tp = doCaseMappingTest("rfc1459", 4);
        final TestIErrorInfo tiei = new TestIErrorInfo();
        
        tp.getCallbackManager().addCallback("OnErrorInfo", tiei);
        
        tp.injectLine(":server 005 nick CASEMAPPING=unknown :are supported by this server");
        
        assertTrue(tiei.error);
    }

}
