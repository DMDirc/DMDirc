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

package com.dmdirc.updater.installing;

import com.dmdirc.updater.retrieving.UpdateRetrievalResult;

/**
 * Describes a strategy for installing updates which have been retrieved by a
 * {@link com.dmdirc.updater.retrieving.UpdateRetrievalStrategy}.
 */
public interface UpdateInstallationStrategy {

    /**
     * Determines whether this strategy can handle the installation of the given retrieved update
     * for the given component.
     *
     * @param retrievalResult The result of the retrieval operation
     *
     * @return True if this strategy can perform installation; false otherwise
     */
    boolean canHandle(UpdateRetrievalResult retrievalResult);

    /**
     * Installs the retrieved update for the given component.
     *
     * @param retrievalResult The result of the retrieval operation
     */
    void install(UpdateRetrievalResult retrievalResult);

    /**
     * Adds a new status listener to this strategy.
     *
     * @param listener The listener to be registered
     */
    void addUpdateInstallationListener(UpdateInstallationListener listener);

    /**
     * Removes the given listener from this strategy.
     *
     * @param listener The listener to be removed
     */
    void removeUpdateInstallationListener(UpdateInstallationListener listener);

}
