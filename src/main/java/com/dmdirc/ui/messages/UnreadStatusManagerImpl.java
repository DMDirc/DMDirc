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

package com.dmdirc.ui.messages;

import com.dmdirc.config.ConfigBinding;
import com.dmdirc.events.BaseChannelTextEvent;
import com.dmdirc.events.BaseQueryTextEvent;
import com.dmdirc.events.ChannelHighlightEvent;
import com.dmdirc.events.DisplayProperty;
import com.dmdirc.events.DisplayableEvent;
import com.dmdirc.events.QueryHighlightEvent;
import com.dmdirc.events.UnreadStatusChangedEvent;
import com.dmdirc.interfaces.EventBus;
import com.dmdirc.interfaces.WindowModel;
import com.dmdirc.util.colours.Colour;

import java.util.Optional;

import net.engio.mbassy.listener.Handler;

/**
 * Tracks unread messages and other notifications.
 */
public class UnreadStatusManagerImpl implements UnreadStatusManager {

    private final EventBus eventBus;
    private final WindowModel container;
    private final ColourManager colourManager;

    private int unreadLines;
    private Optional<Colour> notificationColour = Optional.empty();

    private Optional<Colour> miscellaneousColour = Optional.of(Colour.GREEN);
    private Optional<Colour> messageColour = Optional.of(Colour.BLUE);
    private Optional<Colour> highlightColour = Optional.of(Colour.RED);

    public UnreadStatusManagerImpl(final WindowModel container) {
        this.container = container;
        this.eventBus = container.getEventBus();
        this.colourManager = new ColourManagerImpl(container.getConfigManager());
    }

    @Handler
    public void handleDisplayableEvent(final DisplayableEvent event) {
        if (includeEvent(event)) {
            updateStatus(miscellaneousColour, unreadLines + 1);
        }
    }

    @Handler
    public void handleChannelTextEvent(final BaseChannelTextEvent event) {
        if (includeEvent(event)) {
            updateStatus(messageColour);
        }
    }

    @Handler
    public void handleQueryTextEvent(final BaseQueryTextEvent event) {
        if (includeEvent(event)) {
            updateStatus(messageColour);
        }
    }

    @Handler
    public void handleChannelHighlightEvent(final ChannelHighlightEvent event) {
        if (includeEvent(event)) {
            updateStatus(highlightColour);
        }
    }

    @Handler
    public void handleQueryHighlightEvent(final QueryHighlightEvent event) {
        if (includeEvent(event)) {
            updateStatus(highlightColour);
        }
    }

    private boolean includeEvent(final DisplayableEvent event) {
        return event.getSource().equals(container)
                && !event.getDisplayProperty(DisplayProperty.DO_NOT_DISPLAY).orElse(false);
    }

    @Override
    public int getUnreadLines() {
        return unreadLines;
    }

    @Override
    public Optional<Colour> getNotificationColour() {
        return notificationColour;
    }

    @Override
    public void clearStatus() {
        updateStatus(Optional.empty(), 0);
    }

    private void updateStatus(final Optional<Colour> desiredColour) {
        updateStatus(desiredColour, unreadLines);
    }

    private void updateStatus(final Optional<Colour> desiredColour, final int newUnreadCount) {
        final Optional<Colour> newColour = getBestColour(desiredColour, notificationColour);
        final boolean updated = !newColour.equals(notificationColour)
                || newUnreadCount != unreadLines;
        notificationColour = newColour;
        unreadLines = newUnreadCount;

        if (updated) {
            publishChangedEvent();
        }
    }

    private Optional<Colour> getBestColour(
            final Optional<Colour> desiredColour,
            final Optional<Colour> existingColour) {
        if (!desiredColour.isPresent()) {
            // If we're trying to explicitly reset, go with the empty one.
            return desiredColour;
        }

        if (desiredColour.equals(highlightColour)
                || !existingColour.isPresent()
                || existingColour.equals(miscellaneousColour)) {
            return desiredColour;
        } else {
            return existingColour;
        }
    }

    @ConfigBinding(domain = "ui", key = "miscellaneousNotificationColour")
    void handleMiscellaneousColour(final String colour) {
        final Optional<Colour> newColour = Optional.ofNullable(
                colourManager.getColourFromString(colour, Colour.GREEN));
        if (notificationColour.equals(miscellaneousColour)) {
            notificationColour = newColour;
            publishChangedEvent();
        }
        miscellaneousColour = newColour;
    }

    @ConfigBinding(domain = "ui", key = "messageNotificationColour")
    void handleMessageColour(final String colour) {
        final Optional<Colour> newColour = Optional.ofNullable(
                colourManager.getColourFromString(colour, Colour.BLUE));
        if (notificationColour.equals(messageColour)) {
            notificationColour = newColour;
            publishChangedEvent();
        }
        messageColour = newColour;
    }

    @ConfigBinding(domain = "ui", key = "highlightNotificationColour")
    void handleHighlightColour(final String colour) {
        final Optional<Colour> newColour = Optional.ofNullable(
                colourManager.getColourFromString(colour, Colour.RED));
        if (notificationColour.equals(highlightColour)) {
            notificationColour = newColour;
            publishChangedEvent();
        }
        highlightColour = newColour;
    }

    private void publishChangedEvent() {
        eventBus.publishAsync(new UnreadStatusChangedEvent(container, this, notificationColour,
                unreadLines));
    }

}
