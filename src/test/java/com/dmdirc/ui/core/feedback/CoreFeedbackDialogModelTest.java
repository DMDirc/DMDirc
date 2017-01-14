/*
 * Copyright (c) 2006-2017 DMDirc Developers
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

import com.dmdirc.interfaces.ui.FeedbackDialogModelListener;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CoreFeedbackDialogModelTest {

    private static final String NAME = "Bob Dole";
    private static final String EMAIL = "bob@dole.com";
    private static final String FEEDBACK = "DMDirc Rocks.";

    @Mock private FeedbackSenderFactory feedbackSenderFactory;
    @Mock private FeedbackDialogModelListener listener;
    @Mock private FeedbackHelper feedbackHelper;

    private CoreFeedbackDialogModel instance;

    @Before
    public void setup() {
        instance = new CoreFeedbackDialogModel(feedbackSenderFactory, feedbackHelper);
    }

    @Test
    public void testName() {
        assertEquals("testName", Optional.<String>empty(), instance.getName());
        instance.setName(Optional.of(NAME));
        assertEquals("testName", Optional.of(NAME), instance.getName());
    }

    @Test
    public void testEmail() {
        assertEquals("testEmail", Optional.<String>empty(), instance.getEmail());
        instance.setEmail(Optional.of(EMAIL));
        assertEquals("testEmail", Optional.of(EMAIL), instance.getEmail());
    }

    @Test
    public void testFeedback() {
        assertEquals("testFeedback", Optional.<String>empty(), instance.getFeedback());
        instance.setFeedback(Optional.of(FEEDBACK));
        assertEquals("testFeedback", Optional.of(FEEDBACK), instance.getFeedback());
    }

    @Test
    public void testServerInfo() {
        assertFalse("testServerInfo", instance.getIncludeServerInfo());
        instance.setIncludeServerInfo(true);
        assertTrue("testServerInfo", instance.getIncludeServerInfo());
    }

    @Test
    public void testDMDircInfo() {
        assertFalse("testDMDircInfo", instance.getIncludeDMDircInfo());
        instance.setIncludeDMDircInfo(true);
        assertTrue("testDMDircInfo", instance.getIncludeDMDircInfo());
    }

    @Test
    public void testSaveWithoutServerWithoutDMDirc() {
        instance.setIncludeDMDircInfo(false);
        instance.setIncludeServerInfo(false);
        instance.setName(Optional.of(NAME));
        instance.setEmail(Optional.of(EMAIL));
        instance.setFeedback(Optional.of(FEEDBACK));
        instance.save();
        verify(feedbackSenderFactory).getFeedbackSender(NAME, EMAIL, FEEDBACK,
                feedbackHelper.getVersion(), "", "");
    }

    @Test
    public void testSaveWithoutServerWithDMDirc() {
        instance.setIncludeDMDircInfo(true);
        instance.setIncludeServerInfo(false);
        instance.setName(Optional.of(NAME));
        instance.setEmail(Optional.of(EMAIL));
        instance.setFeedback(Optional.of(FEEDBACK));
        instance.save();
        verify(feedbackSenderFactory).getFeedbackSender(NAME, EMAIL, FEEDBACK,
                feedbackHelper.getVersion(), "", feedbackHelper.getDMDircInfo());
    }

    @Test
    public void testSaveWithoutDMDircWithServer() {
        instance.setIncludeDMDircInfo(false);
        instance.setIncludeServerInfo(true);
        instance.setName(Optional.of(NAME));
        instance.setEmail(Optional.of(EMAIL));
        instance.setFeedback(Optional.of(FEEDBACK));
        instance.save();
        verify(feedbackSenderFactory).getFeedbackSender(NAME, EMAIL, FEEDBACK,
                feedbackHelper.getVersion(), feedbackHelper.getServerInfo(), "");
    }

    @Test
    public void testSaveWithServerWithDMDirc() {
        instance.setIncludeDMDircInfo(true);
        instance.setIncludeServerInfo(true);
        instance.setName(Optional.of(NAME));
        instance.setEmail(Optional.of(EMAIL));
        instance.setFeedback(Optional.of(FEEDBACK));
        instance.save();
        verify(feedbackSenderFactory).getFeedbackSender(NAME, EMAIL, FEEDBACK,
                feedbackHelper.getVersion(), feedbackHelper.getServerInfo(),
                feedbackHelper.getDMDircInfo());
    }

    @Test
    public void testNameListener() {
        instance.addListener(listener);
        instance.setName(Optional.of(NAME));
        verify(listener).nameChanged(Optional.of(NAME));
    }

    @Test
    public void testEmailListener() {
        instance.addListener(listener);
        instance.setEmail(Optional.of(EMAIL));
        verify(listener).emailChanged(Optional.of(EMAIL));
    }

    @Test
    public void testFeedbackListener() {
        instance.addListener(listener);
        instance.setFeedback(Optional.of(FEEDBACK));
        verify(listener).feedbackChanged(Optional.of(FEEDBACK));
    }

    @Test
    public void testServerInfoListener() {
        instance.addListener(listener);
        instance.setIncludeServerInfo(true);
        verify(listener).includeServerInfoChanged(true);
    }

    @Test
    public void testDMDircInfoListener() {
        instance.addListener(listener);
        instance.setIncludeDMDircInfo(true);
        verify(listener).includeDMDircInfoChanged(true);
    }

}
