/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

package com.dmdirc.interfaces.ui;

import com.dmdirc.interfaces.WindowModel;

/**
 * FrameListeners are registered with the {@link com.dmdirc.ui.WindowManager} to receive events
 * pertaining to frames.
 */
public interface FrameListener {

    /**
     * Adds a window to this frame manager.
     *  @param window The server to be added
     * @param focus  Should this window become focused
     */
    void addWindow(WindowModel window, final boolean focus);

    /**
     * Removes a window from this frame manager.
     *
     * @param window The server to be removed
     */
    void delWindow(WindowModel window);

    /**
     * Adds a new window to this frame manager.
     *  @param parent The parent to which the window belongs, or null
     * @param window The custom window to be added
     * @param focus  Should this window become focused
     */
    void addWindow(WindowModel parent, WindowModel window, final boolean focus);

    /**
     * Removes a window from this frame manager.
     *  @param parent The parent to which the window belongs, or null
     * @param window The custom window to be removed
     */
    void delWindow(WindowModel parent, WindowModel window);

}
