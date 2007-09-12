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

package com.dmdirc.ui.swing.dialogs.actionseditor;

import com.dmdirc.actions.ActionSubstitutor;
import com.dmdirc.actions.ActionType;

import java.util.Map.Entry;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;

/**
 * Lists substitutions for use in actions.
 */
public final class SubstitutionsPanel extends JPanel {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** Substitutions list. */
    private final JList list;
    
    /** Action type. */
    private ActionType type;

    /** Creates a new instance of SubstitutionsPanel. */
    public SubstitutionsPanel() {
        super();
        list = new JList(new DefaultListModel());
        list.setVisibleRowCount(5);

        setVisible(true);
    }
    
    /**
     * Creates a new instance of SubstitutionsPanel.
     *
     * @param type ActionType to substitute for
     */
    public SubstitutionsPanel(final ActionType type) {
        super();
        
        this.type = type;
        
        list = new JList(new DefaultListModel());
        populateList();
    }
    
    public void setType(final ActionType type) {
        this.type = type;
        populateList();
    }
    
    /** Populates the list with valid substitutions. */
    public void populateList() {
        //populate the list
        final ActionSubstitutor sub = new ActionSubstitutor(type);
        final DefaultListModel model = (DefaultListModel) list.getModel();
        
        list.setVisible(false);
        
        model.clear();
        
        for (final Entry<String, String> entry : sub.getComponentSubstitutions().entrySet()) {
            model.addElement(new ActionSubstitution(entry.getValue(), entry.getKey()));
        }
        
        for (final String entry : sub.getConfigSubstitutions()) {
            model.addElement(new ActionSubstitution(entry, entry));
        }
        
        for (final Entry<String, String> entry : sub.getServerSubstitutions().entrySet()) {
            model.addElement(new ActionSubstitution(entry.getValue(), entry.getKey()));
        }
        
        list.setVisible(true);
    }
}
