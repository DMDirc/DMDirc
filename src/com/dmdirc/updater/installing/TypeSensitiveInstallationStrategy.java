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

package com.dmdirc.updater.installing;

import com.dmdirc.updater.UpdateComponent;
import com.dmdirc.updater.retrieving.UpdateRetrievalResult;

/**
 * Base class for {@link UpdateInstallationStrategy} implementations that can only handle a specific
 * subclass of {@link UpdateComponent} or {@link UpdateRetrievalResult}.
 *
 * @param <S> The type of {@link UpdateComponent} that can be handled
 * @param <T> The type of {@link UpdateRetrievalResult} that can be handled
 */
public abstract class TypeSensitiveInstallationStrategy<S extends UpdateComponent, T extends UpdateRetrievalResult>
        implements UpdateInstallationStrategy {

    /** The type of {@link UpdateComponent} that can be handled. */
    private final Class<S> updateComponentClass;
    /** The type of {@link UpdateRetrievalResult} that can be handled. */
    private final Class<T> updateRetrievalClass;

    public TypeSensitiveInstallationStrategy(final Class<S> updateComponentClass,
            final Class<T> updateRetrievalClass) {
        this.updateComponentClass = updateComponentClass;
        this.updateRetrievalClass = updateRetrievalClass;
    }

    @Override
    public boolean canHandle(final UpdateRetrievalResult retrievalResult) {
        return updateComponentClass.isAssignableFrom(
                retrievalResult.getCheckResult().getComponent().getClass())
                && updateRetrievalClass.isAssignableFrom(retrievalResult.getClass());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void install(final UpdateRetrievalResult retrievalResult) {
        installImpl((S) retrievalResult.getCheckResult().getComponent(), (T) retrievalResult);
    }

    /**
     * Installs the retrieved update for the given component.
     *
     * @param component       The component to be updated
     * @param retrievalResult The result of the retrieval operation
     */
    protected abstract void installImpl(S component, T retrievalResult);

}
