/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.logging;

import com.dmdirc.FrameContainer;
import com.dmdirc.Main;
import com.dmdirc.Server;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.interfaces.Window;

/**
 * Displays an extended history of a window.
 *
 * @author Chris
 */
public class HistoryWindow extends FrameContainer {

    /** The window we're using. */
    private Window window;

    /** Our parent window. */
    private Window parent;

    /**
     * Creates a new HistoryWindow.
     *
     * @param title The title of the window
     * @param reader The reader to use to get the history
     * @param parent The window this history window was opened from
     * @param numLines The number of lines to show
     */
    public HistoryWindow(final String title, final ReverseFileReader reader,
                         final Window parent, final int numLines) {
        super("raw", title, parent.getConfigManager());

        this.parent = parent;

        window = Main.getUI().getWindow(this);

        WindowManager.addWindow(parent, window);
        window.setTitle(title);
        window.open();
        final int frameBufferSize = IdentityManager.getGlobalConfig().getOptionInt(
                "ui", "frameBufferSize");
        window.addLine(reader.getLinesAsString(Math.min(frameBufferSize, numLines)), false);
    }

    /** {@inheritDoc} */
    @Override
    public Window getFrame() {
        return window;
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

}
