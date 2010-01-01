/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.ui_swing.components.substitutions;

/**
 * Wrapper for action substitutions.
 */
public final class Substitution {

    /** Substitution name. */
    private final String name;
    /** Subsitution value. */
    private final String value;

    /** 
     * Creates a new instance of Substitution. 
     *
     * @param name Name of the substitution
     * @param value Value of the substitution
     */
    public Substitution(final String name, final String value) {
        this.name = name;
        this.value = "$" + value;
    }

    /**
     * Returns the name of this substitution.
     *
     * @return Substitution name   
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the value of this substitution.
     * 
     * @return Substitution value
     */
    public String getValue() {
        return value;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return value;
    }
}
