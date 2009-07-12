/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.parser.interfaces;

/**
 * Facilitates case conversion according to the charset/rules of the specific
 * backend chat system.
 *
 * @since 0.6.3m2
 * @author chris
 */
public interface StringConverter {

    /**
     * Get the lowercase version of a String.
     *
     * @param input String to convert to lowercase
     * @return String converted to lowercase
     */
    String toLowerCase(String input);

    /**
     * Get the uppercase version of a String.
     *
     * @param input String to convert to uppercase
     * @return String converted to uppercase
     */
    String toUpperCase(String input);

    /**
     * Check if the 2 strings are equal to each other ignoring case.
     *
     * @param first First string to check
     * @param second Second string to check
     * @return True if both strings are equal after being lowercased
     */
    boolean equalsIgnoreCase(String first, String second);

}
