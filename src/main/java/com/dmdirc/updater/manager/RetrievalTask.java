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

package com.dmdirc.updater.manager;

import com.dmdirc.updater.checking.UpdateCheckResult;
import com.dmdirc.updater.retrieving.UpdateRetrievalResult;
import com.dmdirc.updater.retrieving.UpdateRetrievalStrategy;

/**
 * Task which retrieves an available update using the given {@link UpdateRetrievalStrategy} and
 * passes the result back to the {@link UpdateManagerImpl}.
 */
public class RetrievalTask implements Runnable {

    /** The update manager launching this task. */
    private final UpdateManagerImpl manager;
    /** The strategy to use to retrieve the update. */
    private final UpdateRetrievalStrategy strategy;
    /** The update which will be downloaded. */
    private final UpdateCheckResult result;
    /** Whether to install afterwards or not. */
    private final boolean install;

    public RetrievalTask(final UpdateManagerImpl manager, final UpdateRetrievalStrategy strategy,
            final UpdateCheckResult result, final boolean install) {
        this.manager = manager;
        this.strategy = strategy;
        this.result = result;
        this.install = install;
    }

    @Override
    public void run() {
        final UpdateRetrievalResult retrievalResult = strategy.retrieve(result);

        if (retrievalResult.isSuccessful()) {
            manager.setRetrievalResult(retrievalResult);

            if (install) {
                manager.install(retrievalResult.getCheckResult().getComponent());
            }
        }
    }

}
