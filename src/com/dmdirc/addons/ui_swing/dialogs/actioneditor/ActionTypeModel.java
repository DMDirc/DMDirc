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

package com.dmdirc.addons.ui_swing.dialogs.actioneditor;

import com.dmdirc.actions.ActionTypeComparator;
import com.dmdirc.actions.interfaces.ActionType;
import com.dmdirc.util.MapList;

import java.awt.FontMetrics;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingUtilities;

/**
 * Model for the "trigger" list of the actions editor. Adds type group headers,
 * and ensures they can't be selected.
 */
public final class ActionTypeModel extends DefaultComboBoxModel {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;    
    /** Max Width. */
    private int maxWidth = -1;
    /** Font metrics. */
    private FontMetrics fm;
    
    /**
     * Creates a new instance of ActionTypeModel.
     * 
     * @param fm Font metrics
     * @param typeGroups The action type groups to use
     */
    public ActionTypeModel(final FontMetrics fm, final MapList<String, ActionType> typeGroups) {
        super();
        
        this.fm = fm;
        
        for (Map.Entry<String, List<ActionType>> entry : typeGroups.entrySet()) {
            addElement(entry.getKey());
            
            final List<ActionType> types = entry.getValue();
            Collections.sort(types, new ActionTypeComparator());
            
            for (ActionType type : types) {
                addElement(type);
            }
        }
    }

    /** 
     * {@inheritDoc}
     * 
     * @param anObject Objerct to add
     */
    @Override
    public void addElement(final Object anObject) {
        super.addElement(anObject);
        int width = maxWidth;
        if (anObject instanceof String) {
            width = SwingUtilities.computeStringWidth(fm, (String) anObject);
        } else if (anObject instanceof ActionType) {
            width = SwingUtilities.computeStringWidth(fm, ((ActionType) anObject).getName());
        }
        maxWidth = Math.max(width, maxWidth);
    }

    /** {@inheritDoc} */
    @Override
    public void setSelectedItem(final Object anObject) {
        if (!(anObject instanceof String)) {
            super.setSelectedItem(anObject);
        }
    }
    
    /**
     * Sets the type group for this model.
     * 
     * @param typeGroup New type group
     */
    public void setTypeGroup(final MapList<String, ActionType> typeGroup) {
        removeAllElements();
        
        for (Map.Entry<String, List<ActionType>> entry : typeGroup.entrySet()) {
            addElement(entry.getKey());
            
            final List<ActionType> types = entry.getValue();
            Collections.sort(types, new ActionTypeComparator());
            
            for (ActionType type : types) {
                addElement(type);
            }
        }
    }
    
    /**
     * Returns the maximum width of a string in this model.
     * 
     * @return String max width
     */
    public int getMaxWidth() {
        return maxWidth;
    }
}
