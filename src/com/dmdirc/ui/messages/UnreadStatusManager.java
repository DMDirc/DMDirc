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

package com.dmdirc.ui.messages;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.FrameContainer;
import com.dmdirc.events.BaseChannelTextEvent;
import com.dmdirc.events.BaseQueryTextEvent;
import com.dmdirc.events.ChannelHighlightEvent;
import com.dmdirc.events.DisplayableEvent;
import com.dmdirc.events.QueryHighlightEvent;
import com.dmdirc.util.colours.Colour;

import java.util.Optional;

import net.engio.mbassy.listener.Handler;

/**
 * Tracks unread messages and other notifications.
 */
public class UnreadStatusManager {

    private final DMDircMBassador eventBus;
    private final FrameContainer container;
    private int unreadLines;
    private Optional<Colour> notificationColour;

    private Colour miscellaneousColour = Colour.GREEN;
    private Colour messageColour = Colour.BLACK;
    private Colour highlightColour = Colour.RED;

    public UnreadStatusManager(final FrameContainer container) {
        this.container = container;
        this.eventBus = container.getEventBus();
    }

    @Handler
    public void handleDisplayableEvent(final DisplayableEvent event) {
        if (event.getSource().equals(container)) {
            unreadLines++;
            raiseNotificationColour(miscellaneousColour);
        }
    }

    @Handler
    public void handleChannelTextEvent(final BaseChannelTextEvent event) {
        if (event.getSource().equals(container)) {
            raiseNotificationColour(messageColour);
        }
    }

    @Handler
    public void handleQueryTextEvent(final BaseQueryTextEvent event) {
        if (event.getSource().equals(container)) {
            raiseNotificationColour(messageColour);
        }
    }

    @Handler
    public void handleChannelHighlightEvent(final ChannelHighlightEvent event) {
        if (event.getCause().getChannel().equals(container)) {
            raiseNotificationColour(highlightColour);
        }
    }

    @Handler
    public void handleQueryHighlightEvent(final QueryHighlightEvent event) {
        if (event.getCause().getQuery().equals(container)) {
            raiseNotificationColour(highlightColour);
        }
    }

    public int getUnreadLines() {
        return unreadLines;
    }

    public Optional<Colour> getNotificationColour() {
        return notificationColour;
    }

    public void clearStatus() {
        unreadLines = 0;
        notificationColour = Optional.empty();
    }

    private void raiseNotificationColour(final Colour colour) {
        if (notificationColour.isPresent()) {
            if (notificationColour.get().equals(miscellaneousColour)
                    || colour.equals(highlightColour)) {
                notificationColour = Optional.of(colour);
            }
        } else {
            notificationColour = Optional.of(colour);
        }
    }

}
