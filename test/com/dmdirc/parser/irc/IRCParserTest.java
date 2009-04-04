/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.parser.irc;

import com.dmdirc.harness.parser.TestIPrivateCTCP;
import com.dmdirc.harness.parser.TestParser;
import com.dmdirc.harness.parser.TestIConnectError;
import com.dmdirc.harness.parser.TestINoticeAuth;
import com.dmdirc.harness.parser.TestINumeric;
import com.dmdirc.harness.parser.TestIServerError;
import com.dmdirc.harness.parser.TestIPost005;
import com.dmdirc.harness.parser.TestIPrivateMessage;
import com.dmdirc.harness.parser.TestIPrivateAction;
import com.dmdirc.parser.irc.callbacks.CallbackNotFoundException;
import com.dmdirc.parser.irc.callbacks.interfaces.IAwayState;
import com.dmdirc.parser.irc.callbacks.interfaces.IChannelKick;

import com.dmdirc.parser.irc.callbacks.interfaces.IErrorInfo;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.TrustManager;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class IRCParserTest {

    @Test
    public void testIssue42() {
        // Invalid callback names are silently ignored instead of raising exceptions
        
        boolean res = false;

        try {
            final IRCParser myParser = new IRCParser();
            myParser.getCallbackManager().addCallback("non-existant", mock(IAwayState.class));
        } catch (CallbackNotFoundException ex) {
            res = true;
        }

        assertTrue("addCallback() should throw exception for non-existant callbacks", res);
    }

    @Test
    public void testIssue1674() {
        // parser nick change error with dual 001
        final IErrorInfo error = mock(IErrorInfo.class);

        final TestParser myParser = new TestParser();
        myParser.getCallbackManager().addCallback("onErrorInfo", error);
        myParser.injectConnectionStrings();
        myParser.nick = "nick2";
        myParser.injectConnectionStrings();
        myParser.injectLine(":nick2!ident@host NICK :nick");

        verify(error, never()).onErrorInfo((IRCParser) anyObject(), (ParserError) anyObject());
    }
    
    @Test
    public void testProxyPortWithBindIP() {
        final TestIConnectError tice = new TestIConnectError();
        final ServerInfo si = new ServerInfo();
        si.setProxyPort(155555);
        si.setUseSocks(true);
        
        final IRCParser myParser = new IRCParser(si);
        myParser.getCallbackManager().addCallback("onConnectError", tice);
        myParser.setBindIP("0.0.0.0");
        myParser.run();
        
        assertTrue("Using an invalid socks proxy port should raise a connect error event",
                tice.error);
    }

    @Test
    public void testTokeniser() {
        final String line1 = "a b c d e";
        final String line2 = "a b c :d e";
        final String line3 = ":a b:c :d e";
        final String line4 = null;

        final String[] res1 = IRCParser.tokeniseLine(line1);
        final String[] res2 = IRCParser.tokeniseLine(line2);
        final String[] res3 = IRCParser.tokeniseLine(line3);
        final String[] res4 = IRCParser.tokeniseLine(line4);

        assertTrue(Arrays.equals(res1, new String[]{"a", "b", "c", "d", "e"}));
        assertTrue(Arrays.equals(res2, new String[]{"a", "b", "c", "d e"}));
        assertTrue(Arrays.equals(res3, new String[]{":a", "b:c", "d e"}));
        assertTrue(Arrays.equals(res4, new String[]{""}));
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
        final TestIServerError test = new TestIServerError();

        final TestParser parser = new TestParser();
        parser.getCallbackManager().addCallback("onServerError", test);
        parser.injectLine("ERROR :You smell of cheese");

        assertNotNull(test.message);
        assertEquals("ERROR message should be passed to callback",
                "You smell of cheese", test.message);
    }

    @Test
    public void testAuthNotices() throws CallbackNotFoundException {
        final TestINoticeAuth test = new TestINoticeAuth();
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
    public void testPre001NickChange() throws CallbackNotFoundException {
        final TestINoticeAuth test = new TestINoticeAuth();
        final TestParser parser = new TestParser();
        parser.getCallbackManager().addCallback("onNoticeAuth", test);
        parser.sendConnectionStrings();
        parser.injectLine(":chris!@ NICK :user2");

        assertNull(test.message);
    }

    @Test
    public void testNumeric() throws CallbackNotFoundException {
        final TestINumeric test = new TestINumeric();
        final TestParser parser = new TestParser();
        parser.getCallbackManager().addCallback("onNumeric", test);

        parser.injectLine(":server 001 nick :Hi there, nick");

        assertEquals(1, test.numeric);
        assertTrue(Arrays.equals(new String[]{":server", "001", "nick", "Hi there, nick"},
                test.data));
    }

    @Test
    public void testPost005() throws CallbackNotFoundException {
        final TestIPost005 test = new TestIPost005();
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
    public void testPrivateMessages() throws CallbackNotFoundException {
        final TestParser parser = new TestParser();
        final TestIPrivateMessage ipmtest = new TestIPrivateMessage();
        final TestIPrivateAction ipatest = new TestIPrivateAction();
        final TestIPrivateCTCP ipctest = new TestIPrivateCTCP();

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
                assertEquals(1001l, item.getTime());
                assertFalse(gotOne);
                gotOne = true;
            } else if (item.getItem().equals("ban2!*@.host")) {
                assertEquals("bansetter2", item.getOwner());
                assertEquals(1002l, item.getTime());
                assertFalse(gotTwo);
                gotTwo = true;
            } else if (item.toString().equals("ban3!ident@*")) {
                assertEquals("bansetter3", item.getOwner());
                assertEquals(1003l, item.getTime());
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
        final IChannelKick ick = mock(IChannelKick.class);
        parser.injectConnectionStrings();

        parser.injectLine(":nick JOIN #D");
        parser.getCallbackManager().addCallback("onChannelKick", ick, "#D");
        parser.injectLine(":bar!me@moo KICK #D nick :Bye!");

        verify(ick).onChannelKick(same(parser), (ChannelInfo) anyObject(),
                (ChannelClientInfo) anyObject(), (ChannelClientInfo) anyObject(),
                anyString(), anyString());
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
    
    @Test
    public void testIllegalPort1() {
        final TestParser tp = new TestParser(new MyInfo(), new ServerInfo("127.0.0.1", 0, ""));
        final TestIConnectError tiei = new TestIConnectError();
        tp.getCallbackManager().addCallback("OnConnectError", tiei);
        tp.runSuper();
        assertTrue(tiei.error);
    }
    
    @Test
    public void testIllegalPort2() {
        final TestParser tp = new TestParser(new MyInfo(), new ServerInfo("127.0.0.1", 1, ""));
        final TestIConnectError tiei = new TestIConnectError();
        tp.getCallbackManager().addCallback("OnConnectError", tiei);
        tp.runSuper();
        assertTrue(tiei.error);
    }    
    
    @Test
    public void testIllegalPort3() {
        final TestParser tp = new TestParser(new MyInfo(), new ServerInfo("127.0.0.1", 65570, ""));
        final TestIConnectError tiei = new TestIConnectError();
        tp.getCallbackManager().addCallback("OnConnectError", tiei);
        tp.runSuper();
        assertTrue(tiei.error);
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

}
