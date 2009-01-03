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

package com.dmdirc.util;

import java.nio.charset.Charset;

/**
 * Facilitates easy transcoding of strings.
 * 
 * @author Chris
 */
public class StringTranscoder {
   
    /** The charset that is used by this transcoder. */
    private final Charset charset;
    
    /**
     * Creates a new StringTranscoder that will transcode strings from the
     * system's default character set into the specified charset.
     * 
     * @param charset The target character set
     */
    public StringTranscoder(final Charset charset) {
        this.charset = charset;
    }
    
    /**
     * Transcodes the specified string from the system's default charset to
     * this transcoder's charset.
     * 
     * @param string The string to be transcoded
     * @return A new string of the appropriate charset
     */
    public String encode(final String string) {
        return new String(string.getBytes(charset));
    }
    
    /**
     * Transcodes the specified string from this transcoder's charset to the
     * system's default charset
     * 
     * @param string The string to be transcoded
     * @return A new string of the appropriate charset
     */
    public String decode(final String string) {
        return new String(string.getBytes(), charset);
    }    
    
}
