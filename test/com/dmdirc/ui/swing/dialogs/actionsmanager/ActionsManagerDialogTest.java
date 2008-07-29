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

package com.dmdirc.ui.swing.dialogs.actionsmanager;

import com.dmdirc.actions.ActionManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.harness.ui.ButtonTextFinder;
import com.dmdirc.harness.ui.ClassFinder;
import com.dmdirc.ui.swing.components.StandardInputDialog;

import javax.swing.JPanel;
import javax.swing.text.JTextComponent;
import org.fest.swing.core.matcher.JLabelByTextMatcher;
import org.fest.swing.finder.WindowFinder;
import org.fest.swing.fixture.DialogFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ActionsManagerDialogTest {
    
    private DialogFixture window;
    
    @Before
    public void setUp() {
        IdentityManager.load();
        ActionManager.loadActions();
        
        if (ActionManager.getGroups().containsKey("amd-ui-test1")) {
            ActionManager.removeGroup("amd-ui-test1");
        }
        
        window = new DialogFixture(ActionsManagerDialog.getActionsManagerDialog());
        window.show();
    }
    
    @After
    public void tearDown() {
        if (window != null) {
            window.cleanUp();
        }
        
        if (ActionManager.getGroups().containsKey("amd-ui-test1")) {
            ActionManager.removeGroup("amd-ui-test1");
        }
    }
    
    @Test
    public void testAddGroup() {
        window.panel(new ClassFinder<JPanel>(JPanel.class, null))
                .button(new ButtonTextFinder("Add")).click();
        
        DialogFixture newwin = WindowFinder.findDialog(StandardInputDialog.class)
                .withTimeout(5000).using(window.robot);
        newwin.requireVisible();
        assertEquals("New action group", newwin.target.getTitle());
        
        newwin.button(new ButtonTextFinder("Cancel")).click();
        newwin.requireNotVisible();
        
        window.panel(new ClassFinder<JPanel>(JPanel.class, null))
                .button(new ButtonTextFinder("Add")).click();
        
        newwin = WindowFinder.findDialog(StandardInputDialog.class)
                .withTimeout(5000).using(window.robot);
        newwin.requireVisible();
        assertEquals("New action group", newwin.target.getTitle());
        newwin.button(new ButtonTextFinder("OK")).requireDisabled();
        
        newwin.textBox(new ClassFinder<JTextComponent>(javax.swing.JTextField.class, null))
                .enterText("amd-ui-test1");

        System.out.println(newwin.textBox(
                new ClassFinder<JTextComponent>(javax.swing.JTextField.class, null))
                .target.getText());
        System.out.println(newwin.label(JLabelByTextMatcher.withText(null))
                .target.getToolTipText());
        
        newwin.button(new ButtonTextFinder("OK")).requireEnabled().click();
        
        // Ensure it's added
        window.list().selectItem("amd-ui-test1").requireSelectedItems("amd-ui-test1");
    }

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(ActionsManagerDialogTest.class);
    }

}
