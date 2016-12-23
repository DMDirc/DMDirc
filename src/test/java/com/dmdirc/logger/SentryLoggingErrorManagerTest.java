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

import com.dmdirc.config.ConfigBinder;
import com.dmdirc.events.ProgramErrorAddedEvent;
import com.dmdirc.interfaces.EventBus;
import com.dmdirc.interfaces.config.AggregateConfigProvider;

import com.google.common.util.concurrent.MoreExecutors;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SentryLoggingErrorManagerTest {

    @Mock private EventBus eventBus;
    @Mock private AggregateConfigProvider config;
    @Mock private ConfigBinder configBinder;
    @Mock private SentryErrorReporter sentryErrorReporter;
    @Mock private ProgramError appError;
    @Mock private ProgramErrorAddedEvent appErrorEvent;
    @Mock private ProgramError userError;
    @Mock private ProgramErrorAddedEvent userErrorEvent;
    private SentryLoggingErrorManager instance;

    @Before
    public void setUp() throws Exception {
        when(appErrorEvent.getError()).thenReturn(appError);
        when(appError.isAppError()).thenReturn(true);
        when(appError.getThrowable()).thenReturn(Optional.of(new IllegalStateException()));
        when(userErrorEvent.getError()).thenReturn(userError);
        when(userError.isAppError()).thenReturn(false);
        when(userError.getThrowable()).thenReturn(Optional.of(new IllegalStateException()));
        when(config.getBinder()).thenReturn(configBinder);
        instance = new SentryLoggingErrorManager(eventBus, sentryErrorReporter,
                MoreExecutors.newDirectExecutorService());
        instance.initialise(config);
        instance.handleSubmitErrors(true);
        instance.handleNoErrorReporting(false);
    }

    @Test
    public void testHandleErrorEvent() throws Exception {
        instance.handleErrorEvent(appErrorEvent);
        verify(sentryErrorReporter).sendException(isNull(), isNull(), isNull(), any());
    }

    @Test
    public void testHandledErrorEvent_UserError() throws Exception {
        instance.handleErrorEvent(userErrorEvent);
        verify(sentryErrorReporter, never()).sendException(anyString(), any(ErrorLevel.class),
                any(LocalDateTime.class), any());
    }

    @Test
    public void testHandledErrorEvent_BannedException() throws Exception {
        final Throwable throwable = new OutOfMemoryError();
        when(appError.getThrowable()).thenReturn(Optional.of(throwable));
        instance.handleErrorEvent(appErrorEvent);
        verify(sentryErrorReporter, never()).sendException(anyString(), any(ErrorLevel.class),
                any(LocalDateTime.class), eq(Optional.of(throwable)));
    }

    @Test
    public void testSendReports_NoSubmit_NoReporting() throws Exception {
        instance.handleSubmitErrors(false);
        instance.handleNoErrorReporting(false);
        instance.handleErrorEvent(appErrorEvent);
        verify(sentryErrorReporter, never()).sendException(anyString(), any(ErrorLevel.class),
                any(LocalDateTime.class), any());
    }

    @Test
    public void testSendReports_Submit_NoError() throws Exception {
        instance.handleSubmitErrors(true);
        instance.handleNoErrorReporting(true);
        instance.handleErrorEvent(appErrorEvent);
        verify(sentryErrorReporter, never()).sendException(anyString(), any(ErrorLevel.class),
                any(LocalDateTime.class), any());
    }

    @Test
    public void testSendReports_Submit_NoReporting() throws Exception {
        instance.handleSubmitErrors(true);
        instance.handleNoErrorReporting(false);
        instance.handleErrorEvent(appErrorEvent);
        verify(sentryErrorReporter).sendException(isNull(), isNull(), isNull(), any());
    }

    @Test
    public void testSendReports_Submit_Error() throws Exception {
        instance.handleSubmitErrors(false);
        instance.handleNoErrorReporting(true);
        instance.handleErrorEvent(appErrorEvent);
        verify(sentryErrorReporter, never()).sendException(anyString(), any(ErrorLevel.class),
                any(LocalDateTime.class), any());
    }
}