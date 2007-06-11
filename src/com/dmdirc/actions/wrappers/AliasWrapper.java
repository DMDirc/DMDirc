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

package com.dmdirc.actions.wrappers;

import com.dmdirc.actions.Action;

/**
 * Encapsulates alias actions.
 * @author chris
 */
public class AliasWrapper implements ActionWrapper {
    
    /** Singleton instance of the alias wrapper. */
    private static AliasWrapper me;
    
    /**
     * Creates a new instance of AliasWrapper.
     */
    private AliasWrapper() {
        super();
    }
    
    /**
     * Retrieves a singleton instance of this alias wrapper.
     * @return A singleton instance of AliasWrapper
     */
    public static AliasWrapper getAliasWrapper() {
        synchronized (me) {
            if (me == null) {
                me = new AliasWrapper();
            }
            
            return me;
        }
    }

    /** {@inheritDoc} */
    public void registerAction(Action action) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    public void unregisterAction(Action action) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}