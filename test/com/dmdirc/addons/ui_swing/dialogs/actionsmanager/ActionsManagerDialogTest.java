/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.ui_swing.dialogs.actionsmanager;

import com.dmdirc.Main;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.harness.ui.UIClassTestRunner;
import com.dmdirc.harness.ui.ClassFinder;
import com.dmdirc.harness.ui.UITestIface;

import com.dmdirc.addons.ui_swing.dialogs.StandardInputDialog;
import com.dmdirc.addons.ui_swing.dialogs.actioneditor.ActionEditorDialog;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.fest.swing.core.EventMode;
import org.fest.swing.core.matcher.JButtonByTextMatcher;
import org.fest.swing.finder.JOptionPaneFinder;
import org.fest.swing.finder.WindowFinder;
import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.JOptionPaneFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(UIClassTestRunner.class)
public class ActionsManagerDialogTest implements UITestIface {
    
    private DialogFixture window;

    @BeforeClass
    public static void setUpClass() {
        Main.setUI(new SwingController());
        IdentityManager.load();
        ActionManager.init();
    }
    
    @Before
    public void setUp() {
        removeGroups();
    }
    
    @After
    public void tearDown() throws InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                close();
            }
        });

        removeGroups();
    }

    protected void close() {
        if (window != null) {
            window.cleanUp();
        }
    }
    
    protected void removeGroups() {
        if (ActionManager.getGroups().containsKey("amd-ui-test1")) {
            ActionManager.removeGroup("amd-ui-test1");
        }
        
        if (ActionManager.getGroups().containsKey("amd-ui-test2")) {
            ActionManager.removeGroup("amd-ui-test2");
        }
    }
    
    @Test
    public void testAddGroup() throws InterruptedException {
        setupWindow();
        
        window.panel(new ClassFinder<JPanel>(JPanel.class, "Groups"))
                .button(JButtonByTextMatcher.withText("Add")).click();
        
        DialogFixture newwin = WindowFinder.findDialog(StandardInputDialog.class)
                .withTimeout(5000).using(window.robot);
                
        newwin.requireVisible();
        assertEquals("New action group", newwin.target.getTitle());
        
        newwin.button(JButtonByTextMatcher.withText("Cancel")).click();
        
        newwin.requireNotVisible();
        
        window.panel(new ClassFinder<JPanel>(JPanel.class, "Groups"))
                .button(JButtonByTextMatcher.withText("Add")).click();
        
        newwin = WindowFinder.findDialog(StandardInputDialog.class)
                .withTimeout(5000).using(window.robot);
        
        newwin.requireVisible();
        newwin.button(JButtonByTextMatcher.withText("OK")).requireDisabled();
        
        newwin.textBox(new ClassFinder<JTextComponent>(javax.swing.JTextField.class,
                null)).enterText("amd-ui-test1");
        
        newwin.button(JButtonByTextMatcher.withText("OK")).requireEnabled().click();
        
        window.list().requireSelectedItems("amd-ui-test1");

        assertTrue(ActionManager.getGroups().containsKey("amd-ui-test1"));
    }
    
    @Test
    public void testDeleteGroup() {
        ActionManager.makeGroup("amd-ui-test1");
        setupWindow();
               
        window.list().selectItem("amd-ui-test1").requireSelectedItems("amd-ui-test1");
        
        window.panel(new ClassFinder<JPanel>(JPanel.class, "Groups"))
                .button(JButtonByTextMatcher.withText("Delete")).requireEnabled().click();
        
        JOptionPaneFixture newwin = JOptionPaneFinder.findOptionPane()
                .withTimeout(5000).using(window.robot);
        newwin.buttonWithText("No").click();
        
        assertTrue(ActionManager.getGroups().containsKey("amd-ui-test1"));
        window.list().selectItem("amd-ui-test1").requireSelectedItems("amd-ui-test1");
        
        window.panel(new ClassFinder<JPanel>(JPanel.class, "Groups"))
                .button(JButtonByTextMatcher.withText("Delete")).click();
        
        newwin = JOptionPaneFinder.findOptionPane()
                .withTimeout(5000).using(window.robot);
        newwin.buttonWithText("Yes").click();
        
        assertTrue(window.list().selection().length != 1 ||   
                !window.list().selection()[0].equals("amd-ui-test1"));
                
        assertFalse(ActionManager.getGroups().containsKey("amd-ui-test1"));
    }
    
    @Test
    public void testEnablingGroupButtons() {
        ActionManager.makeGroup("amd-ui-test1");
        setupWindow();
        
        window.list().selectItem("performs").requireSelectedItems("performs");
        window.panel(new ClassFinder<JPanel>(JPanel.class, "Groups"))
                .button(JButtonByTextMatcher.withText("Delete")).requireDisabled();
        window.panel(new ClassFinder<JPanel>(JPanel.class, "Groups"))
                .button(JButtonByTextMatcher.withText("Edit")).requireDisabled();
        
        window.list().selectItem("amd-ui-test1").requireSelectedItems("amd-ui-test1");
        window.panel(new ClassFinder<JPanel>(JPanel.class, "Groups"))
                .button(JButtonByTextMatcher.withText("Delete")).requireEnabled();
        window.panel(new ClassFinder<JPanel>(JPanel.class, "Groups"))
                .button(JButtonByTextMatcher.withText("Edit")).requireEnabled();
    }

    @Test
    public void testAddAction() {
        ActionManager.makeGroup("amd-ui-test1");
        setupWindow();
        window.list().selectItem("amd-ui-test1").requireSelectedItems("amd-ui-test1");
        window.panel(new ClassFinder<JPanel>(ActionsGroupPanel.class, null))
                .button(JButtonByTextMatcher.withText("Add")).click();

        DialogFixture newwin = WindowFinder.findDialog(ActionEditorDialog.class)
                .withTimeout(5000).using(window.robot);
        newwin.requireVisible();
    }
    
    public void editGroupCheck(final String button) {
        ActionManager.makeGroup("amd-ui-test1");
        setupWindow();
        
        window.list().selectItem("amd-ui-test1").requireSelectedItems("amd-ui-test1");
        window.panel(new ClassFinder<JPanel>(JPanel.class, "Groups"))
                .button(JButtonByTextMatcher.withText("Edit")).requireEnabled().click();
        
        DialogFixture newwin = WindowFinder.findDialog(StandardInputDialog.class)
                .withTimeout(5000).using(window.robot);
                
        newwin.requireVisible();
        assertEquals("Edit action group", newwin.target.getTitle());
        
        assertEquals("amd-ui-test1", 
                newwin.textBox(new ClassFinder<JTextComponent>(javax.swing.JTextField.class,
                null)).target.getText());
        
        newwin.textBox(new ClassFinder<JTextComponent>(javax.swing.JTextField.class,
                null)).deleteText().enterText("amd-ui-test2");
        newwin.button(JButtonByTextMatcher.withText(button)).requireEnabled().click();
    }
    
    @Test
    public void testEditGroupCancel() {
        editGroupCheck("Cancel");
        assertTrue(ActionManager.getGroups().containsKey("amd-ui-test1"));
        assertFalse(ActionManager.getGroups().containsKey("amd-ui-test2"));
    }
    
    @Test
    public void testEditGroupOK() {
        editGroupCheck("OK");
        assertFalse(ActionManager.getGroups().containsKey("amd-ui-test1"));
        assertTrue(ActionManager.getGroups().containsKey("amd-ui-test2"));
    }
    
    protected void setupWindow() {
        window = new DialogFixture(ActionsManagerDialog.getActionsManagerDialog(null, null));
        window.robot.settings().eventMode(EventMode.AWT);
        window.show();
    }

}
