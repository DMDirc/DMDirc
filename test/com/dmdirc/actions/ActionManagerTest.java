/*
 * Copyright (c) 2006-2011 DMDirc Developers
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

import com.dmdirc.interfaces.actions.ActionType;
import com.dmdirc.config.IdentityManager;
import java.io.File;

import java.util.ArrayList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class ActionManagerTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        IdentityManager.load();
        ActionManager.getActionManager().initialise();
        ActionManager.getActionManager().loadUserActions();

        tearDownClass();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        if (ActionManager.getActionManager().getGroupsMap().containsKey("unit-test")) {
            ActionManager.getActionManager().deleteGroup("unit-test");
        }

        if (ActionManager.getActionManager().getGroupsMap().containsKey("unit-test-two")) {
            ActionManager.getActionManager().deleteGroup("unit-test-two");
        }
    }

    @Test
    public void testMakeGroup() {
        ActionManager.getActionManager().createGroup("unit-test");

        assertTrue("makeGroup must create the group directory",
                new File(ActionManager.getDirectory() + "unit-test").isDirectory());
    }

    @Test
    public void testGetGroup() {
        final ActionGroup group = ActionManager.getActionManager().getOrCreateGroup("unit-test");

        assertEquals("getGroup must return an ActionGroup with the right name",
                "unit-test", group.getName());
    }

    @Test
    public void testRenameGroup() {
        ActionManager.getActionManager().changeGroupName("unit-test", "unit-test-two");
        assertFalse("renameGroup must unlink the old directory",
                new File(ActionManager.getDirectory() + "unit-test").isDirectory());
        assertTrue("renameGroup must create the target directory",
                new File(ActionManager.getDirectory() + "unit-test-two").isDirectory());
    }

    @Test
    public void testRenameGroupWithAction() {
        final Action action = new Action("unit-test-two", "test1", new ActionType[0],
                new String[0], new ArrayList<ActionCondition>(), null);
        assertSame("Creating a new action must add it to the correct group",
                action, ActionManager.getActionManager().getOrCreateGroup("unit-test-two").get(0));

        ActionManager.getActionManager().changeGroupName("unit-test-two", "unit-test");
        assertFalse("renameGroup must unlink the old directory",
                new File(ActionManager.getDirectory() + "unit-test-two").isDirectory());
        assertTrue("renameGroup must create the target directory",
                new File(ActionManager.getDirectory() + "unit-test").isDirectory());
        assertSame("renameGroup must move actions to new group",
                action, ActionManager.getActionManager().getOrCreateGroup("unit-test").get(0));
        assertEquals("renameGroup must remove actions from old group",
                0, ActionManager.getActionManager().getOrCreateGroup("unit-test-two").size());
    }

    @Test
    public void testRemoveGroup() {
        ActionManager.getActionManager().deleteGroup("unit-test");
        assertFalse("removeGroup must unlink directory",
                new File(ActionManager.getDirectory() + "unit-test").isDirectory());

        ActionManager.getActionManager().saveAllActions();
        assertFalse("saveActions must not restore removed groups",
                new File(ActionManager.getDirectory() + "unit-test").isDirectory());
    }

}