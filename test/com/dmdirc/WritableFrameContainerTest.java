/*
 * ProgramErrorTest.java
 * JUnit based test
 *
 * Created on 08 April 2007, 13:20
 */

package com.dmdirc;

import com.dmdirc.ui.interfaces.InputWindow;
import junit.framework.*;

public class WritableFrameContainerTest extends TestCase {
    
    public WritableFrameContainerTest(String testName) {
        super(testName);
    }
    
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
