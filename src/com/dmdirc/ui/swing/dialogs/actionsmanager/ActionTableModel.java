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

package com.dmdirc.ui.swing.dialogs.actionsmanager;

import com.dmdirc.actions.Action;
import com.dmdirc.actions.ActionGroup;
import com.dmdirc.actions.interfaces.ActionType;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * Action table model.
 */
public class ActionTableModel extends AbstractTableModel {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Action list. */
    private List<Action> actions;

    /**
     * Instantiates a new table model.
     */
    public ActionTableModel() {
        this(new ArrayList<Action>());
    }

    /**
     * Instantiates a new table model.
     * 
     * @param actions Actions to show
     */
    public ActionTableModel(final List<Action> actions) {
        if (actions == null) {
            this.actions = new ArrayList<Action>();
        } else {
            this.actions = actions;
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getRowCount() {
        return actions.size();
    }

    /** {@inheritDoc} */
    @Override
    public int getColumnCount() {
        return 3;
    }

    /** {@inheritDoc} */
    @Override
    public String getColumnName(final int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "Name";
            case 1:
                return "Trigger";
            case 2:
                return "Response";
            default:
                throw new IllegalArgumentException("Unknown column: " +
                        columnIndex);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        switch (columnIndex) {
            case 0:
                return String.class;
            case 1:
                return ActionType.class;
            case 2:
                return String[].class;
            default:
                throw new IllegalArgumentException("Unknown column: " +
                        columnIndex);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (actions.size() <= rowIndex) {
            throw new IndexOutOfBoundsException(rowIndex + " >= " +
                    actions.size());
        }
        if (rowIndex < 0) {
            throw new IllegalArgumentException("Must specify a positive integer");
        }
        switch (columnIndex) {
            case 0:
                return actions.get(rowIndex).getName();
            case 1:
                return actions.get(rowIndex).getTriggers()[0];
            case 2:
                return actions.get(rowIndex).getResponse();
            default:
                throw new IllegalArgumentException("Unknown column: " +
                        columnIndex);
        }
    }
    
    /**
     * Replaces the model data with the specified action group.
     * 
     * @param group New Action group
     */
    public void setActionGroup(final ActionGroup group) {
        this.actions = group;
        fireTableDataChanged();
    }

    /**
     * Adds an action to the model.
     * 
     * @param action Action to add
     */
    public void add(final Action action) {
        actions.add(action);
        fireTableRowsInserted(actions.size() - 1, actions.size() - 1);
    }

    /**
     * Removes an action from the model.
     * 
     * @param action Action to remove
     */
    public void remove(final Action action) {
        remove(actions.indexOf(action));
    }

    /**
     * Removes an action from the model.
     * 
     * @param index Index of the action to remove
     */
    public void remove(final int index) {
        if (index != -1) {
            actions.remove(index);
            fireTableRowsDeleted(index, index);
        }
    }
}
