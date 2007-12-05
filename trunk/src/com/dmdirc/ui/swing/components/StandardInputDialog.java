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

package com.dmdirc.ui.swing.components;

import com.dmdirc.ui.swing.JWrappingLabel;
import com.dmdirc.ui.swing.components.validating.ValidatingJTextField;
import com.dmdirc.ui.swing.components.validating.Validator;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.miginfocom.swing.MigLayout;

/**
 * Standard input dialog.
 */
public abstract class StandardInputDialog extends StandardDialog {

    /** Validator. */
    private Validator<String> validator;
    /** Text field. */
    private ValidatingJTextField textField;
    /** Blurb label. */
    private JWrappingLabel blurb;
    /** Message. */
    private String message;

    /**
     * Instantiates a new standard input dialog.
     * 
     * @param owner Dialog owner
     * @param modal modal?
     * @param title Dialog title
     * @param message Dialog message
     */
    public StandardInputDialog(Frame owner, boolean modal, final String title,
            final String message) {
        this(owner, modal, title, message, new Validator<String>() {

            /** {@inheritDoc} */
            @Override
            public boolean validate(final String object) {
                return true;
            }

            /** {@inheritDoc} */
            @Override
            public String getFailureReason() {
                return "";
            }
        });
    }

    /**
     * Instantiates a new standard input dialog.
     * 
     * @param owner Dialog owner
     * @param modal modal?
     * @param validator Textfield validator
     * @param title Dialog title
     * @param message Dialog message
     */
    public StandardInputDialog(Frame owner, boolean modal, final String title,
            final String message, final Validator<String> validator) {
        super(owner, modal);

        this.validator = validator;
        this.message = message;

        setTitle(title);

        initComponents();
        addListeners();
        layoutComponents();
    }

    /**
     * Called when the dialog's OK button is clicked.
     * 
     * @return whether the dialog can close
     */
    public abstract boolean save();

    /**
     * Called when the dialog's cancel button is clicked, or otherwise closed.
     */
    public abstract void cancelled();

    /**
     * Initialises the components.
     */
    private final void initComponents() {
        orderButtons(new JButton(), new JButton());
        textField = new ValidatingJTextField(validator);
        blurb = new JWrappingLabel(message);
        validateText();
    }

    /**
     * Adds the listeners
     */
    private final void addListeners() {
        getOkButton().addActionListener(new ActionListener() {

            /** {@inheritDoc} */
            @Override
            public void actionPerformed(ActionEvent e) {
                if (save()) {
                    dispose();
                }
            }
        });
        getCancelButton().addActionListener(new ActionListener() {

            /** {@inheritDoc} */
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelled();
                dispose();
            }
        });
        addWindowListener(new WindowAdapter() {

            /** {@inheritDoc} */
            @Override
            public void windowClosed(WindowEvent e) {
                cancelled();
                dispose();
            }
        });
        textField.getDocument().addDocumentListener(new DocumentListener() {

            /** {@inheritDoc} */
            @Override
            public void insertUpdate(DocumentEvent e) {
                validateText();
            }

            /** {@inheritDoc} */
            @Override
            public void removeUpdate(DocumentEvent e) {
                validateText();
            }

            /** {@inheritDoc} */
            @Override
            public void changedUpdate(DocumentEvent e) {
            //Ignore
            }
        });
    }

    /**
     * Validates the change.
     */
    private void validateText() {
        getOkButton().setEnabled(validator.validate(getText()));
    }

    /**
     * Lays out the components.
     */
    private final void layoutComponents() {
        setLayout(new MigLayout("fill, wrap 1"));

        blurb.setMaximumSize(new Dimension(400, 0));

        add(blurb, "growx");
        add(textField, "growx");
        add(getLeftButton(), "split 2, right");
        add(getRightButton(), "right");
    }

    /**
     * Displays the input dialog.
     */
    public final void display() {
        pack();
        setLocationRelativeTo(getParent());
        setVisible(true);
    }

    /**
     * Returns the text in the input field.
     * 
     * @return Input text
     */
    public final String getText() {
        return textField.getText();
    }
    
    /**
     * Sets the dialogs text to the specified text.
     * 
     * @param text New test
     */
    public final void setText(final String text) {
        textField.setText(text);
    }
}
