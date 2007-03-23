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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import uk.org.ownage.dmdirc.logger.ErrorLevel;
import uk.org.ownage.dmdirc.logger.Logger;

/**
 *
 */
public final class BrowserLauncher {
    
    /** Prevents creation of a new instance of BrowserLauncher. */
    private BrowserLauncher() {
    }
    
    /**
     * Opens a URL in the default browser where possible, else any availble
     * browser it finds.
     * @param url url to open in the browser
     */
    public static void openURL(final String url) {
        final String osName = System.getProperty("os.name");
        try {
            if (osName.startsWith("Mac OS")) {
                final Method openURL = Class.forName("com.apple.eio.FileManager")
                .getDeclaredMethod("openURL", new Class[] {String.class});
                openURL.invoke(null, new Object[] {url});
            } else if (osName.startsWith("Windows")) {
                Runtime.getRuntime()
                .exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else {
                //assume Unix or Linux
                String browser = null;
                if (Config.hasOption("general", "browser")) {
                    browser = Config.getOption("general", "browser");
                } else {
                    final String[] browsers =
                    {"firefox", "konqueror", "epiphany", "opera", "mozilla", };
                    for (int count = 0; count < browsers.length
                            && browser == null; count++) {
                        if (Runtime.getRuntime()
                        .exec(new String[] {"which", browsers[count]})
                        .waitFor() == 0) {
                            browser = browsers[count];
                        }
                    }
                }
                if (browser == null) {
                    Logger.error(ErrorLevel.ERROR, "Unable to find browser, " 
                            + "please set in preferences.");
                } else {
                    Runtime.getRuntime().exec(new String[] {browser, url});
                }
            }
        } catch (ClassNotFoundException e) {
            Logger.error(ErrorLevel.ERROR, e);
        } catch (NoSuchMethodException e) {
            Logger.error(ErrorLevel.ERROR, e);
        } catch (IllegalAccessException e) {
            Logger.error(ErrorLevel.ERROR, e);
        } catch (InterruptedException e) {
            Logger.error(ErrorLevel.ERROR, e);
        } catch (InvocationTargetException e) {
            Logger.error(ErrorLevel.ERROR, e);
        } catch (IOException e) {
            Logger.error(ErrorLevel.ERROR, e);
        }
    }
    
}
