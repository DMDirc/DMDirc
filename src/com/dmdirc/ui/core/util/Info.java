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

package com.dmdirc.ui.core.util;

import com.dmdirc.config.IdentityManager;

import java.util.Locale;

/**
 * Retrives various pieces of information about DMDirc.
 */
public class Info {
    
    /** Not intended to be instantiated. */
    private Info() {
        //Prevent instantiation.
    }

    /**
     * Returns the DMDirc version info.
     * 
     * @return DMDirc version string
     */
    public static String getDMDircVersion() {
        return IdentityManager.getGlobalConfig().getOption("version", "version")
                + " ("
                + IdentityManager.getGlobalConfig().getOption("updater", "channel")
                + ")";
    }

    /**
     * Returns the systems java version.
     * 
     * @return Java version string
     */
    public static String getJavaVersion() {
        return System.getProperty("java.vm.name", "unknown") + " " +
                System.getProperty("java.version", "unknown") +
                " [" + System.getProperty("java.vm.vendor", "uknown") + "]";
    }

    /**
     * Returns the systems OS version.
     * 
     * @return OS version string
     */
    public static String getOSVersion() {
        return System.getProperty("os.name", "unknown") + " " +
                System.getProperty("os.version", "unknown") + " " +
                System.getProperty("os.arch", "unknown") + "; " +
                System.getProperty("file.encoding", "unknown") + "; " + Locale.getDefault().
                toString();
    }
}
