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

package com.dmdirc.addons.ui_dummy;

import com.dmdirc.ui.interfaces.MainWindow;

import javax.swing.ImageIcon;

/**
 * Dummy main window, used for testing.
 */
public final class DummyMainWindow implements MainWindow {
    
    /** Are the windows maximised? */
    private boolean maximised;
    /** Are we visible? */
    private boolean visible = true;
    
    /**
     * Creates a new instance of DummyMainWindow.
     */
    public DummyMainWindow() {
        //Do nothing.
    }
    
    /** {@inheritDoc} */
    @Override
    public ImageIcon getIcon() {
        return null;
    }
    
    /** {@inheritDoc} */
    @Override
    public void quit() {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    @Override
    public void setMaximised(final boolean max) {
        maximised = max;
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean getMaximised() {
        return maximised;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getTitlePrefix() {
        return "DMDirc: Dummy UI: ";
    }
    
    /** {@inheritDoc} */
    @Override
    public void setVisible(final boolean visible) {
        this.visible = visible;
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean isVisible() {
        return visible;
    }
    
}
