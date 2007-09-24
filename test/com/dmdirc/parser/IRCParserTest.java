/*
 * ServerManagerTest.java
 * JUnit 4.x based test
 *
 */

package com.dmdirc.parser;

import com.dmdirc.parser.callbacks.CallbackNotFoundException;
import com.dmdirc.parser.callbacks.interfaces.IAwayState;
import junit.framework.TestCase;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Chris
 */
public class IRCParserTest extends TestCase {
    
    public IRCParserTest() {
    }
    
    @Test
    public void testIssue042() {
        boolean res = false;
        
        try {
            final IRCParser myParser = new IRCParser();
            myParser.getCallbackManager().addCallback("non-existant",new IAwayState() {
                public void onAwayState(IRCParser tParser, boolean currentState, String reason) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            });
        } catch (CallbackNotFoundException ex) {
            res = true;
        }
        
        assertTrue("addCallback() should throw exception for non-existant callbacks", res);
    }
    
    @Test
    public void testTokeniser() {
        final IRCParser myParser = new IRCParser();
        
        final String line1 = "a b c d e";
        final String line2 = "a b c :d e";
        final String line3 = ":a b:c :d e";
        
        final String[] res1 = myParser.tokeniseLine(line1);
        final String[] res2 = myParser.tokeniseLine(line2);
        final String[] res3 = myParser.tokeniseLine(line3);
        
        arrayEquals(res1, new String[]{"a", "b", "c", "d", "e"});
        arrayEquals(res2, new String[]{"a", "b", "c", "d e"});
        arrayEquals(res3, new String[]{":a", "b:c", "d e"});
    }
    
    private void arrayEquals(final String[] a1, final String[] a2) {
        assertEquals(a1.length, a2.length);
        
        for (int i = 0; i < a1.length; i++) {
            assertEquals(a1[i], a2[i]);
        }
    }
    
}
