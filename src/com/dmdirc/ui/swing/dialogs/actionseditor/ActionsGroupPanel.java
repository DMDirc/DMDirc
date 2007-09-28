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

import com.dmdirc.actions.Action;
import com.dmdirc.actions.ActionType;
import com.dmdirc.ui.swing.components.PackingTable;
import static com.dmdirc.ui.swing.UIUtilities.SMALL_BORDER;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * The actions group panel is the control displayed within the tabbed control
 * of the actions manager dialog. It shows the user all actions belonging to
 * a particular group.
 * @author chris
 */
public final class ActionsGroupPanel extends JPanel
        implements ListSelectionListener, MouseListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** The column headers we use. */
    private static final String[] HEADERS = {"Name", "Trigger", "Response"};
    
    /** The dialog that owns this group panel. */
    private final ActionsManagerDialog parent;
    
    /** The actions that belong in this pane. */
    private final List<Action> actions;
    
    /** The JTable we use to display data. */
    private JTable table;
    
    /** group name. */
    private String group;
    
    /**
     * Creates a new instance of ActionsGroupPanel.
     *
     * @param parent parent dialog
     * @param actions Actions list to show
     * @param group Group name
     */
    public ActionsGroupPanel(final ActionsManagerDialog parent,
            final List<Action> actions, final String group) {
        super();
        
        this.parent = parent;
        this.actions = actions;
        this.group = group;
        
        initComponents();
    }
    
    /**
     * Implodes an array into a string, separating arguments with ", ".
     * @param args The arguments to be imploded
     * @return A string representation of the arguments.
     */
    private String implode(final Object[] args) {
        final StringBuffer res = new StringBuffer();
        
        if (args == null) {
            return "";
        }
        
        for (Object object : args) {
            res.append(", ");
            if (object instanceof ActionType) {
                res.append(((ActionType) object).getName());
            } else {
                res.append(object.toString());
            }
        }
        
        return res.substring(2);
    }
    
    /** Initialises the components for this panel. */
    private void initComponents() {
        final JScrollPane pane;
        
        final Object[][] data = new Object[actions.size()][3];
        
        int i = 0;
        for (Action action : actions) {
            data[i][0] = action.getName();
            data[i][1] = implode(action.getTriggers());
            data[i][2] = implode(action.getResponse());
            i++;
        }
        
        pane = new JScrollPane();
        table = new PackingTable(data, HEADERS, false, pane);
        
        table.setCellSelectionEnabled(false);
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        
        table.getTableHeader().setReorderingAllowed(false);
        
        table.getSelectionModel().addListSelectionListener(this);
        table.addMouseListener(this);
        
        pane.setViewportView(table);
        
        pane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(SMALL_BORDER, SMALL_BORDER,
                SMALL_BORDER, SMALL_BORDER),
                BorderFactory.createEtchedBorder()
                ));
        
        setLayout(new BorderLayout());
        add(pane);
    }
    
    /**
     * Returns the table for this panel.
     *
     * @return The table in this panel
     */
    public JTable getTable() {
        return table;
    }
    
    /**
     * Returns the action at the specified index.
     *
     * @param index index of the action
     *
     * @return Returns the action at the specified index
     */
    public Action getAction(final int index) {
        return actions.get(index);
    }
    
    /**
     * Returns the actions list for this group.
     *
     * @return Actions list
     */
    public List<Action> getActions() {
        return actions;
    }
    
    /** {@inheritDoc}. */
    public void valueChanged(final ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            if (table.getSelectedRow() > -1) {
                parent.setEditState(true);
            } else {
                parent.setEditState(false);
            }
        }
    }
    
    /** {@inheritDoc}. */
    public void mouseClicked(final MouseEvent e) {
        if (e.getClickCount() == 2) {
            final int row = table.rowAtPoint(e.getPoint());
            
            ActionsEditorDialog.showActionsEditorDialog(parent,
                    actions.get(row), group);
        }
    }
    
    /** {@inheritDoc}. */
    public void mousePressed(final MouseEvent e) {
        //Ignore
    }
    
    /** {@inheritDoc}. */
    public void mouseReleased(final MouseEvent e) {
        //Ignore
    }
    
    /** {@inheritDoc}. */
    public void mouseEntered(final MouseEvent e) {
        //Ignore
    }
    
    /** {@inheritDoc}. */
    public void mouseExited(final MouseEvent e) {
        //Ignore
    }
    
}
