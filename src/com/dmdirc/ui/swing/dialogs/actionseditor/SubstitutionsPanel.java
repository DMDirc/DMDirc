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

import com.dmdirc.Config;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.ActionSubstitutor;
import com.dmdirc.actions.ActionType;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.config.IdentityManager;
import java.awt.BorderLayout;
import java.awt.Dimension;

import java.util.Map.Entry;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

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
        this(null);
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
        list.setCellRenderer(new ActionSubstititionRenderer());
        list.setDragEnabled(true);
        populateList();
        
        layoutComponents();
    }
    
    public void setType(final ActionType type) {
        this.type = type;
        populateList();
    }
    
    /** Populates the list with valid substitutions. */
    public void populateList() {
        final DefaultListModel model = (DefaultListModel) list.getModel();
        
        //populate the list
        list.setVisible(false);
        
        model.clear();
        
        if (type == null) {
            
            list.setVisible(true);
            
            return;
        }
        
        final ActionSubstitutor sub = new ActionSubstitutor(type);
        
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
    
    /** Lays out the components. */
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        add(new JLabel("Substitutions"), BorderLayout.PAGE_START);
        add(new JScrollPane(list), BorderLayout.CENTER);
        
        setMinimumSize(new Dimension(100, 200));
        setPreferredSize(new Dimension(100, 200));
    }
    
    
    public JList getList() {
        return list;
    }
    
    public static void main(final String[] args) {
        IdentityManager.load();
        Config.init();
        CommandManager.initCommands();
        ActionManager.init();
        ActionManager.loadActions();
        
        final JFrame frame = new JFrame();
        final SubstitutionsPanel subsPanel = new SubstitutionsPanel(CoreActionType.CHANNEL_MESSAGE);
        
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        
        frame.add(subsPanel);
        
        frame.pack();
        frame.setVisible(true);
    }
}
