/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

package com.dmdirc.plugins;

import com.dmdirc.commandline.CommandLineOptionsModule.Directory;
import com.dmdirc.commandline.CommandLineOptionsModule.DirectoryType;
import com.dmdirc.util.resourcemanager.ResourceManager;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.dmdirc.util.LogUtils.USER_ERROR;

/**
 * Utility class that can extract bundled plugins.
 */
@Singleton
public class CorePluginExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(CorePluginExtractor.class);
    /** The plugin manager to inform when plugins are updated. */
    private final PluginManager pluginManager;
    /** The directory to extract plugins to. */
    private final String pluginDir;

    /**
     * Creates a new instance of {@link CorePluginExtractor}.
     *
     * @param pluginManager The plugin manager to inform when plugins are updated.
     * @param pluginDir     The directory to extract plugins to.
     */
    @Inject
    public CorePluginExtractor(final PluginManager pluginManager,
            @Directory(DirectoryType.PLUGINS) final String pluginDir) {
        this.pluginManager = pluginManager;
        this.pluginDir = pluginDir;
    }

    /**
     * Extracts plugins bundled with DMDirc to the user's profile's plugin directory.
     *
     * @param prefix If non-null, only plugins whose file name starts with this prefix will be
     *               extracted.
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
                        resourceName.lastIndexOf('/')) + '/');

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
                            .getAbsolutePath().substring(pluginDir.length()));

                    if (plugin != null) {
                        plugin.pluginUpdated();
                    }
                }
            } catch (PluginException | IOException ex) {
                LOG.info(USER_ERROR, "Failed to extract plugins.", ex);
            }
        }
    }

}
