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

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Validates a string to see if it compiles to a URI.
 */
public class URIValidator implements Validator<String> {

    /** {@inheritDoc} */
    @Override
    public ValidationResponse validate(final String object) {
        ValidationResponse result;
        try {
            final URI uri = new URI(object);
            if (uri.getHost() == null || uri.getHost().isEmpty()) {
                result = new ValidationResponse("Address requires a hostname.");
            } else {
                result = new ValidationResponse();
            }
        } catch (URISyntaxException ex) {
            result = new ValidationResponse(ex.getReason());
        }
        return result;
    }
}
