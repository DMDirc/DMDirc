/*
 * Copyright (c) 2006-2012 DMDirc Developers
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
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.actions.ActionComparison;
import com.dmdirc.interfaces.actions.ActionType;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class AliasWrapperTest {

    private Action action;
    private ActionCondition condition;
    private List<ActionCondition> conditions;

    private AliasWrapper aliasWrapper;

    @Before
    public void setup() {
        final CommandController controller = mock(CommandController.class);
        when(controller.getCommandChar()).thenReturn('!');

        action = mock(Action.class);
        condition = mock(ActionCondition.class);
        conditions = new ArrayList<ActionCondition>();
        aliasWrapper = new AliasWrapper(controller);

        when(condition.getArg()).thenReturn(1);
        when(condition.getComparison()).thenReturn(CoreActionComparison.STRING_EQUALS);
        when(condition.getComponent()).thenReturn(CoreActionComponent.STRING_STRING);
        when(condition.getTarget()).thenReturn("name");
        when(action.getConditions()).thenReturn(conditions);
        when(action.getTriggers()).thenReturn(new ActionType[] { CoreActionType.UNKNOWN_COMMAND });
    }

    @Test
    public void testAddsAlias() {
        conditions.add(condition);

        aliasWrapper.add(action);
        assertFalse(aliasWrapper.getAliases().isEmpty());
    }

    @Test
    public void testDoesNotAddActionWithNoConditions() {
        aliasWrapper.add(action);
        assertTrue(aliasWrapper.getAliases().isEmpty());
    }

    @Test
    public void testDoesNotAddActionWithWrongTrigger() {
        when(action.getTriggers()).thenReturn(new ActionType[] { CoreActionType.ACTION_CREATED });

        aliasWrapper.add(action);
        assertTrue(aliasWrapper.getAliases().isEmpty());
    }

    @Test
    public void testGetsCommandName() {
        conditions.add(condition);
        assertEquals("!name", aliasWrapper.getCommandName(action));
    }

    @Test
    public void testGetsCommandNameWithMultipleConditions() {
        final ActionCondition otherCondition = mock(ActionCondition.class);
        when(otherCondition.getArg()).thenReturn(7);

        conditions.add(otherCondition);
        conditions.add(condition);
        assertEquals("!name", aliasWrapper.getCommandName(action));
    }

    @Test
    public void testGetCommandNameReturnsNullIfUnknown() {
        assertNull(aliasWrapper.getCommandName(action));
    }

}
