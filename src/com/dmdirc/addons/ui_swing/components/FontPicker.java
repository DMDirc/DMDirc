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

package com.dmdirc.addons.ui_swing.components;

import com.dmdirc.addons.ui_swing.components.renderers.FontListCellRenderer;

import java.awt.Font;
import java.awt.GraphicsEnvironment;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

/**
 * System font picking component.
 */
public class FontPicker extends JComboBox {

    private static final long serialVersionUID = -9054812588033935839L;

    /**
     * Creates a new Font picker for the specified font family.
     *
     * @param fontFamily Font family
     */
    public FontPicker(final String fontFamily) {
        super(new DefaultComboBoxModel());

        final String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().
                getAvailableFontFamilyNames();
        final int size = getFont().getSize();
        for (String font : fonts) {
            ((DefaultComboBoxModel) getModel()).addElement(new Font(font,
                    Font.PLAIN, size));
        }
        setSelectedItem(new Font(fontFamily, Font.PLAIN, size));

        setRenderer(new FontListCellRenderer());
    }
}
