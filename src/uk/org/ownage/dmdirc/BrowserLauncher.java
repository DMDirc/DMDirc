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

import java.lang.reflect.Method;

import javax.swing.JOptionPane;

/**
 *
 */
public final class BrowserLauncher {
    
    /** Prevents creation of a new instance of BrowserLauncher. */
    private BrowserLauncher() {
    }
    
    public static void openURL(String url) {
        String osName = System.getProperty("os.name");
        try {
            if (osName.startsWith("Mac OS")) {
                Method openURL = Class.forName("com.apple.eio.FileManager")
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
                    String[] browsers =
                    { "firefox", "konqueror", "epiphany", "opera", "mozilla", };
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
                    throw new Exception("Could not find web browser");
                } else {
                    Runtime.getRuntime().exec(new String[] {browser, url});
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Unable to launch web browser, please edit preferences.");
        }
    }
    
}
