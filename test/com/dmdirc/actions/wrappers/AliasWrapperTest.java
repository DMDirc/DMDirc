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

package com.dmdirc.actions.wrappers;

import com.dmdirc.actions.Action;
import com.dmdirc.actions.ActionCondition;
import com.dmdirc.actions.CoreActionComparison;
import com.dmdirc.actions.CoreActionComponent;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.actions.interfaces.ActionType;
import com.dmdirc.config.IdentityManager;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class AliasWrapperTest {

    @BeforeClass
    public static void setupClass() {
        IdentityManager.load();
    }

    @Test
    public void testNormal() {
        final List<ActionCondition> conditions = new ArrayList<ActionCondition>();
        conditions.add(new ActionCondition(1, CoreActionComponent.STRING_STRING,
                CoreActionComparison.STRING_EQUALS, "testing123"));
        final Action action = new Action("unit-test", "foo",
                new ActionType[]{CoreActionType.UNKNOWN_COMMAND}, new String[0],
                conditions, null);

        assertTrue(AliasWrapper.getAliasWrapper().getAliases().isEmpty());
        AliasWrapper.getAliasWrapper().add(action);
        assertFalse(AliasWrapper.getAliasWrapper().getAliases().isEmpty());
        assertEquals("/testing123", AliasWrapper.getAliasWrapper().getAliases().get(0));
    }

    @Test
    public void testNoConditions() {
        final List<ActionCondition> conditions = new ArrayList<ActionCondition>();
        final Action action = new Action("unit-test", "foo",
                new ActionType[]{CoreActionType.UNKNOWN_COMMAND}, new String[0],
                conditions, null);
        
        final int size = AliasWrapper.getAliasWrapper().getAliases().size();
        AliasWrapper.getAliasWrapper().add(action);
        assertEquals(size, AliasWrapper.getAliasWrapper().getAliases().size());
    }

}
