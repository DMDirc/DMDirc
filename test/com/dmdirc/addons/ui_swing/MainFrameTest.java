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

package com.dmdirc.addons.ui_swing;

import com.dmdirc.Main;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.harness.ui.UIClassTestRunner;
import com.dmdirc.harness.ui.UITestIface;

import com.dmdirc.addons.ui_swing.dialogs.FeedbackDialog;
import com.dmdirc.addons.ui_swing.dialogs.NewServerDialog;
import com.dmdirc.addons.ui_swing.dialogs.about.AboutDialog;
import com.dmdirc.addons.ui_swing.dialogs.actionsmanager.ActionsManagerDialog;
import com.dmdirc.addons.ui_swing.dialogs.aliases.AliasManagerDialog;
import com.dmdirc.addons.ui_swing.dialogs.prefs.SwingPreferencesDialog;
import com.dmdirc.addons.ui_swing.dialogs.profiles.ProfileManagerDialog;
import org.fest.swing.finder.WindowFinder;
import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(UIClassTestRunner.class)
public class MainFrameTest implements UITestIface {
    
    private FrameFixture window;

    @BeforeClass
    public static void setUpClass() {
        Main.setUI(new SwingController());
    }

    @Before
    public void setUp() {
        IdentityManager.load();
        window = new FrameFixture(SwingController.getMainFrame());
        window.show();
    }
    
    @After
    public void tearDown() {
        if (window != null) {
            window.cleanUp();
        }
    }
    
    @Test
    public void testNewServerDialog() {
        window.menuItemWithPath("Server", "New Server...").click();
        DialogFixture newwin = WindowFinder.findDialog(NewServerDialog.class)
                .withTimeout(5000).using(window.robot);
        newwin.requireVisible();
    }
    
    @Test
    public void testAboutDialog() {
        window.menuItemWithPath("Help", "About").click();
        DialogFixture newwin = WindowFinder.findDialog(AboutDialog.class)
                .withTimeout(5000).using(window.robot);
        newwin.requireVisible();
    }
    
    @Test
    public void testFeedbackDialog() {
        window.menuItemWithPath("Help", "Send Feedback").click();
        DialogFixture newwin = WindowFinder.findDialog(FeedbackDialog.class)
                .withTimeout(5000).using(window.robot);
        newwin.requireVisible();
    }
    
    @Test
    public void testPreferencesDialog() {
        window.menuItemWithPath("Settings", "Preferences").click();
        DialogFixture newwin = WindowFinder.findDialog(SwingPreferencesDialog.class)
                .withTimeout(5000).using(window.robot);
        newwin.requireVisible();
    }
    
    @Test
    public void testProfileManagerDialog() {
        window.menuItemWithPath("Settings", "Profile Manager").click();
        DialogFixture newwin = WindowFinder.findDialog(ProfileManagerDialog.class)
                .withTimeout(5000).using(window.robot);
        newwin.requireVisible();
    }
    
    @Test
    public void testActionsManagerDialog() {
        window.menuItemWithPath("Settings", "Actions Manager").click();
        DialogFixture newwin = WindowFinder.findDialog(ActionsManagerDialog.class)
                .withTimeout(5000).using(window.robot);
        newwin.requireVisible();
    }
    
    @Test
    public void testAliasManagerDialog() {
        window.menuItemWithPath("Settings", "Alias Manager").click();
        DialogFixture newwin = WindowFinder.findDialog(AliasManagerDialog.class)
                .withTimeout(5000).using(window.robot);
        newwin.requireVisible();
    }
    
    @Test
    public void testChannelServerSettings() {
        window.menuItemWithPath("Channel", "Channel Settings").requireDisabled();
    }
    
    @Test
    public void testServerServerSettings() {
        window.menuItemWithPath("Server", "Server settings").requireDisabled();
    }

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(MainFrameTest.class);
    }

}
