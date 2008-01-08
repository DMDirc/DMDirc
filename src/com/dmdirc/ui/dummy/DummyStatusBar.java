/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.ui.dummy;

import com.dmdirc.ui.interfaces.StatusBar;
import com.dmdirc.ui.interfaces.StatusMessageNotifier;

import java.awt.Component;

/**
 * Dummy status bar, used for testing.
 */
public final class DummyStatusBar implements StatusBar {

    /** Instantiates DummyStatusBar. */
    public DummyStatusBar() {
        //Do nothing.
    }

    /** {@inheritDoc} */
    public void setMessage(final String newMessage) {
        System.out.println("DummyStatusBar: " + newMessage);
    }

    /** {@inheritDoc} */
    public void setMessage(final String newMessage, 
            final StatusMessageNotifier newNotifier) {
        System.out.println("DummyStatusBar: " + newMessage);
    }

    /** {@inheritDoc} */
    public void setMessage(final String newMessage, 
            final StatusMessageNotifier newNotifier, final int timeout) {
        System.out.println("DummyStatusBar: " + newMessage);
    }

    /** {@inheritDoc} */
    public void clearMessage() {
        System.out.println("DummyStatusBar: message cleared");
    }

    /** {@inheritDoc} */
    public void addComponent(final Component component) {
        // Do nothing
    }

    /** {@inheritDoc} */
    public void removeComponent(final Component component) {
        // Do nothing
    }

    /** {@inheritDoc} */
    public boolean isVisible() {
        return true;
    }

}
