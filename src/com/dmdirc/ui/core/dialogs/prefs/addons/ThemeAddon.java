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

package com.dmdirc.ui.core.dialogs.prefs.addons;

import com.dmdirc.ui.themes.Theme;

/**
 *
 * @since 0.6.3
 * @author chris
 */
public class ThemeAddon extends Addon {

    private final Theme theme;

    public ThemeAddon(Theme theme) {
        this.theme = theme;
    }

    /** {@inheritDoc} */
    @Override
    public AddonType getType() {
        return AddonType.THEME;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return theme.getName();
    }

    /** {@inheritDoc} */
    @Override
    public String getVersion() {
        return theme.getVersion();
    }

    /** {@inheritDoc} */
    @Override
    public AddonStatus getStatus() {
        return theme.isEnabled() ? AddonStatus.ENABLED
                : theme.isValidTheme() ? AddonStatus.ERROR : AddonStatus.DISABLED;
    }

    /** {@inheritDoc} */
    @Override
    public String getStatusText() {
        return "";
    }

    /** {@inheritDoc} */
    @Override
    public String getAuthor() {
        return theme.getAuthor();
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return theme.getDescription();
    }

    /** {@inheritDoc} */
    @Override
    protected boolean enable() {
        theme.applyTheme();
        return true;
    }

    /** {@inheritDoc} */
    @Override
    protected boolean disable() {
        theme.removeTheme();
        return false;
    }

    /** {@inheritDoc} */
    @Override
    protected void install() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    protected void uninstall() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    protected void setUpdateState(boolean check) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
