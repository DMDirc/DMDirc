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

package com.dmdirc.config.prefs.validator;

import com.dmdirc.actions.Action;
import com.dmdirc.actions.ActionGroup;

import java.util.List;

/**
 * Validates action names.
 */
public class ActionNameValidator implements Validator<String> {

    /** Failure reason for duplicates. */
    private static final String FAILURE_EXISTS = "Name must not already exist.";
    /** Associated action group. */
    private ActionGroup group;

    /**
     * Instantiates a new action name validator.
     *
     * @param group Associated action group
     */
    public ActionNameValidator(final ActionGroup group) {
        this.group = group;
    }

    /** {@inheritDoc} */
    @Override
    public ValidationResponse validate(final String object) {
        final List<Action> actions = group.getActions();
        for (Action action : actions) {
            if (action.getName().equals(object)) {
                return new ValidationResponse(FAILURE_EXISTS);
            }
        }
        return new ValidationResponse();
    }
}
