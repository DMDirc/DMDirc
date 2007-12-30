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

package com.dmdirc.updater.components;

import com.dmdirc.updater.UpdateChecker;
import com.dmdirc.updater.UpdateComponent;

import java.io.File;

/**
 * Component for updates of DMDirc's launcher.
 *
 * @author chris
 */
public class LauncherComponent implements UpdateComponent {

    /** The platform of our current launcher. */
    private static String platform = "";

    /** The version of our current launcher. */
    private static int version = -1;

    /**
     * Parses the specified launcher information.
     *
     * @param info The platform and version of the launcher, separated by '-'.
     */
    public static void setLauncherInfo(final String info) {
        final int hpos = info.indexOf('-');

        if (hpos == -1) {
            return;
        }

        try {
            platform = info.substring(0, hpos);
            version = Integer.parseInt(info.substring(hpos + 1));
        } catch (NumberFormatException ex) {
            return;
        }

        UpdateChecker.registerComponent(new LauncherComponent());
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "launcher-" + platform;
    }

    /** {@inheritDoc} */
    @Override
    public int getVersion() {
        return version;
    }

    /** {@inheritDoc} */
    @Override
    public void doInstall(final String path) throws Throwable {
        if (platform.equalsIgnoreCase("Linux")) {
            final File tmpFile = new File(path);
            final File targetFile = new File(tmpFile.getParent() + File.separator + ".launcher.sh");

            if (targetFile.exists()) {
                targetFile.delete();
            }

            tmpFile.renameTo(targetFile);
        } else {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

}
