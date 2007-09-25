/*
 * InvalidIdentityFileExceptionTest.java
 * JUnit based test
 *
 * Created on 25 September 2007, 10:09
 */

package com.dmdirc.config;

import junit.framework.TestCase;

/**
 *
 * @author chris
 */
public class InvalidIdentityFileExceptionTest extends TestCase {
    
    public InvalidIdentityFileExceptionTest(String testName) {
        super(testName);
    }

    public void testMessage() {
        final InvalidIdentityFileException ex = new InvalidIdentityFileException("message here");
        
        assertEquals("message here", ex.getMessage());
    }
    
}
