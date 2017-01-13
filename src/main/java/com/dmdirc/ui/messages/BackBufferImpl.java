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

import com.dmdirc.events.DisplayLocation;
import com.dmdirc.events.DisplayProperty;
import com.dmdirc.events.DisplayableEvent;
import com.dmdirc.events.eventbus.EventBus;
import com.dmdirc.interfaces.WindowModel;
import com.dmdirc.util.EventUtils;
import java.util.Arrays;
import net.engio.mbassy.listener.Handler;

/**
 * Models the history of a window in the client.
 */
public class BackBufferImpl implements BackBuffer {

    private final Document document;
    private final Styliser styliser;
    private final EventBus eventBus;
    private final EventFormatter formatter;
    private final WindowModel owner;

    public BackBufferImpl(
            final WindowModel owner,
            final ColourManagerFactory colourManagerFactory,
            final EventFormatter formatter) {
        this.owner = owner;
        this.styliser = new Styliser(
                owner.getConnection().orElse(null),
                owner.getConfigManager(),
                colourManagerFactory.getColourManager(owner.getConfigManager()));
        this.document = new IRCDocument(owner.getConfigManager(), styliser);
        this.eventBus = owner.getEventBus();
        this.formatter = formatter;
    }

    /**
     * Starts adding events received on the event bus to this buffer's document.
     */
    public void startAddingEvents() {
        eventBus.subscribe(this);
    }

    /**
     * Stops adding events received on the event bus to this buffer's document.
     */
    public void stopAddingEvents() {
        eventBus.unsubscribe(this);
    }

    /**
     * Handles a displayable event received on the event bus.
     *
     * @param event The event to be displayed.
     */
    @Handler(priority = EventUtils.PRIORITY_DISPLAYABLE_EVENT_HANDLER)
    private void handleDisplayableEvent(final DisplayableEvent event) {
        if (shouldDisplay(event)) {
            formatter.format(event).map(s -> s.split("\n")).map(Arrays::stream).ifPresent(
                    t -> t.forEach(line -> document.addText(
                            event.getTimestamp(), event.getDisplayProperties(), line)));
        }
    }

    /**
     * Determines if the specified event should be displayed in this backbuffer.
     *
     * @param event The event to check
     * @return True if the event should be displayed, false otherwise.
     */
    private boolean shouldDisplay(final DisplayableEvent event) {
        return formatter.getFormatDisplayableProperties(event).get(DisplayProperty.DISPLAY_LOCATION)
                    .orElse(DisplayLocation.SOURCE).shouldDisplay(owner, event)
                && !event.hasDisplayProperty(DisplayProperty.DO_NOT_DISPLAY);
    }

    @Override
    public Document getDocument() {
        return document;
    }

    @Override
    public Styliser getStyliser() {
        return styliser;
    }

}
