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

package com.dmdirc.ui.messages;

import com.dmdirc.util.colours.Colour;

import java.util.Optional;

/**
 * Keeps track of the number of unread messages in a window, and the most important notification colour for those
 * messages.
 */
public interface UnreadStatusManager {

    /**
     * Gets the total number of unread lines in the window.
     *
     * @return The number of unread lines
     */
    int getUnreadLines();

    /**
     * Gets the most significant notification colour for the window, if any. More important events will override the
     * colour of less important events (for example, the colour for a highlight event may replace the colour for
     * an unread message event).
     *
     * @return The notification colour for the window. If empty, there is no active notification.
     */
    Optional<Colour> getNotificationColour();

    /**
     * Clears the unread status of the window, marking everything as read.
     */
    void clearStatus();

}
