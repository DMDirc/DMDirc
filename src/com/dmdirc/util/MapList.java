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

package com.dmdirc.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Wraps a Map&lt;S, List&lt;T&gt;&gt; with various convenience methods for
 * accessing the data. Implements a Map-like interface for easier transition.
 * 
 * @param <S> the type of keys maintained by this map
 * @param <T> the type of mapped values
 * @author chris
 */
public class MapList<S,T> {
    
    /** Our internal map. */
    protected final Map<S, List<T>> map;

    /**
     * Creates a new, empty MapList.
     */
    public MapList() {
        map = new HashMap<S, List<T>>();
    }

    /**
     * Creates a new MapList with the values from the specified list.
     * 
     * @param list The MapList whose values should be used
     */
    public MapList(final MapList<S,T> list) {
        map = list.getMap();
    }

    /**
     * Determines if this MapList is empty. An empty MapList is one that either
     * contains no keys, or contains only keys which have no associated values.
     * 
     * @return True if this MapList is empty, false otherwise
     */
    public boolean isEmpty() {
        for (List<T> list : map.values()) {
            if (!list.isEmpty()) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * Determines if this MapList contains the specified key.
     * 
     * @param key The key to look for
     * @return True if this MapList contains the specified key, false otherwise
     */
    public boolean containsKey(final S key) {
        return map.containsKey(key);
    }

    /**
     * Determines if this MapList contains the specified value as a child of
     * the specified key.
     * 
     * @param key The key to search under
     * @param value The value to look for
     * @return True if this MapList contains the specified key/value pair, 
     * false otherwise
     */
    public boolean containsValue(final S key, final T value) {
        return map.containsKey(key) && map.get(key).contains(value);
    }

    /**
     * Retrieves the list of values associated with the specified key.
     * 
     * @param key The key whose values are being retrieved
     * @return The values belonging to the specified key
     */
    public List<T> get(final S key) {
        return map.get(key);
    }
    
    /**
     * Retrieves the value at the specified offset of the specified key.
     * 
     * @param key The key whose values are being retrieved
     * @param index The index of the value to retrieve
     * @return The specified value of the key
     */    
    public T get(final S key, final int index) {
        return map.get(key).get(index);
    }    
    
    /**
     * Retrieves the list of values associated with the specified key, creating
     * the key if neccessary.
     * 
     * @param key The key to retrieve
     * @return A list of the specified key's values
     */
    public List<T> safeGet(final S key) {
        if (!map.containsKey(key)) {
            map.put(key, new ArrayList<T>());
        }
        
        return map.get(key);
    }
    
    /**
     * Adds the specified key to the MapList.
     * 
     * @param key The key to be added
     */
    public void add(final S key) {
        safeGet(key);
    }    

    /**
     * Adds the specified value as a child of the specified key. If the key
     * didn't previous exist, it is created.
     * 
     * @param key The key to which the value is being added
     * @param value The value to be added
     */
    public void add(final S key, final T value) {
        safeGet(key).add(value);
    }

    /**
     * Adds the specified set of values to the specified key. If the key
     * didn't previous exist, it is created.
     * 
     * @param key The key to which the value is being added
     * @param values The values to be added
     */    
    public void add(final S key, final Collection<T> values) {
        safeGet(key).addAll(values);
    }    

    /**
     * Removes the specified key and all of its values.
     * 
     * @param key The key to remove
     */    
    public void remove(final S key) {
        map.remove(key);
    }
    
    /**
     * Removes the specified value from all keys.
     * 
     * @param value The value to remove
     */
    public void removeFromAll(final T value) {
        for (List<T> list : map.values()) {
            list.remove(value);
        }
    }

    /**
     * Removes the specified value from the specified key.
     * 
     * @param key The key whose value is being removed
     * @param value The value to be removed
     */
    public void remove(final S key, final T value) {
        if (map.containsKey(key)) {
            map.get(key).remove(value);
        }
    }    

    /**
     * Entirely clears this MapList.
     */
    public void clear() {
        map.clear();
    }
    
    /**
     * Clears all values of the specified key.
     * 
     * @param key The key to be cleared
     */
    public void clear(final S key) {
        safeGet(key).clear();
    }    

    /**
     * Returns the set of all keys belonging to this MapList.
     * 
     * @return This MapList's keyset
     */
    public Set<S> keySet() {
        return map.keySet();
    }

    /**
     * Returns a collection of all values belonging to the specified key.
     * 
     * @param key The key whose values are being sought
     * @return A collection of values belonging to the key
     */
    public Collection<T> values(final S key) {
        return map.get(key);
    }
    
    /**
     * Retrieves the entry set for this MapList.
     * 
     * @return This MapList's entry set
     */
    public Set<Map.Entry<S, List<T>>> entrySet() {
        return map.entrySet();
    }
    
    /**
     * Retrieves the map behind this maplist.
     * 
     * @return This MapList's map.
     */
    public Map<S, List<T>> getMap() {
        return new HashMap<S, List<T>>(map);
    }

}
