/*
 * Copyright (c) 2006-2017 DMDirc Developers
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

package com.dmdirc.events;

import com.dmdirc.interfaces.WindowModel;

/**
 * Valid values for the DISPLAY_LOCATION property and how to test for them.
 */
@FunctionalInterface
public interface DisplayLocation {
    /** Event came from the same WindowModel. */
    DisplayLocation SOURCE = (model, event) -> event.getSource().equals(model);

    /** Event came from a WindowModel that shares the same connection. */
    DisplayLocation SAME_CONNECTION = (model, event) -> event.getSource().getConnection().isPresent()
            && model.getConnection().isPresent()
            && event.getSource().getConnection().get().equals(model.getConnection().get());
    /**
     * Test to see if this location is valid.
     *
     * @param model WindowModel we are wanting to display the event in.
     * @param event Event we are wanting to display.
     * @return True if the event should be displayed here.
     */
    boolean shouldDisplay(final WindowModel model, final DisplayableEvent event);
};
