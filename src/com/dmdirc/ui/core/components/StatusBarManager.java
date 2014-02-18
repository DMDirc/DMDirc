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

package com.dmdirc.ui.core.components;

import com.dmdirc.interfaces.ui.StatusBar;
import com.dmdirc.interfaces.ui.StatusBarComponent;
import com.dmdirc.ui.StatusMessage;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * A manager for status bars or status bar like components.
 *
 * @since 0.6.5
 */
@Singleton
public class StatusBarManager implements StatusBar {

    /** A collection of known status bars. */
    private final Collection<StatusBar> statusBars = new CopyOnWriteArraySet<>();

    /**
     * Creates a new instance of {@link StatusBarManager}.
     */
    @Inject
    public StatusBarManager() {
    }

    /**
     * Registers a new status bar with the manager. All requests to interact with status bars will
     * be passed on to this status bar until it is unregistered.
     *
     * @param statusBar The status bar to be added to the manager
     */
    public void registerStatusBar(final StatusBar statusBar) {
        statusBars.add(statusBar);
    }

    /**
     * Unregisters the specified status bar with the manager.
     *
     * @param statusBar The status bar to be unregistered
     */
    public void unregisterStatusBar(final StatusBar statusBar) {
        statusBars.remove(statusBar);
    }

    /** {@inheritDoc} */
    @Override
    public void setMessage(final StatusMessage message) {
        for (StatusBar statusBar : statusBars) {
            statusBar.setMessage(message);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void clearMessage() {
        for (StatusBar statusBar : statusBars) {
            statusBar.clearMessage();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void addComponent(final StatusBarComponent component) {
        for (StatusBar statusBar : statusBars) {
            statusBar.addComponent(component);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void removeComponent(final StatusBarComponent component) {
        for (StatusBar statusBar : statusBars) {
            statusBar.removeComponent(component);
        }
    }

}
