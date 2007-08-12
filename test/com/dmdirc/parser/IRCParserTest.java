/*
 * ServerManagerTest.java
 * JUnit 4.x based test
 *
 */

package com.dmdirc.parser;

import com.dmdirc.parser.callbacks.CallbackNotFound;
import com.dmdirc.parser.callbacks.interfaces.IAwayState;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Chris
 */
public class IRCParserTest {
    
    public IRCParserTest() {
    }
    
    @Test
    public void Issue042test() {
        boolean res = false;
        
        try {
            final IRCParser myParser = new IRCParser();
            myParser.getCallbackManager().addCallback("non-existant",new IAwayState() {
                public void onAwayState(IRCParser tParser, boolean currentState, String reason) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            });
        } catch (CallbackNotFound ex) {
            res = true;
        }
        
        assertTrue("addCallback() should throw exception for non-existant callbacks", res);
    }
    
}
