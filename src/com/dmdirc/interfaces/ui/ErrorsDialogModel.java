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

package com.dmdirc.interfaces.ui;

import com.dmdirc.logger.ProgramError;

import java.util.Optional;
import java.util.Set;

/**
 * Model representing an errors dialog for the client.
 */
public interface ErrorsDialogModel {

    /**
     * Loads the model, must be called before the model is used.
     */
    void load();

    /**
     * Unloads the model, should be called when the dialog is closed.
     */
    void unload();

    /**
     * Returns the current list of errors to be displayed.
     *
     * @return List of errors
     */
    Set<ProgramError> getErrors();

    /**
     * Returns the currently selected error.
     *
     * @return Selected error or empty if nothing is selected
     */
    Optional<ProgramError> getSelectedError();

    /**
     * Sets the selected error.
     *
     * @param selectedError New selected error, or optional to remove the selection
     */
    void setSelectedError(final Optional<ProgramError> selectedError);

    /**
     * Deletes the selected error.  This is a noop if there is no selection.
     */
    void deleteSelectedError();

    /**
     * Deletes all errors in the dialog.
     */
    void deleteAllErrors();

    /**
     * Sends the selected error. This is a noop if there is no selection.
     */
    void sendSelectedError();

    /**
     * Are we allowed to delete the selected error?
     *
     * @return true iif the error can be deleted
     */
    boolean isDeletedAllowed();

    /**
     * Are we allowed to delete all the errors?
     *
     * @return true iif all errors can be deleted
     */
    boolean isDeleteAllAllowed();

    /**
     * Are we allowed to send the selected error?
     *
     * @return true iif the error can be sent
     */
    boolean isSendAllowed();

    /**
     * Adds a listener to this model.
     *
     * @param listener Listener to add
     */
    void addListener(final ErrorsDialogModelListener listener);

    /**
     * Removes a listener from this model.
     *
     * @param listener Listener to remove
     */
    void removeListener(final ErrorsDialogModelListener listener);
}
