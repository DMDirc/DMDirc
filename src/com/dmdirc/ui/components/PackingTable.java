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

package com.dmdirc.ui.components;

import static com.dmdirc.ui.UIUtilities.SMALL_BORDER;
import java.awt.Graphics;
import javax.swing.JScrollPane;

import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * Creates a new table that automatically sizes its columns to the size of its
 * data.
 */
public class PackingTable extends JTable {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** Whether the table should be editable. */
    private final boolean editable;
    
    /** Scrollpane. */
    private final JScrollPane scrollPane;
    
    /**
     * Creates a new packing table.
     *
     * @param rows Row data
     * @param cols Column data
     * @param editable Whether the table should be editable or not
     */
    public PackingTable(final Object[][] rows, final Object[] cols,
            final boolean editable, final JScrollPane scrollPane) {
        super(rows, cols);
        
        this.editable = editable;
        this.scrollPane = scrollPane;
        
        super.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    }
    
    /**
     * Creates a new packing table.
     *
     * @param tableModel Table data model
     * @param editable Whether the table should be editable or not
     */
    public PackingTable(final TableModel tableModel, final boolean editable,
            final JScrollPane scrollPane) {
        super(tableModel);
        
        this.editable = editable;
        this.scrollPane = scrollPane;
        
        super.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    }
    
    /** {@inheritDoc} */
    public void setAutoResizeMode(final int mode) {
        //Ignore
    }
    
    /** {@inheritDoc} */
    public final boolean getScrollableTracksViewportHeight() {
        return getPreferredSize().height < getParent().getHeight();
    }
    
    /** {@inheritDoc} */
    public boolean isCellEditable(final int x, final int y) {
        if (editable) {
            return true;
        }
        
        return false;
    }
    
    /** {@inheritDoc} */
    public void paint(final Graphics g) {
        packColumns();
        super.paint(g);
    }
    
    /** Packs the columns to their width. */
    public final void packColumns() {
        if (!isShowing()) {
            return;
        }
        
        if (getColumnCount() == 0) {
            return;
        }
        
        final TableColumnModel columnModel = getTableHeader().getColumnModel();
        final int numCols = columnModel.getColumnCount();
        final int totalSize = scrollPane.getViewportBorderBounds().width;
        final int[] widths = new int[numCols];
        int widthsTotal = 0;
        
        for (int i = 0; i < numCols; i++) { //NOPMD im not copying a damn array fgs
            widths[i] = getWidth(i);
            widthsTotal += widths[i];
        }
        
        final int extra = totalSize - widthsTotal;
        if (extra > 0) {
            widths[numCols - 1] += extra;
        }
        
        for (int i = 0; i < numCols; i++) {
            final TableColumn col = columnModel.getColumn(i);
            col.setPreferredWidth(widths[i]);
        }
        
    }
    
    private int getWidth(final int col) {
        final TableColumn column = getColumnModel().getColumn(col);
        int width = (int) getTableHeader().getDefaultRenderer().
                getTableCellRendererComponent(this, column.getIdentifier()
                , false, false, -1, col).getPreferredSize().getWidth();
        
        for (int row = 0; row < getRowCount(); row++) {
            width = Math.max(width, (int) getCellRenderer(row, col).
                    getTableCellRendererComponent(this, getValueAt(row,
                    col), false, false, row, col).getPreferredSize().getWidth());
        }
        
        return width + SMALL_BORDER;
    }
}
