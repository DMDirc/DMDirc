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

import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.ArrayList;

/**
 * Arraylist transferable.
 */
public final class ArrayListTransferable implements Transferable {
    
    /** Local tranfer flavour. */
    private DataFlavor localArrayListFlavor;
    /** Serial transfer flavour. */
    private final DataFlavor serialArrayListFlavor;
    /** Transferred ArrayList. */
    private final ArrayList data; //NOPMD
    
    /** 
     * Initialises the ArrayListTransferable. 
     *
     * @param alist ArrayList to transfer
     */
    public ArrayListTransferable(final ArrayList alist) { //NOPMD
        super();
        
        data = alist;
        
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
    @Override
    public Object getTransferData(final DataFlavor flavor) throws 
            UnsupportedFlavorException {
        if (!isDataFlavorSupported(flavor)) {
            throw new UnsupportedFlavorException(flavor);
        }
        
        return data;
    }
    
    /** {@inheritDoc} */
    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{localArrayListFlavor, serialArrayListFlavor, };
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean isDataFlavorSupported(final DataFlavor flavor) {
        if (localArrayListFlavor.equals(flavor)) {
            return true;
        }
        
        if (serialArrayListFlavor.equals(flavor)) {
            return true;
        }
        
        return false;
    }
}
