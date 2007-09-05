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

package com.dmdirc.ui.swing.dialogs.error;

import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.ErrorStatus;
import com.dmdirc.logger.ProgramError;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * Table model for displaying program errors.
 */
public final class ErrorTableModel extends AbstractTableModel {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** Data list. */
    private List<ProgramError> errors;
    
    /** Creates a new instance of ErrorTableModel. */
    public ErrorTableModel() {
        this(new ArrayList<ProgramError>());
    }
    
    /** 
     * Creates a new instance of ErrorTableModel. 
     *
     * @param errors List of errors.
     */
    public ErrorTableModel(final List<ProgramError> errors) {
        super();
        
        this.errors = errors;
    }
    
    /**
     * Sets the list of errors.
     *
     * @param errors List of errors
     */
    public void setErrors(final List<ProgramError> errors) {
        this.errors = errors;
        
        fireTableDataChanged();
    }
    
    /** {@inheritDoc} */
    public int getRowCount() {
        return errors.size();
    }
    
    /** {@inheritDoc} */
    public int getColumnCount() {
        return 5;
    }
    
    /** {@inheritDoc} */
    public String getColumnName(final int columnIndex) {
        switch(columnIndex) {
            case 0:
                return "ID";
            case 1:
                return "Time";
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
    public Class<?> getColumnClass(final int columnIndex) {
        switch(columnIndex) {
            case 0:
                return Integer.class;
            case 1:
                return Date.class;
            case 2:
                return ErrorLevel.class;
            case 3:
                return ErrorStatus.class;
            case 4:
                return String.class;
            default:
                throw new IndexOutOfBoundsException(columnIndex + ">= 5");
        }
    }
    
    /** {@inheritDoc} */
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        return false;
    }
    
    /** {@inheritDoc} */
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        switch(columnIndex) {
            case 0:
                return errors.get(rowIndex).getID();
            case 1:
                return errors.get(rowIndex).getDate();
            case 2:
                return errors.get(rowIndex).getLevel();
            case 3:
                return errors.get(rowIndex).getStatus();
            case 4:
                return errors.get(rowIndex).getMessage();
            default:
                throw new IndexOutOfBoundsException(columnIndex + ">= 5");
        }
    }
    
    /** {@inheritDoc} */
    public void setValueAt(final Object aValue, final int rowIndex,
            final int columnIndex) {
        switch(columnIndex) {
            case 3:
                if (aValue instanceof ErrorStatus) {
                    errors.get(rowIndex).setStatus((ErrorStatus) aValue);
                    break;
                } else {
                    throw new IllegalArgumentException("Received: "
                            + aValue.getClass() + ", expecting: "
                            + ErrorStatus.class);
                }
            default:
                throw new UnsupportedOperationException("Only editing the "
                        + "status is allowed");
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
        return errors.get(rowIndex);
    }
    
    /**
     * Returns the index of the specified error or -1 if the error is not found.
     *
     * @param error ProgramError to locate
     *
     * @return Error index or -1 if not found
     */
    public int indexOf(final ProgramError error) {
        return errors.indexOf(error);
    }
    
    /**
     * Adds an error to the list.
     *
     * @param error ProgramError to add
     */
    public void addRow(final ProgramError error) {
        errors.add(error);
        fireTableRowsInserted(errors.indexOf(error), errors.indexOf(error));
    }
    
    /**
     * Removes a specified row from the list.
     *
     * @param row Row to remove
     */
    public void removeRow(final int row) {
        errors.remove(row);
        fireTableRowsDeleted(row, row);
    }
    
    /**
     * Removes a specified error from the list.
     *
     * @param error ProgramError to remove
     */
    public void removeRow(final ProgramError error) {
        if (errors.contains(error)) {
            final int row = errors.indexOf(error);
            errors.remove(row);
            fireTableRowsDeleted(row, row);
        }
    }
    
}
