/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.ui.swing.dialogs.actioneditor;

import java.awt.Cursor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

/**
 * Action substitution label.
 */
public class ActionSubstitutionLabel extends JLabel implements MouseListener,
        DragGestureListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    private DragSource dragSource;
    private ActionSubstitution substition;

    /** Instantiates the panel. */
    public ActionSubstitutionLabel(final ActionSubstitution substition) {
        super();

        this.substition = substition;

        initComponents();
        addListeners();
        layoutComponents();
    }

    /** Initialises the components. */
    private void initComponents() {
        dragSource = DragSource.getDefaultDragSource();
        dragSource.createDefaultDragGestureRecognizer(this,
                DnDConstants.ACTION_COPY, this);

        setText(substition.getValue());
    }

    /** Adds the listeners. */
    private void addListeners() {
        addMouseListener(this);
    }

    /** Lays out the components. */
    private void layoutComponents() {
    }

    /** {@inheritDoc} */
    @Override
    public void mouseClicked(final MouseEvent e) {
    //Ignore
    }

    /** {@inheritDoc} */
    @Override
    public void mousePressed(final MouseEvent e) {
    //Ignore
    }

    /** {@inheritDoc} */
    @Override
    public void mouseReleased(final MouseEvent e) {
    //Ignore
    }

    /** {@inheritDoc} */
    @Override
    public void mouseEntered(final MouseEvent e) {
        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
    }

    /** {@inheritDoc} */
    @Override
    public void mouseExited(final MouseEvent e) {
    //Ignore
    }

    /** {@inheritDoc} */
    @Override
    public void dragGestureRecognized(final DragGestureEvent dge) {
        dragSource.startDrag(dge, Cursor.getPredefinedCursor(Cursor.HAND_CURSOR),
                new StringTransferable(substition.toString()), null);
    }
}
