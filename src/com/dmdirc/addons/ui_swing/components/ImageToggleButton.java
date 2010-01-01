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

package com.dmdirc.addons.ui_swing.components;

import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JToggleButton;

/**
 * Image toggle button.
 */
public class ImageToggleButton extends JToggleButton {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    
    /**
     * Creates a new instance of ImageToggleButton.
     *
     * @param actionCommand Action command for the button
     * @param icon Normal icon for the button
     */
    public ImageToggleButton(final String actionCommand, final Icon icon) {
        this(actionCommand, icon, icon);
    }
    
    /**
     * Creates a new instance of ImageToggleButton.
     *
     * @param actionCommand Action command for the button
     * @param icon Normal icon for the button
     * @param rolloverIcon Rollover icon for the button
     */
    public ImageToggleButton(final String actionCommand, final Icon icon, 
            final Icon rolloverIcon) {
        this(actionCommand, icon, rolloverIcon, rolloverIcon);
    }
    
    /**
     * Creates a new instance of ImageToggleButton.
     *
     * @param actionCommand Action command for the button
     * @param icon Normal icon for the button
     * @param rolloverIcon Rollover icon for the button
     * @param pressedIcon Pressed icon for the button
     */
    public ImageToggleButton(final String actionCommand, final Icon icon, 
            final Icon rolloverIcon, final Icon pressedIcon) {
        super();
        
        setIcon(icon);
        setRolloverIcon(rolloverIcon);
        setPressedIcon(pressedIcon);
        setSelectedIcon(pressedIcon);
        setContentAreaFilled(false);
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        setMargin(new Insets(0, 0, 0, 0));
        setPreferredSize(new Dimension(16, 0));
        setActionCommand(actionCommand);
    }
    
    /**
     * Sets all the image buttons icons
     * 
     * @param icon New icon
     */
    public void setIcons(final Icon icon) {
        setIcon(icon);
        setRolloverIcon(icon);
        setPressedIcon(icon);
    }
}
