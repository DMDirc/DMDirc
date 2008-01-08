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

package com.dmdirc.ui.swing.dialogs.prefs;

import com.dmdirc.updater.UpdateChecker;
import com.dmdirc.updater.UpdateComponent;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * Update component table model
 */
public class UpdateTableModel extends AbstractTableModel {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 3;
    /** Update component list. */
    private final List<UpdateComponent> updates;
    
    /**
     * Instantiates a new table model.
     */
    public UpdateTableModel() {
        this(new ArrayList<UpdateComponent>());
    }

    /**
     * Instantiates a new table model.
     * 
     * @param updates Update components to show
     */
    public UpdateTableModel(final List<UpdateComponent> updates) {
        this.updates = updates;
    }

    /** {@inheritDoc} */
    @Override
    public int getRowCount() {
        return updates.size();
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
                return "Update Component";
            case 1:
                return "Enabled?";
            case 2:
                return "Version";
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
                return Boolean.class;
            case 2:
                return Integer.class;
            default:
                throw new IllegalArgumentException("Unknown column: " +
                        columnIndex);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (updates.size() <= rowIndex) {
            throw new IndexOutOfBoundsException(rowIndex + " >= " +
                    updates.size());
        }
        if (rowIndex < 0) {
            throw new IllegalArgumentException("Must specify a positive integer");
        }
        switch (columnIndex) {
            case 0:
                return updates.get(rowIndex).getName();
            case 1:
                return UpdateChecker.isEnabled(updates.get(rowIndex));
            case 2:
                return updates.get(rowIndex).getVersion();
            default:
                throw new IllegalArgumentException("Unknown column: " +
                        columnIndex);
        }
    }

    /**
     * Adds a update component to the model.
     * 
     * @param component update component to add
     */
    public void add(final UpdateComponent component) {
        updates.add(component);
        fireTableRowsInserted(updates.size() - 1, updates.size() - 1);
    }

    /**
     * Removes a update component to the model.
     * 
     * @param component update component to remove
     */
    public void remove(final UpdateComponent component) {
        remove(updates.indexOf(component));
    }

    /**
     * Removes a update component to the model.
     * 
     * @param index Index of the update component to remove
     */
    public void remove(final int index) {
        if (index != -1) {
            updates.remove(index);
            fireTableRowsDeleted(index, index);
        }
    }
}
