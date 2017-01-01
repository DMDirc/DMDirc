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

package com.dmdirc.interfaces.ui;

import com.dmdirc.commandparser.auto.AutoCommand;
import com.dmdirc.util.validators.Validator;

/**
 * Represents a dialog that can edit the global {@link AutoCommand} in the client.
 */
public interface GlobalAutoCommandsDialogModel {

    /**
     * Loads the model.  This should be called before interacting with the model in any way.
     */
    void load();

    /**
     * Returns the current response to be included in the global {@link AutoCommand}.
     *
     * @return Current response.  Will never be null, may be empty.
     */
    String getResponse();

    /**
     * Sets the response to be included in the global {@link AutoCommand}.
     *
     * @param response New response.  Should never be null, making this empty will remove the
     *                 global auto command
     */
    void setResponse(final String response);

    /**
     * Returns the validator for a {@Link AutoCommand}'s response.
     *
     * @return Validator for a response string
     */
    Validator<String> getResponseValidator();

    /**
     * Is the current response valid?
     *
     * @return true iif the response is valid
     */
    boolean isResponseValid();

    /**
     * Is the dialog allowed to be saved?
     *
     * @return true iif the dialog can be saved
     */
    boolean isSaveAllowed();

    /**
     * Saves the current state into the global {@link AutoCommand}.
     */
    void save();
}
