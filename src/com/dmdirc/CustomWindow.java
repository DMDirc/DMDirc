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

package com.dmdirc;

import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.ui.core.components.WindowComponent;
import com.dmdirc.ui.messages.BackBufferFactory;

import java.util.Collections;
import java.util.Optional;

/**
 * A generic custom window implementation.
 */
public class CustomWindow extends FrameContainer {

    /**
     * Creates a new custom window as a child of the specified window.
     */
    public CustomWindow(
            final String name,
            final String title,
            final FrameContainer parent,
            final BackBufferFactory backBufferFactory) {
        super(parent, "custom", name, title, parent.getConfigManager(), backBufferFactory,
                parent.getEventBus(),
                Collections.singletonList(WindowComponent.TEXTAREA.getIdentifier()));
        initBackBuffer();
    }

    /**
     * Creates a new custom window as a top-level window.
     */
    public CustomWindow(
            final String name,
            final String title,
            final AggregateConfigProvider configProvider,
            final DMDircMBassador eventBus,
            final BackBufferFactory backBufferFactory) {
        super(null, "custom", name, title, configProvider, backBufferFactory,
                eventBus, Collections.singletonList(WindowComponent.TEXTAREA.getIdentifier()));
        initBackBuffer();
    }

    @Override
    public Optional<Connection> getConnection() {
        return getParent().flatMap(FrameContainer::getConnection);
    }

}
