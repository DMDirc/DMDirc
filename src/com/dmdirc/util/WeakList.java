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

package com.dmdirc.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @param <T>
 * @author chris
 */
public class WeakList<T> implements List<T> {

    private final List<WeakReference<T>> list = new ArrayList<WeakReference<T>>();

    public WeakList() {
        super();
    }

    private void cleanUp() {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).get() == null) {
                list.remove(i--);
            }
        }
    }

    private List<T> dereferenceList(final List<WeakReference<T>> list) {
        final List<T> res = new ArrayList<T>();

        for (WeakReference<T> item : list) {
            if (item.get() != null) {
                res.add(item.get());
            }
        }

        return res;
    }

    @SuppressWarnings(value = "unchecked")
    private Collection<WeakReference<T>> referenceCollection(final Collection<?> c) {
        final Collection<WeakReference<T>> res = new ArrayList<WeakReference<T>>();

        for (Object item : c) {
            res.add(new WeakReference(item));
        }

        return res;
    }

    /** {@inheritDoc} */
    @Override
    public int size() {
        cleanUp();

        return list.size();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        cleanUp();

        return list.isEmpty();
    }

    /** {@inheritDoc} */
    @Override @SuppressWarnings(value = "unchecked")
    public boolean contains(Object o) {
        return list.contains(new WeakReference(o));
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<T> iterator() {
        return dereferenceList(list).iterator();
    }

    /** {@inheritDoc} */
    @Override
    public Object[] toArray() {
        return dereferenceList(list).toArray();
    }

    /** {@inheritDoc} */
    @Override
    public <T> T[] toArray(T[] a) {
        return dereferenceList(list).toArray(a);
    }

    /** {@inheritDoc} */
    @Override
    public boolean add(T e) {
        return list.add(new WeakReference<T>(e));
    }

    /** {@inheritDoc} */
    @Override @SuppressWarnings(value = "unchecked")
    public boolean remove(Object o) {
        return list.remove(new WeakReference(o));
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsAll(Collection<?> c) {
        return dereferenceList(list).containsAll(c);
    }

    /** {@inheritDoc} */
    @Override
    public boolean addAll(Collection<? extends T> c) {
        return list.addAll(referenceCollection(c));
    }

    /** {@inheritDoc} */
    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        return list.addAll(index, referenceCollection(c));
    }

    /** {@inheritDoc} */
    @Override
    public boolean removeAll(Collection<?> c) {
        return list.removeAll(referenceCollection(c));
    }

    /** {@inheritDoc} */
    @Override
    public boolean retainAll(Collection<?> c) {
        return list.retainAll(referenceCollection(c));
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
        list.clear();
    }

    /** {@inheritDoc} */
    @Override
    public T get(int index) {
        cleanUp();

        return list.get(index).get();
    }

    /** {@inheritDoc} */
    @Override
    public T set(int index, T element) {
        list.set(index, new WeakReference<T>(element));

        return element;
    }

    /** {@inheritDoc} */
    @Override
    public void add(int index, T element) {
        list.add(index, new WeakReference<T>(element));
    }

    /** {@inheritDoc} */
    @Override
    public T remove(int index) {
        return list.remove(index).get();
    }

    /** {@inheritDoc} */
    @Override @SuppressWarnings(value = "unchecked")
    public int indexOf(Object o) {
        cleanUp();

        return list.indexOf(new WeakReference(o));
    }

    /** {@inheritDoc} */
    @Override @SuppressWarnings(value = "unchecked")
    public int lastIndexOf(Object o) {
        cleanUp();

        return list.lastIndexOf(new WeakReference(o));
    }

    /** {@inheritDoc} */
    @Override
    public ListIterator<T> listIterator() {
        cleanUp();

        return dereferenceList(list).listIterator();
    }

    /** {@inheritDoc} */
    @Override
    public ListIterator<T> listIterator(int index) {
        cleanUp();

        return dereferenceList(list).listIterator(index);
    }

    /** {@inheritDoc} */
    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return dereferenceList(list.subList(fromIndex, toIndex));
    }
}