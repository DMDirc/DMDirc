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

import com.dmdirc.actions.interfaces.ActionType;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import net.miginfocom.swing.MigLayout;

/**
 * Action conditions panel.
 */
public class ActionConditionsPanel extends JPanel {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Action trigger. */
    private ActionType trigger;
    /** Tree panel. */
    private ActionConditionsTreePanel tree;
    /** List Panel. */
    private ActionConditionsListPanel list;

    /** Instantiates the panel. */
    public ActionConditionsPanel() {
        this(null);
    }

    /** 
     * Instantiates the panel.
     * 
     * @param trigger Action trigger
     */
    public ActionConditionsPanel(final ActionType trigger) {
        super();

        this.trigger = trigger;

        initComponents();
        addListeners();
        layoutComponents();
        
        if (trigger == null) {
            setEnabled(false);
        }
    }

    /** Initialises the components. */
    private void initComponents() {
        tree = new ActionConditionsTreePanel();
        list = new ActionConditionsListPanel();
    }

    /** Adds the listeners. */
    private void addListeners() {
    }

    /** Lays out the components. */
    private void layoutComponents() {
        setLayout(new MigLayout("fill, wrap 1"));
        
        setBorder(BorderFactory.createTitledBorder(getBorder(), "Conditions"));
        
        add(tree, "growx, pushx");
        add(new JSeparator(JSeparator.HORIZONTAL), "growx, pushx");
        add(list, "grow");
    }

    /**
     * Sets the trigger for this conditions panel.
     * 
     * @param trigger Action trigger.
     */
    public void setActionTrigger(final ActionType trigger) {
        this.trigger = trigger;
        list.setTrigger(trigger);
    }
    
    /** {@inheritDoc} */
    @Override
    public void setEnabled(final boolean enabled) {
        tree.setEnabled(enabled);
        list.setEnabled(enabled);
    }
}
