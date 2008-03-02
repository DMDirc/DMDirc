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

package com.dmdirc.ui.swing.components;

import com.dmdirc.IconManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.interfaces.InputField;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import net.miginfocom.swing.MigLayout;

/** Swing input field. */
public class SwingInputField extends JComponent implements InputField, DocumentListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Colour picker. */
    private ColourPickerDialog colourPicker;
    /** Input field text field. */
    private JTextField textField;
    /** Line wrap indicator. */
    private JLabel wrapIndicator;

    /**
     * Instantiates a new swing input field.
     */
    public SwingInputField() {
        textField = new JTextField();
        textField.setFocusTraversalKeysEnabled(false);
        wrapIndicator = new JLabel(IconManager.getIconManager().getIcon("linewrap"));
        textField.getDocument().addDocumentListener(this);
        checkLength(0);

        setLayout(new MigLayout("ins 0, hidemode 3"));

        add(textField, "growx, pushx");
        add(wrapIndicator, "");
    }

    /** {@inheritDoc} */
    @Override
    public void requestFocus() {
        textField.requestFocus();
    }

    /** {@inheritDoc} */
    @Override
    public void showColourPicker(boolean irc, boolean hex) {
        if (IdentityManager.getGlobalConfig().getOptionBool("general",
                "showcolourdialog", false)) {
            colourPicker = new ColourPickerDialog(irc, hex);
            colourPicker.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent actionEvent) {
                    try {
                        textField.getDocument().
                                insertString(textField.getCaretPosition(),
                                actionEvent.getActionCommand(), null);
                    } catch (BadLocationException ex) {
                        //Ignore, wont happen
                    }
                    colourPicker.dispose();
                    colourPicker = null;
                }
            });
            colourPicker.setLocation((int) textField.getLocationOnScreen().getX(),
                    (int) textField.getLocationOnScreen().getY() -
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

    /**
     * Returns the textfield for this inputfield.
     * 
     * @return JTextField
     */
    public JTextField getTextField() {
        return textField;
    }

    /** {@inheritDoc} */
    @Override
    public void addActionListener(ActionListener listener) {
        textField.addActionListener(listener);
    }

    /** {@inheritDoc} */
    @Override
    public void addKeyListener(KeyListener listener) {
        textField.addKeyListener(listener);
    }

    /** {@inheritDoc} */
    @Override
    public void removeActionListener(ActionListener listener) {
        textField.removeActionListener(listener);
    }

    /** {@inheritDoc} */
    @Override
    public void removeKeyListener(KeyListener listener) {
        textField.removeKeyListener(listener);
    }

    /** {@inheritDoc} */
    @Override
    public String getSelectedText() {
        return textField.getSelectedText();
    }

    /** {@inheritDoc} */
    @Override
    public int getSelectionEnd() {
        return textField.getSelectionEnd();
    }

    /** {@inheritDoc} */
    @Override
    public int getSelectionStart() {
        return textField.getSelectionStart();
    }

    /** {@inheritDoc} */
    @Override
    public String getText() {
        return textField.getText();
    }

    /** {@inheritDoc} */
    @Override
    public void setText(String text) {
        textField.setText(text);
    }

    /** {@inheritDoc} */
    @Override
    public int getCaretPosition() {
        return textField.getCaretPosition();
    }

    /** {@inheritDoc} */
    @Override
    public void setCaretPosition(int position) {
        textField.setCaretPosition(position);
    }

    /**
     * Replaces the selection with the specified text.
     * 
     * @param clipboard Text to replace selection with
     */
    public void replaceSelection(String clipboard) {
        textField.replaceSelection(clipboard);
    }

    /**
     * Sets the caret colour to the specified coloour.
     * 
     * @param optionColour Colour for the caret
     */
    public void setCaretColor(Color optionColour) {
        textField.setCaretColor(optionColour);
    }

    /**
     * Sets the foreground colour to the specified coloour.
     * 
     * @param optionColour Colour for the caret
     */
    @Override
    public void setForeground(Color optionColour) {
        textField.setForeground(optionColour);
    }

    /**
     * Sets the background colour to the specified coloour.
     * 
     * @param optionColour Colour for the caret
     */
    @Override
    public void setBackground(Color optionColour) {
        textField.setBackground(optionColour);
    }

    /** {@inheritDoc} */
    @Override
    public void insertUpdate(final DocumentEvent e) {
        checkLength(e.getDocument().getLength());
    }

    /** {@inheritDoc} */
    @Override
    public void removeUpdate(final DocumentEvent e) {
        checkLength(e.getDocument().getLength());
    }

    /** {@inheritDoc} */
    @Override
    public void changedUpdate(final DocumentEvent e) {
        //Ignore
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasFocus() {
        return textField.isFocusOwner();
    }
    
    /**
     * Checks the length of the input and shows wrap indicator if required.
     * 
     * @param newLength New length of input
     */
    private void checkLength(final int newLength) {
        wrapIndicator.setVisible(false);
    }
}
