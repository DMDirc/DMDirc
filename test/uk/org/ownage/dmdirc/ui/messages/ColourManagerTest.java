/*
 * ColourManagerTest.java
 * JUnit based test
 *
 * Created on 09 March 2007, 23:09
 */

package uk.org.ownage.dmdirc.ui.messages;

import junit.framework.*;
import java.awt.Color;
import uk.org.ownage.dmdirc.logger.ErrorLevel;
import uk.org.ownage.dmdirc.logger.Logger;

/**
 *
 * @author chris
 */
public class ColourManagerTest extends TestCase {
    
    public ColourManagerTest(String testName) {
        super(testName);
    }
    
    public void testGetColourInt() {
        int spec = 4;
        
        Color expResult = Color.RED;
        Color result = ColourManager.getColour(spec);
        assertEquals(expResult, result);
    }
    
    public void testGetColourHex() {
        String spec = "FFFFFF";
        
        Color expResult = Color.decode("#FFFFFF");
        Color result = ColourManager.getColour(spec);
        assertEquals(expResult, result);
    }    
    
}
