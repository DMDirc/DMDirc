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

package com.dmdirc.updater.retrieving;

import com.dmdirc.updater.checking.UpdateCheckResult;

/**
 * Defines a strategy for retrieving updated files that have been identified by an
 * {@link com.dmdirc.updater.checking.UpdateCheckStrategy}. Retrieval strategies will place updates
 * in a temporary location on disk for use by an
 * {@link com.dmdirc.updater.installing.UpdateInstallationStrategy}.
 */
public interface UpdateRetrievalStrategy {

    /**
     * Determines whether this strategy can handle retrieving the given update.
     *
     * @param checkResult The update check result describing the update requiring retrieval.
     *
     * @return True if this strategy can handle the update; false otherwise
     */
    boolean canHandle(UpdateCheckResult checkResult);

    /**
     * Retrieves the update associated with the given {@link UpdateCheckResult}.
     *
     * @param checkResult The update check result describing the update requiring retrieval.
     *
     * @return An {@link UpdateRetrievalResult} which describes the result of the retrieval,
     *         providing information for an
     *         {@link com.dmdirc.updater.installing.UpdateInstallationStrategy} to use to install
     *         the update.
     */
    UpdateRetrievalResult retrieve(UpdateCheckResult checkResult);

    /**
     * Adds a new status listener to this strategy.
     *
     * @param listener The listener to be registered
     */
    void addUpdateRetrievalListener(UpdateRetrievalListener listener);

    /**
     * Removes the given listener from this strategy.
     *
     * @param listener The listener to be removed
     */
    void removeUpdateRetrievalListener(UpdateRetrievalListener listener);

}
