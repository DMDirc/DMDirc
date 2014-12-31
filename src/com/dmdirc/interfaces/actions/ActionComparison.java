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

/**
 * Describes the methods that must be implemented by an action comparison. Action comparisons allow
 * the user to compare an action component to a string they entered.
 */
public interface ActionComparison {

    /**
     * Tests the specified arguments against each other.
     *
     * @param arg1 The first argument
     * @param arg2 The second argument
     *
     * @return True iff the comparison between the args succeeds, false otherwise
     */
    boolean test(final Object arg1, final Object arg2);

    /**
     * Returns the class that this comparison deals with.
     *
     * @return The Class that this comparison can compare
     */
    Class<?> appliesTo();

    /**
     * Returns a friendly name for this comparison.
     *
     * @return This comparison's friendly name
     */
    String getName();

    /**
     * Returns the internal name of this comparison.
     *
     * @since 0.6.4
     * @return This comparison's internal name
     */
    String name();

}
