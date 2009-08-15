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

package com.dmdirc.updater.components;

import com.dmdirc.updater.UpdateChecker;
import com.dmdirc.updater.UpdateComponent;
import com.dmdirc.updater.Version;
import com.dmdirc.util.resourcemanager.ZipResourceManager;

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

    /**
     * Determines if the client has been run using the launcher.
     *
     * @return True if the launcher has been used, false otherwise
     */
    public static boolean isUsingLauncher() {
        return version != -1;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "launcher-" + platform;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getFriendlyName() {
        return "Launcher";
    }    

    /** {@inheritDoc} */
    @Override
    public String getFriendlyVersion() {
        return String.valueOf(getVersion());
    }

    /** {@inheritDoc} */
    @Override
    public Version getVersion() {
        return new Version(version);
    }

    /** {@inheritDoc} */
    @Override
    public boolean doInstall(final String path) throws Exception {
        final File tmpFile = new File(path);
        if (platform.equalsIgnoreCase("Linux") || platform.equalsIgnoreCase("unix")) {
            final File targetFile = new File(tmpFile.getParent() + File.separator + ".launcher.sh");

            if (targetFile.exists()) {
                targetFile.delete();
            }

            tmpFile.renameTo(targetFile);
            targetFile.setExecutable(true);
            return true;
        } else {
            final ZipResourceManager ziprm = ZipResourceManager.getInstance(path);
            ziprm.extractResources("", tmpFile.getParent()+ File.separator);
            new File(path).delete();
            return true;
        }
    }

}
