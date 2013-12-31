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
package com.dmdirc.logger;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DMDircExceptionHandlerTest {

    @Mock private ErrorManager errorManager;
    private DMDircExceptionHandler eh;

    @Before
    public void setup() {
        Logger.setErrorManager(errorManager);
        eh = new DMDircExceptionHandler();
    }

    @Test
    public void testUncaughtException() {
        final Exception exception = new Exception();
        eh.uncaughtException(null, exception);
        verify(errorManager).addError(eq(ErrorLevel.HIGH), anyString(), same(exception), eq(true));
    }

    @Test
    public void testUncaughtError() {
        final Error error = new Error();
        eh.uncaughtException(null, error);
        verify(errorManager).addError(eq(ErrorLevel.FATAL), anyString(), same(error), eq(true));
    }

}
