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

import com.dmdirc.util.ClientInfo;
import com.getsentry.raven.Raven;
import com.getsentry.raven.RavenFactory;
import com.getsentry.raven.event.Event;
import com.getsentry.raven.event.interfaces.ExceptionInterface;
import com.getsentry.raven.event.interfaces.SentryInterface;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SentryErrorReporterTest {

    private static RavenFactory ravenFactory;
    @Mock private Raven raven;
    @Mock private ClientInfo clientInfo;
    @Captor private ArgumentCaptor<Event> eventCaptor;
    private Optional<Throwable> throwable;
    private SentryErrorReporter sentryErrorReporter;

    @BeforeClass
    public static void setUpClass() {
        // Static calls are horrible.
        ravenFactory = mock(RavenFactory.class);
        RavenFactory.registerFactory(ravenFactory);
    }

    @Before
    public void setUp() {
        when(ravenFactory.createRavenInstance(any())).thenReturn(raven);
        throwable = Optional.of(new IllegalArgumentException());
        sentryErrorReporter = new SentryErrorReporter(clientInfo);
    }

    @Test
    public void testSendsMessage() {
        sentryErrorReporter.sendException("message 123", ErrorLevel.MEDIUM, LocalDateTime.now(),
                throwable);
        verify(raven).sendEvent(eventCaptor.capture());
        assertEquals("message 123", eventCaptor.getValue().getMessage());
    }

    @Test
    public void testSendsTimestamp() {
        final LocalDateTime date = LocalDateTime.ofEpochSecond(56185920L, 0, ZoneOffset.UTC);
        sentryErrorReporter.sendException("message 123", ErrorLevel.MEDIUM, date, throwable);
        verify(raven).sendEvent(eventCaptor.capture());
        assertEquals(56185920000L, eventCaptor.getValue().getTimestamp().getTime());
    }

    @Test
    public void testSendsLowLevelAsInfo() {
        sentryErrorReporter.sendException("message 123", ErrorLevel.LOW, LocalDateTime.now(),
                throwable);
        verify(raven).sendEvent(eventCaptor.capture());
        assertEquals(Event.Level.INFO, eventCaptor.getValue().getLevel());
    }

    @Test
    public void testSendsMediumLevelAsWarning() {
        sentryErrorReporter.sendException("message 123", ErrorLevel.MEDIUM, LocalDateTime.now(),
                throwable);
        verify(raven).sendEvent(eventCaptor.capture());
        assertEquals(Event.Level.WARNING, eventCaptor.getValue().getLevel());
    }

    @Test
    public void testSendsHighLevelAsError() {
        sentryErrorReporter.sendException("message 123", ErrorLevel.HIGH, LocalDateTime.now(),
                throwable);
        verify(raven).sendEvent(eventCaptor.capture());
        assertEquals(Event.Level.ERROR, eventCaptor.getValue().getLevel());
    }

    @Test
    public void testSendsFatalLevelAsFatal() {
        sentryErrorReporter.sendException("message 123", ErrorLevel.FATAL, LocalDateTime.now(),
                throwable);
        verify(raven).sendEvent(eventCaptor.capture());
        assertEquals(Event.Level.FATAL, eventCaptor.getValue().getLevel());
    }

    @Test
    public void testSendsUnknownLevelAsInfo() {
        sentryErrorReporter.sendException("message 123", ErrorLevel.UNKNOWN, LocalDateTime.now(),
                throwable);
        verify(raven).sendEvent(eventCaptor.capture());
        assertEquals(Event.Level.INFO, eventCaptor.getValue().getLevel());
    }

    @Test
    public void testAddsExceptionInterface() {
        final Exception exception = new IndexOutOfBoundsException("Message blah");
        sentryErrorReporter.sendException("message 123", ErrorLevel.UNKNOWN, LocalDateTime.now(),
                Optional.of(exception));
        verify(raven).sendEvent(eventCaptor.capture());
        final SentryInterface iface = eventCaptor.getValue().getSentryInterfaces()
                .get(ExceptionInterface.EXCEPTION_INTERFACE);
        assertNotNull(iface);
        assertTrue(iface instanceof ExceptionInterface);
        assertEquals(exception.getMessage(),
                ((ExceptionInterface) iface).getExceptions().getFirst().getExceptionMessage());
    }

}
