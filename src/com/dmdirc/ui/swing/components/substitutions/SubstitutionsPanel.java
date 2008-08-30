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

package com.dmdirc.ui.swing.components.substitutions;

import com.dmdirc.ui.swing.components.TextLabel;

import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import javax.swing.JSeparator;

import net.miginfocom.swing.MigLayout;

/**
 * Generic substitutions panel
 * 
 * @param T Type of substitution
 */
public abstract class SubstitutionsPanel<T> extends JPanel {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Substitions list. */
    protected List<SubstitutionLabel> substitutions;
    /** Description. */
    private String description;

    /** 
     * Instantiates the panel.
     * 
     * @param description Description
     */
    public SubstitutionsPanel(final String description) {
        this(description, null);
    }
    
    /** 
     * Instantiates the panel.
     * 
     * @param description Description
     * @param type Action type
     */
    public SubstitutionsPanel(final String description, final T type) {
        super();
        
        this.description = description;

        initComponents();
        addListeners();
        setType(type);
    }

    /** Initialises the components. */
    private void initComponents() {
        setBorder(BorderFactory.createTitledBorder(getBorder(), "Substitutions"));
        setLayout(new MigLayout("fillx, wrap 4"));
    }

    /** Adds the listeners. */
    private void addListeners() {
    }

    /** Lays out the components. */
    protected void layoutComponents() {
        removeAll();

        add(new TextLabel(description + ". Drag and drop, or click on an item when " +
                "editing the field, to insert it."), "spany, aligny top, wmin 225, wmax 225");
        add(new JSeparator(JSeparator.VERTICAL), "growy, spany");

        for (SubstitutionLabel label : substitutions) {
            add(label, "sgx subslabel, aligny top, growx");
        }
        
        if (getComponentCount() == 2) {
            add(new JLabel("No substitutions."), "growx, aligny top, align center");
        }
    }

    /**
     * Sets the type for this substitution panel.
     * 
     * @param type New action type
     */
    public abstract void setType(final T type);
}
