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

package com.dmdirc.installer;

/**
 * Simple interface describing available settings for the installer.
 */
public interface Settings {

    /**
     * Returns the state of the shortcutMenu checkbox.
     *
     * @return shortcutMenu checkbox state
     */
    public boolean getShortcutMenuState();

    /**
     * Returns the state of the shortcutDesktop checkbox.
     *
     * @return shortcutDesktop checkbox state
     */
    public boolean getShortcutDesktopState();

    /**
     * Returns the state of the shortcutDesktop checkbox.
     *
     * @return shortcutDesktop checkbox state
     */
    public boolean getShortcutQuickState();

    /**
     * Returns the state of the shortcutProtocol checkbox.
     *
     * @return shortcutDesktop checkbox state
     */
    public boolean getShortcutProtocolState();

    /**
     * Returns the location chosen for installation.
     *
     * @return location chosen for installation.
     */
    public String getInstallLocation();
}