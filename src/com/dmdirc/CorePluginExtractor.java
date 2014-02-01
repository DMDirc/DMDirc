/*
 * Copyright (c) 2006-2014 DMDirc Developers
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

package com.dmdirc;

import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.util.resourcemanager.ResourceManager;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Utility class that can extract bundled plugins.
 */
public class CorePluginExtractor {

    /** The plugin manager to inform when plugins are updated. */
    private final PluginManager pluginManager;
    /** The directory to extract plugins to. */
    private final String pluginDir;

    /**
     * Creates a new instance of {@link CorePluginExtractor}.
     *
     * @param pluginManager The plugin manager to inform when plugins are updated.
     * @param pluginDir The directory to extract plugins to.
     */
    public CorePluginExtractor(final PluginManager pluginManager, final String pluginDir) {
        this.pluginManager = pluginManager;
        this.pluginDir = pluginDir;
    }

    /**
     * Extracts plugins bundled with DMDirc to the user's profile's plugin
     * directory.
     *
     * @param prefix If non-null, only plugins whose file name starts with
     * this prefix will be extracted.
     */
    public void extractCorePlugins(final String prefix) {
        final Map<String, byte[]> resources = ResourceManager.getResourceManager()
                .getResourcesStartingWithAsBytes("plugins");
        for (Map.Entry<String, byte[]> resource : resources.entrySet()) {
            try {
                final String resourceName = pluginDir + resource.getKey().substring(7);

                if (prefix != null && !resource.getKey().substring(8).startsWith(prefix)) {
                    continue;
                }

                final File newDir = new File(resourceName.substring(0,
                        resourceName.lastIndexOf('/')) + "/");

                if (!newDir.exists()) {
                    newDir.mkdirs();
                }

                final File newFile = new File(newDir,
                        resourceName.substring(resourceName.lastIndexOf('/') + 1,
                        resourceName.length()));

                if (!newFile.isDirectory()) {
                    ResourceManager.getResourceManager().
                            resourceToFile(resource.getValue(), newFile);

                    final PluginInfo plugin = pluginManager.getPluginInfo(newFile
                            .getAbsolutePath().substring(pluginManager.getDirectory().length()));

                    if (plugin != null) {
                        plugin.pluginUpdated();
                    }
                }
            } catch (IOException ex) {
                Logger.userError(ErrorLevel.LOW, "Failed to extract plugins", ex);
            }
        }
    }

}
