/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.updater.components;

import com.dmdirc.updater.UpdateComponent;
import com.dmdirc.updater.Version;
import com.dmdirc.updater.manager.UpdateManager;
import com.dmdirc.util.resourcemanager.ZipResourceManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Component for updates of DMDirc's launcher.
 */
public class LauncherComponent implements UpdateComponent {

    /** The platform of our current launcher. */
    private static String platform = "";
    /** The version of our current launcher. */
    private static Version version = new Version();

    /**
     * Parses the specified launcher information.
     *
     * @param manager THe manager to update with the launcher component.
     * @param info    The platform and version of the launcher, separated by '-'.
     */
    public static void setLauncherInfo(final UpdateManager manager, final String info) {
        final int hpos = info.indexOf('-');
        final int cpos = info.indexOf(',');

        if (hpos == -1) {
            return;
        }

        platform = info.substring(0, hpos);
        if (cpos == -1) {
            version = new Version(info.substring(hpos + 1));
        } else {
            version = new Version(info.substring(hpos + 1, cpos));
        }

        manager.addComponent(new LauncherComponent());
    }

    /**
     * Determines if the client has been run using the launcher.
     *
     * @return True if the launcher has been used, false otherwise
     */
    public static boolean isUsingLauncher() {
        return version.isValid();
    }

    @Override
    public String getName() {
        return "launcher-" + platform;
    }

    @Override
    public String getFriendlyName() {
        return "Launcher";
    }

    @Override
    public String getFriendlyVersion() {
        return String.valueOf(getVersion());
    }

    @Override
    public Version getVersion() {
        return version;
    }

    @Override
    public boolean requiresRestart() {
        return true;
    }

    @Override
    public boolean requiresManualInstall() {
        return false;
    }

    @Override
    public String getManualInstructions(final Path path) {
        return "";
    }

    @Override
    public boolean doInstall(final Path path) throws IOException {
        final File tmpFile = path.toFile();
        if ("Linux".equalsIgnoreCase(platform)
                || "unix".equalsIgnoreCase(platform)) {
            final File targetFile = new File(tmpFile.getParent()
                    + File.separator + ".launcher.sh");

            if (targetFile.exists()) {
                targetFile.delete();
            }

            tmpFile.renameTo(targetFile);
            targetFile.setExecutable(true);

        } else {
            ZipResourceManager.getInstance(path.toAbsolutePath().toString())
                    .extractResources("", tmpFile.getParent() + File.separator);
            path.toFile().delete();
        }
        return true;
    }

}
