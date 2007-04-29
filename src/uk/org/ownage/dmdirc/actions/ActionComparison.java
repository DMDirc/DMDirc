/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package uk.org.ownage.dmdirc.actions;

/**
 * An ActionComparison represents a possible comparison between two types of
 * data used in an action condition.
 * @author chris
 */
public enum ActionComparison {
    
    STRING_EQUALS {
        boolean test(final Object arg1, final Object arg2) { return ((String) arg1).equals((String) arg2); }
        Class appliesTo() { return String.class; }
    };
    
    /**
     * Tests the specified arguments against each other.
     * @param arg1 The first argument
     * @param arg2 The second argument
     * @return True iff the comparison between the args succeeds, false
     * otherwise
     */
    abstract boolean test(final Object arg1, final Object arg2);
    
    /**
     * Returns the class that this comparison deals with.
     * @return The Class that this comparison can compare
     */
    abstract Class appliesTo();
    
}
