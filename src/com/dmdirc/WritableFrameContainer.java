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

package com.dmdirc;

import com.dmdirc.ui.interfaces.InputWindow;

/**
 * The writable frame container adds additional methods to the frame container
 * class that allow the sending of lines back to whatever the container's
 * data source is (e.g. an IRC channel or server).
 * 
 * @author chris
 */
public abstract class WritableFrameContainer extends FrameContainer {
    
    /**
     * Sends a line of text to this container's source.
     * 
     * @param line The line to be sent
     */
    public abstract void sendLine(String line);
    
    /**
     * Returns the internal frame associated with this object.
     * 
     * @return The internal frame associated with this object
     */
    public abstract InputWindow getFrame();    
    
    /**
     * Returns the maximum length that a line passed to sendLine() should be,
     * in order to prevent it being truncated or causing protocol violations.
     * 
     * @return The maximum line length for this container
     */
    public abstract int getMaxLineLength();
    
    /**
     * Returns the number of lines that the specified string would be sent as.
     * 
     * @param line The string to be split and sent
     * @return The number of lines required to send the specified string
     */
    public final int getNumLines(final String line) {
        int lines;
        final String[] splitLines = line.split("\n");
        lines = splitLines.length;
        lines = 0;
        for (String splitLine : splitLines) {
            lines += (int) Math.ceil(splitLine.length() / (double) getMaxLineLength());
        }
        return lines;
    }
    
}
