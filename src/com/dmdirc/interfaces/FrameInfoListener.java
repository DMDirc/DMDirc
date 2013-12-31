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

import java.util.EventListener;

/**
 * Defines the methods that should be implemented by classes which wish to
 * receive information about frame icon or title changes.
 *
 * @since 0.6.3m2
 */
public interface FrameInfoListener extends EventListener {

    /**
     * Called when a window's icon is changed.
     *
     * @param window The window whose icon changed
     * @param icon The new icon for the window
     */
    void iconChanged(final FrameContainer window, final String icon);

    /**
     * Called when a window's name is changed.
     *
     * @param window The window whose name changed
     * @param name The new name for the window
     */
    void nameChanged(final FrameContainer window, final String name);

    /**
     * Called when a window's title is changed.
     *
     * @param window The window whose title changed
     * @param title The new title for the window
     */
    void titleChanged(final FrameContainer window, final String title);

}
