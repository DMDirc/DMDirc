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

package com.dmdirc.addons.ui_swing.dialogs.profiles;

import com.dmdirc.Main;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.reorderablelist.ReorderableJList;
import com.dmdirc.config.prefs.validator.FileNameValidator;
import com.dmdirc.config.prefs.validator.IdentValidator;
import com.dmdirc.config.prefs.validator.NotEmptyValidator;
import com.dmdirc.harness.ui.UIClassTestRunner;
import com.dmdirc.harness.ui.UITestIface;
import com.dmdirc.harness.ui.ClassFinder;
import com.dmdirc.harness.ui.ValidatingJTextFieldFinder;

import javax.swing.JList;

import javax.swing.JPanel;
import org.fest.swing.core.EventMode;
import org.fest.swing.fixture.DialogFixture;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(UIClassTestRunner.class)
public class ProfileManagerDialogTest implements UITestIface {

    private DialogFixture window;
    private Profile profile;

    @BeforeClass
    public static void setUpClass() {
        Main.setUI(new SwingController());
    }

    @Before
    public void setUp() {
        UIUtilities.initUISettings();
        profile = new Profile("unit-test1", "nick1", "real name", "ident");
        profile.save();
    }

    @After
    public void tearDown() {
        if (window != null) {
            window.cleanUp();
        }

        profile.delete();
    }

    @Test
    public void testDetails() {
        setupWindow();

        window.list(new ClassFinder<JList>(JList.class, null))
                .selectItem("unit-test1").requireSelection("unit-test1");
        window.textBox(new ValidatingJTextFieldFinder(FileNameValidator.class))
                .requireText("unit-test1"); // Profile name
        window.textBox(new ValidatingJTextFieldFinder(NotEmptyValidator.class))
                .requireText("real name"); // Real name
        window.textBox(new ValidatingJTextFieldFinder(IdentValidator.class))
                .requireText("ident"); // Ident

        assertEquals(1,
                window.list(new ClassFinder<ReorderableJList>(ReorderableJList.class, null))
                .selectItem("nick1").requireSelection("nick1").contents().length);
    }

    protected void setupWindow() {
        window = new DialogFixture(ProfileManagerDialog.getProfileManagerDialog(null));

        window.robot.settings().eventMode(EventMode.AWT);
        window.show();
    }

}
