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

package com.dmdirc.updater.retrieving;

import com.dmdirc.commandline.CommandLineOptionsModule.Directory;
import com.dmdirc.commandline.CommandLineOptionsModule.DirectoryType;
import com.dmdirc.updater.UpdateComponent;
import com.dmdirc.updater.checking.DownloadableUpdate;
import com.dmdirc.util.collections.ListenerList;
import com.dmdirc.util.io.DownloadListener;
import com.dmdirc.util.io.Downloader;

import java.io.IOException;
import java.nio.file.Path;

import javax.inject.Inject;

import org.slf4j.LoggerFactory;

/**
 * An {@link UpdateRetrievalStrategy} that downloads a file specified in a
 * {@link DownloadableUpdate}.
 */
public class DownloadRetrievalStrategy extends TypeSensitiveRetrievalStrategy<DownloadableUpdate> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(
            DownloadRetrievalStrategy.class);
    /** List of registered listeners. */
    private final ListenerList listenerList = new ListenerList();
    /** The directory to put temporary update files in. */
    private final Path directory;
    /** Downloader to download files. */
    private final Downloader downloader;

    /**
     * Creates a new {@link DownloadRetrievalStrategy} which will place its temporary files in the
     * given directory.
     *
     * @param directory  The directory to use to download files to#
     * @param downloader Used to download files
     */
    @Inject
    public DownloadRetrievalStrategy(@Directory(DirectoryType.BASE) final Path directory,
            final Downloader downloader) {
        super(DownloadableUpdate.class);

        this.directory = directory;
        this.downloader = downloader;
    }

    @Override
    protected UpdateRetrievalResult retrieveImpl(final DownloadableUpdate checkResult) {
        try {
            final Path file = getFile();

            listenerList.getCallable(UpdateRetrievalListener.class)
                    .retrievalProgressChanged(checkResult.getComponent(), 0);

            LOG.debug("Downloading file from {} to {}", checkResult.getUrl(), file);
            downloader.downloadPage(checkResult.getUrl().toString(), file,
                    new DownloadProgressListener(checkResult.getComponent()));

            listenerList.getCallable(UpdateRetrievalListener.class)
                    .retrievalCompleted(checkResult.getComponent());

            return new BaseSingleFileResult(checkResult, file);
        } catch (IOException ex) {
            LOG.warn("I/O exception downloading update from {}", checkResult.getUrl(), ex);
            listenerList.getCallable(UpdateRetrievalListener.class)
                    .retrievalFailed(checkResult.getComponent());
        }

        return new BaseRetrievalResult(checkResult, false);
    }

    /**
     * Creates a random local file name to download the remote file to.
     *
     * @return The full, local path to download the remote file to
     */
    private Path getFile() {
        return directory.resolve("update." + Math.round(10000 * Math.random()) + ".tmp");
    }

    @Override
    public void addUpdateRetrievalListener(final UpdateRetrievalListener listener) {
        listenerList.add(UpdateRetrievalListener.class, listener);
    }

    @Override
    public void removeUpdateRetrievalListener(final UpdateRetrievalListener listener) {
        listenerList.remove(UpdateRetrievalListener.class, listener);
    }

    /**
     * A {@link DownloadListener} which proxies progress updates on to this strategy's
     * {@link UpdateRetrievalListener}s.
     */
    private class DownloadProgressListener implements DownloadListener {

        /** The component to fire updates for. */
        private final UpdateComponent component;

        public DownloadProgressListener(final UpdateComponent component) {
            this.component = component;
        }

        @Override
        public void downloadProgress(final float percent) {
            listenerList.getCallable(UpdateRetrievalListener.class)
                    .retrievalProgressChanged(component, percent);
        }

        @Override
        public void setIndeterminate(final boolean indeterminate) {
            // Do nothing
        }

    }

}
