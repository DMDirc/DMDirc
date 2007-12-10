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

package com.dmdirc.ui.swing;

import com.dmdirc.ui.swing.actions.CopyAction;
import com.dmdirc.ui.swing.actions.CutAction;
import com.dmdirc.ui.swing.actions.PasteAction;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

/**
 * Custom event queue to add commong functionality to certain components.
 */
public final class DMDircEventQueue extends EventQueue {
    
    /** Instantiates the DMDircEventQueue. */
    public DMDircEventQueue() {
        super();
    }
    
    /** {@inheritDoc} */
    @Override
    protected void dispatchEvent(final AWTEvent event) {
        super.dispatchEvent(event);
        
        if (!(event instanceof MouseEvent)) {
            return;
        }
        
        final MouseEvent me = (MouseEvent) event;
        
        if (!me.isPopupTrigger()) {
            return;
        }
        
        if (me.getComponent() == null) {
            return;
        }
        
        final Component comp = SwingUtilities.getDeepestComponentAt(
                me.getComponent(), me.getX(), me.getY());
        
        if (!(comp instanceof JTextComponent)) {
            return;
        }
        
        if (MenuSelectionManager.defaultManager().getSelectedPath().length > 0) {
            return;
        }
        
        final JTextComponent tc = (JTextComponent) comp;
        final JPopupMenu menu = new JPopupMenu();
        menu.add(new CutAction(tc));
        menu.add(new CopyAction(tc));
        menu.add(new PasteAction(tc));
        
        final Point pt = SwingUtilities.convertPoint(me.getComponent(), me.getPoint(), tc);
        menu.show(tc, pt.x, pt.y);
    }
}
