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

package com.dmdirc.util;

import com.dmdirc.Main;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/** Handles URLs. */
public class URLHandler {

    /** Config manager. */
    private final ConfigManager config;
    /** Singleton instance. */
    private static final URLHandler me = new URLHandler();
    /** Desktop handler. */
    private final Desktop desktop;

    /** Instantiates a new URL Handler. */
    private URLHandler() {
        config = IdentityManager.getGlobalConfig();
        if (Desktop.isDesktopSupported()) {
            desktop = Desktop.getDesktop();
        } else {
            desktop = null;
        }
    }

    /**
     * Gets an instance of URLHandler.
     *
     * @return URLHandler instance
     */
    public static URLHandler getURLHander() {
        synchronized (me) {
            return me;
        }
    }

    /**
     * Launches an application for a given url.
     *
     * @param url URL to parse
     */
    public void launchApp(final String url) {
        final int end = url.indexOf(':');
        final String protocol = url.substring(0, end);
        final String info = url.substring(end + 3);

        if (!config.hasOption("protocol", protocol)) {
            Main.getUI().showURLDialog(protocol, info);
            return;
        }

        final List<String> app = config.getOptionList("protocol", protocol);

        if (app.size() < 1) {
            Main.getUI().showURLDialog(protocol, info);
            return;
        }

        final String command = app.get(0);

        try {
            if ("DMDIRC".equals(command)) {
                //Handle DMDirc link
            } else if ("BROWSER".equals(command)) {
                execBrowser(new URI(url));
            } else if ("MAIL".equals(command)) {
                execMail(new URI(url));
            } else {
                execApp(app.toArray(new String[app.size()]));
            }
        } catch (URISyntaxException ex) {
            Logger.userError(ErrorLevel.LOW, "Invalid URL: " + ex.getMessage());
        }
    }

    /**
     * Launches an application.
     *
     * @param application Application and arguments
     */
    private void execApp(final String[] application) {
        try {
            Runtime.getRuntime().exec(application);
        } catch (IOException ex) {
            Logger.userError(ErrorLevel.LOW,
                    "Unable to run application: " + ex.getMessage(), ex);
        }
    }

    /**
     * Opens the specified URL in the users browser.
     *
     * @param url URL to open
     */
    private void execBrowser(final URI url) {
        if (desktop != null &&
                desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(url);
            } catch (IOException ex) {
                Logger.userError(ErrorLevel.LOW,
                        "Unable to open URL: " + ex.getMessage());
            }
        } else {
            Logger.userError(ErrorLevel.LOW, "Unable to open your browser.");
        }
    }

    /**
     * Opens the specified URL in the users mail client.
     *
     * @param url URL to open
     */
    private void execMail(final URI url) {
        if (desktop != null && desktop.isSupported(Desktop.Action.MAIL)) {
            try {
                desktop.mail(url);
            } catch (IOException ex) {
                Logger.userError(ErrorLevel.LOW,
                        "Unable to open URL: " + ex.getMessage());
            }
        } else {
            Logger.userError(ErrorLevel.LOW, "Unable to open your mail client.");
        }
    }
}