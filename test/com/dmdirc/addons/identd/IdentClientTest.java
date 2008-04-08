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
package com.dmdirc.addons.identd;

import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.IdentityManager;

import java.util.HashMap;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class IdentClientTest extends junit.framework.TestCase {

    @BeforeClass
    public static void setUpClass() throws Exception {
        IdentityManager.load();
    }

    @Test
    public void testInvalidIdent() {
        final String response = IdentClient.getIdentResponse("invalid request!",
                IdentityManager.getGlobalConfig());
        
        assertContains("Illegal requests must result in an ERROR response",
                response, "ERROR");
    }
    
    @Test
    public void testQuoting() {
        final String response = IdentClient.getIdentResponse("in\\valid:invalid",
                IdentityManager.getGlobalConfig());
        
        assertStartsWith("Special chars in illegal requests must be quoted",
                response, "in\\\\valid\\:invited");
    }    
    
    @Test
    public void testNonNumericPort() {
        final String response = IdentClient.getIdentResponse("abc, def",
                IdentityManager.getGlobalConfig());
        
        assertContains("Non-numeric ports must result in an ERROR response",
                response, "ERROR");
    }
    
    private void doPortTest(final String ports) {
        final String response = IdentClient.getIdentResponse(ports,
                IdentityManager.getGlobalConfig());
        
        assertContains("Illegal ports must result in an ERROR response",
                response, "ERROR");
        assertContains("Illegal ports must result in an INVALID-PORT response",
                response, "INVALID-PORT");
        assertStartsWith("Port numbers must be returned as part of the response",
                response.replaceAll("\\s+", ""), ports.replaceAll("\\s+", ""));
    }
    
    @Test
    public void testOutOfRangePorts() {
        doPortTest("0, 50");
        doPortTest("65536, 50");
        doPortTest("50, 0");
        doPortTest("50, 65536");
    }
    
    @Test
    public void testAlwaysOn() {
        final TestConfigManager tcm = new TestConfigManager();
        tcm.settings.put("plugin-Identd.advanced.alwaysOn", "false");
        
        final String response = IdentClient.getIdentResponse("50, 50", tcm);
        assertContains("Unknown port requests must return an ERROR response",
                response, "ERROR");
        assertContains("Unknown port requests must return a NO-USER response",
                response, "NO-USER");
    }
    
    @Test
    public void testHidden() {
        final TestConfigManager tcm = new TestConfigManager();
        tcm.settings.put("plugin-Identd.advanced.alwaysOn", "true");
        tcm.settings.put("plugin-Identd.advanced.isHiddenUser", "true");
        
        final String response = IdentClient.getIdentResponse("50, 50", tcm);
        assertContains("Hidden requests must return an ERROR response",
                response, "ERROR");
        assertContains("Hidden requests must return a HIDDEN-USER response",
                response, "HIDDEN-USER");
    }
    
    @Test
    public void testSystemNameQuoting() {
        final TestConfigManager tcm = new TestConfigManager();
        tcm.settings.put("plugin-Identd.advanced.alwaysOn", "true");
        tcm.settings.put("plugin-Identd.advanced.isHiddenUser", "false");
        tcm.settings.put("plugin-Identd.advanced.useCustomSystem", "true");
        tcm.settings.put("plugin-Identd.advanced.customSystem", "a:b\\c,d");
        tcm.settings.put("plugin-Identd.general.useCustomName", "false");
        tcm.settings.put("plugin-Identd.general.customName", "");
        
        final String response = IdentClient.getIdentResponse("50, 50", tcm);
        assertContains("Special characters must be quoted in system names",
                response, "a\\:b\\\\c\\,d");        
    }
    
    @Test
    public void testCustomNameQuoting() {
        final TestConfigManager tcm = new TestConfigManager();
        tcm.settings.put("plugin-Identd.advanced.alwaysOn", "true");
        tcm.settings.put("plugin-Identd.advanced.isHiddenUser", "false");
        tcm.settings.put("plugin-Identd.advanced.useCustomSystem", "false");
        tcm.settings.put("plugin-Identd.advanced.customSystem", "");
        tcm.settings.put("plugin-Identd.general.useCustomName", "true");
        tcm.settings.put("plugin-Identd.general.customName", "a:b\\c,d");
        
        final String response = IdentClient.getIdentResponse("50, 50", tcm);
        assertContains("Special characters must be quoted in custom names",
                response, "a\\:b\\\\c\\,d");        
    }
    
    @Test
    public void testCustomNames() {
        final TestConfigManager tcm = new TestConfigManager();
        tcm.settings.put("plugin-Identd.advanced.alwaysOn", "true");
        tcm.settings.put("plugin-Identd.advanced.isHiddenUser", "false");
        tcm.settings.put("plugin-Identd.advanced.useCustomSystem", "true");
        tcm.settings.put("plugin-Identd.advanced.customSystem", "system");
        tcm.settings.put("plugin-Identd.general.useCustomName", "true");
        tcm.settings.put("plugin-Identd.general.customName", "name");
        
        final String response = IdentClient.getIdentResponse("50, 60", tcm);
        final String[] bits = response.split(":");
        
        assertTrue("Responses must include port pair",
                bits[0].matches("\\s*50\\s*,\\s*60\\s*"));
        assertEquals("Positive response must include USERID",
                "USERID", bits[1].trim());
        assertEquals("Must use custom system name", "system", bits[2].trim());
        assertEquals("Must use custom name", "name", bits[3].trim());
    }    
    
    private static void assertContains(final String msg, final String haystack,
            final String needle) {
        assertTrue(msg, haystack.indexOf(needle) > -1);
    }
    
    private static void assertStartsWith(final String msg, final String haystack,
            final String needle) {
        assertTrue(msg, haystack.startsWith(needle));
    }    
    
    private class TestConfigManager extends ConfigManager {
        
        public final Map<String, String> settings = new HashMap<String, String>();

        public TestConfigManager() {
            super("", "", "");
        }

        @Override
        public String getOption(String domain, String option) {
            if (settings.containsKey(domain + "." + option)) {
                return settings.get(domain + "." + option);
            } else {
                return super.getOption(domain, option);
            }
        }

        @Override
        public boolean hasOption(String domain, String option) {
            if (settings.containsKey(domain + "." + option)) {
                return true;
            } else {
                return super.hasOption(domain, option);
            }
        }
        
    }

}