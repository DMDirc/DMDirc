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

package com.dmdirc.ui.input;

import java.util.ArrayList;

/**
 * The AdditionalTabTargets class is a basic wrapper around an arraylist that
 * adds an additional property to determine whether or not to include normal
 * tab-completable targets (such as nicknames and channels).
 * 
 * @author chris
 */
public final class AdditionalTabTargets extends ArrayList<String> {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** Whether to include normal targets. */
    private boolean includeNormal = true;

    /**
     * Determines if this set of tab targets precludes the inclusion of
     * standard targets.
     * 
     * @return True if normal targets are included, false otherwise
     */
    public boolean shouldIncludeNormal() {
        return includeNormal;
    }

    /**
     * Sets whether or not these results preclude the inclusion of the standard
     * tab-completable targets.
     * 
     * @param includeNormal Whether normal targets should be included or not
     */
    public void setIncludeNormal(final boolean includeNormal) {
        this.includeNormal = includeNormal;
    }    

}
