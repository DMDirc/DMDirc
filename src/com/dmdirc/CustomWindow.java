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

package com.dmdirc;

import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.ui.core.components.WindowComponent;
import com.dmdirc.util.URLBuilder;

import com.google.common.base.Optional;
import net.engio.mbassy.bus.MBassador;

import java.util.Arrays;

/**
 * A generic custom window implementation.
 */
public class CustomWindow extends FrameContainer {

    /**
     * Creates a new custom window as a child of the specified window.
     *
     * @param name       The name of this custom window
     * @param title      The title of this custom window
     * @param parent     The parent of this custom window
     * @param urlBuilder The URL builder to use when finding icons.
     */
    public CustomWindow(
            final String name,
            final String title,
            final FrameContainer parent,
            final URLBuilder urlBuilder) {
        super(parent, "custom", name, title, parent.getConfigManager(), urlBuilder,
                parent.getEventBus(),
                Arrays.asList(WindowComponent.TEXTAREA.getIdentifier()));
    }

    /**
     * Creates a new custom window as a top-level window.
     *
     * @param name           The name of this custom window
     * @param title          The parent of this custom window
     * @param configProvider The config provider to read settings from.
     * @param urlBuilder     The URL builder to use when finding icons.
     * @param eventBus       The bus to dispatch events on.
     */
    public CustomWindow(
            final String name,
            final String title,
            final AggregateConfigProvider configProvider,
            final URLBuilder urlBuilder,
            final MBassador eventBus) {
        super(null, "custom", name, title, configProvider, urlBuilder, eventBus,
                Arrays.asList(WindowComponent.TEXTAREA.getIdentifier()));
    }

    @Override
    public Connection getConnection() {
        final Optional<FrameContainer> parent = getParent();
        return parent.isPresent() ? parent.get().getConnection() : null;
    }

}
