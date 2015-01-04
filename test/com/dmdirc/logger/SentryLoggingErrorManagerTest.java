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

package com.dmdirc.logger;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.config.ConfigBinder;
import com.dmdirc.events.ProgramErrorEvent;
import com.dmdirc.interfaces.config.AggregateConfigProvider;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SentryLoggingErrorManagerTest {

    @Mock private DMDircMBassador eventBus;
    @Mock private AggregateConfigProvider config;
    @Mock private ConfigBinder configBinder;
    @Mock private SentryErrorReporter sentryErrorReporter;
    @Mock private ProgramError appError;
    @Mock private ProgramErrorEvent appErrorEvent;
    @Mock private ProgramError userError;
    @Mock private ProgramErrorEvent userErrorEvent;
    private SentryLoggingErrorManager instance;

    @Before
    public void setUp() throws Exception {
        when(appErrorEvent.getError()).thenReturn(appError);
        when(appError.isAppError()).thenReturn(true);
        when(appError.getThrowable()).thenReturn(new IllegalStateException());
        when(appError.getTrace()).thenReturn(Lists.newArrayList("com.dmdirc.Main"));
        when(userErrorEvent.getError()).thenReturn(userError);
        when(userError.isAppError()).thenReturn(false);
        when(config.getBinder()).thenReturn(configBinder);
        instance = new SentryLoggingErrorManager(eventBus, config, sentryErrorReporter,
                MoreExecutors.newDirectExecutorService());
        instance.initialise();
        instance.handleSubmitErrors(true);
        instance.handleNoErrorReporting(false);
    }

    @Test
    public void testHandleErrorEvent() throws Exception {
        instance.handleErrorEvent(appErrorEvent);
        verify(sentryErrorReporter).sendException(anyString(), any(ErrorLevel.class),
                any(Date.class), any(Throwable.class), anyString());
    }

    @Test
    public void testHandleErrorEvent_DMDircEventQueue() throws Exception {
        when(appError.getTrace()).thenReturn(Lists.newArrayList("com.dmdirc.addons.ui_swing" +
                ".DMDircEventQueue", "java.somethingelse"));
        instance.handleErrorEvent(appErrorEvent);
        verify(sentryErrorReporter, never()).sendException(anyString(), any(ErrorLevel.class),
                any(Date.class), any(Throwable.class), anyString());
    }

    @Test
    public void testHandleErrorEvent_No_com_dmdirc() throws Exception {
        when(appError.getTrace()).thenReturn(Lists.newArrayList("java.util", "java.somethingelse"));
        instance.handleErrorEvent(appErrorEvent);
        verify(sentryErrorReporter, never()).sendException(anyString(), any(ErrorLevel.class),
                any(Date.class), any(Throwable.class), anyString());
    }

    @Test
    public void testHandledErrorEvent_UserError() throws Exception {
        instance.handleErrorEvent(userErrorEvent);
        verify(sentryErrorReporter, never()).sendException(anyString(), any(ErrorLevel.class),
                any(Date.class), any(Throwable.class), anyString());
    }

    @Test
    public void testHandledErrorEvent_EmptyTrace() throws Exception {
        when(appError.getTrace()).thenReturn(Lists.newArrayList());
        instance.handleErrorEvent(appErrorEvent);
        verify(sentryErrorReporter, never()).sendException(anyString(), any(ErrorLevel.class),
                any(Date.class), any(Throwable.class), anyString());
    }

    @Test
    public void testHandledErrorEvent_BannedException() throws Exception {
        final Throwable throwable = new OutOfMemoryError();
        when(appError.getThrowable()).thenReturn(throwable);
        instance.handleErrorEvent(appErrorEvent);
        verify(sentryErrorReporter, never()).sendException(anyString(), any(ErrorLevel.class),
                any(Date.class), eq(throwable), anyString());
    }

    @Test
    public void testSendReports_NoSubmit_NoReporting() throws Exception {
        instance.handleSubmitErrors(false);
        instance.handleNoErrorReporting(false);
        instance.handleErrorEvent(appErrorEvent);
        verify(sentryErrorReporter, never()).sendException(anyString(), any(ErrorLevel.class),
                any(Date.class), any(Throwable.class), anyString());
    }

    @Test
    public void testSendReports_Submit_NoError() throws Exception {
        instance.handleSubmitErrors(true);
        instance.handleNoErrorReporting(true);
        instance.handleErrorEvent(appErrorEvent);
        verify(sentryErrorReporter, never()).sendException(anyString(), any(ErrorLevel.class),
                any(Date.class), any(Throwable.class), anyString());
    }

    @Test
    public void testSendReports_Submit_NoReporting() throws Exception {
        instance.handleSubmitErrors(true);
        instance.handleNoErrorReporting(false);
        instance.handleErrorEvent(appErrorEvent);
        verify(sentryErrorReporter).sendException(anyString(), any(ErrorLevel.class),
                any(Date.class), any(Throwable.class), anyString());
    }

    @Test
    public void testSendReports_Submit_Error() throws Exception {
        instance.handleSubmitErrors(false);
        instance.handleNoErrorReporting(true);
        instance.handleErrorEvent(appErrorEvent);
        verify(sentryErrorReporter, never()).sendException(anyString(), any(ErrorLevel.class),
                any(Date.class), any(Throwable.class), anyString());
    }
}