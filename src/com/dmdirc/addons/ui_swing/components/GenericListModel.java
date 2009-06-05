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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;

/**
 * Generic list model, closely following the DefaultListModel API.
 *
 * @param <T> Generic type of the list
 */
public class GenericListModel<T> extends AbstractListModel {

    private static final long serialVersionUID = -4227892376992714545L;
    private List<T> list;

    /**
     * Instantiates an empty generic list model.
     */
    public GenericListModel() {
        this.list = Collections.synchronizedList(new ArrayList<T>());
    }

    /**
     * Instantiates a list model containing the specified list items.
     *
     * @param list
     */
    public GenericListModel(final List<T> list) {
        this.list = Collections.synchronizedList(new ArrayList<T>(list));
    }

    /** {@inheritDoc} */
    @Override
    public int getSize() {
        return list.size();
    }

    /**
     * Returns the specified index in the list
     *
     * @param index Index of the item to return
     *
     * @return Item at index
     */
    public T get(final int index) {
        return list.get(index);
    }

    /**
     * Checks whether the list mode is empty.
     *
     * @return true iif the is the list if empty
     */
    public boolean isEmpty() {
        return list.isEmpty();
    }

    /**
     * Checks whether this list model contains the specified object.
     *
     * @param object Object to check for in the model
     *
     * @return true iif the model object is in this model
     */
    public boolean contains(final T object) {
        return list.contains(object);
    }

    /**
     * Checks the first index of the speficied option.
     *
     * @param object Object to check the index of
     *
     * @return first index of the object or -1 if not found
     */
    public int indexOf(final T object) {
        return list.indexOf(object);
    }

    /**
     * Checks the last index of the speficied option.
     *
     * @param object Object to check the index of
     *
     * @return last index of the object or -1 if not found
     */
    public int lastIndexOf(final T object) {
        return list.lastIndexOf(object);
    }

    /**
     * Sets the object at the specified index to be the specified object.
     *
     * @param index Index of the object
     * @param object Object to set
     */
    public void set(final int index, final T object) {
        list.set(index, object);
        fireContentsChanged(this, index, index);
    }

    /**
     * Removes the object at the specified index.
     *
     * @param index Index to remove
     */
    public void remove(final int index) {
        list.remove(index);
        fireIntervalRemoved(this, index, index);
    }

    /**
     * Adds the specified object at the specified index.
     *
     * @param index Index to insert object at
     * @param object Object to insert
     */
    public void add(final int index, final T object) {
        list.add(index, object);
        fireIntervalAdded(this, index, index);
    }

    /**
     * Adds the specified object.
     *
     * @param object Object to add
     */
    public void add(final T object) {
        final int index = list.size();
        list.add(object);
        fireIntervalAdded(this, index, index);
    }

    /**
     * Removes the specified object.
     *
     * @param obj Object to remove
     *
     * @return true iif object was removed
     */
    public boolean remove(final T obj) {
        final int index = indexOf(obj);
        boolean succes = list.remove(obj);
        if (index >= 0) {
            fireIntervalRemoved(this, index, index);
        }
        return succes;
    }

    /**
     * Clears this model of all items.
     */
    public void clear() {
        final int lastIndex = list.size() - 1;
        list.clear();
        if (lastIndex >= 0) {
            fireIntervalRemoved(this, 0, lastIndex);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return list.toString();
    }

    /**
     * Removes the specified range of items from the list.
     *
     * @param start Index to start removing
     * @param end Index to stop removing
     */
    public void removeRange(final int start, final int end) {
        if (start > end) {
            throw new IllegalArgumentException("start must be greater than or equal to end");
        }
        for (int i = end; i >= start; i--) {
            list.remove(i);
        }
        fireIntervalRemoved(this, start, end);
    }

    /**
     * Adds all the objects in the specified collection to this list
     *
     * @param collection Collection to add
     */
    public void addAll(final Collection<T> collection) {
        list.addAll(collection);
    }

    /**
     * Adds all the objects in the specified collection to this list at the
     * specified index.
     * 
     * @param index Index to add the items
     * @param collection Collection to add
     */
    public void addAll(final int index, final Collection<T> collection) {
        list.addAll(index, collection);
    }

    /** {@inheritDoc} */
    @Override
    public Object getElementAt(final int index) {
        return get(index);
    }
}