/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.config.binding;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic {@link Invocation} implementation that just executes or sets the field on the current
 * thread.
 */
public class BasicInvocation extends Invocation {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicInvocation.class);

    @Override
    public void invoke(final Field field, final Object instance, final Object value) {
        try {
            field.set(instance, value);
        } catch (ReflectiveOperationException ex) {
            LOGGER.warn("Unable to set field: " + ex.getMessage());
        }
    }

    @Override
    public void invoke(final Method method, final Object instance, final Object value) {
        try {
            method.invoke(instance, value);
        } catch (ReflectiveOperationException ex) {
            LOGGER.warn("Unable to set field: " + ex.getMessage());
        }
    }
}
