/*
 * Copyright (c) 2006-2013 DMDirc Developers
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

package com.dmdirc.updater.manager;

import com.dmdirc.updater.UpdateComponent;
import com.dmdirc.updater.checking.CheckResultConsolidator;
import com.dmdirc.updater.checking.UpdateCheckResult;
import com.dmdirc.updater.checking.UpdateCheckStrategy;
import com.dmdirc.updater.installing.UpdateInstallationListener;
import com.dmdirc.updater.installing.UpdateInstallationStrategy;
import com.dmdirc.updater.retrieving.UpdateRetrievalListener;
import com.dmdirc.updater.retrieving.UpdateRetrievalResult;
import com.dmdirc.updater.retrieving.UpdateRetrievalStategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ListenerSupport;
import lombok.extern.slf4j.Slf4j;

/**
 * Concrete implementation of {@link UpdateManager}.
 */
@Slf4j
@AllArgsConstructor
@ListenerSupport(UpdateStatusListener.class)
public class UpdateManagerImpl implements UpdateManager {

    /** Collection of known update checking strategies. */
    private final List<UpdateCheckStrategy> checkers
            = new CopyOnWriteArrayList<UpdateCheckStrategy>();

    /** Collection of known update retrieval strategies. */
    private final List<UpdateRetrievalStategy> retrievers
            = new CopyOnWriteArrayList<UpdateRetrievalStategy>();

    /** Collection of known update installation strategies. */
    private final List<UpdateInstallationStrategy> installers
            = new CopyOnWriteArrayList<UpdateInstallationStrategy>();

    /** Map of known component names to their components. */
    private final Map<String, UpdateComponent> components
            = new HashMap<String, UpdateComponent>();

    /** Listener used to proxy retrieval events. */
    private final UpdateRetrievalListener retrievalListener
            = new RetrievalListener();

    /** Listener used to proxy setRetrievalResult events. */
    private final UpdateInstallationListener installationListener
            = new InstallListener();

    /** Cache of update check results. */
    private final Map<UpdateComponent, UpdateCheckResult> checkResults
            = new HashMap<UpdateComponent, UpdateCheckResult>();

    /** Cache of retrieval results. */
    private final Map<UpdateComponent, UpdateRetrievalResult> retrievalResults
            = new HashMap<UpdateComponent, UpdateRetrievalResult>();

    /** Executor to use to schedule retrieval and installation jobs. */
    private final Executor executor;

    /** Consolidator to use to merge multiple check results. */
    @Getter(AccessLevel.PROTECTED)
    private final CheckResultConsolidator consolidator;

    /** The policy to use to determine whether updates are enabled. */
    @Getter(AccessLevel.PROTECTED)
    private final UpdateComponentPolicy policy;

    /** {@inheritDoc} */
    @Override
    public void addCheckStrategy(final UpdateCheckStrategy strategy) {
        log.trace("Adding new check strategy: {}", strategy);
        this.checkers.add(strategy);
    }

    /** {@inheritDoc} */
    @Override
    public void addRetrievalStrategy(final UpdateRetrievalStategy strategy) {
        log.trace("Adding new retrieval strategy: {}", strategy);
        strategy.addUpdateRetrievalListener(retrievalListener);
        this.retrievers.add(strategy);
    }

    /** {@inheritDoc} */
    @Override
    public void addInstallationStrategy(final UpdateInstallationStrategy strategy) {
        log.trace("Adding new installation strategy: {}", strategy);
        strategy.addUpdateInstallationListener(installationListener);
        this.installers.add(strategy);
    }

    /** {@inheritDoc} */
    @Override
    public void addComponent(final UpdateComponent component) {
        log.trace("Adding new component: {}", component);
        this.components.put(component.getName(), component);
    }

    /** {@inheritDoc} */
    @Override
    public void removeComponent(final UpdateComponent component) {
        log.trace("Removing component: {}", component);
        this.components.remove(component.getName());
        this.checkResults.remove(component);
        this.retrievalResults.remove(component);
    }

    /** {@inheritDoc} */
    @Override
    public Collection<UpdateComponent> getComponents() {
        return Collections.unmodifiableCollection(this.components.values());
    }

    /** {@inheritDoc} */
    @Override
    public UpdateCheckResult getCheckResult(final UpdateComponent component) {
        return checkResults.get(component);
    }

    /** {@inheritDoc} */
    @Override
    public void checkForUpdates() {
        final Collection<Map<UpdateComponent, UpdateCheckResult>> results
                = new ArrayList<Map<UpdateComponent, UpdateCheckResult>>();

        log.info("Checking for updates for {} components using {} strategies",
                components.size(), checkers.size());
        log.trace("Components: {}", components);
        log.trace("Strategies: {}", checkers);

        final List<UpdateComponent> enabledComponents
                = new ArrayList<UpdateComponent>(components.size());

        for (UpdateComponent component : components.values()) {
            if (policy.canCheck(component)) {
                fireUpdateStatusChanged(component, UpdateStatus.CHECKING, 0);
                enabledComponents.add(component);
            } else {
                log.debug("Checking for updates for {} denied by policy", component.getName());
                fireUpdateStatusChanged(component, UpdateStatus.CHECKING_NOT_PERMITTED, 0);
            }
        }

        for (UpdateCheckStrategy strategy : checkers) {
            results.add(strategy.checkForUpdates(enabledComponents));
        }

        checkResults.putAll(consolidator.consolidate(results));

        for (UpdateComponent component : enabledComponents) {
            if (checkResults.containsKey(component) && checkResults.get(component).isUpdateAvailable()) {
                log.trace("Update is available for {}", component);
                fireUpdateStatusChanged(component, UpdateStatus.UPDATE_PENDING, 0);
            } else {
                fireUpdateStatusChanged(component, UpdateStatus.IDLE, 0);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void install(final UpdateComponent component) {
        if (!retrievalResults.containsKey(component) || !retrievalResults.get(component).isSuccessful()) {
            // Not downloaded yet - try retrieving first
            retrieve(component, true);
            return;
        }

        final UpdateRetrievalResult update = retrievalResults.get(component);
        final UpdateInstallationStrategy strategy = getStrategy(update);

        if (strategy == null) {
            fireUpdateStatusChanged(component, UpdateStatus.IDLE, 0);
        } else {
            fireUpdateStatusChanged(component, UpdateStatus.INSTALL_PENDING, 0);
            log.debug("Scheduling install for {}", update);
            executor.execute(new InstallationTask(strategy, update));
        }
    }

    /** {@inheritDoc} */
    @Override
    public void retrieve(final UpdateComponent component) {
        retrieve(component, false);
    }

    /**
     * Retrieves updates for the given component, optionally installing if
     * retrieval is successful.
     *
     * @param component The component to be retrieved
     * @param install True to install automatically, false to just retrieve
     */
    public void retrieve(final UpdateComponent component, final boolean install) {
        if (!checkResults.containsKey(component) || !checkResults.get(component).isUpdateAvailable()) {
            log.warn("Tried to retrieve component with no update: {}", component);
            return;
        }

        final UpdateCheckResult update = checkResults.get(component);
        final UpdateRetrievalStategy strategy = getStrategy(update);

        if (strategy == null) {
            fireUpdateStatusChanged(component, UpdateStatus.IDLE, 0);
        } else {
            fireUpdateStatusChanged(component, UpdateStatus.UPDATE_PENDING, 0);
            log.debug("Scheduling retrieval for {}", update);
            executor.execute(new RetrievalTask(this, strategy, update, install));
        }
    }

    /**
     * Sets the retrieval result for an {@link UpdateComponent}.
     *
     * @param result The result retrieved from the {@link UpdateRetrievalStategy}.
     */
    protected void setRetrievalResult(final UpdateRetrievalResult result) {
        log.debug("Received retrieval result {}", result);
        retrievalResults.put(result.getCheckResult().getComponent(), result);
    }

    /**
     * Gets an appropriate retrieval strategy for the given check result.
     * Iterates over the set of known strategies and returns the first which
     * claims to be able to handle the result.
     *
     * @param result The result to find a strategy for
     * @return A relevant strategy, or <code>null</code> if none are available
     */
    protected UpdateRetrievalStategy getStrategy(final UpdateCheckResult result) {
        log.debug("Trying to find retrieval strategy for {}", result);

        for (UpdateRetrievalStategy strategy : retrievers) {
            log.trace("Testing strategy {}", strategy);

            if (strategy.canHandle(result)) {
                log.debug("Found strategy {}", strategy);
                return strategy;
            }
        }

        log.warn("No strategy found to retrieve {}", result);
        return null;
    }

    /**
     * Gets an appropriate installation strategy for the given retrieval result.
     * Iterates over the set of known strategies and returns the first which
     * claims to be able to handle the result.
     *
     * @param result The result to find a strategy for
     * @return A relevant strategy, or <code>null</code> if none are available
     */
    protected UpdateInstallationStrategy getStrategy(final UpdateRetrievalResult result) {
        log.debug("Trying to find installation strategy for {}", result);

        for (UpdateInstallationStrategy strategy : installers) {
            log.trace("Testing strategy {}", strategy);

            if (strategy.canHandle(result)) {
                log.debug("Found strategy {}", strategy);
                return strategy;
            }
        }

        log.warn("No strategy found to install {}", result);
        return null;
    }

    /**
     * Listens for changes announced by {@link UpdateInstallationStrategy}s
     * and proxies them on to the manager's own {@link UpdateStatusListener}s.
     */
    private class InstallListener implements UpdateInstallationListener {

        /** {@inheritDoc} */
        @Override
        public void installCompleted(final UpdateComponent component) {
            fireUpdateStatusChanged(component, component.requiresRestart()
                    ? UpdateStatus.RESTART_PENDING : UpdateStatus.UPDATED, 0);
        }

        /** {@inheritDoc} */
        @Override
        public void installFailed(final UpdateComponent component) {
            fireUpdateStatusChanged(component, UpdateStatus.IDLE, 0);
        }

        /** {@inheritDoc} */
        @Override
        public void installProgressChanged(final UpdateComponent component, final double progress) {
            fireUpdateStatusChanged(component, UpdateStatus.INSTALLING, progress);
        }

    }

    /**
     * Listens for changes announced by {@link UpdateRetrievalStrategy}s
     * and proxies them on to the manager's own {@link UpdateStatusListener}s.
     */
    private class RetrievalListener implements UpdateRetrievalListener {

        /** {@inheritDoc} */
        @Override
        public void retrievalCompleted(final UpdateComponent component) {
            fireUpdateStatusChanged(component, UpdateStatus.INSTALL_PENDING, 0);
        }

        /** {@inheritDoc} */
        @Override
        public void retrievalFailed(final UpdateComponent component) {
            fireUpdateStatusChanged(component, UpdateStatus.IDLE, 0);
        }

        /** {@inheritDoc} */
        @Override
        public void retrievalProgressChanged(final UpdateComponent component, final double progress) {
            fireUpdateStatusChanged(component, UpdateStatus.RETRIEVING, progress);
        }

    }
}
