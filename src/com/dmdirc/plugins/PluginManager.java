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

import com.dmdirc.DMDircMBassador;
import com.dmdirc.events.AppErrorEvent;
import com.dmdirc.events.ClientPrefsClosedEvent;
import com.dmdirc.events.ClientPrefsOpenedEvent;
import com.dmdirc.events.PluginRefreshEvent;
import com.dmdirc.events.UserErrorEvent;
import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.updater.components.PluginComponent;
import com.dmdirc.updater.manager.UpdateManager;
import com.dmdirc.util.collections.MapList;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import dagger.ObjectGraph;
import net.engio.mbassy.listener.Handler;

/**
 * Searches for and manages plugins and services.
 */
public class PluginManager {

    /** List of known plugins' file names to their corresponding {@link PluginInfo} objects. */
    private final Map<String, PluginInfo> knownPlugins = new HashMap<>();
    /** Set of known plugins' metadata. */
    private final Collection<PluginMetaData> plugins = new HashSet<>();
    /** Directory where plugins are stored. */
    private final String directory;
    /** The identity controller to use to find configuration options. */
    private final IdentityController identityController;
    /** The update manager to inform about plugins. */
    private final UpdateManager updateManager;
    /** A provider of initialisers for plugin injectors. */
    private final Provider<PluginInjectorInitialiser> initialiserProvider;
    /** Global ClassLoader used by plugins from this manager. */
    private final GlobalClassLoader globalClassLoader;
    /** The graph to pass to plugins for DI purposes. */
    private final ObjectGraph objectGraph;
    /** Event bus to pass to plugin info for plugin loaded events. */
    private final DMDircMBassador eventBus;
    /** The service manager to use. */
    private final ServiceManager serviceManager;

    /**
     * Creates a new instance of PluginManager.
     *
     * @param eventBus            The event bus to subscribe to events on
     * @param identityController  The identity controller to use for configuration options.
     * @param updateManager       The update manager to inform about plugins.
     * @param initialiserProvider A provider of initialisers for plugin injectors.
     * @param objectGraph         The graph to pass to plugins for DI purposes.
     * @param directory           The directory to load plugins from.
     */
    public PluginManager(
            final DMDircMBassador eventBus,
            final ServiceManager serviceManager,
            final IdentityController identityController,
            final UpdateManager updateManager,
            final Provider<PluginInjectorInitialiser> initialiserProvider,
            final ObjectGraph objectGraph,
            final String directory) {
        this.identityController = identityController;
        this.serviceManager = serviceManager;
        this.updateManager = updateManager;
        this.initialiserProvider = initialiserProvider;
        this.directory = directory;
        this.globalClassLoader = new GlobalClassLoader(this);
        this.objectGraph = objectGraph;
        this.eventBus = eventBus;

        eventBus.subscribe(this);
    }

    /**
     * Get the global class loader in use for this plugin manager.
     *
     * @return Global Class Loader
     */
    public GlobalClassLoader getGlobalClassLoader() {
        return globalClassLoader;
    }

    /**
     * Autoloads plugins.
     */
    public void doAutoLoad() {
        for (String plugin : identityController.getGlobalConfiguration().getOptionList("plugins",
                "autoload")) {
            plugin = plugin.trim();
            if (!plugin.isEmpty() && plugin.charAt(0) != '#' && getPluginInfo(plugin) != null) {
                getPluginInfo(plugin).loadPlugin();
            }
        }
    }

    /**
     * Tests and adds the specified plugin to the known plugins list. Plugins will only be added if:
     * <ul><li>The file exists,<li>No other plugin with the same name is known,<li>All requirements
     * are met for the plugin,
     * <li>The plugin has a valid config file that can be read</ul>.
     *
     * @param filename Filename of Plugin jar
     *
     * @return True if the plugin is in the known plugins list (either before this invocation or as
     *         a result of it), false if it was not added for one of the reasons outlined above.
     */
    public boolean addPlugin(final String filename) {
        if (knownPlugins.containsKey(filename.toLowerCase())) {
            return true;
        }

        if (!new File(directory, filename).exists()) {
            eventBus.publish(new UserErrorEvent(ErrorLevel.MEDIUM, null,
                    "Error loading plugin " + filename + ": File does not exist", ""));
            return false;
        }

        try {
            final PluginMetaData metadata = new PluginMetaData(this,
                    new URL("jar:file:" + directory + filename
                            + "!/META-INF/plugin.config"),
                    Paths.get(directory, filename));
            metadata.load();
            final PluginInfo pluginInfo = new PluginInfo(this, serviceManager, directory, metadata,
                    initialiserProvider, eventBus,
                    identityController, objectGraph);
            final PluginInfo existing = getPluginInfoByName(metadata.getName());
            if (existing != null) {
                eventBus.publish(new UserErrorEvent(ErrorLevel.MEDIUM, null,
                        "Duplicate Plugin detected, Ignoring. (" + filename
                        + " is the same as " + existing.getFilename() + ")", ""));
                return false;
            }

            if ((metadata.getUpdaterId() > 0 && metadata.getVersion().isValid())
                    || (identityController.getGlobalConfiguration()
                    .hasOptionInt("plugin-addonid", metadata.getName()))) {
                updateManager.addComponent(new PluginComponent(
                        identityController.getGlobalConfiguration(), pluginInfo));
            }

            knownPlugins.put(filename.toLowerCase(), pluginInfo);

            eventBus.publishAsync(new PluginRefreshEvent());
            return true;
        } catch (MalformedURLException mue) {
            eventBus.publish(new UserErrorEvent(ErrorLevel.MEDIUM, mue,
                    "Error creating URL for plugin " + filename + ": " + mue.getMessage(), ""));
        } catch (PluginException e) {
            eventBus.publish(new UserErrorEvent(ErrorLevel.MEDIUM, e,
                    "Error loading plugin " + filename + ": " + e.getMessage(), ""));
        }

        return false;
    }

    /**
     * Remove a plugin.
     *
     * @param filename Filename of Plugin jar
     *
     * @return True if removed.
     */
    public boolean delPlugin(final String filename) {
        if (!knownPlugins.containsKey(filename.toLowerCase())) {
            return false;
        }

        final PluginInfo pluginInfo = getPluginInfo(filename);
        final boolean wasLoaded = pluginInfo.isLoaded();

        if (wasLoaded && !pluginInfo.isUnloadable()) {
            return false;
        }

        pluginInfo.unloadPlugin();

        knownPlugins.remove(filename.toLowerCase());

        return true;
    }

    /**
     * Reload a plugin.
     *
     * @param filename Filename of Plugin jar
     *
     * @return True if reloaded.
     */
    public boolean reloadPlugin(final String filename) {
        if (!knownPlugins.containsKey(filename.toLowerCase())) {
            return false;
        }

        final PluginInfo pluginInfo = getPluginInfo(filename);
        final boolean wasLoaded = pluginInfo.isLoaded();

        if (wasLoaded && !pluginInfo.isUnloadable()) {
            return false;
        }

        delPlugin(filename);
        boolean result = addPlugin(filename);

        if (wasLoaded && result) {
            getPluginInfo(filename).loadPlugin();
            result = getPluginInfo(filename).isLoaded();
        }

        return result;
    }

    /**
     * Reload all plugins.
     */
    public void reloadAllPlugins() {
        for (PluginInfo pluginInfo : getPluginInfos()) {
            reloadPlugin(pluginInfo.getFilename());
        }
    }

    /**
     * Get a plugin instance.
     *
     * @param filename File name of plugin jar
     *
     * @return PluginInfo instance, or null
     */
    public PluginInfo getPluginInfo(final String filename) {
        return knownPlugins.get(filename.toLowerCase());
    }

    /**
     * Get a plugin instance by plugin name.
     *
     * @param name Name of plugin to find.
     *
     * @return PluginInfo instance, or null
     */
    public PluginInfo getPluginInfoByName(final String name) {
        for (PluginInfo pluginInfo : getPluginInfos()) {
            if (pluginInfo.getMetaData().getName().equalsIgnoreCase(name)) {
                return pluginInfo;
            }
        }

        return null;
    }

    /**
     * Get directory where plugins are stored.
     *
     * @return Directory where plugins are stored.
     *
     * @deprecated Should be injected.
     */
    @Deprecated
    public String getDirectory() {
        return directory;
    }

    /**
     * Get directory where plugin files are stored.
     *
     * @return Directory where plugin files are stored.
     */
    public String getFilesDirectory() {
        final String fs = System.getProperty("file.separator");
        String filesDir = directory + "files" + fs;
        if (identityController.getGlobalConfiguration().hasOptionString("plugins", "filesdir")) {
            final String fdopt = identityController.getGlobalConfiguration()
                    .getOptionString("plugins", "filesdir");

            if (fdopt != null && !fdopt.isEmpty() && new File(fdopt).exists()) {
                filesDir = fdopt;
            }
        }

        return filesDir;
    }

    /**
     * Refreshes the list of known plugins.
     */
    public void refreshPlugins() {
        applyUpdates();

        final Collection<PluginMetaData> newPlugins = getAllPlugins();

        for (PluginMetaData plugin : newPlugins) {
            addPlugin(plugin.getRelativeFilename());
        }

        // Update our list of plugins
        synchronized (plugins) {
            plugins.removeAll(newPlugins);

            for (PluginMetaData oldPlugin : new HashSet<>(plugins)) {
                delPlugin(oldPlugin.getRelativeFilename());
            }

            plugins.clear();
            plugins.addAll(newPlugins);
        }

        eventBus.publishAsync(new PluginRefreshEvent());
    }

    /**
     * Recursively scans the plugin directory and attempts to apply any available updates.
     */
    public void applyUpdates() {
        final Deque<File> dirs = new LinkedList<>();

        dirs.add(new File(directory));

        while (!dirs.isEmpty()) {
            final File dir = dirs.pop();
            if (dir.isDirectory()) {
                dirs.addAll(Arrays.asList(dir.listFiles()));
            } else if (dir.isFile() && dir.getName().endsWith(".jar")) {
                final File update = new File(dir.getAbsolutePath() + ".update");

                if (update.exists() && dir.delete()) {
                    update.renameTo(dir);
                }
            }
        }
    }

    /**
     * Retrieves a list of all installed plugins. Any file under the main plugin directory
     * (~/.DMDirc/plugins or similar) that matches *.jar is deemed to be a valid plugin.
     *
     * @return A list of all installed or known plugins
     */
    public Collection<PluginMetaData> getAllPlugins() {
        final Collection<PluginMetaData> res = new HashSet<>(plugins.size());

        final Deque<File> dirs = new LinkedList<>();
        final Collection<String> pluginPaths = new LinkedList<>();

        dirs.add(new File(directory));

        while (!dirs.isEmpty()) {
            final File dir = dirs.pop();
            if (dir.isDirectory()) {
                dirs.addAll(Arrays.asList(dir.listFiles()));
            } else if (dir.isFile() && dir.getName().endsWith(".jar")) {
                pluginPaths.add(dir.getPath().substring(directory.length()));
            }
        }

        final MapList<String, String> newServices = new MapList<>();
        final Map<String, PluginMetaData> newPluginsByName = new HashMap<>();
        final Map<String, PluginMetaData> newPluginsByPath = new HashMap<>();

        // Initialise all of our metadata objects
        for (String target : pluginPaths) {
            try {
                final PluginMetaData targetMetaData = new PluginMetaData(this,
                        new URL("jar:file:" + directory + target
                                + "!/META-INF/plugin.config"),
                        Paths.get(directory, target));
                targetMetaData.load();

                if (targetMetaData.hasErrors()) {
                    eventBus.publish(new UserErrorEvent(ErrorLevel.MEDIUM, null,
                            "Error reading plugin metadata for plugin " + target
                            + ": " + targetMetaData.getErrors(), ""));
                } else {
                    newPluginsByName.put(targetMetaData.getName(), targetMetaData);
                    newPluginsByPath.put(target, targetMetaData);

                    for (String service : targetMetaData.getServices()) {
                        final String[] parts = service.split(" ", 2);
                        newServices.add(parts[1], parts[0]);
                    }

                    for (String export : targetMetaData.getExports()) {
                        final String[] parts = export.split(" ");
                        final String name = parts.length > 4 ? parts[4] : parts[0];
                        newServices.add("export", name);
                    }
                }
            } catch (MalformedURLException mue) {
                eventBus.publish(new UserErrorEvent(ErrorLevel.MEDIUM, mue,
                        "Error creating URL for plugin " + target + ": " + mue.getMessage(), ""));
            }
        }

        // Now validate all of the plugins
        for (Map.Entry<String, PluginMetaData> target : newPluginsByPath.entrySet()) {
            final PluginMetaDataValidator validator = new PluginMetaDataValidator(target.getValue());
            final Collection<String> results = validator.validate(newPluginsByName, newServices);

            if (results.isEmpty()) {
                res.add(target.getValue());
            } else {
                eventBus.publish(new UserErrorEvent(ErrorLevel.MEDIUM, null,
                        "Plugin validation failed for " + target.getKey() + ": " + results, ""));
            }
        }

        return res;
    }

    /**
     * Update the autoLoadList
     *
     * @param plugin to add/remove (Decided automatically based on isLoaded())
     */
    public void updateAutoLoad(final PluginInfo plugin) {
        final List<String> list = identityController.getGlobalConfiguration()
                .getOptionList("plugins", "autoload");
        final String path = plugin.getMetaData().getRelativeFilename();

        if (plugin.isLoaded() && !list.contains(path)) {
            list.add(path);
        } else if (!plugin.isLoaded() && list.contains(path)) {
            list.remove(path);
        }

        identityController.getUserSettings().setOption("plugins", "autoload", list);
    }

    /**
     * Get Collection&lt;PluginInf&gt; of known plugins.
     *
     * @return Collection&lt;PluginInfo&gt; of known plugins.
     */
    public Collection<PluginInfo> getPluginInfos() {
        return new ArrayList<>(knownPlugins.values());
    }

    @Handler
    public void handlePrefsOpened(final ClientPrefsOpenedEvent event) {
        for (PluginInfo pi : getPluginInfos()) {
            if (!pi.isLoaded() && !pi.isTempLoaded()) {
                pi.loadPluginTemp();
            }
            if (pi.isLoaded() || pi.isTempLoaded()) {
                try {
                    pi.getPlugin().showConfig(event.getModel());
                } catch (LinkageError | Exception le) {
                    eventBus.publishAsync(new AppErrorEvent(ErrorLevel.MEDIUM, le,
                            "Unable to show plugin configuration for "
                            + pi.getMetaData().getFriendlyName(), ""));
                }
            }
        }
    }

    @Handler
    public void handlePrefsClosed(final ClientPrefsClosedEvent event) {
        getPluginInfos().stream()
                .filter(PluginInfo::isTempLoaded)
                .forEach(PluginInfo::unloadPlugin);
    }

}
