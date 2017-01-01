/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.ui.core.profiles;

import com.dmdirc.interfaces.ui.ProfilesDialogModel;
import com.dmdirc.util.validators.ValidationResponse;
import com.dmdirc.util.validators.Validator;

/**
 * Validates an existing profile name.
 */
public class EditSelectedProfileNameValidator implements Validator<String> {

    private final ProfilesDialogModel model;

    public EditSelectedProfileNameValidator(final ProfilesDialogModel model) {
        this.model = model;
    }

    @Override
    public ValidationResponse validate(final String object) {
        for (MutableProfile nickname : model.getProfileList()) {
            if (model.getSelectedProfile().isPresent()
                    && nickname == model.getSelectedProfile().get()) {
                continue;
            }
            if (nickname.getName().equals(object)) {
                return new ValidationResponse("This valid already exists.");
            }
        }
        return new ValidationResponse();
    }

}
