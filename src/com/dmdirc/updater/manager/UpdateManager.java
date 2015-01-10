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
import com.dmdirc.updater.installing.UpdateInstallationStrategy;
import com.dmdirc.updater.retrieving.UpdateRetrievalStrategy;

import java.util.Collection;

/**
 * Manages the update process for a collection of {@link UpdateComponent}s.
 */
public interface UpdateManager {

    /**
     * Adds a new check strategy to this manager. {@link UpdateCheckStrategy}s are responsible for
     * checking to see if an updated version of a set of components is available. If multiple
     * strategies are configured, their results are merged using this manager's
     * {@link CheckResultConsolidator}.
     *
     * @param strategy The strategy to be added.
     */
    void addCheckStrategy(final UpdateCheckStrategy strategy);

    /**
     * Adds a new installation strategy to this manager. {@link UpdateInstallationStrategy}s are
     * responsible for handling the installation of an update once it has been retrieved.
     *
     * @param strategy The strategy to be added.
     */
    void addInstallationStrategy(final UpdateInstallationStrategy strategy);

    /**
     * Adds a new retrieval strategy to this manager. {@link UpdateRetrievalStrategy}s are
     * responsible for retrieving the updated version of a component after an update has been
     * identified by a {@link UpdateCheckStrategy}.
     *
     * @param strategy The strategy to be added.
     */
    void addRetrievalStrategy(final UpdateRetrievalStrategy strategy);

    /**
     * Adds a new component to this manager. Components will be checked for updates the next time
     * {@link #checkForUpdates()} is called. If a component with the same name already exists, it
     * will be removed.
     *
     * @param component The component to be added
     */
    void addComponent(final UpdateComponent component);

    /**
     * Removes the specified component from this manager. If any operations are pending for the
     * component they may complete after this method has been called.
     *
     * @param component The component to be removed
     */
    void removeComponent(final UpdateComponent component);

    /**
     * Retrieves the collection of all components known by this manager.
     *
     * @return The set of all known {@link UpdateComponent}s.
     */
    Collection<UpdateComponent> getComponents();

    /**
     * Checks for updates for all registered {@link UpdateComponent}s.
     */
    void checkForUpdates();

    /**
     * Installs any updates for the specified component. If an update for the component is available
     * but has not yet been retrieved, this method should perform in the same way to a synchronous
     * call to {@link #retrieve(UpdateComponent)} followed by {@link #install(UpdateComponent)}.
     *
     * @param component The component to be installed
     */
    void install(UpdateComponent component);

    /**
     * Retrieves any update associated with the given component. The update will not be installed
     * until the {@link #install(UpdateComponent)} method is called.
     *
     * @param component The component to retrieve updates for
     */
    void retrieve(UpdateComponent component);

    /**
     * Retrieves the last update check result for the given component.
     *
     * @param component The component to retrieve a check result for
     *
     * @return The component's most recent associated {@link UpdateCheckResult}, * * * or
     *         <code>null</code> if the component has not been checked or has since been updated.
     */
    UpdateCheckResult getCheckResult(UpdateComponent component);

    /**
     * Adds the given status listener to this manager. The listener will be called for any future
     * status changes caused by this manager.
     *
     * @param listener The listener to be added
     */
    void addUpdateStatusListener(UpdateStatusListener listener);

    /**
     * Removes the given status listener from this manager.
     *
     * @param listener The listener to be removed
     */
    void removeUpdateStatusListener(UpdateStatusListener listener);

}
