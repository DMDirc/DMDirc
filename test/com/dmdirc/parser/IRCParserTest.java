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
import com.dmdirc.parser.callbacks.interfaces.IChannelKick;
import com.dmdirc.parser.callbacks.interfaces.IChannelSelfJoin;
import com.dmdirc.parser.callbacks.interfaces.INoticeAuth;
import com.dmdirc.parser.callbacks.interfaces.INumeric;
import com.dmdirc.parser.callbacks.interfaces.IPost005;
import com.dmdirc.parser.callbacks.interfaces.IPrivateAction;
import com.dmdirc.parser.callbacks.interfaces.IPrivateCTCP;
import com.dmdirc.parser.callbacks.interfaces.IPrivateMessage;
import com.dmdirc.parser.callbacks.interfaces.IServerError;

import java.util.Arrays;

import java.util.List;
import javax.net.ssl.TrustManager;

import org.junit.Test;
import static org.junit.Assert.*;

public class IRCParserTest extends junit.framework.TestCase {

    @Test
    public void testIssue042() {
        boolean res = false;

        try {
            final IRCParser myParser = new IRCParser();
            myParser.getCallbackManager().addCallback("non-existant",new IAwayState() {
                public void onAwayState(IRCParser tParser, boolean currentState,
                        String reason) {
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
    public void testNumeric() throws CallbackNotFoundException {
        final INTest test = new INTest();
        final TestParser parser = new TestParser();
        parser.getCallbackManager().addCallback("onNumeric", test);

        parser.injectLine(":server 001 nick :Hi there, nick");

        assertEquals(1, test.numeric);
        assertTrue(Arrays.equals(new String[]{":server", "001", "nick", "Hi there, nick"},
                test.data));
    }

    @Test
    public void testPost005() throws CallbackNotFoundException {
        final IPFTest test = new IPFTest();
        final TestParser parser = new TestParser();
        parser.getCallbackManager().addCallback("onPost005", test);

        final String[] strings = {
            "NOTICE AUTH :Blah, blah",
            ":server 020 * :Blah! Blah!",
            ":server 001 nick :Welcome to the Testing IRC Network, nick",
            ":server 002 nick :Your host is server.net, running version foo",
            "NOTICE AUTH :I'm a retarded server",
            ":server 003 nick :This server was created Sun Jan 6 2008 at 17:34:54 CET",
            ":server 004 nick server.net foo dioswkgxRXInP biklmnopstvrDcCNuMT bklov",
            ":server 005 nick WHOX WALLCHOPS WALLVOICES USERIP :are supported by this server",
            ":server 005 nick MAXNICKLEN=15 TOPICLEN=250 AWAYLEN=160 :are supported " +
                    "by this server",
            ":server 375 nick :zomg, motd!",
        };

        for (String string : strings) {
            assertFalse("OnPost005 fired too early", test.done);
            parser.injectLine(string);
        }

        assertTrue("OnPost005 not fired", test.done);
    }

    @Test
    public void test005Parsing() {
        final TestParser parser = new TestParser();

        final String[] strings = {
            ":server 001 nick :Welcome to the Testing IRC Network, nick",
            ":server 002 nick :Your host is server.net, running version foo",
            ":server 003 nick :This server was created Sun Jan 6 2008 at 17:34:54 CET",
            ":server 004 nick server.net foo dioswkgxRXInP biklmnopstvrDcCNuMT bklov",
            ":server 005 nick WHOX WALLCHOPS WALLVOICES NETWORK=moo :are supported by" +
                    " this server",
            ":server 005 nick MAXNICKLEN=15 MAXLIST=b:10,e:22,I:45 :are supported by" +
                    " this server",
            ":server 375 nick :zomg, motd!",
        };

        for (String string : strings) {
            parser.injectLine(string);
        }

        assertEquals(10, parser.getMaxListModes('b'));
        assertEquals(22, parser.getMaxListModes('e'));
        assertEquals(45, parser.getMaxListModes('I'));
        assertEquals("getMaxListModes should return 0 for unknowns;", 0,
                parser.getMaxListModes('z'));
        assertEquals("moo", parser.getNetworkName());
        assertEquals("server", parser.getServerName());
    }

    @Test
    public void testBindIP() {
        final TestParser parser = new TestParser();

        parser.setBindIP("abc.def.ghi.123");
        assertEquals("abc.def.ghi.123", parser.getBindIP());
    }

    @Test
    public void testCreateFake() {
        final TestParser parser = new TestParser();

        parser.setCreateFake(false);
        assertFalse(parser.getCreateFake());
        parser.setCreateFake(true);
        assertTrue(parser.getCreateFake());
    }

    @Test
    public void testAutoListMode() {
        final TestParser parser = new TestParser();

        parser.setAutoListMode(false);
        assertFalse(parser.getAutoListMode());
        parser.setAutoListMode(true);
        assertTrue(parser.getAutoListMode());
    }

    @Test
    public void testRemoveAfterCallback() {
        final TestParser parser = new TestParser();

        parser.setRemoveAfterCallback(false);
        assertFalse(parser.getRemoveAfterCallback());
        parser.setRemoveAfterCallback(true);
        assertTrue(parser.getRemoveAfterCallback());
    }

    @Test
    public void testAddLastLine() {
        final TestParser parser = new TestParser();

        parser.setAddLastLine(false);
        assertFalse(parser.getAddLastLine());
        parser.setAddLastLine(true);
        assertTrue(parser.getAddLastLine());
    }

    @Test
    public void testDisconnectOnFatal() {
        final TestParser parser = new TestParser();

        parser.setDisconnectOnFatal(false);
        assertFalse(parser.getDisconnectOnFatal());
        parser.setDisconnectOnFatal(true);
        assertTrue(parser.getDisconnectOnFatal());
    }

    @Test
    public void testTrustManager() {
        final TestParser parser = new TestParser();

        assertTrue(Arrays.equals(parser.getDefaultTrustManager(), parser.getTrustManager()));

        parser.setTrustManager(new TrustManager[0]);

        assertTrue(Arrays.equals(new TrustManager[0], parser.getTrustManager()));
    }

    @Test
    public void testJoinChannel() throws CallbackNotFoundException {
        final TestParser parser = new TestParser();
        final ICSJTest test = new ICSJTest();

        parser.injectConnectionStrings();
        parser.getCallbackManager().addCallback("onChannelSelfJoin", test);
        parser.injectLine(":nick JOIN #DMDirc_testing");

        assertNotNull(test.channel);
        assertEquals("#DMDirc_testing", test.channel.getName());
        assertEquals("#DMDirc_testing", test.channel.toString());
        assertEquals(parser, test.channel.getParser());
        assertEquals(1, parser.getChannels().size());
        assertTrue(parser.getChannels().contains(test.channel));
        assertEquals(test.channel, parser.getChannelInfo("#DMDirc_testing"));
    }

    @Test
    public void testPrivateMessages() throws CallbackNotFoundException {
        final TestParser parser = new TestParser();
        final IPMTest ipmtest = new IPMTest();
        final IPATest ipatest = new IPATest();
        final IPCTest ipctest = new IPCTest();

        parser.injectConnectionStrings();

        parser.getCallbackManager().addCallback("onPrivateMessage", ipmtest);
        parser.getCallbackManager().addCallback("onPrivateAction", ipatest);
        parser.getCallbackManager().addCallback("onPrivateCTCP", ipctest);

        parser.injectLine(":a!b@c PRIVMSG nick :Hello!");
        assertNotNull(ipmtest.host);
        assertNull(ipatest.host);
        assertNull(ipctest.host);
        assertEquals("a!b@c", ipmtest.host);
        assertEquals("Hello!", ipmtest.message);
        ipmtest.host = null;
        ipmtest.message = null;

        parser.injectLine(":a!b@c PRIVMSG nick :" + ((char) 1) + "ACTION meep" + ((char) 1));
        assertNull(ipmtest.host);
        assertNotNull(ipatest.host);
        assertNull(ipctest.host);
        assertEquals("a!b@c", ipatest.host);
        assertEquals("meep", ipatest.message);
        ipatest.host = null;
        ipatest.message = null;

        parser.injectLine(":a!b@c PRIVMSG nick :" + ((char) 1) + "FOO meep" + ((char) 1));
        assertNull(ipmtest.host);
        assertNull(ipatest.host);
        assertNotNull(ipctest.host);
        assertEquals("a!b@c", ipctest.host);
        assertEquals("FOO", ipctest.type);
        assertEquals("meep", ipctest.message);
    }

    private void testListModes(String numeric1, String numeric2, char mode) {
        final TestParser parser = new TestParser();
        parser.injectConnectionStrings();

        parser.injectLine(":nick JOIN #D");
        parser.injectLine(":server " + numeric1 + " nick #D ban1!ident@.host bansetter1 1001");
        parser.injectLine(":server " + numeric1 + " nick #D ban2!*@.host bansetter2 1002");
        parser.injectLine(":server " + numeric1 + " nick #D ban3!ident@* bansetter3 1003");
        parser.injectLine(":server " + numeric2 + " nick #D :End of Channel Something List");

        final List<ChannelListModeItem> items
                = parser.getChannelInfo("#D").getListModeParam(mode);

        assertEquals(3, items.size());
        boolean gotOne = false, gotTwo = false, gotThree = false;

        for (ChannelListModeItem item : items) {
            if (item.getItem().equals("ban1!ident@.host")) {
                assertEquals("bansetter1", item.getOwner());
                assertEquals(1001, item.getTime());
                assertFalse(gotOne);
                gotOne = true;
            } else if (item.getItem().equals("ban2!*@.host")) {
                assertEquals("bansetter2", item.getOwner());
                assertEquals(1002, item.getTime());
                assertFalse(gotTwo);
                gotTwo = true;
            } else if (item.toString().equals("ban3!ident@*")) {
                assertEquals("bansetter3", item.getOwner());
                assertEquals(1003, item.getTime());
                assertFalse(gotThree);
                gotThree = true;
            }
        }

        assertTrue(gotOne);
        assertTrue(gotTwo);
        assertTrue(gotThree);
    }

    @Test
    public void testNormalBans() {
        testListModes("367", "368", 'b');
    }

    @Test
    public void testInvexList() {
        testListModes("346", "347", 'I');
    }

    @Test
    public void testExemptList() {
        testListModes("348", "349", 'e');
    }

    @Test
    public void testReopList() {
        testListModes("344", "345", 'R');
    }

    @Test
    public void testGetParam() {
        assertEquals("abc def", TestParser.getParam("foo :abc def"));
        assertEquals("bar :abc def", TestParser.getParam("foo :bar :abc def"));
        assertEquals("abc def", TestParser.getParam("abc def"));
    }
    
    @Test
    public void testKick() throws CallbackNotFoundException {
        final TestParser parser = new TestParser();
        final ICKTest ick = new ICKTest();
        parser.injectConnectionStrings();

        parser.injectLine(":nick JOIN #D");
        parser.getCallbackManager().addCallback("onChannelKick", ick, "#D");
        parser.injectLine(":bar!me@moo KICK #D nick :Bye!");
     
        assertTrue("onChannelKick must be called", ick.called);
        assertNotNull("cKickedClient must not be null", ick.cKickedClient);
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

    private class INTest implements INumeric {
        int numeric = 0;
        String[] data;

        public void onNumeric(IRCParser tParser, int numeric, String[] token) {
            this.numeric = numeric;
            data = token;
        }
    }

    private class IPFTest implements IPost005 {
        boolean done = false;

        public void onPost005(IRCParser tParser) {
            done = true;
        }
    }

    private class ICSJTest implements IChannelSelfJoin {
        ChannelInfo channel = null;

        public void onChannelSelfJoin(IRCParser tParser, ChannelInfo cChannel) {
            channel = cChannel;
        }
    }

    private class IPMTest implements IPrivateMessage {
        String host = null, message = null;

        public void onPrivateMessage(IRCParser tParser, String sMessage, String sHost) {
            host = sHost;
            message = sMessage;
        }
    }

    private class IPCTest implements IPrivateCTCP {
        String type = null, message = null, host = null;

        public void onPrivateCTCP(IRCParser tParser, String sType, String sMessage,
                String sHost) {
            type = sType;
            message = sMessage;
            host = sHost;
        }
    }

    private class IPATest implements IPrivateAction {
        String message = null, host = null;

        public void onPrivateAction(IRCParser tParser, String sMessage, String sHost) {
            message = sMessage;
            host = sHost;
        }
    }
    
    private class ICKTest implements IChannelKick {
        ChannelClientInfo cKickedClient = null;
        boolean called = false;

        public void onChannelKick(IRCParser tParser, ChannelInfo cChannel,
                ChannelClientInfo cKickedClient, ChannelClientInfo cKickedByClient,
                String sReason, String sKickedByHost) {
            this.cKickedClient = cKickedClient;
            called = true;
        }
        
    }

}
