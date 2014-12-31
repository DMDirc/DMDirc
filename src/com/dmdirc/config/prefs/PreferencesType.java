/*
 * Copyright (c) 2006-2014 DMDirc Developers
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

package com.dmdirc.config.prefs;

/**
 * Enumerates the possible types of preferences.
 */
public enum PreferencesType {

    /** A free-form text preference. */
    TEXT,
    /** A free-form integer preference. */
    INTEGER,
    /** A optional free-form integer preference. */
    OPTIONALINTEGER,
    /** A boolean preference. */
    BOOLEAN,
    /** A colour preference. */
    COLOUR,
    /** An optional colour preference. */
    OPTIONALCOLOUR,
    /** A multiple choice preference. */
    MULTICHOICE,
    /** A duration specified in milliseconds. */
    DURATION,
    /** Font type. */
    FONT,
    /** File-Only browse component. */
    FILE,
    /** Directory-Only browse component. */
    DIRECTORY,
    /** File or Directory browse component. */
    FILES_AND_DIRECTORIES,
    /** Free-form text label. */
    LABEL,
    /** Table type. */
    TABLE,

}
