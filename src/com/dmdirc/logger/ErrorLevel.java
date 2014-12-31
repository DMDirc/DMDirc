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

package com.dmdirc.logger;

/** Specific error levels allowed by Logger. */
public enum ErrorLevel {

    /** Fatal error. */
    FATAL("Fatal", "error"),
    /** High priority error. */
    HIGH("High", "error"),
    /** Medium priority error. */
    MEDIUM("Medium", "warning"),
    /** Low priority error. */
    LOW("Low", "info"),
    /** Unknown priority error. */
    UNKNOWN("Unknown", "info");
    /** Error level string. */
    private final String value;
    /** Error level icon. */
    private final String icon;

    /**
     * Instantiates the enum.
     *
     * @param value toString value
     * @param icon  Error level icon
     */
    ErrorLevel(final String value, final String icon) {
        this.value = value;
        this.icon = icon;
    }

    @Override
    public String toString() {
        return value;
    }

    /**
     * Error levels icon.
     *
     * @return Error levels icon
     */
    public String getIcon() {
        return icon;
    }

    /**
     * Returns if the specified error is more important than this one
     *
     * @param level Error level to compare
     *
     * @return true iff the error is more important
     */
    public boolean moreImportant(final ErrorLevel level) {
        return level != null && ordinal() > level.ordinal();
    }

}
