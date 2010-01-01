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

package com.dmdirc.addons.ui_swing.actions;

import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.text.JTextComponent;

/**
 * Paste action that sanitises filenames
 */
public final class SanitisedFilenamePasteAction extends AbstractAction {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** Invalid filename characters. */
    private static final String INVALID_CHARS = "^[^\\w\\.\\s\\-\\#\\&\\_]";
    
    /**
     * Creates a new instance of SanitisedFilenamePasteAction.
     */
    public SanitisedFilenamePasteAction() {
        super("NoSpacesPasteAction");
    }
    
    /**
     * {@inheritDoc}
     *
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        final JTextComponent comp;
        
        if (e.getSource() instanceof JTextComponent) {
            comp = (JTextComponent) e.getSource();
        } else {
            return;
        }
        
        String clipboard = null;
        
        if (!Toolkit.getDefaultToolkit().getSystemClipboard().
                isDataFlavorAvailable(DataFlavor.stringFlavor)) {
            return;
        }
        
        try {
            //get the contents of the clipboard
            clipboard = (String) Toolkit.getDefaultToolkit().
                    getSystemClipboard().getData(DataFlavor.stringFlavor);
            //remove invalid letters
            clipboard = clipboard.replaceAll(INVALID_CHARS, "");
            
            //insert the contents at the current cursor position
        comp.replaceSelection(clipboard);
        } catch (IOException ex) {
            Logger.userError(ErrorLevel.LOW, "Unable to get clipboard contents: " + ex.getMessage());
        } catch (UnsupportedFlavorException ex) {
            Logger.appError(ErrorLevel.LOW, "Unable to get clipboard contents", ex);
        }
    }
    
}
