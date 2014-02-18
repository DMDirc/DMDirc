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

package com.dmdirc.plugins;

import com.dmdirc.actions.CoreActionType;
import com.dmdirc.config.prefs.PreferencesDialogModel;
import com.dmdirc.interfaces.ActionController;
import com.dmdirc.interfaces.ActionListener;
import com.dmdirc.interfaces.actions.ActionType;
import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.updater.components.PluginComponent;
import com.dmdirc.updater.manager.UpdateManager;
import com.dmdirc.util.collections.MapList;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
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

/**
 * Searches for and manages plugins and services.
 */
public class PluginManager implements ActionListener, ServiceManager {

    /** List of known plugins' file names to their corresponding {@link PluginInfo} objects. */
    private final Map<String, PluginInfo> knownPlugins = new HashMap<>();
    /** Set of known plugins' metadata. */
    private final Collection<PluginMetaData> plugins = new HashSet<>();
    /** Directory where plugins are stored. */
    private final String directory;
    /** The identity controller to use to find configuration options. */
    private final IdentityController identityController;
    /** The action controller to use for events. */
    private final ActionController actionController;
    /** The update manager to inform about plugins. */
    private final UpdateManager updateManager;
    /** A provider of initialisers for plugin injectors. */
    private final Provider<PluginInjectorInitialiser> initialiserProvider;
    /** Map of services. */
    private final Map<String, Map<String, Service>> services = new HashMap<>();
    /** Global ClassLoader used by plugins from this manager. */
    private final GlobalClassLoader globalClassLoader;
    /** The graph to pass to plugins for DI purposes. */
    private final ObjectGraph objectGraph;

    /**
     * Creates a new instance of PluginManager.
     *
     * @param identityController  The identity controller to use for configuration options.
     * @param actionController    The action controller to use for events.
     * @param updateManager       The update manager to inform about plugins.
     * @param initialiserProvider A provider of initialisers for plugin injectors.
     * @param objectGraph         The graph to pass to plugins for DI purposes.
     * @param directory           The directory to load plugins from.
     */
    public PluginManager(
            final IdentityController identityController,
            final ActionController actionController,
            final UpdateManager updateManager,
            final Provider<PluginInjectorInitialiser> initialiserProvider,
            final ObjectGraph objectGraph,
            final String directory) {
        this.identityController = identityController;
        this.actionController = actionController;
        this.updateManager = updateManager;
        this.initialiserProvider = initialiserProvider;
        this.directory = directory;
        this.globalClassLoader = new GlobalClassLoader(this);
        this.objectGraph = objectGraph;

        actionController.registerListener(this,
                CoreActionType.CLIENT_PREFS_OPENED,
                CoreActionType.CLIENT_PREFS_CLOSED);
    }

    /**
     * Get the global class loader in use for this plugin manager.
     *
     * @return Global Class Loader
     */
    public GlobalClassLoader getGlobalClassLoader() {
        return globalClassLoader;
    }

    /** {@inheritDoc} */
    @Override
    public Service getService(final String type, final String name) {
        return getService(type, name, false);
    }

    /** {@inheritDoc} */
    @Override
    public Service getService(final String type, final String name, final boolean create) {
        // Find the type first
        if (services.containsKey(type)) {
            final Map<String, Service> map = services.get(type);
            // Now the name
            if (map.containsKey(name)) {
                return map.get(name);
            } else if (create) {
                final Service service = new Service(type, name);
                map.put(name, service);
                return service;
            }
        } else if (create) {
            final Map<String, Service> map = new HashMap<>();
            final Service service = new Service(type, name);
            map.put(name, service);
            services.put(type, map);
            return service;
        }

        return null;
    }

    /** {@inheritDoc} */
    @Override
    public ServiceProvider getServiceProvider(final String type, final String name) throws
            NoSuchProviderException {
        final Service service = getService(type, name);
        if (service != null) {
            ServiceProvider provider = service.getActiveProvider();
            if (provider != null) {
                return provider;
            } else {
                // Try to activate the service then try again.
                service.activate();
                provider = service.getActiveProvider();
                if (provider != null) {
                    return provider;
                }
            }
        }

        throw new NoSuchProviderException("No provider found for: " + type + "->" + name);
    }

    /** {@inheritDoc} */
    @Override
    public ServiceProvider getServiceProvider(final String type, final List<String> names,
            final boolean fallback) throws NoSuchProviderException {
        for (final String name : names) {
            final ServiceProvider provider = getServiceProvider(type, name);
            if (provider != null) {
                return provider;
            }
        }

        if (fallback) {
            final List<Service> servicesType = getServicesByType(type);
            if (!servicesType.isEmpty()) {
                final Service service = servicesType.get(0);
                return getServiceProvider(type, service.getName());
            }
        }

        throw new NoSuchProviderException("No provider found for " + type + "from the given list");
    }

    /** {@inheritDoc} */
    @Override
    public ExportedService getExportedService(final String name) {
        return getServiceProvider("export", name).getExportedService(name);
    }

    /** {@inheritDoc} */
    @Override
    public List<Service> getServicesByType(final String type) {
        // Find the type first
        if (services.containsKey(type)) {
            final Map<String, Service> map = services.get(type);
            return new ArrayList<>(map.values());
        }

        return new ArrayList<>();
    }

    /** {@inheritDoc} */
    @Override
    public List<Service> getAllServices() {
        // Find the type first
        final List<Service> allServices = new ArrayList<>();
        for (Map<String, Service> map : services.values()) {
            allServices.addAll(map.values());
        }

        return allServices;
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

        if (!new File(getDirectory() + filename).exists()) {
            Logger.userError(ErrorLevel.MEDIUM, "Error loading plugin "
                    + filename + ": File does not exist");
            return false;
        }

        try {
            final PluginMetaData metadata = new PluginMetaData(this,
                    new URL("jar:file:" + getDirectory() + filename
                    + "!/META-INF/plugin.config"),
                    new URL("file:" + getDirectory() + filename));
            metadata.load();
            final PluginInfo pluginInfo = new PluginInfo(metadata, initialiserProvider,
                    identityController, objectGraph);
            final PluginInfo existing = getPluginInfoByName(metadata.getName());
            if (existing != null) {
                Logger.userError(ErrorLevel.MEDIUM,
                        "Duplicate Plugin detected, Ignoring. (" + filename
                        + " is the same as " + existing.getFilename() + ")");
                return false;
            }

            if ((metadata.getUpdaterId() > 0 && metadata.getVersion().isValid())
                    || (identityController.getGlobalConfiguration()
                    .hasOptionInt("plugin-addonid", metadata.getName()))) {
                updateManager.addComponent(new PluginComponent(
                        identityController.getGlobalConfiguration(), pluginInfo));
            }

            knownPlugins.put(filename.toLowerCase(), pluginInfo);

            actionController.triggerEvent(CoreActionType.PLUGIN_REFRESH, null, this);
            return true;
        } catch (MalformedURLException mue) {
            Logger.userError(ErrorLevel.MEDIUM, "Error creating URL for plugin "
                    + filename + ": " + mue.getMessage(), mue);
        } catch (PluginException e) {
            Logger.userError(ErrorLevel.MEDIUM, "Error loading plugin "
                    + filename + ": " + e.getMessage(), e);
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
     */
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

        actionController.triggerEvent(CoreActionType.PLUGIN_REFRESH, null, this);
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
                        new URL("jar:file:" + getDirectory() + target
                        + "!/META-INF/plugin.config"),
                        new URL("file:" + getDirectory() + target));
                targetMetaData.load();

                if (targetMetaData.hasErrors()) {
                    Logger.userError(ErrorLevel.MEDIUM,
                            "Error reading plugin metadata for plugin " + target
                            + ": " + targetMetaData.getErrors());
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
                Logger.userError(ErrorLevel.MEDIUM,
                        "Error creating URL for plugin " + target + ": "
                        + mue.getMessage(), mue);
            }
        }

        // Now validate all of the plugins
        for (Map.Entry<String, PluginMetaData> target : newPluginsByPath.entrySet()) {
            final PluginMetaDataValidator validator = new PluginMetaDataValidator(target.getValue());
            final Collection<String> results = validator.validate(newPluginsByName, newServices);

            if (results.isEmpty()) {
                res.add(target.getValue());
            } else {
                Logger.userError(ErrorLevel.MEDIUM, "Plugin validation failed for "
                        + target.getKey() + ": " + results);
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

    /** {@inheritDoc} */
    @Override
    public void processEvent(final ActionType type, final StringBuffer format,
            final Object... arguments) {
        if (type.equals(CoreActionType.CLIENT_PREFS_OPENED)) {
            for (PluginInfo pi : getPluginInfos()) {
                if (!pi.isLoaded() && !pi.isTempLoaded()) {
                    pi.loadPluginTemp();
                }
                if (pi.isLoaded() || pi.isTempLoaded()) {
                    try {
                        pi.getPlugin().showConfig((PreferencesDialogModel) arguments[0]);
                    } catch (LinkageError | Exception le) {
                        Logger.userError(ErrorLevel.MEDIUM,
                                "Error with plugin (" + pi.getMetaData().getFriendlyName()
                                + "), unable to show config (" + le + ")", le);
                    }
                }
            }
        } else if (type.equals(CoreActionType.CLIENT_PREFS_CLOSED)) {
            for (PluginInfo pi : getPluginInfos()) {
                if (pi.isTempLoaded()) {
                    pi.unloadPlugin();
                }
            }
        }
    }

}
