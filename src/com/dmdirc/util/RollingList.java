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

package com.dmdirc.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements a "rolling list". A rolling list has a maximum capacity, and
 * removes the oldest elements from the list to maintain this capacity.
 * 
 * @param <T> The type if items that this list contains 
 * @author chris
 */
public class RollingList<T> {
   
    /** The items in this rolling list. */
    private final List<T> items = new ArrayList<T>();
    
    /** The maximum capacity of this list. */
    private final int capacity;
    
    /** This list's position pointer. */
    private int position = 0;
    
    /** Whether or not to add a fake empty item to the end of this list. */
    private boolean addEmpty;
    /** The "empty" item to be added. */
    private T empty;
    
    /**
     * Creates a new RollingList of the specified capacity.
     * 
     * @param capacity The capacity of this list.
     */
    public RollingList(final int capacity) {
        this.capacity = capacity;
        this.addEmpty = false;
    }

    /**
     * Creates a new RollingList of the specified capacity, with the specified
     * "empty" element appended to the end.
     * 
     * @param capacity The capacity of this list.
     * @param empty The "empty" element to be added
     */    
    public RollingList(final int capacity, final T empty) {
        this.capacity = capacity;
        this.addEmpty = true;
        this.empty = empty;
    }

    /**
     * Removes the specified element from this list.
     * 
     * @param o The object to be removed from the list.
     * @return True if the list contained the specified element, false otherwise.
     */
    public boolean remove(final Object o) {
        return items.remove(o);
    }

    /**
     * Determines if this list is currently empty.
     * 
     * @return True if the list is empty, false otherwise.
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Retrieves the item at the specified index in this list.
     * 
     * @param index The index to look up
     * @return The item at the specified index
     */
    public T get(final int index) {
        return items.get(index);
    }

    /**
     * Determines if this list contains the specified object.
     * 
     * @param o The object to be checked
     * @return True if this list contains the item, false otherwise.
     */
    public boolean contains(final Object o) {
        return items.contains(o);
    }

    /**
     * Clears all items from this list.
     */
    public void clear() {
        items.clear();
    }  
    
    /**
     * Adds the specified item to this list. If the list has reached its
     * maximum capacity, this method will remove elements from the start of the
     * list until there is sufficient room for the new element.
     * 
     * @param e The element to be added to the list.
     * @return True
     */
    public boolean add(T e) {
        while (items.size() > capacity - 1) {
            items.remove(0);
            position--;
        }
        
        return items.add(e);
    }
    
    /**
     * Retrieves the current position within the list.
     * 
     * @return This list's positional pointer
     */
    public int getPosition() {
        return position;
    }

    /**
     * Sets the positional pointer of this list.
     * 
     * @param position The new position
     */
    public void setPosition(int position) {
        this.position = position;
    }    
    
    /**
     * Determines if there is an element after the positional pointer of the list.
     * 
     * @return True if there is an element, false otherwise.
     */
    public boolean hasNext() {
        return (items.size() > position + 1) || ((items.size() > position) && addEmpty);
    }
    
    /**
     * Retrieves the element after the positional pointer of the list.
     * 
     * @return The next element in the list
     */
    public T getNext() {
        if (items.size() > position + 1 || !addEmpty) {
            return get(++position);
        } else {
            position++;
            return empty;
        }
    }
    
    /**
     * Determines if there is an element befpre the positional pointer of the list.
     * 
     * @return True if there is an element, false otherwise.
     */    
    public boolean hasPrevious() {
        return 0 < position;
    }
    
    /**
     * Retrieves the element before the positional pointer of the list.
     * 
     * @return The previous element in the list
     */    
    public T getPrevious() {
        return get(--position);
    }    
    
    /**
     * Sets the positional pointer of this list to the end.
     */
    public void seekToEnd() {
        position = items.size();
    }
    
    /**
     * Sets the positional pointer of this list to the start.
     */
    public void seekToStart() {
        position = 0;
    }
    
    /**
     * Retrieves a list of items that this rolling list contains.
     * 
     * @return A list of items in this rolling list.
     */
    public List<T> getList() {
        return new ArrayList<T>(items);
    }

}
