/*
 * Copyright (c) 2006-2011 DMDirc Developers
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

import com.dmdirc.ui.StatusMessage;

/**
 * Status bar interface.
 */
public interface StatusBar {

    /**
     * Sets the message for this message label.
     *
     * @param message Message object to show
     *
     * @since 0.6.6
     */
    void setMessage(final StatusMessage message);

    /**
     * Removes the message from the status bar.
     */
    void clearMessage();

    /**
     * Adds a component to the status bar.
     *
     * @param component component to add
     */
    void addComponent(final StatusBarComponent component);

    /**
     * Removes a component to the status bar.
     *
     * @param component component to add
     */
    void removeComponent(final StatusBarComponent component);
}
