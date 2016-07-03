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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a field or method should be bound to a given configuration setting. If a method is
 * targeted, the method should take a single argument.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface ConfigBinding {

    /**
     * The domain of the configuration setting to bind to.
     *
     * @return The configuration setting's domain
     */
    String domain() default "";

    /**
     * The key of the configuration setting to bind to.
     *
     * @return The configuration setting's key
     */
    String key();

    /**
     * Ordered list of fallback domain/key pairs to attempt to use.
     *
     * @return Domain/key pairs to use
     */
    String[] fallbacks() default {};

    /**
     * Whether or not the configuration will be applied initially or only on updates.
     *
     * @return True if the value should be applied initially, false otherwise.
     */
    boolean applyInitially() default true;

    /**
     * For settings that support fallbacks, whether or not a value is required. If it is not
     * required, a "false:" setting will result in {@code null} being returned, rather than
     * the next fallback being tried.
     *
     * @return True if the setting is required, false otherwise.
     */
    boolean required() default true;

    /**
     * {@link Invocation} class that will be created and used to execute the config binding.  The
     * default just directly executes or sets the value.
     */
    Class<? extends Invocation> invocation() default BasicInvocation.class;

}
