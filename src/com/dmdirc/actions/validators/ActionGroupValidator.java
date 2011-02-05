/*
 * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.actions.ActionManager;
import com.dmdirc.util.validators.ValidationResponse;
import com.dmdirc.util.validators.Validator;

/**
 * Validates action groups.
 */
public class ActionGroupValidator implements Validator<String> {

    /** Failure reason for duplicates. */
    private static final String FAILURE_EXISTS = "Must not already exist.";

    /** {@inheritDoc} */
    @Override
    public ValidationResponse validate(final String object) {
        if (ActionManager.getActionManager().getGroupsMap().containsKey(object)) {
            return new ValidationResponse(FAILURE_EXISTS);
        } else {
            return new ValidationResponse();
        }
    }
}
