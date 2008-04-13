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

import com.dmdirc.harness.parser.TestParser;
import org.junit.Test;
import static org.junit.Assert.*;

public class NullCallbacksTest extends junit.framework.TestCase {

    private IRCProcessor getProcessor(String callback, String param) {
        final TestParser parser = new TestParser();

        parser.getCallbackManager().delCallbackType(
                parser.getCallbackManager().getCallbackType(callback));

        try {
            return parser.getProcessingManager().getProcessor(param);
        } catch (ProcessorNotFoundException ex) {
            assertFalse(true);
            return null;
        }
    }

    @Test
    public void testProcess001() {
        boolean exception = false;

        try {
            ((Process001) getProcessor("OnServerReady", "001")).callServerReady();
        } catch (Throwable ex) {
            exception = true;
        }

        assertFalse(exception);
    }

    @Test
    public void testProcess004005() {
        boolean exception = false;

        try {
            ((Process004005) getProcessor("OnGotNetwork", "004")).callGotNetwork();
        } catch (Throwable ex) {
            exception = true;
        }

        assertFalse(exception);
    }

    @Test
    public void testProcess464() {
        boolean exception = false;

        try {
            ((Process464) getProcessor("OnPasswordRequired", "464")).callPasswordRequired();
        } catch (Throwable ex) {
            exception = true;
        }

        assertFalse(exception);
    }

    @Test
    public void testProcessAway() {
        boolean exception = false;

        try {
            ((ProcessAway) getProcessor("OnAwayState", "301")).callAwayState(false, "");
        } catch (Throwable ex) {
            exception = true;
        }

        assertFalse(exception);
    }

    @Test
    public void testProcessInvite() {
        boolean exception = false;

        try {
            ((ProcessInvite) getProcessor("OnInvite", "INVITE")).callInvite("", "");
        } catch (Throwable ex) {
            exception = true;
        }

        assertFalse(exception);
    }

    @Test
    public void testProcessJoin() {
        boolean exception = false;

        try {
            ((ProcessJoin) getProcessor("OnChannelJoin", "JOIN")).callChannelJoin(null, null);
            ((ProcessJoin) getProcessor("OnChannelSelfJoin", "JOIN")).callChannelSelfJoin(null);
        } catch (Throwable ex) {
            exception = true;
        }

        assertFalse(exception);
    }
    
    @Test
    public void testProcessKick() {
        boolean exception = false;

        try {
            ((ProcessKick) getProcessor("OnChannelKick", "KICK")).callChannelKick(null,
                    null, null, null, null);
        } catch (Throwable ex) {
            exception = true;
        }

        assertFalse(exception);
    }
    
    @Test
    public void testProcessListModes() {
        boolean exception = false;

        try {
            ((ProcessListModes) getProcessor("OnChannelGotListModes", "346"))
                    .callChannelGotListModes(null);
        } catch (Throwable ex) {
            exception = true;
        }

        assertFalse(exception);
    }
    
    @Test
    public void testProcessMOTD() {
        boolean exception = false;

        try {
            ((ProcessMOTD) getProcessor("OnMOTDStart", "372")).callMOTDStart(null);
            ((ProcessMOTD) getProcessor("OnMOTDLine", "372")).callMOTDLine(null);
            ((ProcessMOTD) getProcessor("OnMOTDEnd", "372")).callMOTDEnd(false, null);
        } catch (Throwable ex) {
            exception = true;
        }

        assertFalse(exception);
    }    

}
