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
package com.dmdirc.actions;

import com.dmdirc.actions.interfaces.ActionType;

import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.prefs.PreferencesSetting;
import com.dmdirc.config.prefs.PreferencesType;
import com.dmdirc.util.ConfigFile;
import com.dmdirc.util.InvalidConfigFileException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class ActionTest {

    @BeforeClass
    public static void setUp() throws Exception {
        IdentityManager.load();
        ActionManager.init();
    }

    private static Action action;

    @Test
    public void testSave() {
        action = new Action("unit-test", "test1", new ActionType[0],
                new String[0], new ArrayList<ActionCondition>(), null);
        assertTrue("Action constructor must create new file",
                new File(ActionManager.getDirectory() + "unit-test"
                + File.separator + "test1").isFile());
    }

    @Test
    public void testSetGroup() {
        action.setGroup("unit-test-two");

        assertFalse("setGroup must remove old file",
                new File(ActionManager.getDirectory() + "unit-test"
                + File.separator + "test1").isFile());
        assertTrue("setGroup must create new file",
                new File(ActionManager.getDirectory() + "unit-test-two"
                + File.separator + "test1").isFile());
    }

    @Test
    public void testSetName() {
        action.setName("test2");

        assertFalse("setName must remove old file",
                new File(ActionManager.getDirectory() + "unit-test-two"
                + File.separator + "test1").isFile());
        assertTrue("setName must create new file",
                new File(ActionManager.getDirectory() + "unit-test-two"
                + File.separator + "test2").isFile());
    }

    @Test
    public void testDelete() {
        action.delete();

        assertFalse("delete must remove file",
                new File(ActionManager.getDirectory() + "unit-test-two"
                + File.separator + "test2").isFile());
    }

    @Test
    public void testRead() throws IOException, InvalidConfigFileException {
        final Action action = new Action("unit-test", "doesn't_exist");
        action.config = new ConfigFile(getClass().getResourceAsStream("action1"));
        action.config.read();
        action.loadActionFromConfig();

        assertTrue(Arrays.equals(action.getTriggers(),
                new ActionType[]{CoreActionType.SERVER_AWAY}));
        assertEquals("(0&1)", action.getConditionTree().toString());
        assertTrue(Arrays.equals(action.getResponse(), new String[]{"/away"}));
        assertEquals(new ActionCondition(1, CoreActionComponent.STRING_LENGTH,
                CoreActionComparison.INT_EQUALS, "0"), action.getConditions().get(0));
        assertEquals(new ActionCondition("foo", CoreActionComparison.STRING_CONTAINS,
                "bar"), action.getConditions().get(1));
    }

    @Test
    public void testMultipleGroups() throws IOException, InvalidConfigFileException {
        final Action action = new Action("unit-test", "doesn't_exist");
        action.config = new ConfigFile(getClass().getResourceAsStream("action_multisettings"));
        action.config.read();
        action.loadActionFromConfig();

        assertEquals(1, ActionManager.getGroup("unit-test").getSettings().size());

        final PreferencesSetting setting = ActionManager.getGroup("unit-test")
                .getSettings().values().iterator().next();
        assertEquals(PreferencesType.TEXT, setting.getType());
        assertEquals("Highlight Regex", setting.getTitle());
        assertEquals("Regex to use to detect a highlight", setting.getHelptext());
        assertEquals("(?i).*(shane|dataforce|Q${SERVER_MYNICKNAME}E|(?<![#A-Z])DF).*",
                setting.getValue());
    }

}