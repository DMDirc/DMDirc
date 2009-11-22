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

package com.dmdirc;

import com.dmdirc.logger.Logger;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Compares FrameContainers by name.
 */
public final class FrameContainerComparator implements Comparator<FrameContainer>,
        Serializable {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;

    /**
     * Creates a new instance of FrameContainerComparator.
     */
    public FrameContainerComparator() {
        super();
    }

    /**
     * Compares two frame containers names.
     *
     * @param item1 The first container to compare
     * @param item2 The second container to compare
     * @return -1 if item1 is before item2, 0 if they're equal,
     * +1 if item1 is after item2.
     */
    @Override
    @Precondition({
        "item1 is non-null",
        "item2 is non-null",
        "item1.toString() returns a non-null value",
        "item2.toString() returns a non-null value"
    })
    public int compare(final FrameContainer item1, final FrameContainer item2) {
        Logger.assertTrue(item1 != null);
        Logger.assertTrue(item2 != null);
        Logger.assertTrue(item1.toString() != null);
        Logger.assertTrue(item2.toString() != null);

        if (sortBefore(item1, item2)) {
            return -1;
        } else if (sortAfter(item1, item2)) {
            return 1;
        } else {
            final int position = item1.toString().compareToIgnoreCase(
                    item2.toString());
            if (position == 0) {
                return Integer.valueOf(item1.hashCode()).compareTo(
                        Integer.valueOf(item2.hashCode()));
            } else {
                return position;
            }
        }
    }

    /**
     * Compares frame container types and checks order preferences.
     *
     * @param item1 The new container to be tested
     * @param item2 The existing container to test against
     * @return True iff the new container should be before the old container
     */
    private boolean sortBefore(final FrameContainer item1,
            final FrameContainer item2) {

        return getPosition(item1) < getPosition(item2);
    }

    /**
     * Compares frame container types and checks order preferences.
     *
     * @param item1 The new container to be tested
     * @param item2 The existing container to test against
     * @return True iff the new container should be after the old container
     */
    private boolean sortAfter(final FrameContainer item1,
            final FrameContainer item2) {
        return getPosition(item1) > getPosition(item2);
    }

    /**
     * Returns an integer corresponding to the expected order of a frame
     * container.
     *
     * @param item The frame container to be tested
     * @return Position of the frame container
     */
    private int getPosition(final FrameContainer item) {
        if (item instanceof GlobalWindow) {
            return 0;
        } else if (item instanceof Server) {
            return 1;
        } else if (item instanceof Raw) {
            return 2;
        } else if (item instanceof Channel) {
            return 3;
        } else if (item instanceof Query) {
            return 4;
        } else {
            return 5;
        }
    }

}
