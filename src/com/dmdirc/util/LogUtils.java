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

package com.dmdirc.util;

import com.dmdirc.logger.ErrorLevel;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Utility methods for logging in the client.
 */
public final class LogUtils {

    public static final Marker USER_ERROR = MarkerFactory.getMarker("UserError");
    public static final Marker APP_ERROR = MarkerFactory.getMarker("AppError");
    public static final Marker FATAL_USER_ERROR = MarkerFactory.getMarker("FatalUserError");
    public static final Marker FATAL_APP_ERROR = MarkerFactory.getMarker("FatalAppError");

    private LogUtils() {
    }

    /**
     * Converts a SLF4J {@link Level} into a DMDirc {@link ErrorLevel}
     *
     * @param level Error to convert
     *
     * @return Converted error
     */
    @Nonnull
    public static ErrorLevel getErrorLevel(@Nonnull final Level level) {
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

    /**
     * Converts the {@link IThrowableProxy} from an SLF4J {@link ILoggingEvent} into a
     * {@link Throwable}.
     *
     * @param eventObject Event containing the {@link IThrowableProxy}
     *
     * @return {@link Throwable} representation of the {@link IThrowableProxy} or {@code null} if
     *         there is no {@link IThrowableProxy} present.
     */
    @Nullable
    public static Throwable getThrowable(final ILoggingEvent eventObject) {
        if (eventObject.getThrowableProxy() == null) {
            return null;
        }
        try {
            return getThrowable(eventObject.getThrowableProxy());
        } catch (ReflectiveOperationException ex) {
            return new IllegalStateException("Error converting exception", ex);
        }
    }

    /**
     * Converts a SLF4J {@link IThrowableProxy} into a {@link Throwable}.
     *
     * @param proxy {@link IThrowableProxy} to convert
     *
     * @return {@link Throwable} representation of the {@link IThrowableProxy}
     *
     * @throws ReflectiveOperationException If an error ocurred creating the real {@link Throwable}
     */
    @Nonnull
    public static Throwable getThrowable(@Nonnull final IThrowableProxy proxy)
            throws ReflectiveOperationException {
        final Class<? extends Throwable> clazz = getThrowableClass(proxy.getClassName());
        Throwable throwable = null;
        if (proxy.getCause() == null) {
            // Just find a constructor that takes a string.
            final Constructor<? extends Throwable> ctor =
                    clazz.getDeclaredConstructor(String.class);
            throwable = ctor.newInstance(proxy.getMessage());
        } else if (clazz == InvocationTargetException.class) {
            // We don't care about the InvocationTargetException, unwrap it.
            return getThrowable(proxy.getCause());
        } else {
            // Try and find a constructor that takes a string and a throwable.
            for (Constructor<?> ctor : clazz.getDeclaredConstructors()) {
                if (ctor.getParameterCount() == 2) {
                    final Class<?>[] parameterTypes = ctor.getParameterTypes();
                    if (parameterTypes[0] == String.class && parameterTypes[1] == Throwable.class) {
                        throwable = (Throwable) ctor.newInstance(
                                proxy.getMessage(), getThrowable(proxy.getCause()));
                        break;
                    } else if (parameterTypes[1] == String.class
                            && parameterTypes[0] == Throwable.class) {
                        throwable = (Throwable) ctor.newInstance(
                                getThrowable(proxy.getCause()), proxy.getMessage());
                        break;
                    }
                }
            }

            if (throwable == null) {
                throw new ReflectiveOperationException("Unable to find ctor of "
                        + clazz.getClass());
            }
        }
        throwable.setStackTrace(getStackTraceElements(proxy.getStackTraceElementProxyArray()));
        return throwable;
    }

    /**
     * Converts an SLF4F {@link StackTraceElementProxy}[] into a {@link StackTraceElement}[].
     *
     * @param elements {@link StackTraceElementProxy}s to convert
     *
     * @return Non null {@link StackTraceElement}[] of the input
     */
    @Nonnull
    public static StackTraceElement[] getStackTraceElements(
            final StackTraceElementProxy... elements) {
        final StackTraceElement[] returnValue = new StackTraceElement[elements.length];
        for (int i = 0; i < elements.length; i++) {
            returnValue[i] = elements[i].getStackTraceElement();
        }
        return returnValue;
    }

    @SuppressWarnings("unchecked")
    private static Class<Throwable> getThrowableClass(final String string)
            throws ClassNotFoundException {
        return (Class<Throwable>) Class.forName(string);
    }
}
