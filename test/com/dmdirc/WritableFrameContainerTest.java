/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc;

import com.dmdirc.ui.interfaces.InputWindow;

import org.junit.Test;
import static org.junit.Assert.*;

public class WritableFrameContainerTest {
    
    @Test
    public void testGetNumLines() {
        final WritableFrameContainer container10 = new BasicWritableFrameContainer(10);
        
        final int res1a = container10.getNumLines("0123456789");
        final int res1b = container10.getNumLines("");
        final int res1c = container10.getNumLines("qaaa");
        final int res2a = container10.getNumLines("01234567890");
        final int res2b = container10.getNumLines("\n");
        final int res2c = container10.getNumLines("\r\n");
        
        assertEquals(1, res1a);
        assertEquals(1, res1b);
        assertEquals(1, res1c);
        
        assertEquals(2, res2a);
        assertEquals(2, res2b);
        assertEquals(2, res2c);        
    }
    
}

class BasicWritableFrameContainer extends WritableFrameContainer {
    
    private final int lineLength;
    
    public BasicWritableFrameContainer(final int lineLength) {
        this.lineLength = lineLength;
    }
    
    public void sendLine(String line) {
        // Do nothing
    }
    
    public InputWindow getFrame() {
        return null;
    }
    
    public int getMaxLineLength() {
        return lineLength;
    }
    
    public String toString() {
        return null;
    }
    
    public void close() {
        // Do nothing
    }
    
    public Server getServer() {
        return null;
    }
}
