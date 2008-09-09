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
import com.dmdirc.harness.ui.UIClassTestRunner;
import com.dmdirc.harness.ui.ClassFinder;

import com.dmdirc.harness.ui.UITestIface;
import com.dmdirc.ui.swing.JRadioButtonByTextMatcher;
import com.dmdirc.ui.swing.components.ImageButton;

import com.dmdirc.ui.swing.components.TextLabel;
import java.awt.Component;
import javax.swing.JButton;
import javax.swing.JPanel;

import javax.swing.JTextField;
import javax.swing.text.JTextComponent;
import org.fest.swing.core.EventMode;
import org.fest.swing.core.matcher.JButtonByTextMatcher;
import org.fest.swing.core.matcher.JLabelByTextMatcher;
import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.JLabelFixture;
import org.fest.swing.fixture.JPanelFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(UIClassTestRunner.class)
public class ActionEditorDialogTest implements UITestIface {

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
    public void testTriggerWithNoArgs() {
        setupWindow(null);

        window.panel(new ClassFinder<JPanel>(ActionNamePanel.class, null)).textBox()
                .enterText("test1");
        final JPanelFixture triggers = window.panel(
                new ClassFinder<JPanel>(ActionTriggersPanel.class, null));

        triggers.comboBox().selectItem("Client closed");
        triggers.button(JButtonByTextMatcher.withText("Add")).requireEnabled().click();

        window.panel(new ClassFinder<JPanel>(ActionConditionsPanel.class, null))
                .button(JButtonByTextMatcher.withText("Add")).requireDisabled();
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
        window.button(JButtonByTextMatcher.withText("OK")).requireEnabled();

        triggers.button(new ClassFinder<JButton>(ImageButton.class, null)).click();
        
        for (Component comp : triggers.panel(new ClassFinder<JPanel>(ActionTriggersListPanel.class,
                null)).target.getComponents()) {
            assertNotSame(label.target, comp);
        }

        assertEquals(items, triggers.comboBox().target.getItemCount());
        window.button(JButtonByTextMatcher.withText("OK")).requireDisabled();
    }

    @Test
    public void testBasicConditionTrees() {
        setupWindow(null);

        window.panel(new ClassFinder<JPanel>(ActionNamePanel.class, null)).textBox()
                .enterText("test1");
        final JPanelFixture triggers = window.panel(
                new ClassFinder<JPanel>(ActionTriggersPanel.class, null));

        triggers.comboBox().selectItem("Channel message received");
        triggers.button(JButtonByTextMatcher.withText("Add")).requireEnabled().click();

        window.radioButton(new JRadioButtonByTextMatcher("All of the conditions are true"))
                .requireEnabled().requireSelected();
        window.radioButton(new JRadioButtonByTextMatcher("At least one of the conditions is true"))
                .requireEnabled();
        window.radioButton(new JRadioButtonByTextMatcher("The conditions match a custom rule"))
                .requireEnabled();
        window.panel(new ClassFinder<JPanel>(ActionConditionsTreePanel.class, null))
                .textBox(new ClassFinder<JTextComponent>(JTextField.class, null))
                .requireDisabled();

        window.button(JButtonByTextMatcher.withText("OK")).requireEnabled();

        window.radioButton(new JRadioButtonByTextMatcher("The conditions match a custom rule"))
                .click().requireSelected();
        window.panel(new ClassFinder<JPanel>(ActionConditionsTreePanel.class, null))
                .textBox(new ClassFinder<JTextComponent>(JTextField.class, null))
                .requireEnabled().enterText("invalid");

        window.button(JButtonByTextMatcher.withText("OK")).requireDisabled();
    }

    @Test
    public void testConditionText() {
        setupWindow(null);

        window.panel(new ClassFinder<JPanel>(ActionNamePanel.class, null)).textBox()
                .enterText("test1");
        final JPanelFixture triggers = window.panel(
                new ClassFinder<JPanel>(ActionTriggersPanel.class, null));

        triggers.comboBox().selectItem("Channel message received");
        triggers.button(JButtonByTextMatcher.withText("Add")).requireEnabled().click();

        window.panel(new ClassFinder<JPanel>(ActionConditionsPanel.class, null))
                .button(JButtonByTextMatcher.withText("Add")).requireEnabled().click();

        assertEquals("",
                window.panel(new ClassFinder<JPanel>(ActionConditionDisplayPanel.class, null))
                .textBox(new ClassFinder<JTextComponent>(TextLabel.class, null))
                .target.getText());

        window.panel(new ClassFinder<JPanel>(ActionConditionEditorPanel.class, null))
                .comboBox("argument").selectItem("message");

        assertEquals("The message's  ...",
                window.panel(new ClassFinder<JPanel>(ActionConditionDisplayPanel.class, null))
                .textBox(new ClassFinder<JTextComponent>(TextLabel.class, null))
                .target.getText());

        window.panel(new ClassFinder<JPanel>(ActionConditionEditorPanel.class, null))
                .comboBox("component").selectItem("content");

        assertEquals("The message's content  ...",
                window.panel(new ClassFinder<JPanel>(ActionConditionDisplayPanel.class, null))
                .textBox(new ClassFinder<JTextComponent>(TextLabel.class, null))
                .target.getText());

        window.panel(new ClassFinder<JPanel>(ActionConditionEditorPanel.class, null))
                .comboBox("comparison").selectItem("contains");

        assertEquals("The message's content contains ''",
                window.panel(new ClassFinder<JPanel>(ActionConditionDisplayPanel.class, null))
                .textBox(new ClassFinder<JTextComponent>(TextLabel.class, null))
                .target.getText());

        window.panel(new ClassFinder<JPanel>(ActionConditionEditorPanel.class, null))
                .textBox().enterText("foo");

        assertEquals("The message's content contains 'foo'",
                window.panel(new ClassFinder<JPanel>(ActionConditionDisplayPanel.class, null))
                .textBox(new ClassFinder<JTextComponent>(TextLabel.class, null))
                .target.getText());
    }

    @Test
    public void testIllegalCondition() {
        setupWindow(null);

        window.panel(new ClassFinder<JPanel>(ActionNamePanel.class, null)).textBox()
                .enterText("test1");
        final JPanelFixture triggers = window.panel(
                new ClassFinder<JPanel>(ActionTriggersPanel.class, null));

        triggers.comboBox().selectItem("Channel message received");
        triggers.button(JButtonByTextMatcher.withText("Add")).requireEnabled().click();

        window.button(JButtonByTextMatcher.withText("OK")).requireEnabled();

        window.panel(new ClassFinder<JPanel>(ActionConditionsPanel.class, null))
                .button(JButtonByTextMatcher.withText("Add")).requireEnabled().click();

        window.panel(new ClassFinder<JPanel>(ActionConditionEditorPanel.class, null))
                .comboBox("argument").requireEnabled();
        window.panel(new ClassFinder<JPanel>(ActionConditionEditorPanel.class, null))
                .comboBox("component").requireDisabled();
        window.panel(new ClassFinder<JPanel>(ActionConditionEditorPanel.class, null))
                .comboBox("comparison").requireDisabled();
        window.panel(new ClassFinder<JPanel>(ActionConditionEditorPanel.class, null))
                .textBox().requireDisabled();
        window.button(JButtonByTextMatcher.withText("OK")).requireDisabled();

        window.panel(new ClassFinder<JPanel>(ActionConditionEditorPanel.class, null))
                .comboBox("argument").selectItem("message");
        window.panel(new ClassFinder<JPanel>(ActionConditionEditorPanel.class, null))
                .comboBox("component").requireEnabled();
        window.panel(new ClassFinder<JPanel>(ActionConditionEditorPanel.class, null))
                .comboBox("comparison").requireDisabled();
        window.panel(new ClassFinder<JPanel>(ActionConditionEditorPanel.class, null))
                .textBox().requireDisabled();
        window.button(JButtonByTextMatcher.withText("OK")).requireDisabled();

        window.panel(new ClassFinder<JPanel>(ActionConditionEditorPanel.class, null))
                .comboBox("component").selectItem("content");
        window.panel(new ClassFinder<JPanel>(ActionConditionEditorPanel.class, null))
                .comboBox("comparison").requireEnabled();
        window.panel(new ClassFinder<JPanel>(ActionConditionEditorPanel.class, null))
                .textBox().requireDisabled();
        window.button(JButtonByTextMatcher.withText("OK")).requireDisabled();

        window.panel(new ClassFinder<JPanel>(ActionConditionEditorPanel.class, null))
                .comboBox("comparison").selectItem("contains");
        window.panel(new ClassFinder<JPanel>(ActionConditionEditorPanel.class, null))
                .textBox().requireEnabled();
        window.button(JButtonByTextMatcher.withText("OK")).requireEnabled();
    }

    protected void setupWindow(final Action action) {
        window = new DialogFixture(ActionEditorDialog.getActionEditorDialog(null,
                "amd-ui-test1", action));
        window.robot.settings().eventMode(EventMode.AWT);
        window.show();
    }

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(ActionEditorDialogTest.class);
    }

}
