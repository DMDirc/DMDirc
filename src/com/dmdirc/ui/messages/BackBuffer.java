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
import com.dmdirc.config.ConfigBinding;
import com.dmdirc.events.DisplayProperty;
import com.dmdirc.events.DisplayableEvent;
import com.dmdirc.interfaces.config.AggregateConfigProvider;

import net.engio.mbassy.listener.Handler;

/**
 * Models the history of a window in the client.
 */
public class BackBuffer {

    private final IRCDocument document;
    private final Styliser styliser;
    private final DMDircMBassador eventBus;
    private final EventFormatter formatter;
    private final AggregateConfigProvider configProvider;

    @ConfigBinding(domain="dev", key="enableNewEvents")
    private boolean enabled;

    public BackBuffer(
            final FrameContainer owner,
            final ColourManagerFactory colourManagerFactory,
            final EventFormatter formatter) {
        this.styliser = new Styliser(
                owner.getConnection().orElse(null),
                owner.getConfigManager(),
                colourManagerFactory.getColourManager(owner.getConfigManager()));
        this.document = new IRCDocument(owner.getConfigManager(), styliser, owner.getEventBus());
        this.eventBus = owner.getEventBus();
        this.formatter = formatter;
        this.configProvider = owner.getConfigManager();
    }

    /**
     * Starts adding events received on the event bus to this buffer's document.
     */
    public void startAddingEvents() {
        eventBus.subscribe(this);
        configProvider.getBinder().bind(this, BackBuffer.class);
    }

    /**
     * Stops adding events received on the event bus to this buffer's document.
     */
    public void stopAddingEvents() {
        eventBus.unsubscribe(this);
        configProvider.getBinder().unbind(this);
    }

    /**
     * Handles a displayable event received on the event bus.
     *
     * @param event The event to be displayed.
     */
    @Handler
    public void handleDisplayableEvent(final DisplayableEvent event) {
        if (enabled && !event.getDisplayProperty(DisplayProperty.HANDLED).isPresent()) {
            formatter.format(event).ifPresent(
                    t -> document.addText(event.getTimestamp(), event.getDisplayProperties(), t));
            event.setDisplayProperty(DisplayProperty.HANDLED, Boolean.TRUE);
        }
    }

    public IRCDocument getDocument() {
        return document;
    }

    public Styliser getStyliser() {
        return styliser;
    }

}
