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

import com.dmdirc.parser.callbacks.CallbackNotFoundException;
import com.dmdirc.parser.callbacks.interfaces.IErrorInfo;
import org.junit.Test;
import static org.junit.Assert.*;

public class ProcessNamesTest extends junit.framework.TestCase {
    
    @Test
    public void testExternalNames() throws CallbackNotFoundException {
        final TestParser parser = new TestParser();
        final OEITest test = new OEITest();
        parser.injectConnectionStrings();
        parser.getCallbackManager().addCallback("OnErrorInfo", test);
        
        parser.injectLine(":server 366 nick #nonexistant :End of /NAMES list.");
        
        assertFalse("Should not error on unknown NAMES replies", test.error);
    }
    
    private class OEITest implements IErrorInfo {
        
        boolean error = false;

        public void onErrorInfo(IRCParser tParser, ParserError errorInfo) {
            error = true;
        }
        
    }

}
