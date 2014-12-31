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

package com.dmdirc.commandparser.validators;

import com.dmdirc.util.validators.RegexStringValidator;
import com.dmdirc.util.validators.ValidationResponse;
import com.dmdirc.util.validators.Validator;
import com.dmdirc.util.validators.ValidatorChain;

import java.util.regex.Pattern;

/**
 * Validates command names.
 */
public class CommandNameValidator implements Validator<String> {

    private final ValidatorChain<String> validator;

    /**
     * Instantiates a new command name validator using the given command char.
     *
     * @param commandChar the character commands start with (which is therefore disallowed at the
     *                    start of a command name).
     */
    public CommandNameValidator(final char commandChar) {
        validator = ValidatorChain.<String>builder().addValidator(
                new RegexStringValidator("^[^\\s]*$", "Cannot contain spaces"))
                .addValidator(new RegexStringValidator("^[^"
                                + Pattern.quote(String.valueOf(commandChar))
                                + "].*$", "Cannot start with a " + commandChar))
                .build();
    }

    @Override
    public ValidationResponse validate(final String object) {
        return validator.validate(object);
    }

}
