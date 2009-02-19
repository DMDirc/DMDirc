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

package com.dmdirc.commandparser.commands;

import java.util.Date;

/**
 * Stores information about a previously executed command.
 * 
 * @author chris
 */
public final class PreviousCommand {
   
    /** The full command that was executed. */
    private final String line;
    
    /** The timestamp of its execution. */
    private final long time;
    
    /**
     * Creates a new record of the specified command.
     * 
     * @param line The full command that was executed
     */
    public PreviousCommand(final String line) {
        this.line = line;
        this.time = new Date().getTime();
    }
    
    /**
     * Retrieves the time that the command was executed at.
     * 
     * @return The timestamp that the command was executed at
     */
    public long getTime() {
        return time;
    }
    
    /**
     * Retrieves the command that was executed.
     * 
     * @return The command that was executed.
     */
    public String getLine() {
        return line;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        
        if (getClass() != obj.getClass()) {
            return false;
        }
        
        final PreviousCommand other = (PreviousCommand) obj;
        if (this.line != other.line
                && (this.line == null || !this.line.equals(other.line))) {
            return false;
        }
        
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + (this.line == null ? 0 : this.line.hashCode());
        return hash;
    }
    
}