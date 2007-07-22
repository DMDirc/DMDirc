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

package com.dmdirc.addons.nickcolours;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Panel used for the custom nick colour settings component in the plugin's
 * config dialog.
 *
 * @author Chris
 */
public class NickColourPanel extends JPanel {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;    
    
    /** The table used for displaying the options. */
    private JTable table;
    
    /** The table headings. */
    private final String[] headers = {"Network", "Nickname", "Text colour", "Nicklist colour"};
    
    /**
     * Creates a new instance of NickColourPanel.
     */
    public NickColourPanel() {
        final Object[][] data = {
            {"Quakenet", "MD87", null, "4"},
            {"Quakenet", "Dataforce", "FFFF00", "FFFF00"}
        };
        
        table = new JTable(data, headers) {
            
            /**
             * A version number for this class. It should be changed whenever the class
             * structure is changed (or anything else that would prevent serialized
             * objects being unserialized with the new class).
             */
            private static final long serialVersionUID = 1;
            
            /** The colour renderer we're using for colour cells. */
            private final ColourRenderer colourRenderer = new ColourRenderer();
            
            /** {@inheritDoc} */
            @Override
            public TableCellRenderer getCellRenderer(final int row, final int column) {
                if (column == 2 || column == 3) {
                    return colourRenderer;
                } else {
                    return super.getCellRenderer(row, column);
                }
            }
            
            /** {@inheritDoc} */
            @Override
            public boolean isCellEditable(final int row, final int column) {
                return false;
            }
            
        };
        
        final JScrollPane scrollPane = new JScrollPane(table);
        
        table.setFillsViewportHeight(true);
        table.setDefaultRenderer(Color.class, new ColourRenderer());
        
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
    }
    
}
