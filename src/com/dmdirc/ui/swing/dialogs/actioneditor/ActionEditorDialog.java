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

package com.dmdirc.ui.swing.dialogs.actioneditor;

import com.dmdirc.actions.Action;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.swing.UIUtilities;
import com.dmdirc.ui.swing.components.StandardDialog;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JFrame;

import javax.swing.JOptionPane;
import net.miginfocom.swing.MigLayout;

/**
 * Action editor dialog.
 */
public class ActionEditorDialog extends StandardDialog implements ActionListener,
        PropertyChangeListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Name panel. */
    private ActionNamePanel name;
    /** Triggers panel. */
    private ActionTriggersPanel triggers;
    /** Response panel. */
    private ActionResponsePanel response;
    /** Conditions panel. */
    private ActionConditionsPanel conditions;
    /** Substitutions panel. */
    private ActionSubstitutionsPanel substitutions;
    /** Show substitutions button. */
    private JButton showSubstitutions;
    /** Is the name valid? */
    private boolean nameValid = false;
    /** Are the triggers valid? */
    private boolean triggersValid = false;
    /** Is the response valid? */
    private boolean responseValid = false;
    /** Are the conditions valid? */
    private boolean conditionsValid = false;
    /** Action to be edited. */
    private Action action;

    /** 
     * Instantiates the panel.
     * 
     * @param window Parent window
     */
    public ActionEditorDialog(final Window window) {
        this(window, null);
    }

    /** 
     * Instantiates the panel.
     * 
     * @param window Parent window
     * @param action Action to be edited
     */
    public ActionEditorDialog(final Window window, final Action action) {
        super(window, ModalityType.MODELESS);

        this.action = action;

        initComponents();
        addListeners();
        layoutComponents();
    }

    /** Initialises the components. */
    private void initComponents() {
        orderButtons(new JButton(), new JButton());
        name = new ActionNamePanel("");
        triggers = new ActionTriggersPanel();
        response = new ActionResponsePanel();
        conditions = new ActionConditionsPanel();
        substitutions = new ActionSubstitutionsPanel();
        showSubstitutions = new JButton("Show Substitutions");

        triggers.setEnabled(false);
        response.setEnabled(false);
        conditions.setEnabled(false);
        substitutions.setVisible(false);
    }

    /** Adds the listeners. */
    private void addListeners() {
        showSubstitutions.addActionListener(this);
        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);

        name.addPropertyChangeListener("validationResult", this);
        triggers.addPropertyChangeListener("validationResult", this);
    }

    /** Lays out the components. */
    private void layoutComponents() {
        setLayout(new MigLayout("fill, hidemode 3, wrap 2, pack, hmax 80sp"));

        add(name, "grow, wmax 250");
        add(conditions, "spany 3, grow, wmin 410, wmax 410");
        add(triggers, "grow, wmax 250");
        add(response, "grow, wmax 250");
        add(substitutions, "spanx 2, grow, wmax 660");
        add(showSubstitutions, "left, sgx button, split 3, spanx 2");
        add(getLeftButton(), "right, sgx button, gapleft push");
        add(getRightButton(), "right, sgx button");
    }

    /**
     * Test Method.
     * 
     * @param args CLI params
     */
    public static void main(final String[] args) {
        UIUtilities.initUISettings();
        IdentityManager.load();
        ActionManager.init();
        ActionManager.loadActions();
        ActionEditorDialog dialog;
        if (JOptionPane.showOptionDialog(new JFrame(),
                "Do you want to use test editing an action?",
                "Edit or create?",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[]{"Create", "Edit",},
                "Create") == 0) {
            dialog = new ActionEditorDialog(new JFrame());
        } else {
            dialog = new ActionEditorDialog(new JFrame(), ActionManager.getGroups().
                    values().iterator().
                    next().getActions().get(0));
        }

        dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        dialog.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosed(WindowEvent e) {
                System.exit(0);
            }
        });
        dialog.setVisible(true);
    }

    /** 
     * @{inheritDoc}
     * 
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource().equals(showSubstitutions)) {
            substitutions.setVisible(!substitutions.isVisible());
            showSubstitutions.setText(substitutions.isVisible() ? "Hide Substitutions" : "Show Substitutions");
        } else if (e.getSource().equals(getOkButton())) {
            dispose();
        } else if (e.getSource().equals(getCancelButton())) {
            dispose();
        }
    }

    /** @{inheritDoc} */
    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        if (evt.getSource().equals(name)) {
            nameValid = (Boolean) evt.getNewValue();
            triggers.setEnabled((Boolean) evt.getNewValue());
        }
        if (evt.getSource().equals(triggers)) {
            triggersValid = (Boolean) evt.getNewValue();

            response.setEnabled((Boolean) evt.getNewValue());
            conditions.setEnabled((Boolean) evt.getNewValue());
            substitutions.setEnabled((Boolean) evt.getNewValue());

            substitutions.setActionType(triggers.getPrimaryTrigger());
            conditions.setActionTrigger(triggers.getPrimaryTrigger());
        }

        getOkButton().setEnabled(triggersValid && conditionsValid && nameValid &&
                responseValid);
    }
}
