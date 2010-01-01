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

package com.dmdirc.addons.ui_swing.dialogs.error;

import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.ErrorListener;
import com.dmdirc.logger.ErrorManager;
import com.dmdirc.logger.ErrorReportStatus;
import com.dmdirc.logger.ProgramError;

import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * Table model for displaying program errors.
 */
public final class ErrorTableModel extends AbstractTableModel implements
        ErrorListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    /** Data list. */
    private final List<ProgramError> errors;

    /** Creates a new instance of ErrorTableModel. */
    public ErrorTableModel() {
        this(ErrorManager.getErrorManager().getErrors());
    }

    /** 
     * Creates a new instance of ErrorTableModel. 
     *
     * @param errors List of errors.
     */
    public ErrorTableModel(final List<ProgramError> errors) {
        super();

        this.errors = errors;

        ErrorManager.getErrorManager().addErrorListener(this);
    }

    /**
     * Sets the list of errors.
     *
     * @param errors List of errors
     */
    public void setErrors(final List<ProgramError> errors) {
        this.errors.clear();
        this.errors.addAll(errors);

        fireTableDataChanged();
    }

    /** {@inheritDoc} */
    @Override
    public int getRowCount() {
        synchronized (errors) {
            return errors.size();
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getColumnCount() {
        return 5;
    }

    /** {@inheritDoc} */
    @Override
    public String getColumnName(final int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "ID";
            case 1:
                return "Count";
            case 2:
                return "Severity";
            case 3:
                return "Report Status";
            case 4:
                return "Message";
            default:
                throw new IndexOutOfBoundsException(columnIndex + ">= 5");
        }
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Integer.class;
            case 1:
                return Integer.class;
            case 2:
                return ErrorLevel.class;
            case 3:
                return ErrorReportStatus.class;
            case 4:
                return String.class;
            default:
                throw new IndexOutOfBoundsException(columnIndex + ">= 5");
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        synchronized (errors) {
            switch (columnIndex) {
                case 0:
                    return errors.get(rowIndex).getID();
                case 1:
                    return errors.get(rowIndex).getCount();
                case 2:
                    return errors.get(rowIndex).getLevel();
                case 3:
                    return errors.get(rowIndex).getReportStatus();
                case 4:
                    return errors.get(rowIndex).getMessage();
                default:
                    throw new IndexOutOfBoundsException(columnIndex + ">= 5");
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setValueAt(final Object aValue, final int rowIndex,
            final int columnIndex) {
        synchronized (errors) {
            switch (columnIndex) {
                case 3:
                    if (aValue instanceof ErrorReportStatus) {
                        errors.get(rowIndex).setReportStatus(
                                (ErrorReportStatus) aValue);
                        break;
                    } else {
                        throw new IllegalArgumentException("Received: " +
                                aValue.getClass() + ", expecting: " +
                                ErrorReportStatus.class);
                    }
                default:
                    throw new UnsupportedOperationException("Only editing the " +
                            "status is allowed");
            }
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }

    /**
     * Gets the error at the specified row.
     *
     * @param rowIndex Row to retrieve
     *
     * @return Specified error
     */
    public ProgramError getError(final int rowIndex) {
        synchronized (errors) {
            return errors.get(rowIndex);
        }
    }

    /**
     * Returns the index of the specified error or -1 if the error is not found.
     *
     * @param error ProgramError to locate
     *
     * @return Error index or -1 if not found
     */
    public int indexOf(final ProgramError error) {
        synchronized (errors) {
            return errors.indexOf(error);
        }
    }

    /**
     * Adds an error to the list.
     *
     * @param error ProgramError to add
     */
    public void addRow(final ProgramError error) {
        synchronized (errors) {
            errors.add(error);
            fireTableRowsInserted(errors.indexOf(error), errors.indexOf(error));
        }
    }

    /**
     * Removes a specified row from the list.
     *
     * @param row Row to remove
     */
    public void removeRow(final int row) {
        synchronized (errors) {
            errors.remove(row);
            fireTableRowsDeleted(row, row);
        }
    }

    /**
     * Removes a specified error from the list.
     *
     * @param error ProgramError to remove
     */
    public void removeRow(final ProgramError error) {
        synchronized (errors) {
            if (errors.contains(error)) {
                final int row = errors.indexOf(error);
                errors.remove(row);
                fireTableRowsDeleted(row, row);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void errorAdded(final ProgramError error) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                synchronized (ErrorTableModel.this) {
                    addRow(error);
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void errorDeleted(final ProgramError error) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                synchronized (ErrorTableModel.this) {
                    removeRow(error);
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void errorStatusChanged(final ProgramError error) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                synchronized (ErrorTableModel.this) {
                    final int errorRow = indexOf(error);
                    if (errorRow != -1 && errorRow < getRowCount()) {
                        fireTableRowsUpdated(errorRow, errorRow);
                    }
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public boolean isReady() {
        return true;
    }
}
