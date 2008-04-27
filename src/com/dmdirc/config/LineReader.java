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

package com.dmdirc.config;

import java.io.Reader;
import java.util.List;

/**
 * A reader implementation that "reads" from a List of Strings, as provided
 * by a DMDirc text file. This is only used to migrate DMDirc config files.
 * 
 * @author chris
 */
public class LineReader extends Reader {
    
    /** The lines to read from. */
    private final List<String> lines;
    
    /** Character list. */
    char[] mybuf;

    /** Current position. */
    int cur = 0;
    
    /**
     * Creates a new LineReader to read from the specified lines.
     * 
     * @param lines The lines to read from
     */
    public LineReader(final List<String> lines) {
        super();
        
        this.lines = lines;
        
        int size = 0;
        
        for (String line : lines) {
            size += 1 + line.length();
        }
        
        mybuf = new char[size];
        size = 0;
        
        for (String line : lines) {
            System.arraycopy(line.toCharArray(), 0, mybuf, size, line.length());
            mybuf[size + line.length()] = '\n';
            
            size += 1 + line.length();
        }
    }
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** {@inheritDoc} */
    @Override
    public int read(final char[] cbuf, final int off, final int len) {
        int count = -1;
        
        if (cur < mybuf.length) {
            count = Math.min(len, mybuf.length - cur);
            System.arraycopy(mybuf, cur, cbuf, off, count);
            cur += count;
        }
        
        return count;
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        // Do nothing
    }

}
