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
import static com.dmdirc.ui.swing.UIUtilities.SMALL_BORDER;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Map.Entry;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Lists substitutions for use in actions.
 */
public final class SubstitutionsPanel extends JPanel implements MouseListener,
        ActionListener, ListSelectionListener {
    
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
    
    /** Add button. */
    private JButton add;
    
    /** Listener list. */
    private final EventListenerList listeners;
    
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
        
        listeners = new EventListenerList();
        
        add = new JButton("Insert Substitution");
        add.setEnabled(false);
        add.addActionListener(this);
        
        list = new JList(new DefaultListModel());
        list.setCellRenderer(new ActionSubstititionRenderer());
        list.setDragEnabled(true);
        list.addMouseListener(this);
        list.addListSelectionListener(this);
        populateList();
        
        layoutComponents();
    }
    
    /**
     * Sets the action type of the substitutor.
     *
     * @param type Action type to show substitutions for
     */
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
        setLayout(new BorderLayout(SMALL_BORDER, SMALL_BORDER));
        
        add(new JLabel("Substitutions"), BorderLayout.PAGE_START);
        add(new JScrollPane(list), BorderLayout.CENTER);
        add(add, BorderLayout.PAGE_END);
        
        setMinimumSize(new Dimension(100, 200));
        setPreferredSize(new Dimension(100, 200));
    }
    
    /** {@inheritDoc} */
    public void actionPerformed(final ActionEvent e) {
        if (list.getSelectedValue() != null) {
            fireSubstitutionInsert((ActionSubstitution) list.getSelectedValue());
        }
    }
    
    /** {@inheritDoc} */
    public void mouseClicked(final MouseEvent e) {
        if (list.getSelectedValue() != null && e.getClickCount() == 2) {
            fireSubstitutionInsert((ActionSubstitution) list.getSelectedValue());
        }
    }
    
    /** {@inheritDoc} */
    public void mousePressed(final MouseEvent e) {
        //Ignore
    }
    
    /** {@inheritDoc} */
    public void mouseReleased(final MouseEvent e) {
        //Ignore
    }
    
    /** {@inheritDoc} */
    public void mouseEntered(final MouseEvent e) {
        //Ignore
    }
    
    /** {@inheritDoc} */
    public void mouseExited(final MouseEvent e) {
        //Ignore
    }
    
    /**
     * Adds a SubstitutionsPanelListener to the listener list.
     *
     * @param listener Listener to add
     */
    public void addSubstitutionsPanelListener(final SubstitutionsPanelListener listener) {
        synchronized (listeners) {
            if (listener == null) {
                return;
            }
            listeners.add(SubstitutionsPanelListener.class, listener);
        }
    }
    
    /**
     * Removes a SubstitutionsPanelListener from the listener list.
     *
     * @param listener Listener to remove
     */
    public void removeSubstitutionsPanelListener(final SubstitutionsPanelListener listener) {
        listeners.remove(SubstitutionsPanelListener.class, listener);
    }
    
    /**
     * Informs listeners when a substitution needs inserting.
     *
     * @param substitution ActionSubtitution that needs inserting
     */
    private void fireSubstitutionInsert(final ActionSubstitution substitution) {
        final Object[] listenersList = listeners.getListenerList();
        for (int i = 0; i < listenersList.length; i += 2) {
            if (listenersList[i] == SubstitutionsPanelListener.class) {
                ((SubstitutionsPanelListener) listenersList[i + 1]).substitutionInsert(substitution);
            }
        }
    }

    /** {@inheritDoc} */
    public void valueChanged(final ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            if (list.getSelectedIndex() == -1) {
                add.setEnabled(false);
            } else {
                add.setEnabled(true);
            }
        }
    }
}
