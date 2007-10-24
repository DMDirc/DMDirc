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

package com.dmdirc.ui.swing;

import com.dmdirc.IconManager;
import com.dmdirc.ui.swing.actions.RedoAction;
import com.dmdirc.ui.swing.actions.UndoAction;
import com.dmdirc.ui.swing.components.DMDircUndoableEditListener;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.KeyStroke;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.JTextComponent;
import javax.swing.undo.UndoManager;

/**
 * UI constants.
 */
public final class UIUtilities {
    
    /** Size of a large border. */
    public static final int LARGE_BORDER = 10;
    
    /** Size of a small border. */
    public static final int SMALL_BORDER = 5;
    
    /** Standard button size. */
    public static final Dimension BUTTON_SIZE = new Dimension(100, 25);
    
    /** Not intended to be instatiated. */
    private UIUtilities() {
    }
    
    /**
     * Aligns the components in a container horizontally and adds springs
     * vertically.
     *
     * @param parent parent container
     * @param rows number of rows
     * @param columns number of columns
     * @param outerXPadding outer x padding
     * @param outerYPadding outer y padding
     * @param xPadding x padding
     * @param yPadding y padding
     */
    public static void layoutGrid(final Container parent, final int rows,
            final int columns, final int outerXPadding,
            final int outerYPadding, final int xPadding, final int yPadding) {
        final SpringLayout layout = (SpringLayout) parent.getLayout();
        
        Spring x = Spring.constant(outerXPadding);
        Spring y = Spring.constant(outerYPadding);
        SpringLayout.Constraints constraints;
        
        for (int c = 0; c < columns; c++) {
            Spring width = Spring.constant(0);
            for (int r = 0; r < rows; r++) {
                width = Spring.max(width,
                        getConstraintsForCell(r, c, parent, columns).
                        getWidth());
            }
            for (int r = 0; r < rows; r++) {
                constraints = getConstraintsForCell(r, c, parent, columns);
                constraints.setX(x);
                constraints.setWidth(width);
            }
            if (c == columns - 1) {
                x = Spring.sum(x, width);
            } else {
                x = Spring.sum(x, Spring.sum(width, Spring.constant(xPadding)));
            }
        }
        
        for (int r = 0; r < rows; r++) {
            int height = 0;
            for (int c = 0; c < columns; c++) {
                height +=
                        getConstraintsForCell(r, c, parent, columns).
                        getHeight().getValue();
            }
            for (int c = 0; c < columns; c++) {
                constraints = getConstraintsForCell(r, c, parent, columns);
                constraints.setY(y);
                constraints.setHeight(Spring.constant(height));
            }
            if (r == rows - 1) {
                y = Spring.sum(y, Spring.constant(height));
            } else {
                y = Spring.sum(y, Spring.sum(Spring.constant(height),
                        Spring.constant(yPadding)));
            }
        }
        
        x = Spring.sum(x, Spring.constant(outerXPadding));
        y = Spring.sum(y, Spring.constant(outerYPadding));
        
        final SpringLayout.Constraints pCons = layout.getConstraints(parent);
        pCons.setConstraint(SpringLayout.SOUTH, y);
        pCons.setConstraint(SpringLayout.EAST, x);
    }
    
    /**
     * Returns the constraints for a specific cell.
     *
     * @param row Row of cell
     * @param column Column of cell
     * @param parent parent container
     * @param columns number of columns
     *
     * @return Constraits for a specific cell
     */
    private static SpringLayout.Constraints getConstraintsForCell(final int row,
            final int column, final Container parent, final int columns) {
        final SpringLayout layout = (SpringLayout) parent.getLayout();
        final Component constraints = parent.getComponent(row * columns + column);
        return layout.getConstraints(constraints);
    }
    
    /**
     * Adds an undo manager and associated key bindings to the specified text
     * component.
     */
    public static void addUndoManager(final JTextComponent component) {
        final UndoManager undoManager = new UndoManager();
        
        // Listen for undo and redo events
        component.getDocument().addUndoableEditListener(
                new DMDircUndoableEditListener(undoManager));
        
        // Create an undo action and add it to the text component
        component.getActionMap().put("Undo", new UndoAction(undoManager));
        
        // Bind the undo action to ctl-Z
        component.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "Undo");
        
        // Create a redo action and add it to the text component
        component.getActionMap().put("Redo", new RedoAction(undoManager));
        
        // Bind the redo action to ctl-Y
        component.getInputMap().put(KeyStroke.getKeyStroke("control Y"), "Redo");
    }
    
    /**
     * Initialises any settings required by this UI (this is always called
     * before any aspect of the UI is instansiated).
     *
     * @throws UnsupportedOperationException If unable to switch to the system
     * look and feel
     */
    public static void initUISettings() {
        
        try {
            
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (InstantiationException ex) {
            throw new UnsupportedOperationException("Unable to switch to the "
                    + "system look and feel", ex);
        } catch (ClassNotFoundException ex) {
            throw new UnsupportedOperationException("Unable to switch to the "
                    + "system look and feel", ex);
        } catch (UnsupportedLookAndFeelException ex) {
            throw new UnsupportedOperationException("Unable to switch to the "
                    + "system look and feel", ex);
        } catch (IllegalAccessException ex) {
            throw new UnsupportedOperationException("Unable to switch to the "
                    + "system look and feel", ex);
        }
        
        UIManager.put("swing.useSystemFontSettings", true);
        
        UIManager.put("swing.boldMetal", false);
        UIManager.put("InternalFrame.useTaskBar", false);
        UIManager.put("SplitPaneDivider.border", BorderFactory.createEmptyBorder());
        UIManager.put("Tree.collapsedIcon", IconManager.getIconManager().getIcon("nothing"));
        UIManager.put("Tree.expandedIcon", IconManager.getIconManager().getIcon("nothing"));
        UIManager.put("Tree.scrollsOnExpand", true);
        UIManager.put("Tree.scrollsHorizontallyAndVertically", true);
    }
    
    /**
     * Returns the class name of the look and feel from its display name.
     *
     * @param displayName Look and feel display name
     *
     * @return Look and feel class name or a zero length string
     */
    public static String getLookAndFeel(final String displayName) {
        final StringBuilder classNameBuilder = new StringBuilder();
        
        if (displayName != null && !displayName.isEmpty()) {
            for (LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
                if (laf.getName().equals(displayName)) {
                    classNameBuilder.setLength(0);
                    classNameBuilder.append(laf.getClassName());
                    break;
                }
            }
        }
        return classNameBuilder.toString();
    }
}
