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

import com.dmdirc.actions.ActionCondition;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.interfaces.ActionType;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

/**
 * Action conditions list panel.
 */
public class ActionConditionsListPanel extends JPanel implements ActionConditionRemovalListener {

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

    /** Instantiates the panel. */
    public ActionConditionsListPanel() {
        this(null, new ArrayList<ActionConditionDisplayPanel>());
    }

    /** 
     * Instantiates the panel.
     * 
     * @param trigger Action trigger
     */
    public ActionConditionsListPanel(final ActionType trigger) {
        this(trigger, new ArrayList<ActionConditionDisplayPanel>());
    }

    /** 
     * Instantiates the panel.
     * 
     * @param trigger Action trigger
     * @param conditions List of existing conditions;
     */
    public ActionConditionsListPanel(final ActionType trigger,
            final List<ActionConditionDisplayPanel> conditions) {
        super();

        this.trigger = trigger;
        this.conditions = new ArrayList<ActionConditionDisplayPanel>(conditions);

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
            add(new JLabel("You must add at least one trigger before you can add conditions."),
                    "alignx center, aligny top, growx");
        } else {
            synchronized (conditions) {
                for (ActionConditionDisplayPanel condition : conditions) {
                    index++;
                    add(new JLabel(index + "."), "aligny top");
                    add(condition, "growx, aligny top");
                }
            }
            if (index == 0) {
                add(new JLabel("No conditions."),
                        "alignx center, aligny top, growx");
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
        synchronized (conditions) {
            conditions.add(panel);
        }
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

        if (removeCondition != null) {
            conditionRemoved(removeCondition);
        }
    }

    /**
     * Sets the action trigger for the panel.
     * 
     * @param trigger Action trigger
     */
    public void setTrigger(final ActionType trigger) {
        if (this.trigger != null || ActionManager.getCompatibleTypes(trigger).contains(this.trigger)) {
            conditions.clear();
        }
        this.trigger = trigger;
        layoutComponents();
    }

    /** {@inheritDoc} */
    @Override
    public void conditionRemoved(final ActionConditionDisplayPanel condition) {
        synchronized (conditions) {
            conditions.remove(condition);
        }
        layoutComponents();
    }

    /** {@inheritDoc} */
    @Override
    public void setEnabled(final boolean enabled) {
        for (ActionConditionDisplayPanel condition : conditions) {
            condition.setEnabled(false);
        }
    }
}
