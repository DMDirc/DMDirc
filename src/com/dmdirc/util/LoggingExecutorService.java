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

package com.dmdirc.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.dmdirc.util.LogUtils.APP_ERROR;

/**
 * An executor service that takes logs failed executions either via eventbus errors or a custom
 * error function.
 */
public class LoggingExecutorService extends ThreadPoolExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(LoggingExecutorService.class);

    private final BiConsumer<Runnable, Throwable> afterExecute;

    /**
     * Creates a new instance of this executor service.
     *
     * @param coreSize The number of threads to keep in the pool, even if they are idle, unless
     *                 {@code allowCoreThreadTimeOut} is set
     * @param maxSize  The maximum number of threads to allow in the pool
     * @param poolName The naming format to use when naming threads
     */
    public LoggingExecutorService(final int coreSize, final int maxSize, final String poolName) {
        this(coreSize, maxSize, (r, t) -> LOG.error(APP_ERROR, t.getMessage(), t), poolName);
    }

    /**
     * Creates a new instance of this executor service.
     *
     * @param coreSize     The number of threads to keep in the pool, even if they are idle,
     *                     unless {@code allowCoreThreadTimeOut} is set
     * @param maxSize      The maximum number of threads to allow in the pool
     * @param afterExecute The function to call when an exception occurs
     * @param poolName     The naming format to use when naming threads
     */
    public LoggingExecutorService(final int coreSize, final int maxSize,
            final BiConsumer<Runnable, Throwable> afterExecute, final String poolName) {
        super(coreSize, maxSize, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(5));
        this.afterExecute = afterExecute;
        setThreadFactory(new ThreadFactoryBuilder().setNameFormat(poolName + "-%d").build());
    }

    @Override
    protected void afterExecute(final Runnable r, final Throwable t) {
        super.afterExecute(r, t);
        if (t == null && r instanceof Future<?>) {
            try {
            ((Future<?>) r).get();
            } catch (CancellationException | InterruptedException ex) {
                //Ignore
            } catch (ExecutionException ex) {
                afterExecute.accept(r, ex);
            }
        }
        if (t != null) {
            afterExecute.accept(r, t);
        }
    }
}
