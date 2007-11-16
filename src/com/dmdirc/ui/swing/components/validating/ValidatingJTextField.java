/*
 * To change this template, choose Tools | Templates
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

package com.dmdirc.ui.swing.components.validating;

import com.dmdirc.IconManager;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.miginfocom.swing.MigLayout;

/**
 * Validating Text field.
 */
public abstract class ValidatingJTextField extends JComponent implements DocumentListener {

    /** TextField. */
    private final JTextField textField;
    /** Error icon. */
    private final JLabel errorIcon;

    /**
     * Instantiates a new Validating text field.
     */
    public ValidatingJTextField() {
        textField = new JTextField();
        errorIcon =
                new JLabel(IconManager.getIconManager().getIcon("input-error"));

        if (!"javax.swing.plaf.synth.SynthLookAndFeel".equals(UIManager.get("TextFieldUI"))) {
            setBorder(textField.getBorder());
            textField.setBorder(BorderFactory.createEmptyBorder());
            setBackground(textField.getBackground());
        }

        setLayout(new MigLayout("fill, ins 0, hidemode 3, gap 0"));
        add(textField, "grow, pushx");
        add(errorIcon);

        errorIcon.setVisible(false);

        textField.getDocument().addDocumentListener(this);
    }

    /**
     * Validates the text in the textfield.
     * 
     * @return true iif the text is correct
     */
    public abstract boolean validateText();

    /**
     * Checks the text for errors and sets the error state accordingly.
     */
    private void checkError() {
        if (isEnabled()) {
            errorIcon.setVisible(!validateText());
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

    /**
     * @see javax.swing.JTextField#setText(String t)
     * 
     * @param t Text to set
     */
    public void setText(final String t) {
        textField.setText(t);
    }

    /**
     * @see javax.swing.JTextField#getText()
     * 
     * @return Textfield text
     */
    public String getText() {
        return textField.getText();
    }
}
