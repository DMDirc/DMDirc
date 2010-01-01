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

package com.dmdirc.addons.ui_swing.components.reorderablelist;


import com.dmdirc.addons.ui_swing.components.renderers.ReorderableJListCellRenderer;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

/**
 * Reorderable JList.
 */
public final class ReorderableJList extends JList implements DragSourceListener,
        DropTargetListener, DragGestureListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** Drag source. */
    private final DragSource dragSource;
    /** Drag target. */
    private final DropTarget dropTarget;
    /** Drop target. */
    private Object dropTargetCell;
    /** Dragged index. */
    private int draggedIndex = -1;
    /** Data flavor. */
    private DataFlavor dataFlavor;
    /** Below drop target. */
    private boolean belowTarget;
    
    /** Instantiate new ReorderableJList. */
    public ReorderableJList() {
        this(new DefaultListModel());
    }

    /**
     * Instantiate new ReorderableJList.
     *
     * @param model Model
     */
    public ReorderableJList(final DefaultListModel model) {
        super(model);
        
        setCellRenderer(new ReorderableJListCellRenderer(this));
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setTransferHandler(new ArrayListTransferHandler());
        
        dragSource = DragSource.getDefaultDragSource();
        dropTarget = new DropTarget(this, this);
        
        dragSource.createDefaultDragGestureRecognizer(this,
                DnDConstants.ACTION_MOVE, this);
        try {
            
            dataFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType
                    + ";class=java.util.ArrayList");
        } catch (ClassNotFoundException e) {
            Logger.userError(ErrorLevel.LOW, "unable to create data flavor: "
                    + e.getMessage());
            dataFlavor = null;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public DefaultListModel getModel() {
        return (DefaultListModel) super.getModel();
    }
    
    /** 
     * Sets the model for the list.
     *
     * @param model Model for the list
     */
    public void setModel(final DefaultListModel model) { //NOPMD stupid
        super.setModel(model);
    }
    
    /**
     * Returns the target drop item.
     *
     * @return Drop target cell
     */
    public Object getTargetCell() {
        return dropTargetCell;
    }
    
    /**
     * Returns whether the target is below the drop cell.
     *
     * @return if the target is above or below the point
     */
    public boolean getBelowTarget() {
        return belowTarget;
    }
    
    /** {@inheritDoc} */
    @Override
    public void dragEnter(final DragSourceDragEvent dsde) {
        //Ignore
    }
    
    /** {@inheritDoc} */
    @Override
    public void dragOver(final DragSourceDragEvent dsde) {
        //Ignore
    }
    
    /** {@inheritDoc} */
    @Override
    public void dropActionChanged(final DragSourceDragEvent dsde) {
        //Ignore
    }
    
    /** {@inheritDoc} */
    @Override
    public void dragExit(final DragSourceEvent dse) {
        //Ignore
    }
    
    /** {@inheritDoc} */
    @Override
    public void dragDropEnd(final DragSourceDropEvent dsde) {
        //clear drop variables and repaint
        dropTargetCell = null;
        draggedIndex = -1;
        repaint();
    }
    
    /** {@inheritDoc} */
    @Override
    public void dragEnter(final DropTargetDragEvent dtde) {
        //check whether to accept drag
        if (dtde.getSource() == dropTarget) {
            dtde.acceptDrag(DnDConstants.ACTION_MOVE);
        } else {
            dtde.rejectDrag();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void dragOver(final DropTargetDragEvent dtde) {
        //Reject drops on self
        if (dtde.getSource() != dropTarget) {
            dtde.rejectDrag();
        }
        
        //get location and index
        final Point dragPoint = dtde.getLocation();
        final int index = locationToIndex(dragPoint);
        
        //set drag variables and repaint
        if (index == -1) {
            dropTargetCell = null;
        } else {
            dropTargetCell = getModel().getElementAt(index);
            //check whether the drop point is after the last index
            final Rectangle bounds = getCellBounds(index, index);
            if (index == getModel().getSize() - 1 
                    && dragPoint.y > bounds.y + bounds.height) {
                belowTarget = true;
            } else {
                belowTarget = false;
            }
        }
        
        repaint();
    }
    
    /** {@inheritDoc} */
    @Override
    public void dropActionChanged(final DropTargetDragEvent dtde) {
        //Ignore
    }
    
    /** {@inheritDoc} */
    @Override
    public void dragExit(final DropTargetEvent dte) {
        //Ignore
    }
    
    /** {@inheritDoc} */
    @Override
    public void drop(final DropTargetDropEvent dtde) {
        //check source and reject
        if (dtde.getSource() != dropTarget) {
            dtde.rejectDrop();
            return;
        }
        //get object location and index
        final Point dropPoint = dtde.getLocation();
        int index = locationToIndex(dropPoint);
        if (belowTarget) {
            index++;
        }
        
        //reject invalid drops
        if ((index == -1) || (index == draggedIndex)) {
            dtde.rejectDrop();
            return;
        }
        
        //accept drop as a move
        dtde.acceptDrop(DnDConstants.ACTION_MOVE);
        
        //get dropped item
        Object dragged;
        try {
            dragged = dtde.getTransferable().getTransferData(dataFlavor);
        } catch (UnsupportedFlavorException e) {
            return;
        } catch (IOException e) {
            return;
        }
        
        //move items
        final boolean sourceBeforeTarget = draggedIndex < index;
        final DefaultListModel mod = getModel();
        final int newIndex = sourceBeforeTarget ? index - 1 : index;
        mod.remove(draggedIndex);
        for (Object item : (ArrayList) dragged) {
            mod.add(newIndex, item);
        }
        
        getSelectionModel().setSelectionInterval(newIndex, newIndex);
        
        //drop complete
        dtde.dropComplete(true);
    }
    
    /** {@inheritDoc} */
    @Override
    public void dragGestureRecognized(final DragGestureEvent dge) {
        //find the objects location and index
        final Point clickPoint = dge.getDragOrigin();
        final int index = locationToIndex(clickPoint);
        
        if (index == -1) {
            return;
        }
        
        //get the list object
        final Object target = getModel().getElementAt(index);
        //create the trasnferable object
        final ArrayList<Object> transferObject = new ArrayList<Object>();
        transferObject.add(target);
        final Transferable trans = new ArrayListTransferable(transferObject);
        //start drag
        draggedIndex = index;
        dragSource.startDrag(dge, Cursor.getDefaultCursor(), trans, this);
    }
}


