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

package com.dmdirc.parser.irc;

import com.dmdirc.harness.parser.TestIAwayState;
import com.dmdirc.harness.parser.TestParser;
import com.dmdirc.parser.irc.callbacks.CallbackNotFoundException;
import org.junit.Test;
import static org.junit.Assert.*;

public class ProcessWhoTest extends junit.framework.TestCase {

    @Test
    public void testSelfAway() throws CallbackNotFoundException {
        final TestParser parser = new TestParser();
        final TestIAwayState test = new TestIAwayState();
        parser.injectConnectionStrings();
        parser.getCallbackManager().addCallback("OnAwayState", test);

        parser.injectLine(":nick JOIN #DMDirc_testing");

        parser.injectLine(":server 352 nick #DMDirc_testing nick2 host2 server nick2 G@ :0 rn");

        assertFalse(test.called);

        parser.injectLine(":server 352 nick #DMDirc_testing nick host server nick G@ :0 rn");

        assertTrue(test.called);
        assertTrue(test.state);
    }

}
