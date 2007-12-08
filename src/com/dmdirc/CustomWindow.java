/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.interfaces.Window;

/**
 * 
 * @author chris
 */
public class CustomWindow extends FrameContainer {

    /** This custom window's name. */
    private final String name;

    /** This custom window's title. */
    private final String title;

    private final Window window;

    private Window parent = null;

    public CustomWindow(final String name, final String title,
            final Window parent) {
        super();

        this.name = name;
        this.title = title;
        this.parent = parent;

        icon = IconManager.getIconManager().getIcon("custom");

        window = Main.getUI().getWindow(this);
        window.setTitle(title);
        window.setFrameIcon(icon);

        WindowManager.addWindow(parent, window);

        window.setVisible(true);
    }

    public CustomWindow(final String name, final String title) {
        super();

        this.name = name;
        this.title = title;

        icon = IconManager.getIconManager().getIcon("custom");

        window = Main.getUI().getWindow(this);
        window.setTitle(title);
        window.setFrameIcon(icon);

        WindowManager.addWindow(window);

        window.setVisible(true);
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
    public void close() {
        window.setVisible(false);

        WindowManager.removeWindow(window);
        Main.getUI().getMainWindow().delChild(window);
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

    @Override
    public ConfigManager getConfigManager() {
        return parent == null ? IdentityManager.getGlobalConfig() : parent
                .getConfigManager();
    }

}
