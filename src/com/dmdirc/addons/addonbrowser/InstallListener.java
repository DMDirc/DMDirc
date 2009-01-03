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

package com.dmdirc.addons.addonbrowser;

import com.dmdirc.actions.ActionManager;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.util.Downloader;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;
import java.io.IOException;

/**
 * Addon info install listener.
 */
public class InstallListener implements ActionListener {

    /** Addon info. */
    private final AddonInfo info;

    /**
     * Instantiates a new install listener.
     * 
     * @param info Addoninfo to install
     */
    public InstallListener(final AddonInfo info) {
        this.info = info;
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        try {
            final File file = File.createTempFile("dmdirc-addon", ".tmp");
            file.deleteOnExit();

            Downloader.downloadPage("http://addons.dmdirc.com/addondownload/"
                    + info.getStableDownload(), file.getAbsolutePath());

            switch (info.getType()) {
                case TYPE_ACTION_PACK:
                    ActionManager.installActionPack(file.getAbsolutePath());
                    break;
                case TYPE_PLUGIN:
                    throw new UnsupportedOperationException("Not supported yet");
                    //break;
                case TYPE_THEME:
                    throw new UnsupportedOperationException("Not supported yet");
                    //break;
            }
        } catch (IOException ex) {
            Logger.userError(ErrorLevel.MEDIUM, "Unable to download addon: "
                    + ex.getMessage(), ex);
        }
    }
}
