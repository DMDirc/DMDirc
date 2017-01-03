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
import com.dmdirc.events.ProgramErrorEvent;
import com.dmdirc.events.eventbus.EventBus;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.tests.JimFsRule;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DiskLoggingErrorManagerTest {

    @Rule public final JimFsRule jimFsRule = new JimFsRule();

    @Mock private EventBus eventBus;
    @Mock private AggregateConfigProvider config;
    @Mock private ConfigBinder configBinder;
    @Mock private ProgramErrorEvent error;
    @Mock private ProgramError programError;

    private DiskLoggingErrorManager instance;

    @Before
    public void setUp() throws Exception {
        when(error.getTimestamp()).thenReturn(LocalDateTime.now());
        when(error.getError()).thenReturn(programError);
        when(programError.getThrowableAsString()).thenReturn(Optional.of("test"));
        when(programError.getLevel()).thenReturn(ErrorLevel.MEDIUM);
        when(config.getBinder()).thenReturn(configBinder);
        instance = new DiskLoggingErrorManager(jimFsRule.getPath("/errors"),
                eventBus);
    }

    @Test
    public void testInitialise() throws Exception {
        assertFalse(Files.exists(jimFsRule.getPath("/errors")));
        instance.initialise(config);
        verify(configBinder).bind(instance, DiskLoggingErrorManager.class);
        assertTrue(Files.exists(jimFsRule.getPath("/errors")));
        assertFalse(instance.isDirectoryError());
    }

    @Test
    public void testInitialiseFailed() throws Exception {
        // TODO: Test error condition creating directory
    }

    @Test
    public void testHandleErrorEvent() throws Exception {
        instance.initialise(config);
        instance.handleLoggingSetting(true);
        final String logName = error.getTimestamp() + "-" + error.getError().getLevel() + ".log";;
        assertFalse(Files.exists(jimFsRule.getPath("/errors", logName)));
        instance.handleErrorEvent(error);
        final Path errorPath = jimFsRule.getPath("/errors", logName);
        assertTrue(Files.exists(errorPath));
        assertTrue(Files.readAllLines(errorPath).contains("Level: Medium"));
    }

    @Test
    public void testHandleErrorEventNotLogging() throws Exception {
        instance.initialise(config);
        instance.handleLoggingSetting(false);
        final String logName = error.getTimestamp() + "-" + error.getError().getLevel() + ".log";;
        assertFalse(Files.exists(jimFsRule.getPath("/errors", logName)));
        instance.handleErrorEvent(error);
        assertFalse(Files.exists(jimFsRule.getPath("/errors", logName)));
    }

    @Test
    public void testHandledErrorWriting() throws Exception {
        // TODO: Test error condition on write
    }
}