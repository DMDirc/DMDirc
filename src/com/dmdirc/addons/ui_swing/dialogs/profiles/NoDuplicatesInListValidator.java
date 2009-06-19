/*
 * 
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.ui_swing.dialogs.profiles;

import com.dmdirc.config.prefs.validator.ValidationResponse;
import com.dmdirc.config.prefs.validator.Validator;

import javax.swing.DefaultListModel;

/**
 * Validator to check for duplicate values in a list.
 */
public class NoDuplicatesInListValidator implements Validator<String> {

    /** List to validate. */
    private DefaultListModel model;
    /** Case sensitive. */
    private boolean caseSensitive;

    /**
     * Creates a new validator.
     *
     * @param model Model to validate
     */
    public NoDuplicatesInListValidator(final DefaultListModel model) {
        this(true, model);
    }

    /**
     * Creates a new validator.
     *
     * @param caseSensitive Case sensitive check?
     * @param model Model to validate
     */
    public NoDuplicatesInListValidator(final boolean caseSensitive,
            final DefaultListModel model) {
        this.model = model;
        this.caseSensitive = caseSensitive;
    }

    /** {@inheritDoc} */
    @Override
    public ValidationResponse validate(final String object) {
        final String string = caseSensitive ? object : object.toLowerCase();
        if (model.contains(string)) {
            return new ValidationResponse("Value is a duplicate");
        } else {
            return new ValidationResponse();
        }
    }

}
