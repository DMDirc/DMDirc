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
import com.dmdirc.events.AppErrorEvent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DMDircExceptionHandlerTest {

    @Mock private DMDircMBassador eventBus;
    @Captor private ArgumentCaptor<AppErrorEvent> event;
    private DMDircExceptionHandler eh;

    @Before
    public void setup() {
        eh = new DMDircExceptionHandler(eventBus);
    }

    @Test
    public void testUncaughtException() {
        final Exception exception = new UnsupportedOperationException();
        eh.uncaughtException(null, exception);
        verify(eventBus).publish(event.capture());
        assertEquals(ErrorLevel.HIGH, event.getValue().getLevel());
        assertEquals(exception, event.getValue().getThrowable());
        assertEquals(exception.toString(), event.getValue().getMessage());
        assertEquals("", event.getValue().getDetails());
    }

    @Test
    public void testUncaughtError() {
        final Error error = new OutOfMemoryError();
        eh.uncaughtException(null, error);
        verify(eventBus).publish(event.capture());
        assertEquals(ErrorLevel.FATAL, event.getValue().getLevel());
        assertEquals(error, event.getValue().getThrowable());
        assertEquals(error.toString(), event.getValue().getMessage());
        assertEquals("", event.getValue().getDetails());
    }

}
