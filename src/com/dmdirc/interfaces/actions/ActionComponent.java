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

package com.dmdirc.interfaces.actions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The ActionComponent interface describes the methods that action components are required to
 * implement. Components are objects that allow the user to pull a specific piece of information
 * from a certain type of argument.
 */
public interface ActionComponent {

    /**
     * An annotation which allows action components to specify options relating to their use. The
     * annotation should be applied to the {@link #get(java.lang.Object)} method of implementing
     * classes.
     *
     * @since 0.6.4
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface ComponentOptions {

        /**
         * Whether or not the component requires a connected server.
         *
         * @return true if component requires a connected server
         */
        boolean requireConnected() default false;

    }

    /**
     * Retrieves the component of the specified argument that this component represents.
     *
     * @param arg The object to retrieve the component from
     *
     * @return The relevant component of the object
     */
    Object get(Object arg);

    /**
     * Retrieves the type of class that this component applies to.
     *
     * @return The Class that this component can be applied to
     */
    Class<?> appliesTo();

    /**
     * Retrieves the type of this component.
     *
     * @return The Class of this component.
     */
    Class<?> getType();

    /**
     * Retrieves a friendly name for this component.
     *
     * @return This component's friendly name
     */
    String getName();

    /**
     * Retrieves the internal name of this action component.
     *
     * @since 0.6.4
     * @return The internal name of this component
     */
    String name();

}
