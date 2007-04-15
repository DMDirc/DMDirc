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

package uk.org.ownage.dmdirc.ui;

import java.awt.Component;
import java.awt.Container;

import javax.swing.Spring;
import javax.swing.SpringLayout;

/**
 * UI constants.
 */
public final class UIUtilities {
    
    /** Size of a large border. */
    public static final int LARGE_BORDER = 10;
    
    /** Size of a small border. */
    public static final int SMALL_BORDER = 5;
    
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
     * @param initialXPadding initial x padding
     * @param initialYPadding initial y padding
     * @param xPadding x padding
     * @param yPadding y padding
     */
    public static void layoutGrid(final Container parent, final int rows,
            final int columns, final int initialXPadding,
            final int initialYPadding, final int xPadding, final int yPadding) {
        final SpringLayout layout = (SpringLayout) parent.getLayout();
        
        Spring x = Spring.constant(initialXPadding);
        Spring y = Spring.constant(initialYPadding);
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
            x = Spring.sum(x, Spring.sum(width, Spring.constant(xPadding)));
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
            y = Spring.sum(y, Spring.sum(Spring.constant(height),
                    Spring.constant(yPadding)));
        }
        
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
}
