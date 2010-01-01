/*
 * 
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

package com.dmdirc.addons.ui_swing.dialogs;

import com.dmdirc.addons.ui_swing.UIUtilities;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;

import javax.swing.JButton;

/**
 * Dialog key listener.
 */
public class DialogKeyListener implements KeyEventDispatcher {

    /** {@inheritDoc} */
    @Override
    public boolean dispatchKeyEvent(final KeyEvent e) {
        if (KeyboardFocusManager.getCurrentKeyboardFocusManager().
                getFocusedWindow() instanceof StandardDialog) {
            final StandardDialog dialog = (StandardDialog) KeyboardFocusManager.
                    getCurrentKeyboardFocusManager().getFocusedWindow();
            if (e.getKeyCode() == KeyEvent.VK_ENTER && e.getModifiers() == UIUtilities.
                    getCtrlMask()) {
                dialog.ctrlEnterPressed();
            }
            if (e.getKeyCode() == KeyEvent.VK_ENTER && e.getModifiers() != UIUtilities.
                    getCtrlMask()) {
                if (dialog.getFocusOwner() instanceof JButton) {
                    dialog.executeAction(((JButton) dialog.getFocusOwner()));
                } else {
                    return dialog.enterPressed();
                }
            }
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                return dialog.escapePressed();
            }
        }
        return false;
    }
}
