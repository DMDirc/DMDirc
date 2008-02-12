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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

public class ChannelInfoTest extends junit.framework.TestCase {

    final ChannelInfo ci = new ChannelInfo(null, "name");

    @Test
    public void testGetName() {
        assertEquals("name", ci.getName());
    }

    @Test
    public void testAddingNames() {
        assertTrue(ci.getAddingNames());

        ci.setAddingNames(false);

        assertFalse(ci.getAddingNames());
    }

    @Test
    public void testMap() {
        final Map map = new HashMap();

        ci.setMap(map);

        assertEquals(map, ci.getMap());
    }

    @Test
    public void testCreateTime() {
        ci.setCreateTime(12345l);

        assertEquals(12345l, ci.getCreateTime());
    }

    @Test
    public void testTopicTime() {
        ci.setTopicTime(12345l);

        assertEquals(12345l, ci.getTopicTime());
    }

    @Test
    public void testTopic() {
        ci.setTopic("abcdef");

        assertEquals("abcdef", ci.getTopic());
    }

    @Test
    public void testSendMessage() {
        final TestParser parser = new TestParser();
        getChannelInfo(parser).sendMessage("hello");

        assertEquals("PRIVMSG #DMDirc_testing :hello", parser.sentLines.get(0));
    }

    @Test
    public void testSendNotice() {
        final TestParser parser = new TestParser();
        getChannelInfo(parser).sendNotice("hello");

        assertEquals("NOTICE #DMDirc_testing :hello", parser.sentLines.get(0));
    }

    @Test
    public void testSendCTCP() {
        final TestParser parser = new TestParser();
        getChannelInfo(parser).sendCTCP("type", "hello");

        assertEquals("PRIVMSG #DMDirc_testing :" + ((char) 1) + "TYPE hello" + ((char) 1),
                parser.sentLines.get(0));
    }

    @Test
    public void testSendCTCPEmpty() {
        final TestParser parser = new TestParser();
        getChannelInfo(parser).sendCTCP("type", "");

        assertEquals("PRIVMSG #DMDirc_testing :" + ((char) 1) + "TYPE" + ((char) 1),
                parser.sentLines.get(0));
    }

    @Test
    public void testSendAction() {
        final TestParser parser = new TestParser();
        getChannelInfo(parser).sendAction("moo");

        assertEquals("PRIVMSG #DMDirc_testing :" + ((char) 1) + "ACTION moo" + ((char) 1),
                parser.sentLines.get(0));
    }

    @Test
    public void testSendCTCPReply() {
        final TestParser parser = new TestParser();
        getChannelInfo(parser).sendCTCPReply("type", "moo");

        assertEquals("NOTICE #DMDirc_testing :" + ((char) 1) + "TYPE moo" + ((char) 1),
                parser.sentLines.get(0));
    }

    @Test
    public void testSendCTCPReplyEmpty() {
        final TestParser parser = new TestParser();
        getChannelInfo(parser).sendCTCPReply("type", "");

        assertEquals("NOTICE #DMDirc_testing :" + ((char) 1) + "TYPE" + ((char) 1),
                parser.sentLines.get(0));
    }
    
    @Test
    public void testSendEmptyMessages() {
        final TestParser parser = new TestParser();
        final ChannelInfo info = getChannelInfo(parser);
        
        info.sendAction("");
        info.sendCTCP("", "");
        info.sendCTCPReply("", "");
        info.sendMessage("");
        info.sendNotice("");

        assertEquals(0, parser.sentLines.size());
    }    

    private ChannelInfo getChannelInfo(final TestParser parser) {
        parser.injectConnectionStrings();
        parser.injectLine(":nick JOIN #DMDirc_testing");

        parser.sentLines.clear();
        return parser.getChannels().iterator().next();
    }

}