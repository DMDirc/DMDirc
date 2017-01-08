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

package com.dmdirc.config.validators;

import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.util.validators.ValidationResponse;
import com.dmdirc.util.validators.Validator;

/**
 * Validates URLProtocols.
 */
public class URLProtocolValidator implements Validator<String> {

    /** The global configuration to read settings from. */
    private final AggregateConfigProvider globalConfig;

    /**
     * Creates a {@link URLProtocolValidator} that will read from the given config.
     *
     * @param globalConfig The config manager to read protocol info from.
     */
    public URLProtocolValidator(final AggregateConfigProvider globalConfig) {
        this.globalConfig = globalConfig;
    }

    @Override
    public ValidationResponse validate(final String object) {
        if (object == null || object.isEmpty()) {
            return new ValidationResponse("Cannot be empty");
        } else if (globalConfig.hasOptionString("protocol", object)) {
            return new ValidationResponse("Cannot already exist");
        } else {
            return new ValidationResponse();
        }
    }

}
