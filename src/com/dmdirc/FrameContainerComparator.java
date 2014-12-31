/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.GroupChat;

import java.io.Serializable;
import java.util.Comparator;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Compares FrameContainers by name.
 */
public class FrameContainerComparator implements Comparator<FrameContainer>, Serializable {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;

    /**
     * Compares two frame containers names.
     *
     * @param item1 The first container to compare
     * @param item2 The second container to compare
     *
     * @return -1 if item1 is before item2, 0 if they're equal, +1 if item1 is after item2.
     */
    @Override
    @Precondition({
        "item1 is non-null",
        "item2 is non-null",
        "item1.toString() returns a non-null value",
        "item2.toString() returns a non-null value"
    })
    public int compare(final FrameContainer item1, final FrameContainer item2) {
        checkNotNull(item1);
        checkNotNull(item2);
        checkNotNull(item1.getName());
        checkNotNull(item2.getName());

        if (sortBefore(item1, item2)) {
            return -1;
        } else if (sortAfter(item1, item2)) {
            return 1;
        } else {
            final int position = item1.getName()
                    .compareToIgnoreCase(item2.getName());
            if (position == 0) {
                return Integer.valueOf(item1.hashCode()).compareTo(item2.hashCode());
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
     *
     * @return True iff the new container should be before the old container
     */
    private static boolean sortBefore(
            final FrameContainer item1,
            final FrameContainer item2) {
        return getPosition(item1) < getPosition(item2);
    }

    /**
     * Compares frame container types and checks order preferences.
     *
     * @param item1 The new container to be tested
     * @param item2 The existing container to test against
     *
     * @return True iff the new container should be after the old container
     */
    private static boolean sortAfter(final FrameContainer item1,
            final FrameContainer item2) {
        return getPosition(item1) > getPosition(item2);
    }

    /**
     * Returns an integer corresponding to the expected order of a frame container.
     *
     * @param item The frame container to be tested
     *
     * @return Position of the frame container
     */
    private static int getPosition(final FrameContainer item) {
        if (item instanceof GlobalWindow) {
            return 0;
        } else if (item instanceof Connection) {
            return 1;
        } else if (item instanceof GroupChat) {
            return 2;
        } else if (item instanceof Query) {
            return 3;
        } else {
            return 4;
        }
    }

}
