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

package com.dmdirc.installer.ui;

import com.dmdirc.installer.ui.EtchedLineBorder.BorderSide;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

/**
 * Simple title panel for a wizard.
 */
public class TitlePanel extends JPanel {

    private static final long serialVersionUID = 7173184984913948951L;
    private final JLabel title;

    /**
     * Instantiates a new title panel.
     * 
     * @param titleText Initial title text
     */
    public TitlePanel(final String titleText) {
        super(new BorderLayout());
        title = new JLabel(titleText);

        title.setFont(title.getFont().deriveFont((float) (title.getFont().
                getSize() * 2)));
        add(title, BorderLayout.CENTER);
        setBackground(Color.WHITE);
        setBorder(new EtchedLineBorder(EtchedBorder.RAISED, BorderSide.BOTTOM));
    }

    /**
     * Sets the title text.
     *
     * @param titleText new title text
     */
    public void setText(final String titleText) {
        title.setText(titleText);
    }
}
