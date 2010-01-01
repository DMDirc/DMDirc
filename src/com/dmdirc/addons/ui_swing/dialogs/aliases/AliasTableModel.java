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

package com.dmdirc.addons.ui_swing.dialogs.aliases;

import com.dmdirc.actions.wrappers.Alias;
import com.dmdirc.actions.ActionCondition;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * Table model for displaying aliases.
 */
public final class AliasTableModel extends AbstractTableModel {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 3;
    
    /** Data list. */
    private List<Alias> aliases;
    
    /** Creates a new instance of AliasTableModel. */
    public AliasTableModel() {
        this(new ArrayList<Alias>());
    }
    
    /**
     * Creates a new instance of AliasTableModel.
     *
     * @param aliases List of aliases
     */
    public AliasTableModel(final List<Alias> aliases) {
        super();
        
        this.aliases = new ArrayList<Alias>(aliases);
    }
    
    /**
     * Sets the alias list.
     *
     * @param aliases List of aliases
     */
    public void setAliases(final List<Alias> aliases) {
        this.aliases = new ArrayList<Alias>(aliases);
        
        fireTableDataChanged();
    }
    
    /** {@inheritDoc} */
    public int getRowCount() {
        return aliases.size();
    }
    
    /** {@inheritDoc} */
    public int getColumnCount() {
        return 3;
    }
    
    /** {@inheritDoc} */
    public String getColumnName(final int columnIndex) {
        switch(columnIndex) {
            case 0:
                return "Command";
            case 1:
                return "# of Arguments";
            case 2:
                return "Response";
            default:
                throw new IllegalArgumentException("Unknown column: "
                        + columnIndex);
        }
    }
    
    /** {@inheritDoc} */
    public Class<?> getColumnClass(final int columnIndex) {
        switch(columnIndex) {
            case 0:
                return String.class;
            case 1:
                return ActionCondition.class;
            case 2:
                return String[].class;
            default:
                throw new IllegalArgumentException("Unknown column: "
                        + columnIndex);
        }
    }
    
    /** {@inheritDoc} */
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        return false;
    }
    
    /** {@inheritDoc} */
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        if (aliases.size() <= rowIndex) {
            throw new IndexOutOfBoundsException(rowIndex + " >= " + aliases.size());
        }
        if (rowIndex < 0) {
            throw new IllegalArgumentException("Must specify a positive integer");
        }
        switch(columnIndex) {
            case 0:
                return aliases.get(rowIndex).getCommand();
            case 1:
                return aliases.get(rowIndex).getArgsArgument();
            case 2:
                return aliases.get(rowIndex).getResponse();
            default:
                throw new IllegalArgumentException("Unknown column: "
                        + columnIndex);
        }
    }
    
    /** {@inheritDoc} */
    public void setValueAt(final Object aValue, final int rowIndex,
            final int columnIndex) {
        if (aliases.size() <= rowIndex) {
            throw new IndexOutOfBoundsException(rowIndex + " >= " + aliases.size());
        }
        if (rowIndex < 0) {
            throw new IllegalArgumentException("Must specify a positive integer");
        }
        switch(columnIndex) {
            case 0:
                aliases.get(rowIndex).setCommand((String) aValue);
                break;
            case 1:
                aliases.get(rowIndex).getArguments().set(1, (ActionCondition) aValue);
                break;
            case 2:
                aliases.get(rowIndex).setResponse((String[]) aValue);
                break;
            default:
                throw new IllegalArgumentException("Unknown column: "
                        + columnIndex);
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }
    
    /**
     * Gets the alias at the specified row.
     *
     * @param rowIndex Row to retrieve
     *
     * @return Specified Alias
     */
    public Alias getAlias(final int rowIndex) {
        return aliases.get(rowIndex);
    }
    
    /**
     * Gets a list of all aliases (including deleted ones).
     *
     * @return Complete alias list
     */
    public List<Alias> getAliases() {
        return new ArrayList<Alias>(aliases);
    }
    
    /**
     * Adds an alias to the list.
     *
     * @param alias Alias to add
     */
    public void addRow(final Alias alias) {
        aliases.add(alias);
        fireTableRowsInserted(aliases.indexOf(alias), aliases.indexOf(alias));
    }
    
    /**
     * Removes a specified row from the list.
     *
     * @param row Row to remove
     */
    public void removeRow(final int row) {
        aliases.remove(row);
        fireTableRowsDeleted(row, row);
    }
    
    /**
     * Returns the index of the specified alias.
     *
     * @param alias Alias to get index of
     *
     * @return Index of the alias or -1 if not found.
     */
    public int indexOf(final Alias alias) {
        return aliases.indexOf(alias);
    }
}
