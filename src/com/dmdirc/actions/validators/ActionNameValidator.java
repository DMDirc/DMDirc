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
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.dmdirc.actions.validators;

import com.dmdirc.actions.Action;
import com.dmdirc.actions.ActionGroup;
import com.dmdirc.util.validators.ValidationResponse;
import com.dmdirc.util.validators.Validator;

import java.util.List;

/**
 * Validates action names.
 *
 * @since 0.6.3
 */
public class ActionNameValidator implements Validator<String> {

    /** Failure reason for duplicates. */
    private static final String FAILURE_EXISTS = "Name must not already exist.";
    /** Associated action group. */
    private final ActionGroup group;
    /** Original name. */
    private final String originalName;

    /**
     * Instantiates a new action name validator for an existing action.
     *
     * @param group        Associated action group
     * @param originalName Existing action name
     */
    public ActionNameValidator(final ActionGroup group,
            final String originalName) {
        this.group = group;
        this.originalName = originalName;
    }

    @Override
    public ValidationResponse validate(final String object) {
        final List<Action> actions = group.getActions();
        for (Action action : actions) {
            if (action.getName().equals(object) && !action.getName().equals(originalName)) {
                return new ValidationResponse(FAILURE_EXISTS);
            }
        }
        return new ValidationResponse();
    }

}
