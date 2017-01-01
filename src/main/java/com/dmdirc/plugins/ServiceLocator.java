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

package com.dmdirc.plugins;

import java.util.Collection;

import javax.annotation.Nullable;

/**
 * Provides an easy-to-use way to locate services provided by other components.
 */
public interface ServiceLocator {

    /**
     * Gets all known services of the specified type.
     *
     * @param <T>         The type of service that will be returned.
     * @param serviceType The type of service that will be returned.
     *
     * @return A possibly-empty collection of services of the given type.
     */
    <T> Collection<T> getAllServices(final Class<T> serviceType);

    /**
     * Gets a single service of the specified type.
     *
     * @param <T>            The type of service that will be returned.
     * @param serviceType    The type of service that will be returned.
     * @param implementation The fully-qualified classname of the desired implementation.
     *
     * @return The given implementation of the service, or {@code null} if it wasn't found.
     */
    @Nullable
    <T> T getService(final Class<T> serviceType, final String implementation);

}
