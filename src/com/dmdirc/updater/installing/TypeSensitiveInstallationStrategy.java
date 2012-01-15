
package com.dmdirc.updater.installing;

import com.dmdirc.updater.UpdateComponent;
import com.dmdirc.updater.retrieving.UpdateRetrievalResult;

import lombok.AllArgsConstructor;

/**
 * Base class for {@link UpdateInstallationStrategy} implementations that
 * can only handle a specific sublcass of {@link UpdateComponent} or
 * {@link UpdateRetrievalResult}.
 *
 * @param <S> The type of {@link UpdateComponent} that can be handled
 * @param <T> The type of {@link UpdateRetrievalResult} that can be handled
 */
@AllArgsConstructor
public abstract class TypeSensitiveInstallationStrategy<S extends UpdateComponent,T extends UpdateRetrievalResult>
        implements UpdateInstallationStrategy {

    /** The type of {@link UpdateComponent} that can be handled. */
    private final Class<S> updateComponentClass;

    /** The type of {@link UpdateRetrievalResult} that can be handled. */
    private final Class<T> updateRetrievalClass;

    /** {@inheritDoc} */
    @Override
    public boolean canHandle(final UpdateRetrievalResult retrievalResult) {
        return updateComponentClass.isAssignableFrom(retrievalResult.getCheckResult().getComponent().getClass())
                && updateRetrievalClass.isAssignableFrom(retrievalResult.getClass());
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public void install(final UpdateRetrievalResult retrievalResult) {
        installImpl((S) retrievalResult.getCheckResult().getComponent(), (T) retrievalResult);
    }

    /**
     * Installs the retrieved update for the given component.
     *
     * @param component The component to be updated
     * @param retrievalResult The result of the retrieval operation
     */
    protected abstract void installImpl(S component, T retrievalResult);

}
