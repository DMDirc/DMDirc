/*
 * 
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

package com.dmdirc.addons.ui_swing.components;

import java.util.Comparator;
import javax.swing.DefaultListModel;

/**
 * Sorted implementation of DefaultListModel.
 *
 * @param <T> Comparator type
 */
public class SortedListModel<T> extends DefaultListModel {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1L;
    /** Comparator. */
    private Comparator<T> comparator;

    /**
     * Creates a new sorted list model using the specified comparator.
     *
     * @param comparator Comparator
     */
    public SortedListModel(final Comparator<T> comparator) {
        this.comparator = comparator;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public void addElement(final Object obj) {
        final T object = (T) obj;
        boolean added = false;
        for (int i = 0; i < getSize(); i++) {
            final T currentObj = (T) getElementAt(i);
            if (comparator.compare(currentObj, object) > 0) {
                add(i, obj);
                added = true;
                break;
            }
        }
        if (!added) {
            add(getSize(), obj);
        }
    }
}
