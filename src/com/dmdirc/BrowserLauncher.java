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

package uk.org.ownage.dmdirc;

import java.awt.Desktop;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;

import uk.org.ownage.dmdirc.logger.ErrorLevel;
import uk.org.ownage.dmdirc.logger.Logger;

/**
 * The BrowserLauncher handles the opening of the user's web browser when
 * they click a link.
 */
public final class BrowserLauncher {
    
    /** The time a browser was last launched. */
    private static Date lastLaunch;
    
    /** Prevents creation of a new instance of BrowserLauncher. */
    private BrowserLauncher() {
    }
    
    /**
     * Opens a URL in the default browser where possible, else any availble
     * browser it finds.
     * @param url url to open in the browser
     */
    public static void openURL(final String url) {
        try {
            if (url.startsWith("http://") || url.startsWith("https://")) {
                openURL(new URL(url));
            } else {
                openURL(new URL("http://" + url));
            }
            return;
        } catch (MalformedURLException ex) {
            Logger.error(ErrorLevel.WARNING, "Invalid URL", ex);
        }
    }
    
    /**
     * Opens a URL in the default browser where possible, else any availble
     * browser it finds.
     * @param url url to open in the browser
     */
    public static void openURL(final URL url) {
        final Date oldLaunch = lastLaunch;
        lastLaunch = new Date();
        
        if (Config.getOptionBool("browser", "uselaunchdelay") && lastLaunch != null) {
            final Long diff = lastLaunch.getTime() - oldLaunch.getTime();
            
            if (diff < Config.getOptionInt("browser", "launchdelay", 500)) {
                return;
            }
        }
        
        Desktop desktop = null;
        if (Desktop.isDesktopSupported()) {
            desktop = Desktop.getDesktop();
        }
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(url.toURI());
            } catch (IOException ex) {
                openURLLinux(url.toString());
            } catch (URISyntaxException ex) {
                Logger.error(ErrorLevel.WARNING, "Invalid URL", ex);
            }
        } else {
            openURLLinux(url.toString());
            
        }
    }
    
    /**
     * Attempts to open the url in a linux browser.
     *
     * @param url url to open
     */
    private static void openURLLinux(final String url) {
        String browser;
        browser = getBrowserLinux();
        if (browser == null) {
            Logger.error(ErrorLevel.ERROR, "Unable to find browser, "
                    + "please set in preferences.");
        } else {
            runBrowser(url, browser);
        }
    }
    
    /**
     * Attempts to obtain a browser for linux, using specified values if
     * possible.
     *
     * @return full browser binary path
     */
    private static String getBrowserLinux() {
        String browser = null;
        if (Config.hasOption("general", "browser")) {
            browser = Config.getOption("general", "browser");
        } else {
            final String[] browsers =
            {"firefox", "konqueror", "epiphany", "opera", "mozilla", };
            for (int count = 0; count < browsers.length
                    && browser == null; count++) {
                try {
                    if (Runtime.getRuntime()
                    .exec(new String[] {"which", browsers[count]}).waitFor() == 0) {
                        browser = browsers[count];
                    }
                } catch (IOException ex) {
                    Logger.error(ErrorLevel.WARNING, "Unable to find a browser", ex);
                } catch (InterruptedException ex) {
                    Logger.error(ErrorLevel.WARNING, "Unable to find a browser", ex);
                }
            }
        }
        return browser;
    }
    
    /**
     * Attempts to open the url in the specified browser.
     * @param url url to open
     * @param browser browser to use
     * @throws IOException if unable to open browser
     */
    private static void runBrowser(final String url, final String browser) {
        try {
            Runtime.getRuntime().exec(new String[] {browser, url});
        } catch (IOException ex) {
            Logger.error(ErrorLevel.WARNING, "Unable to run the browser", ex);
        }
    }
    
}
