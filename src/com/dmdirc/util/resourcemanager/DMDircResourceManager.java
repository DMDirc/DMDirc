/*
 * Copyright (c) 2006-2013 DMDirc Developers
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

package com.dmdirc.util.resourcemanager;

import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;

/**
 * Provides utility methods for working with resources from the DMDirc
 * distribution.
 */
public class DMDircResourceManager {

    private DMDircResourceManager() {
        //Prevent instantiation
    }

    /**
     * Returns the working directory for the application.
     *
     * @return Current working directory
     */
    public static synchronized String getCurrentWorkingDirectory() {
        return getWorkingDirectoryString(Thread.currentThread()
                .getContextClassLoader().getResource("com/dmdirc/Main.class"));
    }

    /**
     * Returns the working directory for the application
     *
     * @param mainClassURL Main class of the application
     *
     * @return location or null
     */
    public static String getWorkingDirectoryString(final URL mainClassURL) {
        try {
            return URLDecoder.decode(getWorkingDirectoryURL(mainClassURL).getPath(), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.userError(ErrorLevel.MEDIUM, "Unable to decode path");
        }
        return "";
    }

    /**
     * Returns the working directory for the application
     *
     * @param mainClassURL Main class of the application
     *
     * @return URL location or null
     */
    public static URL getWorkingDirectoryURL(final URL mainClassURL) {
        if (isRunningFromJar(mainClassURL)) {
            return getJarURL(mainClassURL);
        } else {
            return getFileURL();
        }
    }

    /**
     * Returns the URL of the folder the client is loaded from.
     *
     * @return URL of client folder file or null
     */
    protected static URL getFileURL() {
        return Thread.currentThread().getContextClassLoader().getResource("");
    }

    /**
     * Returns the URL of the jar the class is loaded from.
     *
     * @param mainClassURL Class to get Jar file from.
     *
     * @return URL of jar file or null
     */
    protected static URL getJarURL(final URL mainClassURL) {
        try {
            return new URI(mainClassURL.getPath().substring(0, mainClassURL.getPath()
                    .indexOf("!/"))).toURL();
        } catch (MalformedURLException | URISyntaxException ex) {
            return null;
        }
    }

    /**
     * Determines if this instance of DMDirc is running from a jar or not.
     *
     * @return True if this instance is running from a JAR, false otherwise
     */
    public static boolean isRunningFromJar() {
        return isRunningFromJar(Thread.currentThread().getContextClassLoader().
                getResource("com/dmdirc/Main.class"));

    }

    /**
     * Determines if this instance of DMDirc is running from a jar or not.
     *
     * @param mainClassURL URL of main class
     *
     * @return True if this instance is running from a JAR, false otherwise
     */
    protected static boolean isRunningFromJar(final URL mainClassURL) {
        return "jar".equals(mainClassURL.getProtocol());
    }
}
