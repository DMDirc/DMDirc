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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class ActionModelTest {

    @BeforeClass
    public static void setUpClass() {
        IdentityManager.load();
    }

    @Test
    public void testConditions() {
        final ActionModel model = new ActionModel("group", "name");

        assertTrue("ActionModel must start with no conditions",
                model.getConditions().isEmpty());

        final List<ActionCondition> conds = new ArrayList<ActionCondition>();

        model.setConditions(conds);

        assertEquals("setConditions must set conditions",
                conds, model.getConditions());
    }

    @Test
    public void testTriggers() {
        final ActionModel model = new ActionModel("group", "name");

        assertNull("ActionModel must start with null triggers",
                model.getTriggers());

        final ActionType[] triggers = {CoreActionType.CHANNEL_ACTION};

        model.setTriggers(triggers);

        assertEquals("setTriggers must set triggers",
                Arrays.asList(triggers), Arrays.asList(model.getTriggers()));
    }

    @Test
    public void testNewFormat() {
        final ActionModel model = new ActionModel("group", "name");

        assertNull("ActionModel must start with null format",
                model.getNewFormat());

        model.setNewFormat("");

        assertEquals("setNewFormat must set format (empty string)",
                "", model.getNewFormat());

        model.setNewFormat("newformat");

        assertEquals("setNewFormat must set format (non-empty string)",
                "newformat", model.getNewFormat());
    }

    @Test
    public void testResponse() {
        final ActionModel model = new ActionModel("group", "name");

        assertNull("ActionModel must start with null response",
                model.getResponse());

        final String[] newResponse = {"a", "b", "c"};

        model.setResponse(newResponse);

        assertEquals("setResponse must set response",
                Arrays.asList(newResponse), Arrays.asList(model.getResponse()));
    }

    @Test
    public void testGroup() {
        final ActionModel model = new ActionModel("group", "name");

        assertEquals("ActionModel constructor must set group",
                "group", model.getGroup());

        model.setGroup("group2");

        assertEquals("setGroup must set group",
                "group2", model.getGroup());
    }

    @Test
    public void testName() {
        final ActionModel model = new ActionModel("group", "name");

        assertEquals("ActionModel constructor must set name",
                "name", model.getName());

        model.setName("name2");

        assertEquals("setName must set name",
                "name2", model.getName());
    }

    @Test
    public void testTest() {
        final ActionModel model = new ActionModel("group", "name",
                new ActionType[]{CoreActionType.CHANNEL_ACTION},
                new String[0], Arrays.asList(new ActionCondition[]{
                    new ActionCondition(2, CoreActionComponent.STRING_STRING,
                            CoreActionComparison.STRING_REGEX, ".*e{5}.*"),
                    new ActionCondition(2, CoreActionComponent.STRING_STRING,
                            CoreActionComparison.STRING_STARTSWITH, "abc"),
                }), ConditionTree.parseString("0|1"), null);
        final ActionSubstitutor sub = new ActionSubstitutor(CoreActionType.CHANNEL_ACTION);

        assertTrue("test must pass if one condition in disjunction passes (cond 1)",
                model.test(sub, null, null, "abcdef"));
        assertTrue("test must pass if one condition in disjunction passes (cond 2)",
                model.test(sub, null, null, "bcdeeeeeeeeef"));
        assertFalse("test must fail if all conditions fail",
                model.test(sub, null, null, "abeeef"));
    }
    
    @Test
    public void testTestNoCondTree() {
        final ActionModel model = new ActionModel("group", "name",
                new ActionType[]{CoreActionType.CHANNEL_ACTION},
                new String[0], Arrays.asList(new ActionCondition[]{
                    new ActionCondition(2, CoreActionComponent.STRING_STRING,
                            CoreActionComparison.STRING_REGEX, ".*e{5}.*"),
                    new ActionCondition(2, CoreActionComponent.STRING_STRING,
                            CoreActionComparison.STRING_STARTSWITH, "abc"),
                }), null, null);
        final ActionSubstitutor sub = new ActionSubstitutor(CoreActionType.CHANNEL_ACTION);

        assertFalse("test must fail if one condition in conjunction fails (cond 1)",
                model.test(sub, null, null, "abcdef"));
        assertFalse("test must fail if one condition in conjunction fails (cond 2)",
                model.test(sub, null, null, "abdeeeeeeeeef"));
        assertTrue("test must pass if both conditions in conjunction pass",
                model.test(sub, null, null, "abcdeeeeeeeeef"));
    }
}