/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

package com.dmdirc.ui.core.feedback;

import com.dmdirc.events.StatusBarMessageEvent;
import com.dmdirc.events.eventbus.EventBus;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.util.io.Downloader;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FeedbackSenderTest {

    private static final String NAME = "Bob Dole";
    private static final String EMAIL = "bob@dole.com";
    private static final String FEEDBACK = "DMDirc Rocks.";
    private static final String VERSION = "0.1";
    private static final String SERVER_INFO = "serverInfo";
    private static final String DMDIRC_INFO = "dmdircInfo";

    @Mock private AggregateConfigProvider config;
    @Mock private Downloader downloader;
    @Mock private EventBus eventBus;
    @Captor private ArgumentCaptor<StatusBarMessageEvent> messageEvent;

    private FeedbackSender feedbackSender;
    private FeedbackSenderFactory feedbackSenderFactory;
    private Map<String, String> expectedInfo;

    @Before
    public void setUp() throws Exception {
        feedbackSenderFactory = new FeedbackSenderFactory(config, downloader, eventBus);
        when(downloader.getPage(anyString(), anyMap())).thenReturn(Lists.newArrayList("Success."));
        expectedInfo = Maps.newHashMap(ImmutableMap.<String, String>builder()
                .put("name", NAME).put("email", EMAIL)
                .put("feedback", FEEDBACK)
                .put("version", VERSION)
                .put("serverInfo", SERVER_INFO)
                .put("dmdircInfo", DMDIRC_INFO)
                .build());
    }

    @Test
    public void testRun_WithAll() throws Exception {
        feedbackSender = feedbackSenderFactory.getFeedbackSender(NAME, EMAIL, FEEDBACK, VERSION,
                SERVER_INFO, DMDIRC_INFO);
        feedbackSender.run();
        verify(downloader).getPage("https://feedback.dmdirc.com/dialog/", expectedInfo);
        verify(eventBus).publishAsync(messageEvent.capture());
        assertEquals("Success.", messageEvent.getValue().getMessage().getMessage());
    }

    @Test
    public void testRun_WithoutName() throws Exception {
        feedbackSender = feedbackSenderFactory.getFeedbackSender("", EMAIL, FEEDBACK, VERSION,
                SERVER_INFO, DMDIRC_INFO);
        expectedInfo.remove("name");
        feedbackSender.run();
        verify(downloader).getPage("https://feedback.dmdirc.com/dialog/", expectedInfo);
        verify(eventBus).publishAsync(messageEvent.capture());
        assertEquals("Success.", messageEvent.getValue().getMessage().getMessage());
    }

    @Test
    public void testRun_WithoutEmail() throws Exception {
        feedbackSender = feedbackSenderFactory.getFeedbackSender(NAME, "", FEEDBACK, VERSION,
                SERVER_INFO, DMDIRC_INFO);
        expectedInfo.remove("email");
        feedbackSender.run();
        verify(downloader).getPage("https://feedback.dmdirc.com/dialog/", expectedInfo);
        verify(eventBus).publishAsync(messageEvent.capture());
        assertEquals("Success.", messageEvent.getValue().getMessage().getMessage());
    }

    @Test
    public void testRun_WithoutFeedback() throws Exception {
        feedbackSender = feedbackSenderFactory.getFeedbackSender(NAME, EMAIL, "", VERSION,
                SERVER_INFO, DMDIRC_INFO);
        expectedInfo.remove("feedback");
        feedbackSender.run();
        verify(downloader).getPage("https://feedback.dmdirc.com/dialog/", expectedInfo);
        verify(eventBus).publishAsync(messageEvent.capture());
        assertEquals("Success.", messageEvent.getValue().getMessage().getMessage());
    }

    @Test
    public void testRun_WithoutVersion() throws Exception {
        feedbackSender = feedbackSenderFactory.getFeedbackSender(NAME, EMAIL, FEEDBACK, "",
                SERVER_INFO, DMDIRC_INFO);
        expectedInfo.remove("version");
        feedbackSender.run();
        verify(downloader).getPage("https://feedback.dmdirc.com/dialog/", expectedInfo);
        verify(eventBus).publishAsync(messageEvent.capture());
        assertEquals("Success.", messageEvent.getValue().getMessage().getMessage());
    }

    @Test
    public void testRun_WithoutServerInfo() throws Exception {
        feedbackSender = feedbackSenderFactory.getFeedbackSender(NAME, EMAIL, FEEDBACK, VERSION,
                "", DMDIRC_INFO);
        expectedInfo.remove("serverInfo");
        feedbackSender.run();
        verify(downloader).getPage("https://feedback.dmdirc.com/dialog/", expectedInfo);
        verify(eventBus).publishAsync(messageEvent.capture());
        assertEquals("Success.", messageEvent.getValue().getMessage().getMessage());
    }

    @Test
    public void testRun_WithoutDMDircInfo() throws Exception {
        feedbackSender = feedbackSenderFactory.getFeedbackSender(NAME, EMAIL, FEEDBACK, VERSION,
                SERVER_INFO, "");
        expectedInfo.remove("dmdircInfo");
        feedbackSender.run();
        verify(downloader).getPage("https://feedback.dmdirc.com/dialog/", expectedInfo);
        verify(eventBus).publishAsync(messageEvent.capture());
        assertEquals("Success.", messageEvent.getValue().getMessage().getMessage());
    }

    @Test
    public void testRun_WithFail() throws Exception {
        when(downloader.getPage(anyString(), anyMap())).thenReturn(Lists.newArrayList());
        feedbackSender = feedbackSenderFactory.getFeedbackSender(NAME, EMAIL, FEEDBACK, VERSION,
                SERVER_INFO, DMDIRC_INFO);
        feedbackSender.run();
        verify(downloader).getPage("https://feedback.dmdirc.com/dialog/", expectedInfo);
        verify(eventBus).publishAsync(messageEvent.capture());
        assertEquals("Feedback failure: Unknown response from the server.",
                messageEvent.getValue().getMessage().getMessage());
    }

    @Test
    public void testRun_WithException() throws Exception {
        when(downloader.getPage(anyString(), anyMap())).thenThrow(new IOException("Fail."));
        feedbackSender = feedbackSenderFactory.getFeedbackSender(NAME, EMAIL, FEEDBACK, VERSION,
                SERVER_INFO, DMDIRC_INFO);
        feedbackSender.run();
        verify(downloader).getPage("https://feedback.dmdirc.com/dialog/", expectedInfo);
        verify(eventBus).publishAsync(messageEvent.capture());
        assertEquals("Feedback failure: Fail.", messageEvent.getValue().getMessage().getMessage());
    }
}