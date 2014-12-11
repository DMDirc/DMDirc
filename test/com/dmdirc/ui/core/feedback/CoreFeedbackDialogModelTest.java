/*
 * Copyright (c) 2006-2014 DMDirc Developers
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

import com.dmdirc.interfaces.ConnectionManager;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.ui.FeedbackDialogModelListener;
import com.dmdirc.util.ClientInfo;

import java.nio.file.Path;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CoreFeedbackDialogModelTest {

    @Mock private AggregateConfigProvider config;
    @Mock private ConnectionManager connectionManager;
    @Mock private FeedbackSenderFactory feedbackSenderFactory;
    @Mock private FeedbackDialogModelListener listener;
    @Mock private Path path;
    @Mock private ClientInfo clientInfo;
    private static final String NAME = "Bob Dole";
    private static final String EMAIL = "bob@dole.com";
    private static final String FEEDBACK = "DMDirc Rocks.";

    @Test
    public void testName() {
        final CoreFeedbackDialogModel instance = new CoreFeedbackDialogModel(config,
                connectionManager, feedbackSenderFactory, path, clientInfo);
        assertEquals("testName", Optional.empty(), instance.getName());
        instance.setName(Optional.ofNullable(NAME));
        assertEquals("testName", Optional.ofNullable(NAME), instance.getName());
    }

    @Test
    public void testEmail() {
        final CoreFeedbackDialogModel instance = new CoreFeedbackDialogModel(config,
                connectionManager, feedbackSenderFactory, path, clientInfo);
        assertEquals("testEmail", Optional.empty(), instance.getEmail());
        instance.setEmail(Optional.ofNullable(EMAIL));
        assertEquals("testEmail", Optional.ofNullable(EMAIL), instance.getEmail());
    }

    @Test
    public void testFeedback() {
        final CoreFeedbackDialogModel instance = new CoreFeedbackDialogModel(config,
                connectionManager, feedbackSenderFactory, path, clientInfo);
        assertEquals("testFeedback", Optional.empty(), instance.getFeedback());
        instance.setFeedback(Optional.ofNullable(FEEDBACK));
        assertEquals("testFeedback", Optional.ofNullable(FEEDBACK), instance.getFeedback());
    }

    @Test
    public void testServerInfo() {
        final CoreFeedbackDialogModel instance = new CoreFeedbackDialogModel(config,
                connectionManager, feedbackSenderFactory, path, clientInfo);
        assertEquals("testServerInfo", false, instance.getIncludeServerInfo());
        instance.setIncludeServerInfo(true);
        assertEquals("testServerInfo", true, instance.getIncludeServerInfo());
    }

    @Test
    public void testDMDircInfo() {
        final CoreFeedbackDialogModel instance = new CoreFeedbackDialogModel(config,
                connectionManager, feedbackSenderFactory, path, clientInfo);
        assertEquals("testDMDircInfo", false, instance.getIncludeDMDircInfo());
        instance.setIncludeDMDircInfo(true);
        assertEquals("testDMDircInfo", true, instance.getIncludeDMDircInfo());
    }

    @Test
    public void testSave() {
        // TODO: fix ClientInfo being static somehow and test.
    }

    @Test
    public void testNameListener() {
        final CoreFeedbackDialogModel instance = new CoreFeedbackDialogModel(config,
                connectionManager, feedbackSenderFactory, path, clientInfo);
        instance.addListener(listener);
        instance.setName(Optional.ofNullable("Bob Dole"));
        verify(listener).nameChanged(Optional.ofNullable("Bob Dole"));
    }

    @Test
    public void testEmailListener() {
        final CoreFeedbackDialogModel instance = new CoreFeedbackDialogModel(config,
                connectionManager, feedbackSenderFactory, path, clientInfo);
        instance.addListener(listener);
        instance.setEmail(Optional.ofNullable("bob@dole.com"));
        verify(listener).emailChanged(Optional.ofNullable("bob@dole.com"));
    }

    @Test
    public void testFeedbackListener() {
        final CoreFeedbackDialogModel instance = new CoreFeedbackDialogModel(config,
                connectionManager, feedbackSenderFactory, path, clientInfo);
        instance.addListener(listener);
        instance.setFeedback(Optional.ofNullable("DMDirc Rocks."));
        verify(listener).feedbackChanged(Optional.ofNullable("DMDirc Rocks."));
    }

    @Test
    public void testServerInfoListener() {
        final CoreFeedbackDialogModel instance = new CoreFeedbackDialogModel(config,
                connectionManager, feedbackSenderFactory, path, clientInfo);
        instance.addListener(listener);
        instance.setIncludeServerInfo(true);
        verify(listener).includeServerInfoChanged(true);
    }

    @Test
    public void testDMDircInfoListener() {
        final CoreFeedbackDialogModel instance = new CoreFeedbackDialogModel(config,
                connectionManager, feedbackSenderFactory, path, clientInfo);
        instance.addListener(listener);
        instance.setIncludeDMDircInfo(true);
        verify(listener).includeDMDircInfoChanged(true);
    }

}
