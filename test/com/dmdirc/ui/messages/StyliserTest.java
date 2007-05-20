/*
 * StyliserTest.java
 * JUnit based test
 *
 * Created on 09 March 2007, 23:20
 */

package com.dmdirc.ui.messages;

import junit.framework.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;

/**
 *
 * @author chris
 */
public class StyliserTest extends TestCase {
    
    public StyliserTest(String testName) {
        super(testName);
    }
        
    public void testStripControlCodes1() {
        String input = "This"+((char) 2)+" is "+((char) 17)+"a test";
        
        String expResult = "This is a test";
        String result = Styliser.stipControlCodes(input);
        assertEquals(expResult, result);
    }
    
    public void testStripControlCodes2() {
        String input = "This is "+((char) 3)+"5a "+((char) 4)+"FF0000test";
        
        String expResult = "This is a test";
        String result = Styliser.stipControlCodes(input);
        assertEquals(expResult, result);
    }    
    
    public void testReadUntilControl1() {
        String input = "This"+((char) 2)+" is "+((char) 17)+"a test";
        String expResult = "This";
        String result = Styliser.readUntilControl(input);
        assertEquals(expResult, result);
    }
    
    public void testReadUntilControl2() {
        String input = "This"+((char) 17)+" is "+((char) 17)+"a test";
        String expResult = "This";
        String result = Styliser.readUntilControl(input);
        assertEquals(expResult, result);
    }
    
    public void testReadUntilControl3() {
        String input = ((char) 31)+" is "+((char) 17)+"a test";
        String expResult = "";
        String result = Styliser.readUntilControl(input);
        assertEquals(expResult, result);
    }
    
}
