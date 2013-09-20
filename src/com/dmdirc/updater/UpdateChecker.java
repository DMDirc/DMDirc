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

package com.dmdirc.updater;

import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.updater.manager.CachingUpdateManager;
import com.dmdirc.updater.manager.DMDircUpdateManager;
import com.dmdirc.updater.manager.UpdateStatus;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

import lombok.Getter;

/**
 * The update checker contacts the DMDirc website to check to see if there
 * are any updates available.
 */
public final class UpdateChecker implements Runnable {

    /** The domain to use for updater settings. */
    private static final String DOMAIN = "updater";

    /** Semaphore used to prevent multiple invocations. */
    private static final Semaphore MUTEX = new Semaphore(1);

    /** Our timer. */
    private static Timer timer = new Timer("Update Checker Timer");

    /** The update manager to use. */
    @Getter
    private static CachingUpdateManager manager;

    /** {@inheritDoc} */
    @Override
    public void run() {
        if (!MUTEX.tryAcquire()) {
            // Duplicate invocation

            return;
        }

        final ConfigManager config = IdentityManager.getIdentityManager().getGlobalConfiguration();

        if (!config.getOptionBool(DOMAIN, "enable")) {
            IdentityManager.getIdentityManager().getGlobalConfigIdentity().setOption(DOMAIN,
                    "lastcheck", String.valueOf((int) (new Date().getTime() / 1000)));

            MUTEX.release();
            initTimer();
            return;
        }

        manager.checkForUpdates();

        MUTEX.release();

        IdentityManager.getIdentityManager().getGlobalConfigIdentity().setOption(DOMAIN,
                "lastcheck", String.valueOf((int) (new Date().getTime() / 1000)));

        UpdateChecker.initTimer();

        if (config.getOptionBool(DOMAIN, "autoupdate")) {
            for (UpdateComponent component : manager.getComponents()) {
                if (manager.getStatus(component) == UpdateStatus.UPDATE_PENDING) {
                    manager.install(component);
                }
            }
        } else if (config.getOptionBool(DOMAIN, "autodownload")) {
            for (UpdateComponent component : manager.getComponents()) {
                if (manager.getStatus(component) == UpdateStatus.UPDATE_PENDING) {
                    manager.retrieve(component);
                }
            }
        }
    }

    /**
     * Initialises the update checker. Sets a timer to check based on the
     * frequency specified in the config.
     *
     * @param main Parent Main class
     */
    public static void init() {
        manager = new DMDircUpdateManager(IdentityManager.getIdentityManager()
                .getGlobalConfiguration(), IdentityManager.getIdentityManager().getConfigDir(), 3);

        initTimer();
    }

    /**
     * Initialises the timer to check for updates.
     *
     * @since 0.6.5
     */
    protected static void initTimer() {
        final int last = IdentityManager.getIdentityManager().getGlobalConfiguration()
                .getOptionInt(DOMAIN, "lastcheck");
        final int freq = IdentityManager.getIdentityManager().getGlobalConfiguration()
                .getOptionInt(DOMAIN, "frequency");
        final int timestamp = (int) (new Date().getTime() / 1000);
        int time = 0;

        if (last + freq > timestamp) {
            time = last + freq - timestamp;
        }

        if (time > freq || time < 0) {
            Logger.userError(ErrorLevel.LOW, "Attempted to schedule update check "
                    + (time < 0 ? "in the past" : "too far in the future")
                    + ", rescheduling.");
            time = 1;
        }

        timer.cancel();
        timer = new Timer("Update Checker Timer");
        timer.schedule(new TimerTask() {
            /** {@inheritDoc} */
            @Override
            public void run() {
                checkNow();
            }
        }, time * 1000);
    }

    /**
     * Checks for updates now.
     */
    public static void checkNow() {
        new Thread(new UpdateChecker(), "Update Checker thread").start();
    }

}
