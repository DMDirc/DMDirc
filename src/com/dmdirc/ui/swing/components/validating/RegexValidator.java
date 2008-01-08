/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.ui.swing.components.validating;

/**
 * Regex validator.
 */
public class RegexValidator implements Validator<String> {

    /** Regex. */
    private final String regex;
    /** Failure reason. */
    private final String failedReason;

    /**
     * Instantiates a new regex validator.
     * 
     * @param regex Regex to validate text against
     * @param failedReason Reason for validation failure
     */
    public RegexValidator(final String regex, final String failedReason) {
        this.regex = regex;
        this.failedReason = failedReason;
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(final String object) {
        return object.matches(regex);
    }

    /** {@inheritDoc} */
    @Override
    public String getFailureReason() {
        return failedReason;
    }
}
