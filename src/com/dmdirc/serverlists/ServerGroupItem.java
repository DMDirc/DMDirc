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

package com.dmdirc.serverlists;

import java.net.URI;

/**
 * An item which is included in a server group.
 *
 * @author chris
 * @since 0.6.4
 */
public interface ServerGroupItem {

    /**
     * Retrieves a group that is either the same as this item, or contains this
     * item.
     *
     * @return A group containing or equal to this item
     */
    ServerGroup getGroup();

    /**
     * Retrieves the name of this item.
     *
     * @return A string containing this item's name
     */
    String getName();

    /**
     * Initiates a connection attempt for this item.
     */
    void connect();

    /**
     * Retrieves a URI for this item.
     *
     * @return A URI that represents this item or one of its children, or null
     * if the item has no URIs associated with it.
     */
    URI getAddress();

}
