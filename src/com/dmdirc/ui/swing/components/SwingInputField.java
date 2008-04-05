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

import com.dmdirc.ui.IconManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.interfaces.InputField;
import com.dmdirc.ui.interfaces.InputValidationListener;
import com.dmdirc.util.ListenerList;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.text.BadLocationException;

import net.miginfocom.swing.MigLayout;

/** Swing input field. */
public class SwingInputField extends JComponent implements InputField,
        KeyListener, InputValidationListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Colour picker. */
    private ColourPickerDialog colourPicker;
    /** Input field text field. */
    private final JTextField textField;
    /** Line wrap indicator. */
    private final JLabel wrapIndicator;
    /** Error indicator. */
    private final JLabel errorIndicator;
    /** Listener list. */
    private final ListenerList listeners;

    /**
     * Instantiates a new swing input field.
     */
    public SwingInputField() {
        super();
        
        listeners = new ListenerList();
        
        textField = new JTextField();
        textField.setFocusTraversalKeysEnabled(false);
        textField.addKeyListener(this);
        wrapIndicator =
                new JLabel(IconManager.getIconManager().getIcon("linewrap"));
        wrapIndicator.setVisible(false);
        errorIndicator =
                new JLabel(IconManager.getIconManager().getIcon("error"));
        errorIndicator.setVisible(false);

        setLayout(new MigLayout("ins 0, hidemode 3"));

        add(textField, "growx, pushx");
        add(wrapIndicator, "");
        add(errorIndicator, "");

        setActionMap(textField.getActionMap());
        setInputMap(SwingInputField.WHEN_FOCUSED,
                textField.getInputMap(SwingInputField.WHEN_FOCUSED));
        setInputMap(SwingInputField.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
                textField.getInputMap(SwingInputField.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT));
        setInputMap(SwingInputField.WHEN_IN_FOCUSED_WINDOW,
                textField.getInputMap(SwingInputField.WHEN_IN_FOCUSED_WINDOW));
    }

    /** {@inheritDoc} */
    @Override
    public void requestFocus() {
        textField.requestFocus();
    }

    /** {@inheritDoc} */
    @Override
    public void showColourPicker(final boolean irc, final boolean hex) {
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
    public void addActionListener(final ActionListener listener) {
        textField.addActionListener(listener);
    }

    /** {@inheritDoc} */
    @Override
    public void addKeyListener(final KeyListener listener) {
        listeners.add(KeyListener.class, listener);
    }

    /** {@inheritDoc} */
    @Override
    public void removeActionListener(final ActionListener listener) {
        textField.removeActionListener(listener);
    }

    /** {@inheritDoc} */
    @Override
    public void removeKeyListener(final KeyListener listener) {
        listeners.remove(KeyListener.class, listener);
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
    public void setText(final String text) {
        textField.setText(text);
    }

    /** {@inheritDoc} */
    @Override
    public int getCaretPosition() {
        return textField.getCaretPosition();
    }

    /** {@inheritDoc} */
    @Override
    public void setCaretPosition(final int position) {
        textField.setCaretPosition(position);
    }

    /**
     * Replaces the selection with the specified text.
     * 
     * @param clipboard Text to replace selection with
     */
    public void replaceSelection(final String clipboard) {
        textField.replaceSelection(clipboard);
    }

    /**
     * Sets the caret colour to the specified coloour.
     * 
     * @param optionColour Colour for the caret
     */
    public void setCaretColor(final Color optionColour) {
        textField.setCaretColor(optionColour);
    }

    /**
     * Sets the foreground colour to the specified coloour.
     * 
     * @param optionColour Colour for the caret
     */
    @Override
    public void setForeground(final Color optionColour) {
        textField.setForeground(optionColour);
    }

    /**
     * Sets the background colour to the specified coloour.
     * 
     * @param optionColour Colour for the caret
     */
    @Override
    public void setBackground(final Color optionColour) {
        textField.setBackground(optionColour);
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasFocus() {
        return textField.isFocusOwner();
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Key event
     */
    @Override
    public void keyTyped(final KeyEvent e) {
        for(KeyListener listener : listeners.get(KeyListener.class)) {
            listener.keyTyped(e);
        }
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Key event
     */
    @Override
    public void keyPressed(final KeyEvent e) {
        wrapIndicator.setVisible(false);
        errorIndicator.setVisible(false);
        for(KeyListener listener : listeners.get(KeyListener.class)) {
            listener.keyPressed(e);
        }
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Key event
     */
    @Override
    public void keyReleased(final KeyEvent e) {
        for(KeyListener listener : listeners.get(KeyListener.class)) {
            listener.keyReleased(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void illegalCommand(final String reason) {
        errorIndicator.setVisible(true);
        errorIndicator.setToolTipText(reason);
    }

    /** {@inheritDoc} */
    @Override
    public void wrappedText(final int count) {
        wrapIndicator.setVisible(true);
        wrapIndicator.setToolTipText(count + " lines");
    }
}
