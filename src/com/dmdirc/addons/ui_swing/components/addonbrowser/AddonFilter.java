/*
 * 
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

package com.dmdirc.addons.ui_swing.components.addonbrowser;

import javax.swing.ButtonModel;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.RowFilter.Entry;
import javax.swing.table.DefaultTableModel;

/**
 * Addon filter.
 */
public class AddonFilter extends RowFilter<DefaultTableModel, Integer> {

    private ButtonModel verifiedBox;
    private ButtonModel unverifiedBox;
    private ButtonModel installedBox;
    private ButtonModel notinstalledBox;
    private ButtonModel pluginsBox;
    private ButtonModel themesBox;
    private ButtonModel actionsBox;
    private JTextField searchBox;

    /**
     * Creates a new addon filter.
     * 
     * @param verifiedBox
     * @param unverifiedBox
     * @param installedBox
     * @param notinstalledBox
     * @param pluginsBox
     * @param themesBox
     * @param actionsBox
     * @param searchBox
     */
    public AddonFilter(final ButtonModel verifiedBox,
            final ButtonModel unverifiedBox, final ButtonModel installedBox,
            final ButtonModel notinstalledBox, final ButtonModel pluginsBox,
            final ButtonModel themesBox, final ButtonModel actionsBox,
            final JTextField searchBox) {
        this.verifiedBox = verifiedBox;
        this.unverifiedBox = unverifiedBox;
        this.installedBox = installedBox;
        this.notinstalledBox = notinstalledBox;
        this.pluginsBox = pluginsBox;
        this.themesBox = themesBox;
        this.actionsBox = actionsBox;
        this.searchBox = searchBox;
    }

    /** {@inheritDoc} */
    @Override
    public boolean include(
            Entry<? extends DefaultTableModel, ? extends Integer> entry) {
        AddonInfo info = ((AddonInfoLabel) entry.getModel().getValueAt(entry.
                getIdentifier(), 0)).getAddonInfo();
        if ((!verifiedBox.isSelected() && info.isVerified()) ||
                (!unverifiedBox.isSelected() && !info.isVerified()) ||
                (!installedBox.isSelected() && info.isInstalled()) ||
                (!notinstalledBox.isSelected() &&
                !info.isInstalled()) || (!pluginsBox.isSelected() &&
                info.getType() == AddonType.TYPE_PLUGIN) ||
                (!themesBox.isSelected() && info.getType() ==
                AddonType.TYPE_THEME) ||
                (!actionsBox.isSelected() && info.getType() ==
                AddonType.TYPE_ACTION_PACK) || (!searchBox.getText().
                isEmpty() && !info.matches(searchBox.getText()))) {
            return false;
        }
        return true;
    }
}
