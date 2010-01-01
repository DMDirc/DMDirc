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

package com.dmdirc.addons.ui_swing.dialogs.actioneditor;

import com.dmdirc.actions.ActionCondition;
import com.dmdirc.actions.interfaces.ActionType;
import com.dmdirc.ui.IconManager;
import com.dmdirc.addons.ui_swing.components.ImageButton;
import com.dmdirc.addons.ui_swing.components.ImageToggleButton;
import com.dmdirc.addons.ui_swing.components.text.TextLabel;
import com.dmdirc.util.ListenerList;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import net.miginfocom.swing.MigLayout;

/**
 * Action condition display panel.
 */
public class ActionConditionDisplayPanel extends JPanel implements ActionListener,
        PropertyChangeListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Info label. */
    private TextLabel label;
    /** Edit button. */
    private JToggleButton editButton;
    /** Delete button. */
    private ImageButton deleteButton;
    /** Edit panel. */
    private ActionConditionEditorPanel editPanel;
    /** Listeners. */
    private ListenerList listeners;
    /** Action condition. */
    private ActionCondition condition;
    /** Action trigger. */
    private ActionType trigger;

    /** 
     * Instantiates the panel.
     * 
     * @param condition Action condition
     * @param trigger Action trigger
     */
    public ActionConditionDisplayPanel(final ActionCondition condition,
            final ActionType trigger) {
        super();

        this.trigger = trigger;
        this.condition = new ActionCondition(condition.getArg(),
                condition.getComponent(), condition.getComparison(),
                condition.getTarget());

        initComponents();
        addListeners();
        layoutComponents();
        validate();
        layoutComponents();

        if (condition.getArg() == -1 && condition.getComponent() == null &&
                condition.getComparison() == null && condition.getTarget().isEmpty()) {
            editPanel.setVisible(true);
            editButton.setSelected(true);
        }
    }

    /**
     * Sets the action trigger.
     * 
     * @param trigger new trigger
     */
    void setTrigger(final ActionType trigger) {
        this.trigger = trigger;
        editPanel.setTrigger(trigger);

        editPanel.setVisible(trigger == null);
        editButton.setSelected(trigger == null);

        label.setText(updateSentence());
    }

    /** Initialises the components. */
    private void initComponents() {
        label = new TextLabel("", false);
        editButton = new ImageToggleButton("edit", IconManager.getIconManager().
                getIcon("edit-inactive"),
                IconManager.getIconManager().getIcon("edit"));
        deleteButton = new ImageButton("delete", IconManager.getIconManager().
                getIcon("close-inactive"), IconManager.getIconManager().
                getIcon("close-inactive"),
                IconManager.getIconManager().getIcon("close-active"));

        editPanel = new ActionConditionEditorPanel(condition, trigger);
        listeners = new ListenerList();

        editPanel.setVisible(trigger == null);
        editButton.setSelected(trigger == null);
    }

    /** Adds the listeners. */
    private void addListeners() {
        editButton.addActionListener(this);
        deleteButton.addActionListener(this);
        editPanel.addPropertyChangeListener("edit", this);
        editPanel.addPropertyChangeListener("validationResult", this);
    }

    /** Lays out the components. */
    private void layoutComponents() {
        setLayout(new MigLayout("ins 0, fillx, hidemode 3, pack, wmax 90%"));
        add(label, "grow, push, wmax 85%");
        add(editButton, "right");
        add(deleteButton, "right, wrap");
        add(editPanel, "alignx right");
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource().equals(deleteButton)) {
            fireConditionRemoved(this);
        } else if (e.getSource().equals(editButton)) {
            editPanel.setVisible(editButton.getModel().isSelected());
        }
    }

    /**
     * Adds an ActionConditionRemovalListener to the listener list.
     *
     * @param listener Listener to add
     */
    public void addConditionListener(final ActionConditionRemovalListener listener) {
        if (listener == null) {
            return;
        }

        listeners.add(ActionConditionRemovalListener.class, listener);
    }

    /**
     * Removes an ActionConditionRemovalListener from the listener list.
     *
     * @param listener Listener to remove
     */
    public void removeConditionListener(final ActionConditionRemovalListener listener) {
        listeners.remove(ActionConditionRemovalListener.class, listener);
    }

    /**
     * Fired when the an action condition is removed.
     *
     * @param condition Removed condition
     */
    protected void fireConditionRemoved(final ActionConditionDisplayPanel condition) {
        for (ActionConditionRemovalListener listener : listeners.get(ActionConditionRemovalListener.class)) {
            listener.conditionRemoved(condition);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setEnabled(final boolean enabled) {
        editPanel.setEnabled(enabled);
        editButton.setEnabled(enabled);
        deleteButton.setEnabled(enabled);
    }

    /**
     * Updates the condition sentence.
     * 
     * @return Updated sentence
     */
    private String updateSentence() {
        if (trigger == null) {
            return "...";
        } else {
            final StringBuilder sb = new StringBuilder();
            sb.append("The ");
            if (condition.getArg() != -1) {
                sb.append(trigger.getType().getArgNames()[condition.getArg()]);
            } else {
                sb.append(" ...");
                return sb.toString();
            }
            sb.append("'s ");
            if (condition.getComponent() != null) {
                sb.append(condition.getComponent().getName());
            } else {
                sb.append(" ...");
                return sb.toString();
            }
            sb.append(" ");
            if (condition.getComparison() != null) {
                sb.append(condition.getComparison().getName());
            } else {
                sb.append(" ...");
                return sb.toString();
            }
            sb.append(" '");
            if (condition.getTarget() != null) {
                sb.append(condition.getTarget().replaceAll("<", "&lt;"));
            } else {
                sb.append(" ...");
                return sb.toString();
            }
            sb.append("'");
            return sb.toString();
        }
    }

    /**
     * Returns the action condition represented by this panel.
     * 
     * @return Action condition
     */
    public ActionCondition getCondition() {
        return condition;
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        if ("edit".equals(evt.getPropertyName())) {
            label.setText(updateSentence());
        } else {
            firePropertyChange("validationResult", evt.getOldValue(),
                    evt.getNewValue());
        }
    }
    
    /**
     * Checks if this editor panel has errored.
     * 
     * @return true iif the content it valid
     */
    public boolean checkError() {
        return editPanel.checkError();
    }
}
