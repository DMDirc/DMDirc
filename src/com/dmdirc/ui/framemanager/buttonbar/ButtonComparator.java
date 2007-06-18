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

package com.dmdirc.ui.framemanager.buttonbar;

import com.dmdirc.Channel;
import com.dmdirc.FrameContainer;
import com.dmdirc.Query;
import com.dmdirc.Raw;
import com.dmdirc.Server;

import java.io.Serializable;

import java.util.Comparator;

/**
 * Implements a basic comparator for frame containers, so that they're sorted
 * as follows: Server, Raw, Channel, Query.
 *
 * @author chris
 */
public final class ButtonComparator implements Comparator<FrameContainer>,
        Serializable {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /**
     * Returns an integer corresponding to the expected order of an object.
     *
     * @param object The object to be tested
     * @return Position of the object
     */
    private int getPosition(final FrameContainer object) {
        if (object instanceof Server) {
            return 1;
        } else if (object instanceof Raw) {
            return 2;
        } else if (object instanceof Channel) {
            return 3;
        } else if (object instanceof Query) {
            return 4;
        } else {
            return 5;
        }
    }
    
    /** {@inheritDoc} */
    public int compare(final FrameContainer o1, final FrameContainer o2) {
        return getPosition(o1) - getPosition(o2);
    }
    
}
