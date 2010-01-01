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
import com.dmdirc.addons.ui_swing.components.text.TextLabel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

/**
 * Action conditions list panel.
 */
public class ActionConditionsListPanel extends JPanel implements ActionConditionRemovalListener,
        PropertyChangeListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Action trigger. */
    private ActionType trigger;
    /** Conditions list. */
    private List<ActionConditionDisplayPanel> conditions;
    /** Condition tree panel. */
    private ActionConditionsTreePanel treePanel;
    /** Condition validation results. */
    private Map<ActionConditionDisplayPanel, Boolean> validations;
    /** validates. */
    private boolean validates = true;

    /** 
     * Instantiates the panel.
     * 
     * @param treePanel Condition tree panel. 
     */
    public ActionConditionsListPanel(final ActionConditionsTreePanel treePanel) {
        this(null, new ArrayList<ActionConditionDisplayPanel>(), treePanel);
    }

    /** 
     * Instantiates the panel.
     * 
     * @param trigger Action trigger
     * @param treePanel Condition tree panel.
     */
    public ActionConditionsListPanel(final ActionType trigger,
            final ActionConditionsTreePanel treePanel) {
        this(trigger, new ArrayList<ActionConditionDisplayPanel>(), treePanel);
    }

    /** 
     * Instantiates the panel.
     * 
     * @param trigger Action trigger
     * @param conditions List of existing conditions;
     * @param treePanel Condition tree panel.
     */
    public ActionConditionsListPanel(final ActionType trigger,
            final List<ActionConditionDisplayPanel> conditions,
            final ActionConditionsTreePanel treePanel) {
        super();

        validations = new HashMap<ActionConditionDisplayPanel, Boolean>();

        this.trigger = trigger;
        this.conditions = new ArrayList<ActionConditionDisplayPanel>(conditions);
        this.treePanel = treePanel;

        initComponents();
        addListeners();
        layoutComponents();
    }

    /** Initialises the components. */
    private void initComponents() {
        setLayout(new MigLayout("fillx, wrap 2, pack"));

        if (trigger == null) {
            setEnabled(false);
        }
    }

    /** Adds the listeners. */
    private void addListeners() {
        for (ActionConditionDisplayPanel condition : conditions) {
            condition.addConditionListener(this);
        }
    }

    /** Lays out the components. */
    private void layoutComponents() {
        setVisible(false);
        removeAll();
        int index = 0;
        if (trigger == null) {
            add(new TextLabel("You must add at least one trigger before you can add conditions."),
                    "alignx center, aligny top, grow, push, w 90%!");
        } else if (trigger.getType().getArgNames().length == 0) {
            add(new TextLabel("Trigger does not have any arguments."),
                    "alignx center, aligny top, grow, push, w 90%!");
        } else {
            synchronized (conditions) {
                for (ActionConditionDisplayPanel condition : conditions) {
                    add(new JLabel(index + "."), "aligny top");
                    add(condition, "growx, pushx, aligny top");
                    index++;
                }
            }
            if (index == 0) {
                add(new JLabel("No conditions."),
                        "alignx center, aligny top, growx, pushx");
            }
        }
        setVisible(true);

    }

    /**
     * Adds an action condition to the list.
     * 
     * @param condition Action condition
     */
    public void addCondition(final ActionCondition condition) {
        final ActionConditionDisplayPanel panel =
                new ActionConditionDisplayPanel(condition, trigger);
        panel.addConditionListener(this);
        panel.addPropertyChangeListener("validationResult", this);
        validations.put(panel, panel.checkError());
        propertyChange(null);
        synchronized (conditions) {
            conditions.add(panel);
        }
        treePanel.setConditionCount(conditions.size());
        layoutComponents();
    }

    /**
     * Deletes an action condition from the list.
     * 
     * @param condition Action condition
     */
    public void delCondition(final ActionCondition condition) {
        ActionConditionDisplayPanel removeCondition = null;

        synchronized (conditions) {
            for (ActionConditionDisplayPanel localCondition : conditions) {
                if (localCondition.getCondition().equals(condition)) {
                    removeCondition = localCondition;
                    break;
                }
            }
        }

        treePanel.setConditionCount(conditions.size());

        if (removeCondition != null) {
            conditionRemoved(removeCondition);
        }
    }

    /**
     * Clear conditions.
     */
    public void clearConditions() {
        for (ActionConditionDisplayPanel condition : conditions) {
            delCondition(condition.getCondition());
        }
    }

    /**
     * Returns the condition list.
     * 
     * @return condition list
     */
    public List<ActionCondition> getConditions() {
        final List<ActionCondition> conditionList =
                new ArrayList<ActionCondition>();

        synchronized (conditions) {
            for (ActionConditionDisplayPanel condition : conditions) {
                conditionList.add(condition.getCondition());
            }
        }

        return conditionList;
    }

    /**
     * Sets the action trigger for the panel.
     * 
     * @param trigger Action trigger
     */
    public void setTrigger(final ActionType trigger) {
        if (this.trigger == null) {
            conditions.clear();
        }

        for (ActionConditionDisplayPanel panel : conditions) {
            panel.setTrigger(trigger);
        }
        this.trigger = trigger;
        setEnabled(trigger != null);
        layoutComponents();
    }

    /** {@inheritDoc} */
    @Override
    public void conditionRemoved(final ActionConditionDisplayPanel condition) {
        synchronized (conditions) {
            conditions.remove(condition);
            validations.remove(condition);
        }
        propertyChange(null);
        layoutComponents();
    }

    /** {@inheritDoc} */
    @Override
    public void setEnabled(final boolean enabled) {
        for (ActionConditionDisplayPanel condition : conditions) {
            condition.setEnabled(enabled);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        if (evt != null) {
            validations.put((ActionConditionDisplayPanel) evt.getSource(),
                    (Boolean) evt.getNewValue());
        }

        boolean pass = true;
        for (boolean validation : validations.values()) {
            if (!validation) {
                pass = false;
                break;
            }
        }
        
        firePropertyChange("validationResult", validates, pass);
        validates = pass;
    }
}
