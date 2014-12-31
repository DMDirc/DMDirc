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

package com.dmdirc.updater.installing;

import com.dmdirc.updater.UpdateComponent;
import com.dmdirc.updater.retrieving.SingleFileRetrievalResult;
import com.dmdirc.util.collections.ListenerList;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link UpdateInstallationStrategy} which uses the old
 * {@link UpdateComponent#doInstall(java.nio.file.Path)} methods to perform installation.
 */
public class LegacyInstallationStrategy extends TypeSensitiveInstallationStrategy<UpdateComponent, SingleFileRetrievalResult> {

    private static final Logger LOG = LoggerFactory.getLogger(
            LegacyInstallationStrategy.class);
    /** List of registered listeners. */
    private final ListenerList listenerList = new ListenerList();

    /**
     * Creates a new {@link LegacyInstallationStrategy}.
     */
    @Inject
    public LegacyInstallationStrategy() {
        super(UpdateComponent.class, SingleFileRetrievalResult.class);
    }

    @Override
    protected void installImpl(final UpdateComponent component,
            final SingleFileRetrievalResult retrievalResult) {
        LOG.info("Installing file from {} for component {} using legacy strategy",
                retrievalResult.getFile(), component.getName());

        try {
            component.doInstall(retrievalResult.getFile());
            listenerList.getCallable(UpdateInstallationListener.class).installCompleted(component);
        } catch (Exception ex) {
            LOG.warn("Error installing update for {}", component.getName(), ex);
            listenerList.getCallable(UpdateInstallationListener.class).installFailed(component);
        }
    }

    @Override
    public void addUpdateInstallationListener(final UpdateInstallationListener listener) {
        listenerList.add(UpdateInstallationListener.class, listener);
    }

    @Override
    public void removeUpdateInstallationListener(final UpdateInstallationListener listener) {
        listenerList.remove(UpdateInstallationListener.class, listener);
    }

}
