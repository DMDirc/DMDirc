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
 * Validates idents.
 */
public class IdentValidator implements Validator<String> {

    /** Ident regex. */
    private static final String IDENT_REGEX = "[A-Za-z0-9\\[\\]{|}\\-\\^\\\\]*";
    /** Failure reason. */
    private static final String FAILURE_REASON = "Ident must only contain letters, numbers and []{}|-^\\.";

    /**
     * Instantiates a new ident validator.
     */
    public IdentValidator() {
        //Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public ValidationResponse validate(final String object) {
        if (object.matches(IDENT_REGEX)) {
            return new ValidationResponse();
        } else {
            return new ValidationResponse(FAILURE_REASON);
        }
    }
}
