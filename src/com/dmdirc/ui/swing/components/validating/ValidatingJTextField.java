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
    private Validator validator;
    /** Error icon. */
    private final JLabel errorIcon;

    /**
     * Instantiates a new Validating text field.
     * 
     * @param validator Validator instance
     */
    public ValidatingJTextField(final Validator validator) {
        this(new JTextField(), validator);
    }
    
    /**
     * Instantiates a new Validating text field.
     * 
     * @param textField JTextField to wrap
     * @param validator Validator instance
     */
    public ValidatingJTextField(final JTextField textField, 
            final Validator validator) {
        this.textField = textField;
        this.validator = validator;
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
     * Checks the text for errors and sets the error state accordingly.
     */
    private void checkError() {
        if (isEnabled()) {
            errorIcon.setVisible(!validator.validate(textField.getText()));
        } else {
            errorIcon.setVisible(false);
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
}
