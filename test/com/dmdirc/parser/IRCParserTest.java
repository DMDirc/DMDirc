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

package com.dmdirc.parser;

import com.dmdirc.parser.callbacks.CallbackNotFoundException;
import com.dmdirc.parser.callbacks.interfaces.IAwayState;

import com.dmdirc.parser.callbacks.interfaces.INoticeAuth;
import com.dmdirc.parser.callbacks.interfaces.IServerError;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

public class IRCParserTest extends junit.framework.TestCase {
    
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
    public void testCaseConversion() {
        final IRCParser asciiParser = new IRCParser();
        asciiParser.updateCharArrays((byte) 0);

        final IRCParser rfcParser = new IRCParser();
        rfcParser.updateCharArrays((byte) 4);

        final IRCParser strictParser = new IRCParser();
        strictParser.updateCharArrays((byte) 3);

        final String[][] testcases = {
            {"12345", "12345", "12345", "12345"},
            {"HELLO", "hello", "hello", "hello"},
            {"^[[MOO]]^", "^[[moo]]^", "~{{moo}}~", "^{{moo}}^"},
            {"«—»", "«—»", "«—»", "«—»"}
        };

        for (String[] testcase : testcases) {
            final String asciiL = asciiParser.toLowerCase(testcase[0]);
            final String rfcL = rfcParser.toLowerCase(testcase[0]);
            final String strictL = strictParser.toLowerCase(testcase[0]);

            final String asciiU = asciiParser.toUpperCase(testcase[1]);
            final String rfcU = rfcParser.toUpperCase(testcase[2]);
            final String strictU = strictParser.toUpperCase(testcase[3]);
            
            assertEquals(testcase[1], asciiL);
            assertEquals(testcase[2], rfcL);
            assertEquals(testcase[3], strictL);
            
            assertTrue(asciiParser.equalsIgnoreCase(testcase[0], testcase[1]));
            assertTrue(rfcParser.equalsIgnoreCase(testcase[0], testcase[2]));
            assertTrue(strictParser.equalsIgnoreCase(testcase[0], testcase[3]));
            
            assertEquals(testcase[0], asciiU);
            assertEquals(testcase[0], rfcU);
            assertEquals(testcase[0], strictU);
        }
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

        assertTrue(Arrays.equals(res1, new String[]{"a", "b", "c", "d", "e"}));
        assertTrue(Arrays.equals(res2, new String[]{"a", "b", "c", "d e"}));
        assertTrue(Arrays.equals(res3, new String[]{":a", "b:c", "d e"}));
    }
    
    @Test
    public void testSendConnectionStrings1() {
        final ServerInfo serverInfo = new ServerInfo("irc.testing.dmdirc", 6667, "");
        final MyInfo myInfo = new MyInfo();
        myInfo.setNickname("Nickname");
        myInfo.setRealname("Real name");
        myInfo.setUsername("Username");
        
        final TestParser parser = new TestParser(myInfo, serverInfo);
        parser.sendConnectionStrings();
        
        assertEquals(2, parser.sentLines.size());
        
        assertTrue("Should send nickname line",
                Arrays.equals(parser.getLine(0), new String[]{"NICK", "Nickname"}));
        
        final String[] userParts = parser.getLine(1);
        assertEquals("First token should be USER", "USER", userParts[0]);
        assertEquals("USER should contain username", myInfo.getUsername().toLowerCase(),
                userParts[1].toLowerCase());
        assertEquals("USER should contain server name", serverInfo.getHost(), userParts[3]);
        assertEquals("USER should contain real name", "Real name", userParts[4]);
    }
    
    @Test
    public void testSendConnectionStrings2() {
        final ServerInfo serverInfo = new ServerInfo("irc.testing.dmdirc", 6667, "password");
        final MyInfo myInfo = new MyInfo();
        myInfo.setNickname("Nickname");
        myInfo.setRealname("Real name");
        myInfo.setUsername("Username");
        
        final TestParser parser = new TestParser(myInfo, serverInfo);
        parser.sendConnectionStrings();
        
        assertEquals(3, parser.sentLines.size());
        
        assertTrue("Should send password line",
                Arrays.equals(parser.getLine(0), new String[]{"PASS", "password"}));
    }    
    
    @Test
    public void testPingPong() {
        final TestParser parser = new TestParser();
        
        parser.injectLine("PING :flubadee7291");
        
        assertTrue("Should reply to PINGs with PONGs",
                Arrays.equals(parser.getLine(0), new String[]{"PONG", "flubadee7291"}));
    }
    
    @Test
    public void testError() throws CallbackNotFoundException {
        final ISETest test = new ISETest();

        final TestParser parser = new TestParser();
        parser.getCallbackManager().addCallback("onServerError", test);
        parser.injectLine("ERROR :You smell of cheese");
        
        assertNotNull(test.message);
        assertEquals("ERROR message should be passed to callback",
                "You smell of cheese", test.message);
    }
    
    @Test
    public void testAuthNotices() throws CallbackNotFoundException {
        final INATest test = new INATest();
        final TestParser parser = new TestParser();
        parser.getCallbackManager().addCallback("onNoticeAuth", test);
        parser.sendConnectionStrings();
        parser.injectLine("NOTICE AUTH :Random auth notice?");
        
        assertNotNull(test.message);
        assertEquals("Random auth notice?", test.message);
        
        test.message = null;
        
        parser.injectLine(":us.ircnet.org 020 * :Stupid notice");
        
        assertNotNull(test.message);
        assertEquals("Stupid notice", test.message);        
    }
    
    @Test
    public void testIRCds() {
        doIRCdTest("u2.10.12.10+snircd(1.3.4)", "snircd");
        doIRCdTest("u2.10.12.12", "ircu");
        doIRCdTest("hyperion-1.0.2b", "hyperion");
        doIRCdTest("hybrid-7.2.3", "hybrid");
        doIRCdTest("Unreal3.2.6", "unreal");
        doIRCdTest("bahamut-1.8(04)", "bahamut");
    }
    
    private void doIRCdTest(final String ircd, final String expected) {
        final TestParser parser = new TestParser();
        
        String[] strings = {
            ":server 001 nick :Welcome to the Testing IRC Network, nick",
            ":server 002 nick :Your host is server.net, running version %s",
            ":server 003 nick :This server was created Sun Jan 6 2008 at 17:34:54 CET",
            ":server 004 nick server.net %s dioswkgxRXInP biklmnopstvrDcCNuMT bklov"
        };
        
        for (String line : strings) {
            parser.injectLine(String.format(line, ircd));
        }
        
        assertEquals(ircd, parser.getIRCD(false));
        assertEquals(expected.toLowerCase(), parser.getIRCD(true).toLowerCase());
    }
    
    private class ISETest implements IServerError {
        String message = null;

        public void onServerError(IRCParser tParser, String sMessage) {
            message = sMessage;
        }
    }
    
    private class INATest implements INoticeAuth {
        String message = null;

        public void onNoticeAuth(IRCParser tParser, String sData) {
            message = sData;
        }
        
    }

}
