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

package com.dmdirc.addons.ui_swing.components.validating;

import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.config.prefs.validator.Validator;
import com.dmdirc.ui.IconManager;
import com.dmdirc.config.prefs.validator.ValidationResponse;
import java.awt.Color;

import java.awt.Font;
import java.awt.event.KeyListener;

import java.awt.event.MouseListener;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.TransferHandler;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import net.miginfocom.swing.MigLayout;

/**
 * Validating Text field.
 */
public class ValidatingJTextField extends JComponent implements DocumentListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** TextField. */
    private final JTextField textField;
    /** Validator. */
    private Validator<String> validator;
    /** Error icon. */
    private final JLabel errorIcon;

    /**
     * Instantiates a new Validating text field.
     * 
     * @param validator Validator instance
     */
    public ValidatingJTextField(final Validator<String> validator) {
        this(new JTextField(), validator);
    }

    /**
     * Instantiates a new Validating text field.
     * 
     * @param textField JTextField to wrap
     * @param validator Validator instance
     */
    public ValidatingJTextField(final JTextField textField,
            final Validator<String> validator) {
        super();
        this.textField = textField;
        this.validator = validator;
        errorIcon =
                new JLabel(IconManager.getIconManager().getIcon("input-error"));

        setLayout(new MigLayout("fill, ins 0, hidemode 3, gap 0"));
        add(textField, "grow, pushx");
        add(errorIcon);

        checkError();

        textField.getDocument().addDocumentListener(this);
    }

    /**
     * Checks the text for errors and sets the error state accordingly.
     */
    public void checkError() {
        if (textField.isEnabled()) {
            final ValidationResponse vr =
                    validator.validate(textField.getText());
            errorIcon.setToolTipText(vr.getFailureReason());
            firePropertyChange("validationResult", !errorIcon.isVisible(), !vr.isFailure());
            errorIcon.setVisible(vr.isFailure());
        } else {
            firePropertyChange("validationResult", !errorIcon.isVisible(), true);
            errorIcon.setVisible(false);
        }
    }

    /**
     * Checks if the text validates.
     * 
     * @see com.dmdirc.ui.swing.components.validating.Validator#validate(Object)
     * 
     * @return true iif the text validates
     */
    public boolean validateText() {
        if (textField.isEnabled()) {
            return !validator.validate(getText()).isFailure();
        } else {
            return true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void changedUpdate(final DocumentEvent e) {
        checkError();
    }

    /** {@inheritDoc} */
    @Override
    public void insertUpdate(final DocumentEvent e) {
        checkError();
    }

    /** {@inheritDoc} */
    @Override
    public void removeUpdate(final DocumentEvent e) {
        checkError();
    }

    /** {@inheritDoc} */
    @Override
    public void setToolTipText(final String text) {
        textField.setToolTipText(text);
    }

    /** {@inheritDoc} */
    @Override
    public void setEnabled(final boolean enabled) {
        textField.setEnabled(enabled);
        checkError();
    }
    
    /** {@inheritDoc} */
    @Override
    public void requestFocus() {
        textField.requestFocus();
    }

    /** {@inheritDoc} */
    @Override
    public boolean requestFocusInWindow() {
        return textField.requestFocusInWindow();
    }

    /**
     * Sets the text in the textfield.
     * 
     * @see javax.swing.JTextField#setText(String)
     * 
     * @param t Text to set
     */
    public void setText(final String t) {
        textField.setText(t);
    }

    /**
     * Sets the selection start.
     * 
     * @see javax.swing.JTextField#setSelectionStart(int)
     * 
     * @param selectionStart Start of the selection
     */
    public void setSelectionStart(final int selectionStart) {
        textField.setSelectionStart(selectionStart);
    }

    /**
     * Sets the selection end.
     * 
     * @see javax.swing.JTextField#setSelectionEnd(int)
     * 
     * @param selectionEnd End of the selection
     */
    public void setSelectionEnd(final int selectionEnd) {
        textField.setSelectionEnd(selectionEnd);
    }

    /**
     * Sets whether the component is editable.
     * 
     * @see javax.swing.JTextField#setEditable(boolean)
     * 
     * @param b editable state for the component
     */
    public void setEditable(final boolean b) {
        textField.setEditable(b);
    }

    /**
     * Selects all text in the textfield.
     * 
     * @see javax.swing.JTextField#selectAll()
     */
    public void selectAll() {
        textField.selectAll();
    }

    /**
     * Selects the specified text in the textfield.
     * 
     * @see javax.swing.JTextField#select(int, int)
     * 
     * @param selectionStart Selection start
     * @param selectionEnd Selection end
     */
    public void select(final int selectionStart, final int selectionEnd) {
        textField.select(selectionStart, selectionEnd);
    }

    /**
     * Replaces the textfields selection with the specified content.
     * 
     * @see javax.swing.JTextField#replaceSelection(String)
     * 
     * @param content Text to replace selection with
     */
    public void replaceSelection(final String content) {
        textField.replaceSelection(content);
    }

    /**
     * Paste's the system clipboard into the textfield.
     * 
     * @see javax.swing.JTextField#paste()
     */
    public void paste() {
        textField.paste();
    }

    /**
     * Checks if the textfield is editable.
     * 
     * @see javax.swing.JTextField#isEditable()
     * 
     * @return true iif the textfield is editable
     */
    public boolean isEditable() {
        return textField.isEditable();
    }

    /**
     * Returns the text in the textfield.
     * 
     * @see javax.swing.JTextField#getText()
     * 
     * @return Textfield content
     */
    public String getText() {
        return textField.getText();
    }

    /**
     * Returns the specified section of text in the textfield.
     * 
     * @see javax.swing.JTextField#getText(int, int)
     * 
     * @param offs Start offset
     * @param len section length
     * 
     * @return Specified textfield content
     * 
     * @throws javax.swing.text.BadLocationException
     */
    public String getText(final int offs, final int len) throws BadLocationException {
        return textField.getText(offs, len);
    }

    /**
     * Returns the start of the selection in the textfield.
     * 
     * @see javax.swing.JTextField#getSelectionStart()
     * 
     * @return Selection start
     */
    public int getSelectionStart() {
        return textField.getSelectionStart();
    }

    /**
     * Returns the end of the textfield selection.
     * 
     * @see javax.swing.JTextField#getSelectionEnd()
     * 
     * @return Selection end
     */
    public int getSelectionEnd() {
        return textField.getSelectionEnd();
    }

    /**
     * Returns the selected text in the textfield.
     * 
     * @see javax.swing.JTextField#getSelectedText()
     * 
     * @return Selected text
     */
    public String getSelectedText() {
        return textField.getSelectedText();
    }

    /**
     * Returns the textfield's document.
     * 
     * @see javax.swing.JTextField#getDocument()
     * 
     * @return Textfield's document
     */
    public Document getDocument() {
        return textField.getDocument();
    }

    /**
     * Cuts the selected text from the textfield into the clipboard.
     * 
     * @see javax.swing.JTextField#cut()
     */
    public void cut() {
        textField.cut();
    }

    /**
     * Copies the selected text from the textfield into the clipboard.
     * 
     * @see javax.swing.JTextField#copy()
     */
    public void copy() {
        textField.copy();
    }

    /**
     * Returns the font for the textfield.
     * 
     * @see javax.swing.JTextField#copy()
     */
    @Override
    public Font getFont() {
        return textField.getFont();
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void addKeyListener(final KeyListener l) {
        textField.addKeyListener(l);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void removeKeyListener(final KeyListener l) {
        textField.removeKeyListener(l);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void addMouseListener(final MouseListener l) {
        textField.addMouseListener(l);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void removeMouseListener(final MouseListener l) {
        textField.removeMouseListener(l);
    }
    
    /**
     * Sets the drag enabled property on the textfield.
     * 
     * @param enabled Enabled?
     */
    public void setDragEnabled(final boolean enabled) {
        textField.setDragEnabled(enabled);
    }
    
    /** {@inheritDoc} */
    @Override
    public void setTransferHandler(final TransferHandler newHandler) {
        textField.setTransferHandler(newHandler);
    }

    /**
     * Returns the validator used by this text field.
     * 
     * @since 0.6.3m1
     * 
     * @return This field's validator
     */
    public Validator<String> getValidator() {
        return validator;
    }

    /**
     * Returns the text field used by this validating text field.
     *
     * @since 0.6.3m1
     *
     * @return This field's text field
     */
    public JTextField getTextField() {
        return textField;
    }

    /**
     * Sets the caret colour to the specified coloour.
     *
     * @param optionColour Colour for the caret
     */
    public void setCaretColor(final Color optionColour) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                textField.setCaretColor(optionColour);
            }
        });
    }

    /**
     * Sets the foreground colour to the specified coloour.
     *
     * @param optionColour Colour for the foreground
     */
    @Override
    public void setForeground(final Color optionColour) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                textField.setForeground(optionColour);
            }
        });
    }

    /**
     * Sets the disabled text colour to the specified coloour.
     *
     * @param optionColour Colour for the disabled text
     */
    public void setDisabledTextColour(final Color optionColour) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                textField.setDisabledTextColor(optionColour);
            }
        });
    }

    /**
     * Sets the background colour to the specified coloour.
     *
     * @param optionColour Colour for the caret
     */
    @Override
    public void setBackground(final Color optionColour) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                textField.setBackground(optionColour);
            }
        });
    }
}
