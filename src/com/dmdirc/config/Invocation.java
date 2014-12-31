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

package com.dmdirc.config;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Class to invoke a method or set a value on a field.
 */
public abstract class Invocation {

    /**
     * Invokes the specified element on the specified instance with the specified value.
     *
     * @param element  Element to call, can be a {@link Method} or {@link Field}
     * @param instance Instance to call the element on
     * @param value    Value to be passed to the element
     */
    protected void invoke(final AccessibleObject element, final Object instance, final Object value) {
        if (element instanceof Field) {
            invoke((Field) element, instance, value);
        } else if (element instanceof Method) {
            invoke((Method) element, instance, value);
        } else {
            throw new IllegalArgumentException("Can only invoke on a Field or Method.");
        }
    }

    /**
     * Sets a field on the specified instance to the specified value.
     *
     * @param field    Field to set
     * @param instance Instance to set the field on
     * @param value    Value to set the field to
     */
    public abstract void invoke(final Field field, final Object instance, final Object value);

    /**
     * Sets a field on the specified instance to the specified value.
     *
     * @param method   Method to call
     * @param instance Instance to call the method on
     * @param value    Value to call the method with
     */
    public abstract void invoke(final Method method, final Object instance, final Object value);
}
