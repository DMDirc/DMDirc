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

package com.dmdirc;

import com.dmdirc.events.AppErrorEvent;
import com.dmdirc.events.UserErrorEvent;
import com.dmdirc.logger.ErrorLevel;

import java.lang.reflect.Constructor;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.AppenderBase;

/**
 *
 */
public class ProgramErrorAppender extends AppenderBase<ILoggingEvent> {

    private DMDircMBassador eventBus;
    private Marker appError;
    private Marker userError;

    @Override
    public void start() {
        appError = MarkerFactory.getMarker("AppError");
        userError = MarkerFactory.getMarker("UserError");
        if (eventBus == null) {
            addError("No eventBus set for the appender named ["+ name +"].");
            return;
        }
        super.start();
    }

    @Override
    protected void append(final ILoggingEvent eventObject) {
        if (eventObject.getMarker() == null) {
            return;
        }
        if (eventObject.getMarker() == appError) {
            eventBus.publish(new AppErrorEvent(level(eventObject), throwable
                    (eventObject), eventObject.getFormattedMessage(), ""));
        }
        if (eventObject.getMarker() == userError) {
            eventBus.publish(new UserErrorEvent(level(eventObject), throwable
                    (eventObject), eventObject.getFormattedMessage(), ""));
        }
    }

    public void setEventBus(final DMDircMBassador eventBus) {
        this.eventBus = eventBus;
    }

    private ErrorLevel level(final ILoggingEvent eventObject) {
        final Level level = eventObject.getLevel();
        if (level.equals(Level.ERROR)) {
            return ErrorLevel.HIGH;
        } else if (level.equals(Level.WARN)) {
            return ErrorLevel.MEDIUM;
        } else if (level.equals(Level.INFO)) {
            return ErrorLevel.LOW;
        } else {
            return ErrorLevel.UNKNOWN;
        }
    }

    private Throwable throwable(final ILoggingEvent eventObject) {
        try {
            return convert(eventObject.getThrowableProxy());
        } catch (ReflectiveOperationException ex) {
            return new IllegalStateException("Error converting exception");
        }
    }

    @SuppressWarnings("unchecked")
    private Throwable convert(final IThrowableProxy proxy)
            throws ReflectiveOperationException {
        final Class<Throwable> clazz = (Class<Throwable>) Class.forName(proxy.getClassName());
        final Throwable throwable;
        if (proxy.getCause() == null) {
            final Constructor<Throwable> ctor = clazz.getDeclaredConstructor(String.class);
            throwable = ctor.newInstance(proxy.getMessage());
        } else {
            final Constructor<Throwable> ctor = clazz.getDeclaredConstructor(String.class,
                    Throwable.class);
            throwable = ctor.newInstance(proxy.getMessage(), convert(proxy.getCause()));
        }
        throwable.setStackTrace(convert(proxy.getStackTraceElementProxyArray()));
        return throwable;
    }

    private StackTraceElement[] convert(final StackTraceElementProxy... elements) {
        final StackTraceElement[] returnValue = new StackTraceElement[elements.length];
        for (int i = 0; i < elements.length; i++) {
            returnValue[i] = elements[i].getStackTraceElement();
        }
        return returnValue;
    }
}
