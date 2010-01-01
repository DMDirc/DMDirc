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

package com.dmdirc.addons.ui_swing.components.renderers;

import com.dmdirc.addons.ui_swing.components.reorderablelist.ReorderableJList;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

import net.miginfocom.swing.MigLayout;

/**
 * Renderer for the reorderable JList, procides visual clues to DnD.
 */
public class ReorderableJListCellRenderer implements ListCellRenderer {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** Parent list. */
    private final ReorderableJList parent;
    
    /**
     * Instantiates a new ReorderableJListCellRenderer.
     *
     * @param parent Parent list
     */
    public ReorderableJListCellRenderer(final ReorderableJList parent) {
        super();
        
        this.parent = parent;
    }
    
    /** {@inheritDoc} */
    @Override
    public Component getListCellRendererComponent(final JList list,
            final Object value, final int index, final boolean isSelected,
            final boolean cellHasFocus) {
        final JPanel panel = new JPanel();
        panel.setLayout(new MigLayout("fill, ins 0"));
        final boolean isTargetCell = value == parent.getTargetCell();
        
        final boolean showSelected = isSelected & (parent.getTargetCell() == null);
        
        panel.add(new JLabel(value.toString()), "dock center");
        
        if (showSelected) {
            panel.setForeground(UIManager.getColor("List.selectionForeground"));
            panel.setBackground(UIManager.getColor("List.selectionBackground"));
        } else {
            if (isSelected) {
                panel.setForeground(UIManager.getColor("List.selectionForeground"));
                panel.setBackground(UIManager.getColor("List.selectionBackground"));
            } else {
                panel.setForeground(UIManager.getColor("List.foreground"));
                panel.setBackground(UIManager.getColor("List.background"));
            }
        }
        
        
        if (isTargetCell) {
            if (parent.getBelowTarget()) {
                panel.add(new JSeparator(), "dock south");
            } else {
                panel.add(new JSeparator(), "dock north");
            }
        }
        
        return panel;
    }
}
