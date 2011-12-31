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

package com.dmdirc;

import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.core.components.WindowComponent;

import java.util.Arrays;

/**
 * A generic custom window implementation.
 */
public class CustomWindow extends FrameContainer {

    /**
     * Creates a new custom window as a child of the specified window.
     *
     * @param name The name of this custom window
     * @param title The title of this custom window
     * @param parent The parent of this custom window
     */
    public CustomWindow(final String name, final String title,
            final FrameContainer parent) {
        super("custom", name, title, parent.getConfigManager(),
                Arrays.asList(WindowComponent.TEXTAREA.getIdentifier()));

        WindowManager.getWindowManager().addWindow(parent, this);
    }

    /**
     * Creates a new custom window as a top-level window.
     *
     * @param name The name of this custom window
     * @param title The parent of this custom window
     */
    public CustomWindow(final String name, final String title) {
        super("custom", name, title,
                IdentityManager.getIdentityManager().getGlobalConfiguration(),
                Arrays.asList(WindowComponent.TEXTAREA.getIdentifier()));

        WindowManager.getWindowManager().addWindow(this);
    }

    /** {@inheritDoc} */
    @Override
    public void windowClosing() {
        // 2: Remove any callbacks or listeners
        // 3: Trigger any actions neccessary
        // 4: Trigger action for the window closing
        // 5: Inform any parents that the window is closing
    }

    /** {@inheritDoc} */
    @Override
    public void windowClosed() {
        // 7: Remove any references to the window and parents
    }

    /** {@inheritDoc} */
    @Override
    public Server getServer() {
        return getParent() == null ? null : getParent().getServer();
    }

}
