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

package com.dmdirc.logger;

/**
 * Specific error levels allowed by Logger.
 */
public enum ErrorLevel {        
    /** Fatal error. */
    FATAL ("fatal error", "a fatal error"), 
    /** Normal error. */
    ERROR ("error", "an error"), 
    /** Warning error. */
    WARNING ("warning", "a warning"), 
    /** Info error. */
    TRIVIAL ("trivial error", "a trivial error");
    /** toString value. */
    private String value;
    /** toSentenceString value. */
    private String sentenceValue;
    
    /** 
     * Creates the error level. 
     * @param newValue error level name
     * @param newSentenceValue error level sentence name
     */
    ErrorLevel(final String newValue, final String newSentenceValue) {
	this.value = newValue;
	this.sentenceValue = newSentenceValue;
    }
    
    /** 
     * returns the name of the error level.
     * @return error name
     */
    
    public String toString() {
	return value;
    }
    
    /**
     * returns the name of the error level that can be used in a sentence.
     * @return sentence error name
     */
    public String toSentenceString() {
	return sentenceValue;
    }
}
