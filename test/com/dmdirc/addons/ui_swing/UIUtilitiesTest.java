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

package com.dmdirc.addons.ui_swing;


import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.actions.RedoAction;
import com.dmdirc.addons.ui_swing.actions.UndoAction;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.text.JTextComponent;

import org.junit.Test;
import static org.junit.Assert.*;

public class UIUtilitiesTest {
    
    @Test
    public void testAddUndoManager() {
        final JTextComponent comp = new JTextField();
        UIUtilities.addUndoManager(comp);
        //check actions
        assertTrue(comp.getActionMap().get("Undo") instanceof UndoAction);
        assertTrue(comp.getActionMap().get("Redo") instanceof RedoAction);
        //check key bindings
        assertEquals("Undo", comp.getInputMap().get(KeyStroke.getKeyStroke("control Z")));
        assertEquals("Redo", comp.getInputMap().get(KeyStroke.getKeyStroke("control Y")));
    }

    @Test
    public void testGetLookAndFeel() {
        final String sysLAF = UIManager.getSystemLookAndFeelClassName();
        //null look and feel name = system
        assertEquals(sysLAF, UIUtilities.getLookAndFeel(null));
        //blank look and feel name = system
        assertEquals(sysLAF, UIUtilities.getLookAndFeel(""));
        //incorrect look and feel name = system
        assertEquals(sysLAF, UIUtilities.getLookAndFeel("incorrectName"));
        //Look and feel name -> class name
        final LookAndFeelInfo[] LAFs = UIManager.getInstalledLookAndFeels();
        for (LookAndFeelInfo lookAndFeel : LAFs) {
            assertEquals(lookAndFeel.getClassName(), UIUtilities.getLookAndFeel(lookAndFeel.getName()));
        }
    }

}
