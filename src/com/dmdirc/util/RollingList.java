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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @param <T> The type if items that this list contains 
 * @author chris
 */
public class RollingList<T> {
    
    private final List<T> items = new ArrayList<T>();
    
    private final int capacity;
    
    private int position = 0;
    
    private boolean addEmpty;
    private T empty;
    
    public RollingList(int capacity) {
        this.capacity = capacity;
        this.addEmpty = false;
    }

    public RollingList(int capacity, T empty) {
        this.capacity = capacity;
        this.addEmpty = true;
        this.empty = empty;
    }

    public boolean remove(Object o) {
        return items.remove(o);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public T get(int index) {
        return items.get(index);
    }

    public boolean contains(Object o) {
        return items.contains(o);
    }

    public void clear() {
        items.clear();
    }  
    
    public boolean add(T e) {
        while (items.size() > capacity - 1) {
            items.remove(0);
            position--;
        }
        
        return items.add(e);
    }
    
    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }    
    
    public boolean hasNext() {
        return (items.size() > position + 1) || ((items.size() > position) && addEmpty);
    }
    
    public T getNext() {
        if (items.size() > position + 1 || !addEmpty) {
            return get(++position);
        } else {
            return empty;
        }
    }
    
    public boolean hasPrevious() {
        return 0 < position;
    }
    
    public T getPrevious() {
        return get(--position);
    }    
    
    public void seekToEnd() {
        position = items.size();
    }
    
    public void seekToStart() {
        position = 0;
    }

}
