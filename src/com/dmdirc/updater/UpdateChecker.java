/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.Main;
import com.dmdirc.Precondition;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.interfaces.UpdateListener;
import com.dmdirc.interfaces.UpdateCheckerListener;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.updater.Update.STATUS;
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
        /** New updates are available. */
        UPDATES_AVAILABLE
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
        public void updateStatusChange(final Update update, final STATUS status) {
            if (status == Update.STATUS.INSTALLED
                || status == Update.STATUS.ERROR) {
                removeUpdate(update);
            }
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

        if (!config.getOptionBool("updater", "enable", true)) {
            IdentityManager.getConfigIdentity().setOption("updater",
                    "lastcheck", String.valueOf((int) (new Date().getTime() / 1000)));
        
            init();
            return;
        }

        setStatus(STATE.CHECKING);

        updates.clear();

        final StringBuilder data = new StringBuilder();
        final String updateChannel
                = config.getOption("updater", "channel", Main.UPDATE_CHANNEL.toString());

        for (UpdateComponent component : components) {
            if (config.getOptionBool("updater", "enable-" + component.getName(), true)) {
                data.append(component.getName());
                data.append(',');
                data.append(updateChannel);
                data.append(',');
                data.append(component.getVersion());
                data.append(';');
            }
        }

        if (data.length() > 0) {
            try {
                final List<String> response
                    = Downloader.getPage("http://updates.dmdirc.com/", "data=" + data);

                updates.clear();

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
            setStatus(STATE.UPDATES_AVAILABLE);
        }
        
        mutex.release();

        UpdateChecker.init();

        IdentityManager.getConfigIdentity().setOption("updater",
                "lastcheck", String.valueOf((int) (new Date().getTime() / 1000)));
    }

    /**
     * Checks the specified line to determine the message from the update server.
     *
     * @param line The line to be checked
     */
    private void checkLine(final String line) {
        if (line.startsWith("uptodate")) {
            // Do nothing
        } else if (line.startsWith("outofdate")) {
            doUpdateAvailable(line);
        } else if (line.startsWith("error")) {
            Logger.userError(ErrorLevel.LOW, "Error when checking for updates: "
                    + line.substring(6));
        } else {
            Logger.userError(ErrorLevel.LOW, "Unknown update line received from server: "
                    + line);
        }
    }

    /**
     * Informs the user that there's an update available.
     *
     * @param line The line that was received from the update server
     */
    public void doUpdateAvailable(final String line) {
        final Update update = new Update(line);

        updates.add(update);
        update.addUpdateListener(listener);
    }

    /**
     * Initialises the update checker. Sets a timer to check based on the
     * frequency specified in the config.
     */
    public static void init() {
        final int last
                = IdentityManager.getGlobalConfig().getOptionInt("updater", "lastcheck", 0);
        final int freq
                = IdentityManager.getGlobalConfig().getOptionInt("updater", "frequency", 86400);
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
     * the update has finished or has encountered an error.
     *
     * @param update The update to be removed
     */
    private static void removeUpdate(final Update update) {
        updates.remove(update);

        if (updates.isEmpty()) {
            setStatus(STATE.IDLE);
        }
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
     * 
     * @return true iif the update component is enabled
     */
    public static boolean isEnabled(final UpdateComponent component) {
        return IdentityManager.getGlobalConfig().getOptionBool("updater", 
                component.getName(), true);
    }

}
