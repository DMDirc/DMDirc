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

package com.dmdirc.events;

import com.dmdirc.interfaces.WindowModel;
import com.dmdirc.ui.messages.UnreadStatusManager;
import com.dmdirc.util.colours.Colour;

import java.util.Optional;

/**
 * Event raised when the unread status of a window has changed.
 */
public class UnreadStatusChangedEvent extends DMDircEvent {

    private final WindowModel source;
    private final UnreadStatusManager manager;
    private final Optional<Colour> notificationColour;
    private final int unreadCount;

    public UnreadStatusChangedEvent(final WindowModel source, final UnreadStatusManager manager,
            final Optional<Colour> notificationColour, final int unreadCount) {
        this.source = source;
        this.manager = manager;
        this.notificationColour = notificationColour;
        this.unreadCount = unreadCount;
    }

    public WindowModel getSource() {
        return source;
    }

    public UnreadStatusManager getManager() {
        return manager;
    }

    public Optional<Colour> getNotificationColour() {
        return notificationColour;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

}
