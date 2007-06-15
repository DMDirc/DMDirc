/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.ui.components;

import com.dmdirc.ui.MainFrame;

import java.awt.event.ActionListener;

import javax.swing.JDialog;

/**
 * Colour picker dialog.
 */
public final class ColourPickerDialog extends StandardDialog {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** Colour chooser panel. */
    private ColourPickerPanel colourChooser;
    
    /**
     * Creates a new instance of ColourPickerDialog.
     */
    public ColourPickerDialog() {
        this(true, true);
    }
    
    /**
     * Creates a new instance of ColourPickerDialog.
     * @param showIRC show irc colours
     * @param showHex show hex colours
     */
    public ColourPickerDialog(final boolean showIRC, final boolean showHex) {
        super(MainFrame.getMainFrame(), false);
        
        colourChooser = new ColourPickerPanel(showIRC, showHex);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.add(colourChooser);
        this.pack();
        this.setResizable(false);
        this.setFocusableWindowState(false);
    }
    
    /** 
     * Creates and shows a new Colour picker dialog. 
     * @return Colour picker dialog
     */
    public static ColourPickerDialog showColourPicker() {
        return showColourPicker(true, true);
    }
    
    /** 
     * Creates and shows a new Colour picker dialog. 
     * @return Colour picker dialog
     * @param showIRC show irc colours
     * @param showHex show hex colours
     */
    public static ColourPickerDialog showColourPicker(final boolean showIRC, 
            final boolean showHex) {
        final ColourPickerDialog cpd = new ColourPickerDialog(showIRC, showHex);
        cpd.setVisible(true);
        return cpd;
    }
    
    /** 
     * Adds an actions listener to this dialog.
     *
     * @param listener the listener to add
     */
    public void addActionListener(final ActionListener listener) {
        colourChooser.addActionListener(listener);
    }
    
}
