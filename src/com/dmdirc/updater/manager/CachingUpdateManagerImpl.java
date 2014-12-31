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
import com.dmdirc.util.collections.ListenerList;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Executor;

/**
 * An extension of {@link UpdateManagerImpl} which implements status caching functionality.
 */
public class CachingUpdateManagerImpl extends UpdateManagerImpl implements CachingUpdateManager {

    /** Map of component to their most recent status. */
    private final Map<UpdateComponent, UpdateStatus> cachedStatuses = new ConcurrentSkipListMap<>(
            new UpdateComponentComparator());
    /** List of registered listeners. */
    private final ListenerList listenerList = new ListenerList();
    /** Our current status. */
    private UpdateManagerStatus managerStatus = UpdateManagerStatus.IDLE;

    /**
     * Creates a new instance of {@link CachingUpdateManagerImpl}.
     *
     * @param executor     The executor to use to schedule tasks
     * @param consolidator The consolidator to use to merge check results
     * @param policy       The policy to apply to update components
     */
    public CachingUpdateManagerImpl(final Executor executor,
            final CheckResultConsolidator consolidator,
            final UpdateComponentPolicy policy) {
        super(executor, consolidator, policy);

        addUpdateStatusListener(new Listener());
    }

    public UpdateManagerStatus getManagerStatus() {
        return managerStatus;
    }

    @Override
    public UpdateStatus getStatus(final UpdateComponent component) {
        return cachedStatuses.get(component);
    }

    @Override
    public void addComponent(final UpdateComponent component) {
        super.addComponent(component);
        cachedStatuses.put(component, getPolicy().canCheck(component)
                ? UpdateStatus.IDLE : UpdateStatus.CHECKING_NOT_PERMITTED);
    }

    @Override
    public void removeComponent(final UpdateComponent component) {
        super.removeComponent(component);
        cachedStatuses.remove(component);
    }

    /**
     * Determines the current status of this manager, using the cached status of each update
     * component. If the status has changed, fires the
     * {@link UpdateManagerListener#updateManagerStatusChanged(com.dmdirc.updater.manager.UpdateManager, com.dmdirc.updater.manager.UpdateManagerStatus)}
     * method on registered listeners.
     */
    private void checkStatus() {
        UpdateManagerStatus newStatus = UpdateManagerStatus.IDLE;
        UpdateManagerStatus componentStatus;

        for (UpdateStatus cachedStatus : cachedStatuses.values()) {
            switch (cachedStatus) {
                case CHECKING:
                case INSTALLING:
                case RETRIEVING:
                    componentStatus = UpdateManagerStatus.WORKING;
                    break;
                case UPDATE_PENDING:
                case INSTALL_PENDING:
                    componentStatus = UpdateManagerStatus.IDLE_UPDATE_AVAILABLE;
                    break;
                case RESTART_PENDING:
                    componentStatus = UpdateManagerStatus.IDLE_RESTART_NEEDED;
                    break;
                case UPDATED:
                case IDLE:
                default:
                    componentStatus = UpdateManagerStatus.IDLE;
            }

            if (componentStatus.compareTo(newStatus) < 0) {
                newStatus = componentStatus;
            }
        }

        if (managerStatus != newStatus) {
            managerStatus = newStatus;
            listenerList.getCallable(UpdateManagerListener.class)
                    .updateManagerStatusChanged(this, managerStatus);
        }
    }

    @Override
    public void addUpdateManagerListener(final UpdateManagerListener listener) {
        listenerList.add(UpdateManagerListener.class, listener);
    }

    @Override
    public void removeUpdateManagerListener(final UpdateManagerListener listener) {
        listenerList.remove(UpdateManagerListener.class, listener);
    }

    /**
     * Status listener which updates the {@link #cachedStatuses} map.
     */
    private class Listener implements UpdateStatusListener {

        @Override
        public void updateStatusChanged(final UpdateComponent component,
                final UpdateStatus status, final double progress) {
            cachedStatuses.put(component, status);
            checkStatus();
        }

    }

    /**
     * Comparator which compares components based on their name.
     */
    private static class UpdateComponentComparator implements Comparator<UpdateComponent> {

        @Override
        public int compare(final UpdateComponent o1, final UpdateComponent o2) {
            return o1.getName().compareTo(o2.getName());
        }

    }

}
