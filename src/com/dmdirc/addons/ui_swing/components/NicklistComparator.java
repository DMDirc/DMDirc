/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.ui_swing.components;

import com.dmdirc.parser.interfaces.ChannelClientInfo;
import java.io.Serializable;
import java.util.Comparator;

/**
 * Compares nicklist entries to each other, for sorting purposes.
 */
public final class NicklistComparator implements Comparator<ChannelClientInfo>,
        Serializable {
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /**
     * whether to sort the nicklist by modes.
     */
    private final boolean sortByMode;
    
    /**
     * whether to sort the nicklist by case.
     */
    private final boolean sortByCase;
    
    /**
     * Creates a new instance of NicklistComparator.
     * @param newSortByMode sorts by channel mode of the user
     * @param newSortByCase sorts by nickname case
     */
    public NicklistComparator(final boolean newSortByMode,
            final boolean newSortByCase) {
        this.sortByMode = newSortByMode;
        this.sortByCase = newSortByCase;
    }
    
    /**
     * Compares two ChannelClient objects based on the settings the comparator
     * was initialised with.
     * @param client1 the first client to be compared
     * @param client2 the second client to be compared
     * @return a negative integer, zero, or a positive integer as the first
     * argument is less than, equal to, or greater than the second.
     */
    @Override
    public int compare(final ChannelClientInfo client1,
            final ChannelClientInfo client2) {
        final String nickname1 = client1.getClient().getNickname();
        final String nickname2 = client2.getClient().getNickname();
        
        if (sortByMode) {
            final int modeCom = client2.compareTo(client1);

            if (modeCom != 0) {
                return modeCom;
            }
        }
        
        if (sortByCase) {
            return nickname1.compareTo(nickname2);
        } else {
            return nickname1.compareToIgnoreCase(nickname2);
        }
    }
}
