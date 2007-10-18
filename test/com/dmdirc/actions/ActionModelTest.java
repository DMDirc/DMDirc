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

package com.dmdirc.actions;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class ActionModelTest {

    /*@Test
    public void trigger() {
    }*/

    @Test
    public void testConditions() {
        final ActionModel model = new ActionModel("group", "name");
        
        assertTrue(model.getConditions().isEmpty());
        
        final List<ActionCondition> conds = new ArrayList<ActionCondition>();
        
        model.setConditions(conds);

        assertEquals(conds, model.getConditions());
    }

    @Test
    public void testTriggers() {
        final ActionModel model = new ActionModel("group", "name");

        assertNull(model.getTriggers());

        final ActionType[] triggers = {CoreActionType.CHANNEL_ACTION};

        model.setTriggers(triggers);

        assertEquals(triggers, model.getTriggers());
    }

    @Test
    public void testNewFormat() {
        final ActionModel model = new ActionModel("group", "name");

        assertNull(model.getNewFormat());

        model.setNewFormat("");

        assertEquals("", model.getNewFormat());

        model.setNewFormat("newformat");

        assertEquals("newformat", model.getNewFormat());
    }

    @Test
    public void testResponse() {
        final ActionModel model = new ActionModel("group", "name");

        assertNull(model.getResponse());

        final String[] newResponse = {"a", "b", "c"};

        model.setResponse(newResponse);

        assertEquals(newResponse, model.getResponse());
    }

    @Test
    public void testGroup() {
        final ActionModel model = new ActionModel("group", "name");

        assertEquals("group", model.getGroup());

        model.setGroup("group2");

        assertEquals("group2", model.getGroup());
    }

    @Test
    public void testName() {
        final ActionModel model = new ActionModel("group", "name");

        assertEquals("name", model.getName());

        model.setName("name2");

        assertEquals("name2", model.getName());
    }
}