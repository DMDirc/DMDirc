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

import com.dmdirc.actions.ActionCondition;
import com.dmdirc.actions.CoreActionComparison;

import javax.swing.table.DefaultTableCellRenderer;

/**
 * Renders an action condition in plain english.
 */
public final class ActionConditionCellRenderer extends DefaultTableCellRenderer {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    
    /** Creates a new instance of ActionConditionCellRenderer. */
    public ActionConditionCellRenderer() {
        super();
    }
    
    /** {@inheritDoc} */
    @Override
    public void setValue(final Object value) {
        if (value == null) {
            setText("Any");
            return;
        }
        
        final ActionCondition condition = (ActionCondition) value;
        
        if (condition.getComparison() == CoreActionComparison.INT_EQUALS 
                || condition.getComparison() == CoreActionComparison.INT_GREATER 
                || condition.getComparison() == CoreActionComparison.INT_LESS) {
            setText(condition.getComparison().getName() + " " + condition.getTarget());
        } else {
            setText(value.toString());
        }
    }
    
}
