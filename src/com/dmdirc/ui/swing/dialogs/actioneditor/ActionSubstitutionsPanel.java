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

import com.dmdirc.actions.ActionSubstitutor;
import com.dmdirc.actions.interfaces.ActionType;
import com.dmdirc.ui.swing.components.TextLabel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

/**
 * Action substitutions panel
 */
public class ActionSubstitutionsPanel extends JPanel {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Action type. */
    private ActionType type;
    /** Substitions list. */
    private List<ActionSubstitutionLabel> substitutions;

    /** Instantiates the panel. */
    public ActionSubstitutionsPanel() {
        this(null);
    }
    
    /** 
     * Instantiates the panel.
     * 
     * @param type Action type
     */
    public ActionSubstitutionsPanel(final ActionType type) {
        super();

        initComponents();
        addListeners();
        setActionType(type);
    }

    /** Initialises the components. */
    private void initComponents() {
        setBorder(BorderFactory.createTitledBorder(getBorder(), "Substitutions"));
        setLayout(new MigLayout("fill, wrap 5"));
    }

    /** Adds the listeners. */
    private void addListeners() {
    }

    /** Lays out the components. */
    private void layoutComponents() {
        removeAll();

        add(new TextLabel("Substitutions may be used in the response and " +
                "target fields. Drag and drop, or click on an item when " +
                "editing the field, to insert it."), "spany, aligny top, wmin 225, wmax 225");
        add(new JSeparator(JSeparator.VERTICAL), "growy");

        for (ActionSubstitutionLabel label : substitutions) {
            add(label, "sgx subslabel, aligny top");
        }
        
        if (getComponentCount() == 2) {
            add(new JLabel("No substitutions."), "growx, aligny top");
        }
    }

    /**
     * Sets the action type for this substitution panel.
     * 
     * @param type New action type
     */
    public void setActionType(final ActionType type) {
        SwingUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                ActionSubstitutionsPanel.this.type = type;

                substitutions = new ArrayList<ActionSubstitutionLabel>();

                if (type != null) {
                    final ActionSubstitutor sub = new ActionSubstitutor(type);

                    for (final Entry<String, String> entry : sub.getComponentSubstitutions().
                            entrySet()) {
                        substitutions.add(new ActionSubstitutionLabel(new ActionSubstitution(entry.getValue(),
                                entry.getKey())));
                    }

                    for (final String entry : sub.getConfigSubstitutions()) {
                        substitutions.add(new ActionSubstitutionLabel(new ActionSubstitution(entry,
                                entry)));
                    }

                    for (final Entry<String, String> entry : sub.getServerSubstitutions().
                            entrySet()) {
                        substitutions.add(new ActionSubstitutionLabel(new ActionSubstitution(entry.getValue(),
                                entry.getKey())));
                    }
                }

                layoutComponents();
            }
        });
    }
}
