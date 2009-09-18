/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.Precondition;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.updater.components.ClientComponent;
import com.dmdirc.updater.components.DefaultsComponent;
import com.dmdirc.updater.components.ModeAliasesComponent;
import com.dmdirc.util.Downloader;
import com.dmdirc.util.ListenerList;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

/**
 * The update checker contacts the DMDirc website to check to see if there
 * are any updates available.
 *
 * @author chris
 */
public final class UpdateChecker implements Runnable {

    /** The possible states for the checker. */
    public static enum STATE {
        /** Nothing's happening. */
        IDLE,
        /** Currently checking for updates. */
        CHECKING,
        /** Currently updating. */
        UPDATING,
        /** New updates are available. */
        UPDATES_AVAILABLE,
        /** Updates installed but restart needed. */
        RESTART_REQUIRED,
    }

    /** Semaphore used to prevent multiple invocations. */
    private static final Semaphore mutex = new Semaphore(1);

    /** A list of components that we're to check. */
    private static final List<UpdateComponent> components
            = new ArrayList<UpdateComponent>();

    /** Our timer. */
    private static Timer timer = new Timer("Update Checker Timer");

    /** The list of updates that are available. */
    private static final List<Update> updates = new ArrayList<Update>();

    /** A list of our listeners. */
    private static final ListenerList listeners = new ListenerList();

    /** Our current state. */
    private static STATE status = STATE.IDLE;

    /** A reference to the listener we use for update status changes. */
    private static final UpdateListener listener = new UpdateListener() {
        @Override
        public void updateStatusChange(final Update update, final UpdateStatus status) {
            if (status == UpdateStatus.INSTALLED
                || status == UpdateStatus.ERROR) {
                removeUpdate(update);
            } else if (status == UpdateStatus.RESTART_NEEDED && UpdateChecker.status
                    == STATE.UPDATING) {
                doNextUpdate();
            }
        }

        @Override
        public void updateProgressChange(final Update update, final float progress) {
            // Don't care
        }
    };

    static {
        components.add(new ClientComponent());
        components.add(new ModeAliasesComponent());
        components.add(new DefaultsComponent());
    }

    /**
     * Instantiates an Updatechecker.
     */
    public UpdateChecker() {
        //Ignore
    }

    /** {@inheritDoc} */
    @Override
    public void run() {
        if (!mutex.tryAcquire()) {
            // Duplicate invocation

            return;
        }

        final ConfigManager config = IdentityManager.getGlobalConfig();

        if (!config.getOptionBool("updater", "enable")
                || status == STATE.UPDATING) {
            IdentityManager.getConfigIdentity().setOption("updater",
                    "lastcheck", String.valueOf((int) (new Date().getTime() / 1000)));
        
            mutex.release();
            init();
            return;
        }

        setStatus(STATE.CHECKING);

        // Remove any existing update that isn't waiting for a restart.
        for (Update update : new ArrayList<Update>(updates)) {
            if (update.getStatus() != UpdateStatus.RESTART_NEEDED) {
                updates.remove(update);
            }
        }

        final StringBuilder data = new StringBuilder();
        final String updateChannel = config.getOption("updater", "channel");

        // Build the data string to send to the server
        for (UpdateComponent component : components) {
            if (isEnabled(component)) {
                data.append(component.getName());
                data.append(',');
                data.append(updateChannel);
                data.append(',');
                data.append(component.getVersion());
                data.append(';');
            }
        }

        // If we actually have components to check
        if (data.length() > 0) {
            try {
                final List<String> response
                    = Downloader.getPage("http://updates.dmdirc.com/", "data=" + data);

                for (String line : response) {
                    checkLine(line);
                }
            } catch (MalformedURLException ex) {
                Logger.appError(ErrorLevel.LOW, "Error when checking for updates", ex);
            } catch (IOException ex) {
                Logger.userError(ErrorLevel.LOW,
                        "I/O error when checking for updates: " + ex.getMessage());
            }
        }

        if (updates.isEmpty()) {
            setStatus(STATE.IDLE);
        } else {
            boolean available = false;

            // Check to see if the updates are outstanding or just waiting for
            // a restart
            for (Update update : updates) {
                if (update.getStatus() == UpdateStatus.PENDING) {
                    available = true;
                }
            }
            
            setStatus(available ? STATE.UPDATES_AVAILABLE : STATE.RESTART_REQUIRED);
        }

        mutex.release();
        
        IdentityManager.getConfigIdentity().setOption("updater",
                "lastcheck", String.valueOf((int) (new Date().getTime() / 1000)));
        
        UpdateChecker.init();
        
        if (config.getOptionBool("updater", "autoupdate")) {
            applyUpdates();
        }        
    }

    /**
     * Checks the specified line to determine the message from the update server.
     *
     * @param line The line to be checked
     */
    private void checkLine(final String line) {
        if (line.startsWith("outofdate")) {
            doUpdateAvailable(line);
        } else if (line.startsWith("error")) {
            String errorMessage = "Error when checking for updates: " + line.substring(6);
            final String[] bits = line.split(" ");
            if (bits.length > 2) {
                final UpdateComponent thisComponent = findComponent(bits[2]);
                if (thisComponent != null) {
                    if (thisComponent instanceof FileComponent) {
                        errorMessage = errorMessage + " (" + ((FileComponent)thisComponent).getFileName() + ")";
                    }
                }
            }
            Logger.userError(ErrorLevel.LOW, errorMessage);
        } else if (!line.startsWith("uptodate")) {
            Logger.userError(ErrorLevel.LOW, "Unknown update line received from server: "
                    + line);
        }
    }

    /**
     * Informs the user that there's an update available.
     *
     * @param line The line that was received from the update server
     */
    private void doUpdateAvailable(final String line) {
        final Update update = new Update(line);

        if (update.getUrl() != null) {
            updates.add(update);
            update.addUpdateListener(listener);
        }
    }

    /**
     * Initialises the update checker. Sets a timer to check based on the
     * frequency specified in the config.
     */
    public static void init() {
        final int last = IdentityManager.getGlobalConfig()
                .getOptionInt("updater", "lastcheck");
        final int freq = IdentityManager.getGlobalConfig()
                .getOptionInt("updater", "frequency");
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

    /**
     * Registers an update component.
     *
     * @param component The component to be registered
     */
    public static void registerComponent(final UpdateComponent component) {
        components.add(component);
    }

    /**
     * Unregisters an update component with the specified name.
     *
     * @param name The name of the component to be removed
     */
    public static void removeComponent(final String name) {
        UpdateComponent target = null;

        for (UpdateComponent component : components) {
            if (name.equals(component.getName())) {
                target = component;
            }
        }

        if (target != null) {
            components.remove(target);
        }
    }

    /**
     * Finds and returns the component with the specified name.
     *
     * @param name The name of the component that we're looking for
     * @return The corresponding UpdateComponent, or null if it's not found
     */
    @Precondition("The specified name is not null")
    public static UpdateComponent findComponent(final String name) {
        assert(name != null);

        for (UpdateComponent component : components) {
            if (name.equals(component.getName())) {
                return component;
            }
        }

        return null;
    }

    /**
     * Removes the specified update from the list. This should be called when
     * the update has finished, has encountered an error, or the user does not
     * want the update to be performed.
     *
     * @param update The update to be removed
     */
    public static void removeUpdate(final Update update) {
        update.removeUpdateListener(listener);
        updates.remove(update);

        if (updates.isEmpty()) {
            setStatus(STATE.IDLE);
        } else if (status == STATE.UPDATING) {
            doNextUpdate();
        }
    }
    
    /**
     * Downloads and installs all known updates.
     */
    public static void applyUpdates() {
        if (!updates.isEmpty()) {
            setStatus(STATE.UPDATING);
            doNextUpdate();
        }
    }
    
    /**
     * Finds and applies the next pending update, or sets the state to idle
     * / restart needed if appropriate.
     */
    private static void doNextUpdate() {
        boolean restart = false;
        
        for (Update update : updates) {
            if (update.getStatus() == UpdateStatus.PENDING) {
                update.doUpdate();
                return;
            } else if (update.getStatus() == UpdateStatus.RESTART_NEEDED) {
                restart = true;
            }
        }
        
        setStatus(restart ? STATE.RESTART_REQUIRED : STATE.IDLE);
    }

    /**
     * Retrieves a list of components registered with the checker.
     *
     * @return A list of registered components
     */
    public static List<UpdateComponent> getComponents() {
        return components;
    }

    /**
     * Retrives a list of available updates from the checker.
     *
     * @return A list of available updates
     */
    public static List<Update> getAvailableUpdates() {
        return updates;
    }


    /**
     * Adds a new status listener to the update checker.
     *
     * @param listener The listener to be added
     */
    public static void addListener(final UpdateCheckerListener listener) {
        listeners.add(UpdateCheckerListener.class, listener);
    }

    /**
     * Removes a status listener from the update checker.
     *
     * @param listener The listener to be removed
     */
    public static void removeListener(final UpdateCheckerListener listener) {
        listeners.remove(UpdateCheckerListener.class, listener);
    }

    /**
     * Retrieves the current status of the update checker.
     *
     * @return The update checker's current status
     */
    public static STATE getStatus() {
        return status;
    }

    /**
     * Sets the status of the update checker to the specified new status.
     *
     * @param newStatus The new status of this checker
     */
    private static void setStatus(final STATE newStatus) {
        status = newStatus;

        for (UpdateCheckerListener myListener : listeners.get(UpdateCheckerListener.class)) {
            myListener.statusChanged(newStatus);
        }
    }

    /**
     * Checks is a specified component is enabled.
     *
     * @param component Update component to check state
     * @return true iif the update component is enabled
     */
    public static boolean isEnabled(final UpdateComponent component) {
        return !IdentityManager.getGlobalConfig().hasOptionBool("updater",
                "enable-" + component.getName()) || IdentityManager.getGlobalConfig()
                .getOptionBool("updater", "enable-" + component.getName());
    }

}
