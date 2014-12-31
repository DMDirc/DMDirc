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

import com.dmdirc.actions.ConditionTree;
import com.dmdirc.util.validators.ValidationResponse;
import com.dmdirc.util.validators.Validator;

/**
 * Validates a condition tree.
 */
public class ConditionRuleValidator implements Validator<String> {

    /** The number of arguments to the tree. */
    private int args;

    /**
     * Creates a new ConditionRuleValidator.
     *
     * @param args The number of arguments allowed in the ConditionTree (i.e., the number of
     *             ActionConditions)
     */
    public ConditionRuleValidator(final int args) {
        this.args = args;
    }

    /**
     * Updates the number of arguments used to validate the condition tree.
     *
     * @param args New number of arguments
     */
    public void setArgs(final int args) {
        this.args = args;
    }

    @Override
    public ValidationResponse validate(final String object) {
        final ConditionTree tree = ConditionTree.parseString(object);

        if (tree == null) {
            return new ValidationResponse("Invalid rule.");
        } else if (tree.getMaximumArgument() >= args) {
            return new ValidationResponse("Condition "
                    + tree.getMaximumArgument() + " does not exist");
        } else {
            return new ValidationResponse();
        }
    }

}
