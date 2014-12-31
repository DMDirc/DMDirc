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
import com.dmdirc.updater.checking.UpdateCheckStrategy;
import com.dmdirc.updater.installing.UpdateInstallationStrategy;
import com.dmdirc.updater.retrieving.UpdateRetrievalStrategy;

import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Specialised update manager for DMDirc.
 */
@Singleton
public class DMDircUpdateManager extends CachingUpdateManagerImpl {

    /** Number of threads to use. */
    private static final int THREAD_COUNT = 3;

    /**
     * Creates a new instance of the update manager.
     *
     * @param updatePolicy           The policy to use to decide if components should be updated.
     * @param checkStrategies        The strategies to use to actually perform checks.
     * @param consolidator           The consolidator to use to pick from multiple available
     *                               updates.
     * @param retrievalStrategies     The strategies to use to retrieve updates.
     * @param installationStrategies The strategies to use to install updates.
     * @param components             The default components to add to the manager.
     */
    @Inject
    public DMDircUpdateManager(
            final UpdateComponentPolicy updatePolicy,
            final Set<UpdateCheckStrategy> checkStrategies,
            final CheckResultConsolidator consolidator,
            final Set<UpdateRetrievalStrategy> retrievalStrategies,
            final Set<UpdateInstallationStrategy> installationStrategies,
            final Set<UpdateComponent> components) {
        super(new ThreadPoolExecutor(THREAD_COUNT, THREAD_COUNT, 1, TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(), new NamedThreadFactory()),
                consolidator, updatePolicy);

        checkStrategies.forEach(DMDircUpdateManager.this::addCheckStrategy);

        retrievalStrategies.forEach(DMDircUpdateManager.this::addRetrievalStrategy);

        installationStrategies.forEach(DMDircUpdateManager.this::addInstallationStrategy);

        components.forEach(DMDircUpdateManager.this::addComponent);
    }

    /**
     * Thread factory which returns named threads.
     */
    private static class NamedThreadFactory implements ThreadFactory {

        @Override
        public Thread newThread(@Nonnull final Runnable r) {
            return new Thread(r, "Updater thread");
        }

    }

}
