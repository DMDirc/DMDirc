/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
import com.dmdirc.ui.interfaces.Window;

/**
 * A generic custom window implementation.
 *
 * @author chris
 */
public class CustomWindow extends FrameContainer {

    /** This custom window's name. */
    private final String name;

    /** This custom window's title. */
    private final String title;

    /** The window used by this container. */
    private Window window;

    /** This window's parent window. */
    private Window parent;

    /**
     * Creates a new custom window as a child of the specified window.
     *
     * @param name The name of this custom window
     * @param title The title of this custom window
     * @param parent The parent of this custom window
     */
    public CustomWindow(final String name, final String title,
            final Window parent) {
        super("custom", parent.getConfigManager());

        this.name = name;
        this.title = title;
        this.parent = parent;

        window = Main.getUI().getWindow(this);
        window.setTitle(title);

        WindowManager.addWindow(parent, window);

        window.open();
    }

    /**
     * Creates a new custom window as a top-level window.
     *
     * @param name The name of this custom window
     * @param title The parent of this custom window
     */
    public CustomWindow(final String name, final String title) {
        super("custom", IdentityManager.getGlobalConfig());

        this.name = name;
        this.title = title;

        window = Main.getUI().getWindow(this);
        window.setTitle(title);

        WindowManager.addWindow(window);

        window.open();
    }

    /** {@inheritDoc} */
    @Override
    public Window getFrame() {
        return window;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public void windowClosing() {
        // 1: Make the window non-visible
        window.setVisible(false);

        // 2: Remove any callbacks or listeners
        // 3: Trigger any actions neccessary
        // 4: Trigger action for the window closing
        // 5: Inform any parents that the window is closing

        // 6: Remove the window from the window manager
        WindowManager.removeWindow(window);

        // 7: Remove any references to the window and parents
        window = null; // NOPMD
        parent = null; // NOPMD
    }

    /** {@inheritDoc} */
    @Override
    public Server getServer() {
        return parent == null ? null : parent.getContainer().getServer();
    }

    /**
     * Retrieves this custom window's name.
     *
     * @return This custom window's name
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieves this custom window's title.
     *
     * @return This custom window's title
     */
    public String getTitle() {
        return title;
    }

}
