/*
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
package com.dmdirc.config.prefs.validator;

/**
 * Validates a port number.
 * 
 * @author chris
 */
public class PortValidator implements Validator<String> {
    
    /** The minimum port number. */
    private static final int MIN_PORT = 1;
    
    /** The maximum port number. */
    private static final int MAX_PORT = 65535;    

    /** {@inheritDoc} */
    @Override
    public ValidationResponse validate(final String object) {
        try {
            int port = Integer.parseInt(object);
            
            if (port >= MIN_PORT) {
                if (port <= MAX_PORT) {
                    return new ValidationResponse();
                } else {
                    return new ValidationResponse("Port must be less than " + (MAX_PORT + 1));
                }
            } else {
                return new ValidationResponse("Port must be greater than " + (MIN_PORT - 1));
            }
        } catch (NumberFormatException ex) {
            return new ValidationResponse("Port must be numerical");
        }
    }

}
