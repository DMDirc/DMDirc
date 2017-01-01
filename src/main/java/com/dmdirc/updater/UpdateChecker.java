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

package com.dmdirc.updater;

import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.updater.manager.CachingUpdateManager;
import com.dmdirc.updater.manager.UpdateStatus;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.dmdirc.util.LogUtils.USER_ERROR;

/**
 * The update checker contacts the DMDirc website to check to see if there are any updates
 * available.
 */
public final class UpdateChecker implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateChecker.class);
    /** The domain to use for updater settings. */
    private static final String DOMAIN = "updater";
    /** Semaphore used to prevent multiple invocations. */
    private static final Semaphore MUTEX = new Semaphore(1);
    /** Our timer. */
    private static Timer timer = new Timer("Update Checker Timer");
    /** The update manager to use. */
    private final CachingUpdateManager updateManager;
    /** The controller to use to read and write settings. */
    private final IdentityController identityController;

    /**
     * Creates a new instance of {@link UpdateChecker}.
     *
     * @param updateManager      The manager to use to perform updates.
     * @param identityController The controller to use to read and write settings.
     */
    public UpdateChecker(
            final CachingUpdateManager updateManager,
            final IdentityController identityController) {
        this.updateManager = updateManager;
        this.identityController = identityController;
    }

    @Override
    public void run() {
        if (!MUTEX.tryAcquire()) {
            // Duplicate invocation
            return;
        }

        final AggregateConfigProvider config = identityController.getGlobalConfiguration();

        if (!config.getOptionBool(DOMAIN, "enable")) {
            identityController.getUserSettings().setOption(DOMAIN,
                    "lastcheck", String.valueOf((int) (new Date().getTime() / 1000)));

            MUTEX.release();
            init(updateManager, identityController);
            return;
        }

        updateManager.checkForUpdates();

        MUTEX.release();

        identityController.getUserSettings().setOption(DOMAIN,
                "lastcheck", String.valueOf((int) (new Date().getTime() / 1000)));

        init(updateManager, identityController);

        if (config.getOptionBool(DOMAIN, "autoupdate")) {
            updateManager.getComponents().stream()
                    .filter(component -> updateManager.getStatus(component) ==
                            UpdateStatus.UPDATE_PENDING).forEach(updateManager::install);
        } else if (config.getOptionBool(DOMAIN, "autodownload")) {
            updateManager.getComponents().stream()
                    .filter(component -> updateManager.getStatus(component) ==
                            UpdateStatus.UPDATE_PENDING).forEach(updateManager::retrieve);
        }
    }

    /**
     * Initialises the update checker. Sets a timer to check based on the frequency specified in the
     * config.
     *
     * @param manager    Manager to monitor updates
     * @param controller The controller to use to retrieve and update settings.
     */
    public static void init(
            final CachingUpdateManager manager,
            final IdentityController controller) {
        final int last = controller.getGlobalConfiguration()
                .getOptionInt(DOMAIN, "lastcheck");
        final int freq = controller.getGlobalConfiguration()
                .getOptionInt(DOMAIN, "frequency");
        final int timestamp = (int) (new Date().getTime() / 1000);
        int time = 0;

        if (last + freq > timestamp) {
            time = last + freq - timestamp;
        }

        if (time > freq || time < 0) {
            LOG.info(USER_ERROR, "Attempted to schedule update check " + (time < 0
                            ? "in the past" : "too far in the future") + ", rescheduling.");
            time = 1;
        }

        timer.cancel();
        timer = new Timer("Update Checker Timer");
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                checkNow(manager, controller);
            }
        }, time * 1000);
    }

    /**
     * Checks for updates now.
     *
     * @param updateManager      The manager to use for checking.
     * @param identityController The controller to use to retrieve and update settings.
     */
    public static void checkNow(
            final CachingUpdateManager updateManager,
            final IdentityController identityController) {
        checkNow(updateManager, identityController, "Update Checker thread");
    }

    /**
     * Checks for updates now.
     *
     * @param updateManager      The manager to use for checking.
     * @param identityController The controller to use to retrieve and update settings.
     * @param threadName         The name of the thread to use to run the checker in.
     */
    public static void checkNow(
            final CachingUpdateManager updateManager,
            final IdentityController identityController,
            final String threadName) {
        new Thread(new UpdateChecker(updateManager, identityController), threadName)
                .start();
    }

}
