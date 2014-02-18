/*
 * Copyright (c) 2006-2014 DMDirc Developers
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

package com.dmdirc.interfaces;

import com.dmdirc.FrameContainer;

/**
 * An interface for classes interested in changes made to a {@link FrameContainer}'s components.
 *
 * @since 0.6.6
 */
public interface FrameComponentChangeListener {

    /**
     * Called whenever a new component is added to the container.
     *
     * @param container The container the component was added to
     * @param component The name of the component that was added
     */
    void componentAdded(FrameContainer container, String component);

    /**
     * Called whenever a new component is removed from the container.
     *
     * @param container The container the component was removed from
     * @param component The name of the component that was removed
     */
    void componentRemoved(FrameContainer container, String component);

}
