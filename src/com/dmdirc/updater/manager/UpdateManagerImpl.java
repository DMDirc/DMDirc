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

package com.dmdirc.updater.manager;

import com.dmdirc.updater.UpdateComponent;
import com.dmdirc.updater.checking.CheckResultConsolidator;
import com.dmdirc.updater.checking.UpdateCheckResult;
import com.dmdirc.updater.checking.UpdateCheckStrategy;
import com.dmdirc.updater.installing.UpdateInstallationListener;
import com.dmdirc.updater.installing.UpdateInstallationStrategy;
import com.dmdirc.updater.retrieving.UpdateRetrievalListener;
import com.dmdirc.updater.retrieving.UpdateRetrievalResult;
import com.dmdirc.updater.retrieving.UpdateRetrievalStrategy;
import com.dmdirc.util.collections.ListenerList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete implementation of {@link UpdateManager}.
 */
public class UpdateManagerImpl implements UpdateManager {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateManagerImpl.class);
    /** Collection of known update checking strategies. */
    private final List<UpdateCheckStrategy> checkers = new CopyOnWriteArrayList<>();
    /** Collection of known update retrieval strategies. */
    private final Collection<UpdateRetrievalStrategy> retrievers = new CopyOnWriteArrayList<>();
    /** Collection of known update installation strategies. */
    private final Collection<UpdateInstallationStrategy> installers = new CopyOnWriteArrayList<>();
    /** Map of known component names to their components. Guarded by {@link #componentsLock}. */
    private final Map<String, UpdateComponent> components = new HashMap<>();
    /** Listener used to proxy retrieval events. */
    private final UpdateRetrievalListener retrievalListener = new RetrievalListener();
    /** Listener used to proxy setRetrievalResult events. */
    private final UpdateInstallationListener installationListener = new InstallListener();
    /** Cache of update check results. */
    private final Map<UpdateComponent, UpdateCheckResult> checkResults = new HashMap<>();
    /** Cache of retrieval results. */
    private final Map<UpdateComponent, UpdateRetrievalResult> retrievalResults = new HashMap<>();
    /** List of registered listeners. */
    private final ListenerList listenerList = new ListenerList();
    /** Lock for accessing {@link #components}. */
    private final Object componentsLock = new Object();
    /** Executor to use to schedule retrieval and installation jobs. */
    private final Executor executor;
    /** Consolidator to use to merge multiple check results. */
    private final CheckResultConsolidator consolidator;
    /** The policy to use to determine whether updates are enabled. */
    private final UpdateComponentPolicy policy;

    public UpdateManagerImpl(final Executor executor, final CheckResultConsolidator consolidator,
            final UpdateComponentPolicy policy) {
        this.executor = executor;
        this.consolidator = consolidator;
        this.policy = policy;
    }

    protected CheckResultConsolidator getConsolidator() {
        return consolidator;
    }

    protected UpdateComponentPolicy getPolicy() {
        return policy;
    }

    @Override
    public void addCheckStrategy(final UpdateCheckStrategy strategy) {
        LOG.trace("Adding new check strategy: {}", strategy);
        checkers.add(strategy);
    }

    @Override
    public void addRetrievalStrategy(final UpdateRetrievalStrategy strategy) {
        LOG.trace("Adding new retrieval strategy: {}", strategy);
        strategy.addUpdateRetrievalListener(retrievalListener);
        retrievers.add(strategy);
    }

    @Override
    public void addInstallationStrategy(final UpdateInstallationStrategy strategy) {
        LOG.trace("Adding new installation strategy: {}", strategy);
        strategy.addUpdateInstallationListener(installationListener);
        installers.add(strategy);
    }

    @Override
    public void addComponent(final UpdateComponent component) {
        LOG.trace("Adding new component: {}", component);
        synchronized (componentsLock) {
            components.put(component.getName(), component);
        }
    }

    @Override
    public void removeComponent(final UpdateComponent component) {
        LOG.trace("Removing component: {}", component);
        synchronized (componentsLock) {
            components.remove(component.getName());
        }

        checkResults.remove(component);
        retrievalResults.remove(component);
    }

    @Override
    public Collection<UpdateComponent> getComponents() {
        synchronized (componentsLock) {
            return Collections.unmodifiableCollection(components.values());
        }
    }

    @Override
    public UpdateCheckResult getCheckResult(final UpdateComponent component) {
        return checkResults.get(component);
    }

    @Override
    public void checkForUpdates() {
        final Collection<Map<UpdateComponent, UpdateCheckResult>> results = new ArrayList<>();

        LOG.info("Checking for updates for {} components using {} strategies", components.size(),
                checkers.size());
        LOG.trace("Components: {}", components);
        LOG.trace("Strategies: {}", checkers);

        final Collection<UpdateComponent> enabledComponents = new ArrayList<>(components.size());
        final Collection<UpdateComponent> disabledComponents = new ArrayList<>(components.size());

        synchronized (componentsLock) {
            for (UpdateComponent component : components.values()) {
                if (policy.canCheck(component)) {
                    enabledComponents.add(component);
                } else {
                    LOG.debug("Checking for updates for {} denied by policy", component.getName());
                    disabledComponents.add(component);
                }
            }
        }

        // Fire the listeners now we don't care about concurrent modifications.
        for (UpdateComponent component : enabledComponents) {
            listenerList.getCallable(UpdateStatusListener.class)
                    .updateStatusChanged(component, UpdateStatus.CHECKING, 0);
        }

        for (UpdateComponent component : disabledComponents) {
            listenerList.getCallable(UpdateStatusListener.class)
                    .updateStatusChanged(component, UpdateStatus.CHECKING_NOT_PERMITTED, 0);
        }

        results.addAll(
                checkers.stream().map(strategy -> strategy.checkForUpdates(enabledComponents))
                        .collect(Collectors.toList()));

        checkResults.putAll(consolidator.consolidate(results));

        for (UpdateComponent component : enabledComponents) {
            if (checkResults.containsKey(component) && checkResults.get(component).
                    isUpdateAvailable()) {
                LOG.trace("Update is available for {}", component);
                listenerList.getCallable(UpdateStatusListener.class)
                        .updateStatusChanged(component, UpdateStatus.UPDATE_PENDING, 0);
            } else {
                listenerList.getCallable(UpdateStatusListener.class)
                        .updateStatusChanged(component, UpdateStatus.IDLE, 0);
            }
        }
    }

    @Override
    public void install(final UpdateComponent component) {
        if (!retrievalResults.containsKey(component) || !retrievalResults.get(component).
                isSuccessful()) {
            // Not downloaded yet - try retrieving first
            retrieve(component, true);
            return;
        }

        final UpdateRetrievalResult update = retrievalResults.get(component);
        final UpdateInstallationStrategy strategy = getStrategy(update);

        if (strategy == null) {
            listenerList.getCallable(UpdateStatusListener.class)
                    .updateStatusChanged(component, UpdateStatus.IDLE, 0);
        } else {
            listenerList.getCallable(UpdateStatusListener.class)
                    .updateStatusChanged(component, UpdateStatus.INSTALL_PENDING, 0);
            LOG.debug("Scheduling install for {}", update);
            executor.execute(new InstallationTask(strategy, update));
        }
    }

    @Override
    public void retrieve(final UpdateComponent component) {
        retrieve(component, false);
    }

    /**
     * Retrieves updates for the given component, optionally installing if retrieval is successful.
     *
     * @param component The component to be retrieved
     * @param install   True to install automatically, false to just retrieve
     */
    public void retrieve(final UpdateComponent component, final boolean install) {
        if (!checkResults.containsKey(component)
                || !checkResults.get(component).isUpdateAvailable()) {
            LOG.warn("Tried to retrieve component with no update: {}", component);
            return;
        }

        final UpdateCheckResult update = checkResults.get(component);
        final UpdateRetrievalStrategy strategy = getStrategy(update);

        if (strategy == null) {
            listenerList.getCallable(UpdateStatusListener.class)
                    .updateStatusChanged(component, UpdateStatus.IDLE, 0);
        } else {
            listenerList.getCallable(UpdateStatusListener.class)
                    .updateStatusChanged(component, UpdateStatus.UPDATE_PENDING, 0);
            LOG.debug("Scheduling retrieval for {}", update);
            executor.execute(new RetrievalTask(this, strategy, update, install));
        }
    }

    /**
     * Sets the retrieval result for an {@link UpdateComponent}.
     *
     * @param result The result retrieved from the {@link UpdateRetrievalStrategy}.
     */
    protected void setRetrievalResult(final UpdateRetrievalResult result) {
        LOG.debug("Received retrieval result {}", result);
        retrievalResults.put(result.getCheckResult().getComponent(), result);
    }

    /**
     * Gets an appropriate retrieval strategy for the given check result. Iterates over the set of
     * known strategies and returns the first which claims to be able to handle the result.
     *
     * @param result The result to find a strategy for
     *
     * @return A relevant strategy, or {@code null} if none are available
     */
    protected UpdateRetrievalStrategy getStrategy(final UpdateCheckResult result) {
        LOG.debug("Trying to find retrieval strategy for {}", result);

        for (UpdateRetrievalStrategy strategy : retrievers) {
            LOG.trace("Testing strategy {}", strategy);

            if (strategy.canHandle(result)) {
                LOG.debug("Found strategy {}", strategy);
                return strategy;
            }
        }

        LOG.warn("No strategy found to retrieve {}", result);
        return null;
    }

    /**
     * Gets an appropriate installation strategy for the given retrieval result. Iterates over the
     * set of known strategies and returns the first which claims to be able to handle the result.
     *
     * @param result The result to find a strategy for
     *
     * @return A relevant strategy, or {@code null} if none are available
     */
    protected UpdateInstallationStrategy getStrategy(final UpdateRetrievalResult result) {
        LOG.debug("Trying to find installation strategy for {}", result);

        for (UpdateInstallationStrategy strategy : installers) {
            LOG.trace("Testing strategy {}", strategy);

            if (strategy.canHandle(result)) {
                LOG.debug("Found strategy {}", strategy);
                return strategy;
            }
        }

        LOG.warn("No strategy found to install {}", result);
        return null;
    }

    @Override
    public void addUpdateStatusListener(final UpdateStatusListener listener) {
        listenerList.add(UpdateStatusListener.class, listener);
    }

    @Override
    public void removeUpdateStatusListener(final UpdateStatusListener listener) {
        listenerList.remove(UpdateStatusListener.class, listener);
    }

    /**
     * Listens for changes announced by {@link UpdateInstallationStrategy}s and proxies them on to
     * the manager's own {@link UpdateStatusListener}s.
     */
    private class InstallListener implements UpdateInstallationListener {

        @Override
        public void installCompleted(final UpdateComponent component) {
            listenerList.getCallable(UpdateStatusListener.class)
                    .updateStatusChanged(component, component.requiresRestart()
                            ? UpdateStatus.RESTART_PENDING : UpdateStatus.UPDATED, 0);
        }

        @Override
        public void installFailed(final UpdateComponent component) {
            listenerList.getCallable(UpdateStatusListener.class)
                    .updateStatusChanged(component, UpdateStatus.IDLE, 0);
        }

        @Override
        public void installProgressChanged(final UpdateComponent component, final double progress) {
            listenerList.getCallable(UpdateStatusListener.class)
                    .updateStatusChanged(component, UpdateStatus.INSTALLING, progress);
        }

    }

    /**
     * Listens for changes announced by {@link UpdateRetrievalStrategy}s and proxies them on to the
     * manager's own {@link UpdateStatusListener}s.
     */
    private class RetrievalListener implements UpdateRetrievalListener {

        @Override
        public void retrievalCompleted(final UpdateComponent component) {
            listenerList.getCallable(UpdateStatusListener.class)
                    .updateStatusChanged(component, UpdateStatus.INSTALL_PENDING, 0);
        }

        @Override
        public void retrievalFailed(final UpdateComponent component) {
            listenerList.getCallable(UpdateStatusListener.class)
                    .updateStatusChanged(component, UpdateStatus.IDLE, 0);
        }

        @Override
        public void retrievalProgressChanged(final UpdateComponent component, final double progress) {
            listenerList.getCallable(UpdateStatusListener.class)
                    .updateStatusChanged(component, UpdateStatus.RETRIEVING, progress);
        }

    }

}
