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
 * Describes the response from a Validator. The response may be either positive
 * or negative. Negative responses have an associated failure reason.
 * 
 * @author chris
 */
public class ValidationResponse {
   
    /** The failure reason, or null if the validation passed. */
    private final String failure;
    
    /**
     * Creates a new ValidationResponse for a positive response.
     */
    public ValidationResponse() {
        this.failure = null;
    }
    
    /**
     * Creates a new ValidationResponse for a negative response.
     * 
     * @param failure The reason for the negative response.
     */
    public ValidationResponse(final String failure) {
        this.failure = failure;
    }
    
    /**
     * Determines whether or not this response indicates that a failure occured.
     * 
     * @return True if a failure occured, false otherwise.
     */
    public boolean isFailure() {
        return failure != null;
    }
    
    /**
     * Retrieves the reason for the validation failure.
     * 
     * @return The reason for the validation failure.
     */
    public String getFailureReason() {
        return failure;
    }

}
