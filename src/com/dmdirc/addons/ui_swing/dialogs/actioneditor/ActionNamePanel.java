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

package com.dmdirc.addons.ui_swing.dialogs.actioneditor;

import com.dmdirc.config.prefs.validator.FileNameValidator;
import com.dmdirc.addons.ui_swing.components.validating.ValidatingJTextField;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import javax.swing.UIManager;
import net.miginfocom.swing.MigLayout;

/**
 * Action name panel.
 */
public class ActionNamePanel extends JPanel implements PropertyChangeListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Original name. */
    private final String originalName;
    /** Action name field. */
    private ValidatingJTextField name;

    /** Instantiates the panel. */
    public ActionNamePanel() {
        this("");
    }

    /** 
     * Instantiates the panel.
     * 
     * @param name Initial name of the action
     */
    public ActionNamePanel(final String name) {
        super();

        if (name == null) {
            this.originalName = "";
        } else {
            this.originalName = name;
        }

        initComponents();
        addListeners();
        layoutComponents();
        this.name.checkError();
    }

    /**
     * Sets the action name.
     * 
     * @param name new name
     */
    void setActionName(final String name) {
        this.name.setText(name);
    }

    /** Validates the name. */
    public void validateName() {
        name.checkError();
    }

    /** Initialises the components. */
    private void initComponents() {
        name = new ValidatingJTextField(new JTextField(originalName),
                new FileNameValidator());
    }

    /** Adds the listeners. */
    private void addListeners() {
        name.addPropertyChangeListener("validationResult", this);
    }

    /** Lays out the components. */
    private void layoutComponents() {
        setLayout(new MigLayout("wrap 1"));

        setBorder(BorderFactory.createTitledBorder(UIManager.getBorder(
                "TitledBorder.border"), "Name"));

        add(new JLabel("This action's name:"));
        add(name, "growx, pushx");
    }

    /**
     * Has the action's name changed.
     * 
     * @return true if the action name has changed.

     */
    public boolean hasNameChanged() {
        return getActionName().equals(originalName);
    }

    /**
     * Returns the name represented by this component.
     * 
     * @return Current name of this action
     */
    public String getActionName() {
        return name.getText();
    }

    /** {@inheritDoc} */
    @Override
    public void setEnabled(final boolean enabled) {
        name.setEnabled(enabled);
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        firePropertyChange("validationResult", evt.getOldValue(), evt.
                getNewValue());
    }
}
