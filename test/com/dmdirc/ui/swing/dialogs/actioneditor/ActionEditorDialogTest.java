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

package com.dmdirc.ui.swing.dialogs.actioneditor;

import com.dmdirc.actions.Action;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.harness.ui.ClassFinder;

import com.dmdirc.ui.swing.components.ImageButton;

import java.awt.Component;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.fest.swing.core.EventMode;
import org.fest.swing.core.matcher.JButtonByTextMatcher;
import org.fest.swing.core.matcher.JLabelByTextMatcher;
import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.JLabelFixture;
import org.fest.swing.fixture.JPanelFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ActionEditorDialogTest {

    private DialogFixture window;

    @Before
    public void setUp() {
        IdentityManager.load();
        ActionManager.init();

        if (!ActionManager.getGroups().containsKey("amd-ui-test1")) {
            ActionManager.makeGroup("amd-ui-test1");
        }
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
    public void testName() {
        setupWindow(null);

        window.panel(new ClassFinder<JPanel>(ActionNamePanel.class, null)).textBox()
                .requireEnabled().requireEditable().requireEmpty();
        window.button(JButtonByTextMatcher.withText("OK")).requireDisabled();
    }

    @Test
    public void testBasicTriggers() {
        setupWindow(null);

        window.panel(new ClassFinder<JPanel>(ActionNamePanel.class, null)).textBox()
                .enterText("test1");
        final JPanelFixture triggers = window.panel(
                new ClassFinder<JPanel>(ActionTriggersPanel.class, null));

        final int items = triggers.comboBox().target.getItemCount();
        triggers.comboBox().selectItem("Channel message received");
        triggers.button(JButtonByTextMatcher.withText("Add")).requireEnabled().click();

        final JLabelFixture label = triggers
                .label(JLabelByTextMatcher.withText("Channel message received"));
        label.requireVisible();

        assertTrue(items > triggers.comboBox().target.getItemCount());

        triggers.button(new ClassFinder<JButton>(ImageButton.class, null)).click();
        
        for (Component comp : triggers.panel(new ClassFinder<JPanel>(ActionTriggersListPanel.class,
                null)).target.getComponents()) {
            assertNotSame(label.target, comp);
        }

        assertEquals(items, triggers.comboBox().target.getItemCount());
    }

    protected void setupWindow(final Action action) {
        window = new DialogFixture(ActionEditorDialog.getActionEditorDialog(null,
                "amd-ui-test1", action));
        window.robot.settings().eventMode(EventMode.AWT);
        window.robot.settings().delayBetweenEvents(150);
        window.show();
    }

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(ActionEditorDialogTest.class);
    }

}
