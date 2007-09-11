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

package com.dmdirc.addons.nowplaying.plugin;

import com.dmdirc.addons.nowplaying.MediaSource;

import java.util.Comparator;
import java.util.List;

/**
 * Sorts media sources according to an ordered list of their names.
 *
 * @author chris
 */
public class MediaSourceComparator implements Comparator<MediaSource> {
    
    /** The order that the sources should be checked. */
    private final List<String> order;
    
    /**
     * Creates a new instance of MediaSourceComparator.
     * NB: The order list may be altered during comparisons.
     *
     * @param order An ordered list of media source names
     */
    public MediaSourceComparator(final List<String> order) {
        this.order = order;
    }

    /** {@inheritDoc} */
    public int compare(final MediaSource o1, final MediaSource o2) {
        return getPosition(o1) - getPosition(o2);
    }
    
    /**
     * Retrieves the position of the source within the order list.
     * If the source is not present it is appended to the list.
     *
     * @param source The media source to be tested
     */
    private int getPosition(final MediaSource source) {
        if (!order.contains(source.getName())) {
            order.add(source.getName());
        }
        
        return order.indexOf(source.getName());
    }
    
}
