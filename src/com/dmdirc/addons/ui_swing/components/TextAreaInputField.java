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

import com.dmdirc.addons.ui_swing.components.colours.ColourPickerDialog;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.interfaces.InputField;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

/**
 *
 * @author chris
 */
public class TextAreaInputField extends JTextArea implements InputField {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    
    /** Colour picker. */
    protected ColourPickerDialog colourPicker;    

    /**
     * Creates a new text area with the specified number of rows and columns.
     * 
     * @param rows The number of rows to use
     * @param columns The number of columns to use
     */
    public TextAreaInputField(final int rows, final int columns) {
        super(rows, columns);
    }

    /**
     * Creates a new text area containing the specified text.
     * 
     * @param text The text to contain initially
     */
    public TextAreaInputField(String text) {
        super(text);
    }
    
    /** {@inheritDoc} */
    @Override
    public void addActionListener(final ActionListener listener) {
        // Ignore request - we don't handle returns for text areas
    }

    /** {@inheritDoc} */
    @Override    
    public void removeActionListener(final ActionListener listener) {
        // Ignore request - we don't handle returns for text areas
    }

    /** {@inheritDoc} */
    @Override
    public void showColourPicker(final boolean irc, final boolean hex) {
        if (IdentityManager.getGlobalConfig().getOptionBool("general",
                "showcolourdialog")) {
            colourPicker = new ColourPickerDialog(irc, hex);
            colourPicker.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent actionEvent) {
                    try {
                        getDocument().insertString(getCaretPosition(), 
                                actionEvent.getActionCommand(), null);
                    } catch (BadLocationException ex) {
                        //Ignore, wont happen
                    }
                    colourPicker.dispose();
                    colourPicker = null;
                }
            });
            colourPicker.setLocation((int) getLocationOnScreen().getX(),
                    (int) getLocationOnScreen().getY() -
                    colourPicker.getHeight());
            colourPicker.setVisible(true);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void hideColourPicker() {
        if (colourPicker != null) {
            colourPicker.dispose();
            colourPicker = null;
        }
    }

}
