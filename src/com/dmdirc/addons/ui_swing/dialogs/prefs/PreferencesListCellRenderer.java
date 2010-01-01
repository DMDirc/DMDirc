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

package com.dmdirc.addons.ui_swing.dialogs.prefs;

import com.dmdirc.config.prefs.PreferencesCategory;

import java.awt.Component;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;


/**
 * Prefs dialog list cell renderer.
 *
 * @since 0.6.3m1
 */
public class PreferencesListCellRenderer extends JLabel implements ListCellRenderer {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Number of categories shown. */
    private final int numCats;
    /** Label map. */
    private Map<PreferencesCategory, JLabel> labelMap;
    
    /**
     * Instantiates a new prefs list cell renderer.
     *
     * @param numCats Number of categories in the list
     */
    public PreferencesListCellRenderer(final int numCats) {
        labelMap = new HashMap<PreferencesCategory, JLabel>();
        this.numCats = numCats;
    }

    /** {@inheritDoc} */
    @Override
    public final Component getListCellRendererComponent(final JList list,
            final Object value, final int index, final boolean isSelected,
            final boolean cellHasFocus) {
        final PreferencesCategory cat = (PreferencesCategory) value;
        if (!labelMap.containsKey(cat)) {
            labelMap.put(cat, new CategoryLabel(list, cat, numCats, index));
        }
        final JLabel label = labelMap.get(cat);

        if (isSelected) {
            label.setFont(getFont().deriveFont(Font.BOLD));
        } else {
            label.setFont(getFont().deriveFont(Font.PLAIN));
        }

        return label;
    }

}
