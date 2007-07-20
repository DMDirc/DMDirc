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

package com.dmdirc.ui.components.reorderablelist;

import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;

/**
 * Arraylist Transfer handler.
 */
public class ArrayListTransferHandler extends TransferHandler {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    private DataFlavor localArrayListFlavor;
    private final DataFlavor serialArrayListFlavor;
    private JList source = null;
    private int[] indices = null;
    private int addIndex = -1; //Location where items were added
    private int addCount = 0;  //Number of items added
    
    public ArrayListTransferHandler() {
        super();
        
        try {
            localArrayListFlavor = new DataFlavor(
                    DataFlavor.javaJVMLocalObjectMimeType
                    + ";class=java.util.ArrayList");
        } catch (ClassNotFoundException e) {
            Logger.userError(ErrorLevel.LOW, "unable to create data flavor: "
                    + e.getMessage());
        }
        serialArrayListFlavor = new DataFlavor(ArrayList.class, "ArrayList"); //NOPMD
    }
    
    /** {@inheritDoc} */
    public boolean importData(final JComponent c, final Transferable t) {
        JList target = null;
        ArrayList alist = null;
        
        if (!canImport(c, t.getTransferDataFlavors())) {
            return false;
        }
        
        try {
            target = (JList)c;
            if (hasLocalArrayListFlavor(t.getTransferDataFlavors())) {
                alist = (ArrayList)t.getTransferData(localArrayListFlavor);
            } else if (hasSerialArrayListFlavor(t.getTransferDataFlavors())) {
                alist = (ArrayList)t.getTransferData(serialArrayListFlavor);
            } else {
                return false;
            }
        } catch (UnsupportedFlavorException e) {
            Logger.userError(ErrorLevel.LOW, "Unsupported data flavor: "
                    + e.getMessage());
            return false;
        } catch (IOException e) {
            Logger.userError(ErrorLevel.LOW, "Unable to import data: "
                    + e.getMessage());
            return false;
        }
        
        int index = target.getSelectedIndex();
        
        if (source.equals(target) && indices != null && index >= indices[0] - 1
                && index <= indices[indices.length - 1]) {
            indices = null;
            return true;
        }
        
        final DefaultListModel listModel = (DefaultListModel)target.getModel();
        final int max = listModel.getSize();
        
        if (index < 0) {
            index = max;
        } else {
            index++;
            if (index > max) {
                index = max;
            }
            
        }
        addIndex = index;
        addCount = alist.size();
        
        for (int i=0; i < alist.size(); i++) {
            listModel.add(index++, alist.get(i));
        }
        
        return true;
    }
    
    /** {@inheritDoc} */
    protected void exportDone(final JComponent c, final Transferable data,
            final int action) {
        if ((action == MOVE) && (indices != null)) {
            final DefaultListModel model = (DefaultListModel)source.getModel();
            
            if (addCount > 0) {
                for (int i = 0; i < indices.length; i++) {
                    if (indices[i] > addIndex) {
                        indices[i] += addCount;
                    }
                }
            }
            for (int i = indices.length -1; i >= 0; i--) {
                model.remove(indices[i]);
            }
        }
        
        indices = null;
        addIndex = -1;
        addCount = 0;
    }
    
    /** {@inheritDoc} */
    private boolean hasLocalArrayListFlavor(final DataFlavor[] flavors) {
        if (localArrayListFlavor == null) {
            return false;
        }
        
        for (int i = 0; i < flavors.length; i++) {
            if (flavors[i].equals(localArrayListFlavor)) {
                return true;
            }
        }
        
        return false;
    }
    
    /** {@inheritDoc} */
    private boolean hasSerialArrayListFlavor(final DataFlavor[] flavors) {
        if (serialArrayListFlavor == null) {
            return false;
        }
        
        for (int i = 0; i < flavors.length; i++) {
            if (flavors[i].equals(serialArrayListFlavor)) {
                return true;
            }
        }
        
        return false;
    }
    
    /** {@inheritDoc} */
    public boolean canImport(final JComponent c, final DataFlavor[] flavors) {
        if (hasLocalArrayListFlavor(flavors))  {
            return true; 
        }
        
        if (hasSerialArrayListFlavor(flavors)) { 
            return true; 
        }
        
        return false;
    }
    
    /** {@inheritDoc} */
    protected Transferable createTransferable(final JComponent c) {
        if (c instanceof JList) {
            source = (JList) c;
            indices = source.getSelectedIndices();
            final Object[] values = source.getSelectedValues();
            
            if (values == null || values.length == 0) {
                return null;
            }
            
            final ArrayList<Object> alist = new ArrayList<Object>(values.length);
            
            for (int i = 0; i < values.length; i++) {
                final Object o = values[i];
                String str = o.toString();
                if (str == null) {
                    str = "";
                }
                alist.add(str);
            }
            
            return new ArrayListTransferable(alist);
        }
        
        return null;
    }
    
    /** {@inheritDoc} */
    public int getSourceActions(final JComponent c) {
        return COPY_OR_MOVE;
    }
}
