/*
 * IdentityManagerTest.java
 * JUnit based test
 *
 * Created on 03 October 2007, 18:34
 */

package com.dmdirc.config;

import junit.framework.TestCase;

/**
 *
 * @author chris
 */
public class IdentityManagerTest extends TestCase {
    
    public IdentityManagerTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        
        IdentityManager.load();
    }

    public void testGetGlobalConfig() {
        final ConfigManager gcm = IdentityManager.getGlobalConfig();
        
        for (Identity identity : gcm.getSources()) {
            assertTrue(identity.getTarget().getType() == ConfigTarget.TYPE.GLOBAL
                    || identity.getTarget().getType() == ConfigTarget.TYPE.GLOBALDEFAULT
                    || identity.getTarget().getType() == ConfigTarget.TYPE.THEME);
        }
    }
    
}
