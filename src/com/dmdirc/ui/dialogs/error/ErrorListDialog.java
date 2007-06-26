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

package com.dmdirc.ui.dialogs.error;

import com.dmdirc.logger.ProgramError;
import com.dmdirc.ui.MainFrame;
import com.dmdirc.ui.components.StandardDialog;
import com.dmdirc.ui.interfaces.ErrorManager;

/**
 * Error list dialog.
 */
public class ErrorListDialog extends StandardDialog implements ErrorManager {
    
    /** Previously instantiated instance of ErrorListDialog. */
    private static ErrorListDialog me;
    
    /** Creates a new instance of ErrorListDialog. */
    private ErrorListDialog() {
        super(MainFrame.getMainFrame(), false);
    }
    
    /**
     * Returns the instance of ErrorListDialog.
     *
     * @return Instance of ErrorListDialog
     */
    public static synchronized ErrorListDialog getErrorListDialog() {
        if (me == null) {
            me = new ErrorListDialog();
        }
        return me;
    }

    /** {@inheritDoc} */
    public void errorAdded(final ProgramError error) {
    }

    /** {@inheritDoc} */
    public void errorStatusChanged(final ProgramError error) {
    }
    
}
