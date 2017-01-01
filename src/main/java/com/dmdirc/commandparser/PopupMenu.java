/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.commandparser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents an abstract, UI-independent popup menu.
 */
public class PopupMenu {

    /** The items contained within this popup menu. */
    private final List<PopupMenuItem> items = new ArrayList<>();

    /**
     * Retrieves a list of items contained within this popup menu.
     *
     * @return A list of this popup menu's items.
     */
    public List<PopupMenuItem> getItems() {
        return items;
    }

    /**
     * Adds the specified item to this popup menu.
     *
     * @param e The item to be added to the popup menu.
     */
    public void add(final PopupMenuItem e) {
        items.add(e);
    }

    /**
     * Adds all of the items in the specified collection to this popup menu.
     *
     * @param c The collection whose items should be added.
     */
    public void addAll(final Collection<? extends PopupMenuItem> c) {
        items.addAll(c);
    }

}
