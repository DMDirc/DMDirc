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

package com.dmdirc.addons.ui_swing.components.frames;

import com.dmdirc.addons.ui_swing.components.*;
import com.dmdirc.Main;
import com.dmdirc.commandparser.parsers.GlobalCommandParser;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.InvalidIdentityFileException;
import com.dmdirc.harness.TestConfigManagerMap;
import com.dmdirc.harness.TestWritableFrameContainer;
import com.dmdirc.harness.ui.ClassFinder;
import com.dmdirc.harness.ui.UIClassTestRunner;
import com.dmdirc.harness.ui.UITestIface;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.plugins.PluginManager;

import java.awt.event.KeyEvent;

import org.fest.swing.core.EventMode;
import org.fest.swing.core.KeyPressInfo;
import org.fest.swing.core.matcher.DialogByTitleMatcher;
import org.fest.swing.core.matcher.JButtonByTextMatcher;
import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.JInternalFrameFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(UIClassTestRunner.class)
public class InputTextFrameTest implements UITestIface {

    static FrameFixture mainframe;
    static JInternalFrameFixture window;
    static TestConfigManagerMap cmmap;
    static TestWritableFrameContainer owner;
    static SwingController controller;

    @BeforeClass
    public static void setUpClass() throws InvalidIdentityFileException {
        IdentityManager.load();
        controller = new SwingController();
        controller.onLoad();
        Main.setUI(controller);
        Main.ensureExists(PluginManager.getPluginManager(), "tabcompletion");
    }

    @Before
    public void setUp() {
        cmmap = new TestConfigManagerMap();
        cmmap.settings.put("ui.pasteProtectionLimit", "1");

        if (window == null) {
            owner = new TestWritableFrameContainer(512, cmmap);

            setupWindow(cmmap);
        }
    }

    @After
    public void tearDown() {
        // ??
    }

    @Test
    public void testPasteDialogContents() throws InterruptedException {
        ((InputTextFrame) window.target).doPaste("line1\nline2");

        final DialogFixture dlg = mainframe.dialog(DialogByTitleMatcher
                .withTitleAndShowing("Multi-line paste"));

        dlg.requireVisible().button(JButtonByTextMatcher.withText("Edit")).click();
        dlg.textBox(new ClassFinder<TextAreaInputField>(TextAreaInputField.class, null))
                .requireText("line1\nline2");
        dlg.target.dispose();
    }

    @Test @Ignore
    public void testPasteDialogWithTextBefore() throws InterruptedException {
        window.textBox().enterText("testing:");
        ((InputTextFrame) window.target).doPaste("line1\nline2");

        final DialogFixture dlg = mainframe.dialog(DialogByTitleMatcher
                .withTitleAndShowing("Multi-line paste"));

        dlg.requireVisible().button(JButtonByTextMatcher.withText("Edit")).click();
        dlg.textBox(new ClassFinder<TextAreaInputField>(TextAreaInputField.class, null))
                .requireText("testing:line1\nline2");
        dlg.target.dispose();
    }

    @Test @Ignore
    public void testPasteDialogWithTextAfter() throws InterruptedException {
        window.textBox().enterText("<- testing").pressAndReleaseKey(
                KeyPressInfo.keyCode(KeyEvent.VK_HOME));
        ((InputTextFrame) window.target).doPaste("line1\nline2");

        final DialogFixture dlg = mainframe.dialog(DialogByTitleMatcher
                .withTitleAndShowing("Multi-line paste"));

        dlg.requireVisible().button(JButtonByTextMatcher.withText("Edit")).click();
        dlg.textBox(new ClassFinder<TextAreaInputField>(TextAreaInputField.class, null))
                .requireText("line1\nline2<- testing");
        dlg.target.dispose();
    }

    @Test @Ignore
    public void testPasteDialogWithTextAround() throws InterruptedException {
        window.textBox().enterText("testing:<- testing").selectText(8, 8);
        ((InputTextFrame) window.target).doPaste("line1\nline2");

        final DialogFixture dlg = mainframe.dialog(DialogByTitleMatcher
                .withTitleAndShowing("Multi-line paste"));

        dlg.requireVisible().button(JButtonByTextMatcher.withText("Edit")).click();
        dlg.textBox(new ClassFinder<TextAreaInputField>(TextAreaInputField.class, null))
                .requireText("testing:line1\nline2<- testing");
        dlg.target.dispose();
    }

    protected void setupWindow(final ConfigManager configManager) {
        UIUtilities.initUISettings();

        mainframe = new FrameFixture(controller.getMainWindow());
        mainframe.robot.settings().eventMode(EventMode.AWT);

        final CustomInputFrame titf = new CustomInputFrame(owner,
                GlobalCommandParser.getGlobalCommandParser(), controller);

        titf.setTitle("testing123");

        owner.window = titf;

        WindowManager.addWindow(titf);

        titf.open();

        window = new JInternalFrameFixture(mainframe.robot, titf);
    }

}
