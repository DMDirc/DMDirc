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
package com.dmdirc.config.prefs.validator;

/**
 * Validates that the length of a string is within certain bounds.
 * 
 * @author chris
 */
public class StringLengthValidator implements Validator<String> {
    
    /** The minimum string length. */
    protected final int min;
    
    /** The maximum string length. */
    protected final int max;

    /**
     * Creates a new string length validator that requires a string be between
     * the specified min/max length.
     * 
     * @param min The minimum length of the string, or -1 for unlimited.
     * @param max The maximum length of the string, or -1 for unlimited.
     */
    public StringLengthValidator(int min, int max) {
        this.min = min;
        this.max = max;
    }

    /** {@inheritDoc} */
    @Override
    public ValidationResponse validate(final String object) {
        if (object.length() < min && min != -1) {
            return new ValidationResponse("Must be at least " + min + " characters long");
        } else if (object.length() > max && max != -1) {
            return new ValidationResponse("Must be at most " + max + " characters long");
        } else {
            return new ValidationResponse();
        }
    }

}
