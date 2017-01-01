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
 * Base class for {@link UpdateRetrievalStrategy} implementations that can handle a single subclass
 * of {@link UpdateCheckResult}.
 *
 * @param <T> The type of result that this strategy can handle
 */
public abstract class TypeSensitiveRetrievalStrategy<T extends UpdateCheckResult>
        implements UpdateRetrievalStrategy {

    /** The type of result that this strategy can handle. */
    private final Class<T> clazz;

    public TypeSensitiveRetrievalStrategy(final Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public boolean canHandle(final UpdateCheckResult checkResult) {
        return clazz.isAssignableFrom(checkResult.getClass());
    }

    @Override
    @SuppressWarnings("unchecked")
    public UpdateRetrievalResult retrieve(final UpdateCheckResult checkResult) {
        return retrieveImpl((T) checkResult);
    }

    /**
     * Retrieves the update associated with the given {@link UpdateCheckResult}.
     *
     * @param checkResult The update check result describing the update requiring retrieval.
     *
     * @return An {@link UpdateRetrievalResult} which describes the result of the retrieval,
     *         providing information for an
     *         {@link com.dmdirc.updater.installing.UpdateInstallationStrategy} to use to install
     *         the update.
     *
     * @see #retrieve(com.dmdirc.updater.checking.UpdateCheckResult)
     */
    protected abstract UpdateRetrievalResult retrieveImpl(T checkResult);

}
