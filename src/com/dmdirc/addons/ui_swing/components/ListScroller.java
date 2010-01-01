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

package com.dmdirc.addons.ui_swing.components;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

/**
 * Utility class to provide mouse wheel scrolling to a JList.
 */
public class ListScroller implements MouseWheelListener {

    /** List to scroll. */
    private final ListModel model;
    /** List to scroll. */
    private final ListSelectionModel selectionModel;

    /**
     * Creates a new instance of ListScroller.
     *
     * @param list List to scroll over
     */
    public ListScroller(final JList list) {
        this(list.getModel(), list.getSelectionModel());

        list.addMouseWheelListener(this);
    }

    /**
     * Creates a new instance of ListScroller.
     *
     * @param model List model to scroll over
     * @param selectionModel List selection model to scroll over
     */
    public ListScroller(final ListModel model,
            final ListSelectionModel selectionModel) {
        this.model = model;
        this.selectionModel = selectionModel;
    }

    /**
     * {@inheritDoc}
     * 
     * @param e Moust wheel event
     */
    @Override
    public void mouseWheelMoved(final MouseWheelEvent e) {
        if (e.getWheelRotation() < 0) {
            changeFocus(true);
        } else {
            changeFocus(false);
        }
    }

    /**
     * Activates the node above or below the active node in the list.
     *
     * @param direction true = up, false = down.
     */
    public void changeFocus(final boolean direction) {
        int index;

        //are we going up or down?
        if (direction) {
            //up
            index = changeFocusUp(selectionModel.getMinSelectionIndex());
        } else {
            //down
            index = changeFocusDown(selectionModel.getMinSelectionIndex());
        }
        selectionModel.setSelectionInterval(index, index);
    }

    /**
     * Changes the list focus up.
     *
     * @param index Start index
     *
     * @return next index
     */
    private int changeFocusUp(final int index) {
        int nextIndex;

        if (index == 0 || index == -1) {
            nextIndex = model.getSize() - 1;
        } else {
            nextIndex = index - 1;
        }

        return nextIndex;
    }

    /**
     * Changes the list focus down.
     *
     * @param index Start index
     *
     * @return next index
     */
    private int changeFocusDown(final int index) {
        int nextIndex;

        if (index == model.getSize() - 1) {
            nextIndex = 0;
        } else {
            nextIndex = index + 1;
        }

        return nextIndex;
    }
}
