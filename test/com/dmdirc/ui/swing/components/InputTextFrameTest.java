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

package com.dmdirc.ui.swing.components;

import com.dmdirc.WritableFrameContainer;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.harness.TestConfigManagerMap;
import com.dmdirc.harness.TestWritableFrameContainer;
import com.dmdirc.harness.ui.TestInputTextFrame;
import com.dmdirc.harness.ui.UIClassTestRunner;
import com.dmdirc.harness.ui.UITestIface;

import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.swing.SwingController;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import org.fest.swing.core.EventMode;
import org.fest.swing.core.KeyPressInfo;
import org.fest.swing.core.matcher.DialogByTitleMatcher;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.JInternalFrameFixture;
import org.fest.swing.util.Platform;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(UIClassTestRunner.class)
public class InputTextFrameTest implements UITestIface {

    FrameFixture mainframe;
    JInternalFrameFixture window;
    TestConfigManagerMap cmmap;
    TestWritableFrameContainer owner;

    @Before
    public void setUp() {
        IdentityManager.load();

        cmmap = new TestConfigManagerMap();
        cmmap.settings.put("ui.pasteProtectionLimit", "1");

        owner = new TestWritableFrameContainer(512, cmmap);
    }

    @After
    public void tearDown() {
        if (window != null) {
            window.close();
        }

        if (mainframe != null) {
            mainframe.close();
        }
    }

    @Test
    public void testPasteDialogAppearing() throws InterruptedException {
        setupWindow(cmmap);

        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                new StringSelection("line1\nline2"), null);

        while (true) {
            Thread.yield();
            Thread.sleep(1000);
        }

        //window.textBox().pressAndReleaseKey(KeyPressInfo.keyCode(KeyEvent.VK_C)
        //        .modifiers(Platform.controlOrCommandMask()));
        //mainframe.dialog(DialogByTitleMatcher.withTitle("Multi-line paste")).
          //      requireVisible();
    }

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(InputTextFrameTest.class);
    }

    protected void setupWindow(final ConfigManager configManager) {
        mainframe = new FrameFixture(SwingController.getMainFrame());
        mainframe.robot.settings().eventMode(EventMode.AWT);

        final TestInputTextFrame titf = new TestInputTextFrame(owner);

        titf.setTitle("testing123");

        owner.window = titf;

        WindowManager.addWindow(titf);

        titf.open();
        titf.show();

        window = new JInternalFrameFixture(mainframe.robot, titf);
    }

}
