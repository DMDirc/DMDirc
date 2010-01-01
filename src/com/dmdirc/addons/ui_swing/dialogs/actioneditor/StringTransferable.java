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

import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

/**
 * String transfer handler.
 */
public class StringTransferable implements Transferable {

    /** Local tranfer flavour. */
    private DataFlavor localStringFlavor;
    /** Serial transfer flavour. */
    private final DataFlavor serialStringFlavor;
    /** Transferred string. */
    private final String data;

    /**
     * Creates a new string transferable object.
     * 
     * @param data String to transger
     */
    public StringTransferable(final String data) {
        super();

        this.data = data;

        try {
            localStringFlavor = new DataFlavor(
                    DataFlavor.javaJVMLocalObjectMimeType +
                    ";class=java.lang.String");
        } catch (ClassNotFoundException e) {
            Logger.userError(ErrorLevel.LOW, "unable to create data flavor: " +
                    e.getMessage());
        }
        serialStringFlavor = new DataFlavor(String.class, "String");
    }

    /** {@inheritDoc} */
    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{localStringFlavor, serialStringFlavor,};
    }

    /** {@inheritDoc} */
    @Override
    public boolean isDataFlavorSupported(final DataFlavor flavor) {
        if (localStringFlavor.equals(flavor)) {
            return true;
        }

        if (serialStringFlavor.equals(flavor)) {
            return true;
        }

        return false;
    }

    /** 
     * {@inheritDoc}
     * 
     * @return String to transfer
     */
    @Override
    public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException {
        if (!isDataFlavorSupported(flavor)) {
            throw new UnsupportedFlavorException(flavor);
        }

        return data;
    }
}
