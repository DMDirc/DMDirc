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

import com.dmdirc.actions.Action;
import com.dmdirc.addons.ui_swing.dialogs.StandardDialog;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;

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
    /** Previously created instance of ActionEditorDialog. */
    private static volatile ActionEditorDialog me;
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
    /** Are the conditions valid? */
    private boolean conditionsValid = false;
    /** Action to be edited. */
    private Action action;
    /** Action group. */
    private String group;

    /** 
     * Instantiates the panel.
     * 
     * @param window Parent window
     * @param group Action's group
     */
    private ActionEditorDialog(final Window window, final String group) {
        this(window, group, null);
    }

    /** 
     * Instantiates the panel.
     * 
     * @param window Parent window
     * @param action Action to be edited
     * @param group Action's group
     */
    private ActionEditorDialog(final Window window, final String group,
            final Action action) {
        super(window, ModalityType.DOCUMENT_MODAL);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setTitle("DMDirc: Action Editor");

        this.group = group;
        this.action = action;

        initComponents();
        addListeners();
        doComponents();
        layoutComponents();
        
        setResizable(false);
    }
    
    /**
     * Is the dialog open?
     * 
     * @return is the dialog open
     */
    public static boolean isOpen() {
        synchronized (ActionEditorDialog.class) {
            return me != null;
        }
    }

    /** 
     * Creates the dialog if one doesn't exist, and displays it. 
     * 
     * @param window Parent window
     * @param group Action's group
     */
    public static void showActionEditorDialog(final Window window,
            final String group) {
        showActionEditorDialog(window, group, null);
    }

    /** 
     * Creates the dialog if one doesn't exist, and displays it. 
     * 
     * @param window Parent window
     * @param group Action's group
     * @param action Action to be edited
     */
    public static void showActionEditorDialog(final Window window,
            final String group, final Action action) {
        getActionEditorDialog(window, group, action);

        me.pack();
        me.setLocationRelativeTo(window);
        me.setVisible(true);
    }

    /**
     * Returns the current instance of the ActionEditorDialog.
     *
     * @param window Parent window
     * @param group Action's group
     * 
     * @return The current ActionEditorDialog instance
     */
    public static ActionEditorDialog getActionEditorDialog(final Window window,
            final String group) {
        return getActionEditorDialog(window, group, null);
    }

    /**
     * Returns the current instance of the ActionEditorDialog.
     * 
     * @param window Parent window
     * @param group Action's group
     * @param action Action to be edited
     *
     * @return The current ActionEditorDialog instance
     */
    public static ActionEditorDialog getActionEditorDialog(final Window window,
            final String group, final Action action) {
        synchronized (ActionEditorDialog.class) {
            if (me == null) {
                me = new ActionEditorDialog(window, group, action);
            }
        }

        return me;
    }

    /** Sets components initial states and stuff. */
    private void doComponents() {
        triggers.setEnabled(action != null);
        response.setEnabled(action != null);
        conditions.setEnabled(action != null);
        substitutions.setVisible(false);

        triggersValid = action != null;
        conditionsValid = true;
        nameValid = action != null;
        getOkButton().setEnabled(action != null);

        if (action != null) {
            name.setActionName(action.getName());
            triggers.setTriggers(action.getTriggers());
            response.setResponse(action.getResponse());
            response.setFormatter(action.getNewFormat());
            conditions.setActionTrigger(action.getTriggers()[0]);
            conditions.setConditions(action.getConditions());
            conditions.setConditionTree(action.getRealConditionTree());
        }
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
    }

    /** Adds the listeners. */
    private void addListeners() {
        showSubstitutions.addActionListener(this);
        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);

        name.addPropertyChangeListener("validationResult", this);
        triggers.addPropertyChangeListener("validationResult", this);
        conditions.addPropertyChangeListener("validationResult", this);
        
        addWindowListener(new WindowAdapter() {

            /** {@inheritDoc} */
            @Override
            public void windowClosing(final WindowEvent e) {
                getCancelButton().doClick();
            }
        });
    }

    /** Lays out the components. */
    private void layoutComponents() {
        setMinimumSize(new Dimension(800, 600));
        setLayout(new MigLayout("fill, hidemode 3, wrap 2, pack, hmax 80sp, wmin 800, wmax 800"));

        add(name, "grow, w 50%");
        add(conditions, "spany 3, grow, pushx, w 50%");
        add(triggers, "grow, w 50%");
        add(response, "grow, pushy, w 50%");
        add(substitutions, "spanx, grow, push");
        add(showSubstitutions, "left, sgx button, split 3, spanx 2");
        add(getLeftButton(), "right, sgx button, gapleft push");
        add(getRightButton(), "right, sgx button");
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
            showSubstitutions.setText(substitutions.isVisible() ? "Hide Substitutions"
                    : "Show Substitutions");
        } else if (e.getSource().equals(getOkButton())) {
            save();
            dispose();
        } else if (e.getSource().equals(getCancelButton())) {
            dispose();
        }
    }

    /** Saves the action being edited. */
    private void save() {
        name.getActionName();
        triggers.getTriggers();
        response.getResponse();
        response.getFormatter();
        conditions.getConditions();
        conditions.getConditionTree();
        if (action == null) {
            new Action(group, name.getActionName(), triggers.getTriggers(),
                    response.getResponse(), conditions.getConditions(),
                    conditions.getConditionTree(), response.getFormatter());
        } else {
            action.setName(name.getActionName());
            action.setConditionTree(conditions.getConditionTree());
            action.setConditions(conditions.getConditions());
            action.setNewFormat(response.getFormatter());
            action.setResponse(response.getResponse());
            action.setTriggers(triggers.getTriggers());
            action.save();
        }
    }

    /** @{inheritDoc} */
    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        if (evt.getSource().equals(name)) {
            nameValid = (Boolean) evt.getNewValue();
            triggers.setEnabled((Boolean) evt.getNewValue());
            conditions.setEnabled((Boolean) evt.getNewValue());
            response.setEnabled((Boolean) evt.getNewValue());
        } else if (evt.getSource().equals(triggers)) {
            triggersValid = (Boolean) evt.getNewValue();

            response.setEnabled((Boolean) evt.getNewValue());
            conditions.setEnabled((Boolean) evt.getNewValue());
            substitutions.setEnabled((Boolean) evt.getNewValue());

            substitutions.setType(triggers.getPrimaryTrigger());
            conditions.setActionTrigger(triggers.getPrimaryTrigger());
        } else if (evt.getSource().equals(conditions)) {
            conditionsValid = (Boolean) evt.getNewValue();
        }

        getOkButton().setEnabled(triggersValid && conditionsValid && nameValid);
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        if (me == null) {
            return;
        }
        synchronized (me) {
            super.dispose();
            me = null;
        }
    }
}
