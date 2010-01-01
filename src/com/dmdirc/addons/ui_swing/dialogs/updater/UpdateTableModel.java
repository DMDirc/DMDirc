/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.ui_swing.dialogs.updater;

import com.dmdirc.updater.UpdateListener;
import com.dmdirc.updater.Update;
import com.dmdirc.updater.UpdateComponent;
import com.dmdirc.updater.UpdateStatus;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

/**
 * Update table model.
 */
public class UpdateTableModel extends AbstractTableModel implements UpdateListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 3;
    /** Data list. */
    private List<Update> updates;
    /** Enabled list. */
    private Map<Update, Boolean> enabled;
    /** Number formatter. */
    private NumberFormat formatter;

    /** Creates a new instance of UpdateTableModel. */
    public UpdateTableModel() {
        this(new ArrayList<Update>());
    }

    /**
     * Creates a new instance of UpdateTableModel.
     *
     * @param updates List of updates
     */
    public UpdateTableModel(final List<Update> updates) {
        super();

        setUpdates(updates);
        formatter = NumberFormat.getNumberInstance();
        formatter.setMaximumFractionDigits(1);
        formatter.setMinimumFractionDigits(1);
    }

    /**
     * Sets the updates list.
     *
     * @param updates List of updates
     */
    public void setUpdates(final List<Update> updates) {
        this.updates = new ArrayList<Update>(updates);
        this.enabled = new HashMap<Update, Boolean>();

        for (Update update : updates) {
            update.addUpdateListener(this);
            enabled.put(update, true);
        }

        fireTableDataChanged();
    }

    /** {@inheritDoc} */
    @Override
    public int getRowCount() {
        return updates.size();
    }

    /** {@inheritDoc} */
    @Override
    public int getColumnCount() {
        return 4;
    }

    /** {@inheritDoc} */
    @Override
    public String getColumnName(final int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "Update?";
            case 1:
                return "Component";
            case 2:
                return "New version";
            case 3:
                return "Status";
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
                return Boolean.class;
            case 1:
                return UpdateComponent.class;
            case 2:
                return String.class;
            case 3:
                return UpdateStatus.class;
            default:
                throw new IllegalArgumentException("Unknown column: "
                        + columnIndex);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        return columnIndex == 0;
    }

    /** {@inheritDoc} */
    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        if (updates.size() <= rowIndex) {
            throw new IndexOutOfBoundsException(rowIndex + " >= "
                    + updates.size());
        }
        if (rowIndex < 0) {
            throw new IllegalArgumentException("Must specify a positive integer");
        }
        switch (columnIndex) {
            case 0:
                return enabled.get(updates.get(rowIndex));
            case 1:
                return updates.get(rowIndex).getComponent();
            case 2:
                return updates.get(rowIndex).getRemoteVersion();
            case 3:
                if (updates.get(rowIndex).getStatus().equals(UpdateStatus.DOWNLOADING)) {
                    return updates.get(rowIndex).getStatus() + " ("
                            + formatter.format(updates.get(rowIndex).getProgress()) + "%)";
                } else {
                    return updates.get(rowIndex).getStatus();
                }
            default:
                throw new IllegalArgumentException("Unknown column: "
                        + columnIndex);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setValueAt(final Object aValue, final int rowIndex,
            final int columnIndex) {
        if (updates.size() <= rowIndex) {
            throw new IndexOutOfBoundsException(rowIndex + " >= " +
                    updates.size());
        }
        if (rowIndex < 0) {
            throw new IllegalArgumentException("Must specify a positive integer");
        }
        switch (columnIndex) {
            case 0:
                enabled.put(updates.get(rowIndex), (Boolean) aValue);
                break;
            default:
                throw new IllegalArgumentException("Unknown column: "
                        + columnIndex);
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    /**
     * Gets the update at the specified row.
     *
     * @param rowIndex Row to retrieve
     *
     * @return Specified Update
     */
    public Update getUpdate(final int rowIndex) {
        return updates.get(rowIndex);
    }

    /**
     * Gets a list of all updates.
     *
     * @return Update list
     */
    public List<Update> getUpdates() {
        return new ArrayList<Update>(updates);
    }

    /**
     * Is the specified component to be updated?
     * 
     * @param update Component to check
     * 
     * @return true iif the component needs to be updated
     */
    public boolean isEnabled(final Update update) {
        return enabled.get(update);
    }

    /**
     * Is the component at the specified index to be updated?
     * 
     * @param rowIndex Component index to check
     * 
     * @return true iif the component needs to be updated
     */
    public boolean isEnabled(final int rowIndex) {
        return isEnabled(updates.get(rowIndex));
    }

    /**
     * Adds an update to the list.
     *
     * @param update Update to add
     */
    public void addRow(final Update update) {
        updates.add(update);
        update.addUpdateListener(this);
        fireTableRowsInserted(updates.indexOf(update), updates.indexOf(update));
    }

    /**
     * Removes a specified row from the list.
     *
     * @param row Row to remove
     */
    public void removeRow(final int row) {
        updates.get(row).removeUpdateListener(this);
        updates.remove(row);
        fireTableRowsDeleted(row, row);
    }

    /**
     * Returns the index of the specified update.
     *
     * @param update Update to get index of
     *
     * @return Index of the update or -1 if not found.
     */
    public int indexOf(final Update update) {
        return updates.indexOf(update);
    }

    /** {@inheritDoc} */
    @Override
    public void updateStatusChange(final Update update, final UpdateStatus status) {
        fireTableCellUpdated(updates.indexOf(update), 3);
    }

    /** {@inheritDoc} */
    @Override
    public void updateProgressChange(final Update update, final float progress) {
        fireTableCellUpdated(updates.indexOf(update), 3);
    }
}

