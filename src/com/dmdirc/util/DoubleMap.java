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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An object that maps keys to values, and values back to keys. Currently
 * does no checking for duplicates. Does not allow null values.
 * 
 * @param <A> The first type of data to be mapped
 * @param <B> The second type of data to be mapped 
 * @author chris
 */
public class DoubleMap<A,B> {
    
    /** The keys in this map. */
    protected final List<A> keys = new ArrayList<A>();
    /** The values in this map. */
    protected final List<B> values = new ArrayList<B>();
    
    /**
     * Adds the specified pair to this map.
     * 
     * @param key The key for the map
     * @param value The value for the map
     */
    public void put(final A key, final B value) {
        if (key == null || value == null) {
            throw new NullPointerException();
        }
        
        keys.add(key);
        values.add(value);
    }
    
    /**
     * Retrieves the value associated with the specified key.
     * 
     * @param key The key to search for 
     * @return The value of the specified key
     */
    public B getValue(final A key) {
        return values.get(keys.indexOf(key));
    }
    
    /**
     * Retrieves the key associated with the specified value.
     * 
     * @param value The value to search for
     * @return The key of the specified value
     */
    public A getKey(final B value) {
        return keys.get(values.indexOf(value));
    }
    
    /**
     * Retrieves the set of keys in this double map.
     * 
     * @return This map's key set
     */
    public Set<A> keySet() {
        return new HashSet<A>(keys);
    }
    
    /**
     * Retrieves the set of values in this double map.
     * 
     * @return This map's value set
     */
    public Set<B> valueSet() {
        return new HashSet<B>(values);
    }

}
