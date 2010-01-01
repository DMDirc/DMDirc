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

import java.util.List;

/**
 * Wraps a Map&lt;S, List&lt;T&gt;&gt; with various convenience methods for
 * accessing the data. Implements a Map-like interface for easier transition.
 * This implementation uses WeakLists (i.e., lists of weak references) - all
 * references to values are wrapped in WeakReferences.
 * 
 * @param <S> the type of keys maintained by this map
 * @param <T> the type of mapped values
 * @author chris
 */
public class WeakMapList<S,T> extends MapList<S,T> {
    
    /**
     * Retrieves the list of values associated with the specified key, creating
     * the key if neccessary.
     * 
     * @param key The key to retrieve
     * @return A list of the specified key's values
     */
    @Override
    public List<T> safeGet(final S key) {
        if (!map.containsKey(key)) {
            map.put(key, new WeakList<T>());
        }
        
        return map.get(key);
    }
}
