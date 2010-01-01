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

package com.dmdirc.addons.ui_swing.components.colours;

import com.dmdirc.addons.ui_swing.dialogs.StandardDialog;

import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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
    /** Parent window. */
    private Window window;

    /**
     * Creates a new instance of ColourPickerDialog.
     * 
     * @since 0.6   
     */
    public ColourPickerDialog() {
        this(true, true);
    }

    /**
     * Creates a new instance of ColourPickerDialog.
     * 
     * @param window Parent window
     * 
     * @since 0.6
     */
    public ColourPickerDialog(final Window window) {
        this(true, true, window);
    }

    /**
     * Creates a new instance of ColourPickerDialog.
     * 
     * @param showIRC show irc colours
     * @param showHex show hex colours
     */
    public ColourPickerDialog(final boolean showIRC, final boolean showHex) {
        this(showIRC, showHex, null);
    }

    /**
     * Creates a new instance of ColourPickerDialog.
     * 
     * @param showIRC show irc colours
     * @param showHex show hex colours
     * @param window Parent window
     * 
     * @since 0.6
     */
    public ColourPickerDialog(final boolean showIRC, final boolean showHex,
            final Window window) {
        super(window, ModalityType.MODELESS);

        colourChooser = new ColourPickerPanel(showIRC, showHex);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        add(colourChooser);
        setResizable(false);
        setFocusableWindowState(false);

        setWindow(window);
    }

    /** 
     * Creates and shows a new Colour picker dialog. 
     * @return Colour picker dialog
     */
    public static ColourPickerDialog showColourPicker() {
        final ColourPickerDialog cpd = showColourPicker(true, true);
        return cpd;
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
        cpd.display();
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

    /**
     * Sets the Parent window.
     * 
     * @param window Parent window
     */
    public void setWindow(final Window window) {
        this.window = window;

        if (window != null) {
            window.addWindowListener(new WindowAdapter() {

                /** {@inheritDoc} */
                @Override
                public void windowClosed(WindowEvent e) {
                    dispose();
                }
            });
        }
    }

    /** {@inheritDoc} */
    @Override
    public void display() {
        super.display();
        setSize(colourChooser.getPreferredSize());
    }
}
