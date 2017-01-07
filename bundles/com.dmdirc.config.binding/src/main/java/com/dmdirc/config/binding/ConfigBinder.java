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

import javax.annotation.Nonnull;

/**
 * Facilitates automatically binding fields or methods annotated with a {@link ConfigBinding}
 * element to a configuration value.
 */
public interface ConfigBinder {

    /**
     * Binds all annotated methods and fields of the given instance to this binder's configuration
     * manager.
     *
     * @param instance The instance to be bound
     * @param clazz    The class to read bindings from
     */
    void bind(final Object instance, final Class<?> clazz);

    /**
     * Unbinds all elements of the given instance that have been bound using this ConfigBinder.
     *
     * @param instance The instance to be unbound
     */
    void unbind(final Object instance);

    /**
     * Returns a new config binder with the specified default domain.
     *
     * @param domain The default domain to use if one is not specified
     * @return A config binder with the specified default domain.
     */
    ConfigBinder withDefaultDomain(@Nonnull final String domain);

}
