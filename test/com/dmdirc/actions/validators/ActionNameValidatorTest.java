/*
 * Copyright (c) 2006-2015 DMDirc Developers
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
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.dmdirc.actions.validators;

import com.dmdirc.actions.Action;
import com.dmdirc.actions.ActionGroup;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ActionNameValidatorTest {

    private ActionGroup getGroup() {
        final List<Action> actions = new ArrayList<>();
        final Action action1 = mock(Action.class);
        final Action action2 = mock(Action.class);
        final ActionGroup group = mock(ActionGroup.class);
        actions.add(action1);
        actions.add(action2);

        when(group.getActions()).thenReturn(actions);
        when(action1.getName()).thenReturn("test1");
        when(action2.getName()).thenReturn("test2");

        return group;
    }

    @Test
    public void testConflicting() {
        final ActionNameValidator instance = new ActionNameValidator(getGroup(), "blah");

        assertTrue(instance.validate("test1").isFailure());
    }

    @Test
    public void testRename() {
        final ActionNameValidator instance = new ActionNameValidator(getGroup(), "test1");

        assertFalse(instance.validate("test").isFailure());
    }

    @Test
    public void testNoChange() {
        final ActionNameValidator instance = new ActionNameValidator(getGroup(), "test");

        assertFalse(instance.validate("test").isFailure());
    }
}
