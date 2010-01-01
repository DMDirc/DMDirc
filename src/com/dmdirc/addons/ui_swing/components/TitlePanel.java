/*
 * 
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

package com.dmdirc.addons.ui_swing.components;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import net.miginfocom.swing.MigLayout;

/**
 * Displays a title panel.
 */
public class TitlePanel extends JPanel {

    /**
     * A version number for this class. It should be changed whenever the
     * class structure is changed (or anything else that would prevent
     * serialized objects being unserialized with the new class).
     */
    private static final long serialVersionUID = -4026633984970698130L;
    /** Title label. */
    private JLabel title;
    /** Title border. */
    final Border border;

    /**
     * Instantiates a new title panel.
     *
     * @param border Border to use for the panel
     */
    public TitlePanel(final Border border) {
        this(border, "");
    }

    /**
     * Instantiates a new title panel.
     *
     * @param text Initial text
     */
    public TitlePanel(final String text) {
        this(BorderFactory.createLineBorder(Color.BLACK), text);
    }

    /**
     * Instantiates a new title panel.
     *
     * @param border Border to use for the panel
     * @param text Initial text
     */
    public TitlePanel(final Border border, final String text) {
        super(new MigLayout());

        this.border = border;

        title = new JLabel(text);
        title.setFont(title.getFont().deriveFont((float) (title.getFont().
                getSize() * 1.5)));

        add(title, "grow, push");
        setBorder(border);
        setBackground(Color.WHITE);
    }

    /**
     * Sets the text of this title panel.
     *
     * @param text New title text.
     */
    public void setText(final String text) {
        title.setText(text);
    }
}
