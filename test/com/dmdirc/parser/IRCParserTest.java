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
    
}
