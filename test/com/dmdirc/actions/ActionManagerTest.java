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
        ActionManager.init();
        
        tearDownClass();
    }
    
    @AfterClass
    public static void tearDownClass() throws Exception {
        if (ActionManager.getGroups().containsKey("unit-test")) {
            ActionManager.removeGroup("unit-test");
        }
        
        if (ActionManager.getGroups().containsKey("unit-test-two")) {
            ActionManager.removeGroup("unit-test-two");
        }
    }

    @Test
    public void testMakeGroup() {
        ActionManager.makeGroup("unit-test");
        
        assertTrue("makeGroup must create the group directory",
                new File(ActionManager.getDirectory() + "unit-test").isDirectory());
    }
    
    @Test
    public void testGetGroup() {
        final ActionGroup group = ActionManager.getGroup("unit-test");
        
        assertEquals("getGroup must return an ActionGroup with the right name",
                "unit-test", group.getName());
    }
    
    @Test
    public void testRenameGroup() {
        ActionManager.renameGroup("unit-test", "unit-test-two");
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
                action, ActionManager.getGroup("unit-test-two").get(0));
        
        ActionManager.renameGroup("unit-test-two", "unit-test");
        assertFalse("renameGroup must unlink the old directory",
                new File(ActionManager.getDirectory() + "unit-test-two").isDirectory());
        assertTrue("renameGroup must create the target directory",
                new File(ActionManager.getDirectory() + "unit-test").isDirectory());        
        assertSame("renameGroup must move actions to new group",
                action, ActionManager.getGroup("unit-test").get(0));
        assertEquals("renameGroup must remove actions from old group",
                0, ActionManager.getGroup("unit-test-two").size());
    }
    
    @Test
    public void testRemoveGroup() {
        ActionManager.removeGroup("unit-test");
        assertFalse("removeGroup must unlink directory",
                new File(ActionManager.getDirectory() + "unit-test").isDirectory());
        
        ActionManager.saveActions();
        assertFalse("saveActions must not restore removed groups",
                new File(ActionManager.getDirectory() + "unit-test").isDirectory());
    }

}