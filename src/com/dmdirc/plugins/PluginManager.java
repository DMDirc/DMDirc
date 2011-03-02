/*
 * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.Main;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.actions.interfaces.ActionType;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.prefs.PreferencesDialogModel;
import com.dmdirc.interfaces.ActionListener;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.updater.components.PluginComponent;
import com.dmdirc.util.MapList;

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

/**
 * Searches for and manages plugins and services.
 */
public class PluginManager implements ActionListener {

    /** Singleton instance of the plugin manager. */
    private static PluginManager me;

    /** List of known plugins' file names to their corresponding {@link PluginInfo} objects. */
    private final Map<String, PluginInfo> knownPlugins = new HashMap<String, PluginInfo>();

    /** Set of known plugins' metadata. */
    private final Collection<PluginMetaData> plugins = new HashSet<PluginMetaData>();

    /** Directory where plugins are stored. */
    private final String myDir;

    /** Map of services. */
    private final Map<String, Map<String, Service>> services = new HashMap<String, Map<String, Service>>();

    /**
     * Creates a new instance of PluginManager.
     */
    private PluginManager() {
        final String fs = System.getProperty("file.separator");
        myDir = Main.getConfigDir() + "plugins" + fs;
        ActionManager.getActionManager().registerListener(this,
                CoreActionType.CLIENT_PREFS_OPENED,
                CoreActionType.CLIENT_PREFS_CLOSED);
    }

    /**
     * Get a service object for the given name/type if one exists.
     *
     * @param type Type of this service
     * @param name Name of this service
     * @return The service requested, or null if service wasn't found and create wasn't specifed
     */
    public Service getService(final String type, final String name) {
        return getService(type, name, false);
    }

    /**
     * Get a service object for the given name/type.
     *
     * @param type Type of this service
     * @param name Name of this service
     * @param create If the requested service doesn't exist, should it be created?
     * @return The service requested, or null if service wasn't found and create wasn't specifed
     */
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
            final Map<String, Service> map = new HashMap<String, Service>();
            final Service service = new Service(type, name);
            map.put(name, service);
            services.put(type, map);
            return service;
        }

        return null;
    }

    /**
     * Get a ServiceProvider object for the given name/type if one exists.
     *
     * @param type Type of this service
     * @param name Name of this service
     * @return A ServiceProvider that provides the requested service.
     * @throws NoSuchProviderException If no provider exists for the requested service
     */
    public ServiceProvider getServiceProvider(final String type, final String name) throws NoSuchProviderException {
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

    /**
     * Get a ServiceProvider object for the given tpye, prioritising those in the list of names.
     *
     * @param type Type to look for
     * @param names Names to look for
     * @param fallback Fallback to the first provider of type that exists if one from the list is not found.
     * @return A ServiceProvider that provides the requested service.
     * @throws NoSuchProviderException If no provider exists for the requested service and fallback is false, or no providers exist at all.
     */
    public ServiceProvider getServiceProvider(final String type, final List<String> names, final boolean fallback) throws NoSuchProviderException {
        for (final String name : names) {
            final ServiceProvider provider = getServiceProvider(type, name);
            if (provider != null) {
                return provider;
            }
        }

        if (fallback) {
            final List<Service> servicesType = getServicesByType(type);
            if (servicesType.size() > 0) {
                final Service service = servicesType.get(0);
                return getServiceProvider(type, service.getName());
            }
        }

        throw new NoSuchProviderException("No provider found for " + type + "from the given list");
    }

    /**
     * Get an ExportedService object of the given name from any provider that provides it.
     * This is the same as doing getServiceProvider("export", name).getExportedService(name)
     *
     * @param name Name of this service
     * @return An ExportedService object.
     * @throws NoSuchProviderException If no provider exists for the requested service.
     */
    public ExportedService getExportedService(final String name) {
        return getServiceProvider("export", name).getExportedService(name);
    }

    /**
     * Get a List of all services of a specifed type.
     *
     * @param type Type of service
     * @return The list of services requested.
     */
    public List<Service> getServicesByType(final String type) {
        // Find the type first
        if (services.containsKey(type)) {
            final Map<String, Service> map = services.get(type);
            return new ArrayList<Service>(map.values());
        }

        return new ArrayList<Service>();
    }

    /**
     * Get a List of all services
     *
     * @return The list of all services.
     */
    public List<Service> getAllServices() {
        // Find the type first
        final List<Service> allServices = new ArrayList<Service>();
        for (Map<String, Service> map : services.values()) {
            allServices.addAll(map.values());
        }

        return allServices;
    }

    /**
     * Autoloads plugins.
     */
    public void doAutoLoad() {
        for (String plugin : IdentityManager.getGlobalConfig().getOptionList("plugins", "autoload")) {
            plugin = plugin.trim();
            if (!plugin.isEmpty() && plugin.charAt(0) != '#' && getPluginInfo(plugin) != null) {
                getPluginInfo(plugin).loadPlugin();
            }
        }
    }

    /**
     * Retrieves the singleton instance of the plugin manager.
     *
     * @return A singleton instance of PluginManager.
     */
    public static synchronized PluginManager getPluginManager() {
        if (me == null) {
            me = new PluginManager();
            me.refreshPlugins();
        }

        return me;
    }

    /**
     * Tests and adds the specified plugin to the known plugins list. Plugins
     * will only be added if: <ul><li>The file exists,<li>No other plugin with
     * the same name is known,<li>All requirements are met for the plugin,
     * <li>The plugin has a valid config file that can be read</ul>.
     *
     * @param filename Filename of Plugin jar
     * @return True if the plugin is in the known plugins list (either before
     * this invocation or as a result of it), false if it was not added for
     * one of the reasons outlined above.
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
            final PluginMetaData metadata = new PluginMetaData(
                    new URL("jar:file://" + getDirectory() + filename
                    + "!/META-INF/plugin.config"),
                    new URL("file:" + getDirectory() + filename));
            metadata.load();
            final PluginInfo pluginInfo = new PluginInfo(metadata);
            final PluginInfo existing = getPluginInfoByName(metadata.getName());
            if (existing != null) {
                Logger.userError(ErrorLevel.MEDIUM,
                        "Duplicate Plugin detected, Ignoring. (" + filename
                        + " is the same as " + existing.getFilename() + ")");
                return false;
            }
            new PluginComponent(pluginInfo);

            knownPlugins.put(filename.toLowerCase(), pluginInfo);

            ActionManager.getActionManager().triggerEvent(
                    CoreActionType.PLUGIN_REFRESH, null, this);
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
     * @return True if removed.
     */
    public boolean delPlugin(final String filename) {
        if (!knownPlugins.containsKey(filename.toLowerCase())) {
            return false;
        }

        final PluginInfo pluginInfo = getPluginInfo(filename);
        final boolean wasLoaded = pluginInfo.isLoaded();

        if (wasLoaded && !pluginInfo.isUnloadable()) { return false; }

        pluginInfo.unloadPlugin();

        knownPlugins.remove(filename.toLowerCase());

        return true;
    }

    /**
     * Reload a plugin.
     *
     * @param filename Filename of Plugin jar
     * @return True if reloaded.
     */
    public boolean reloadPlugin(final String filename) {
        if (!knownPlugins.containsKey(filename.toLowerCase())) {
            return false;
        }

        final PluginInfo pluginInfo = getPluginInfo(filename);
        final boolean wasLoaded = pluginInfo.isLoaded();

        if (wasLoaded && !pluginInfo.isUnloadable()) { return false; }

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
     * @return PluginInfo instance, or null
     */
    public PluginInfo getPluginInfo(final String filename) {
        return knownPlugins.get(filename.toLowerCase());
    }

    /**
     * Get a plugin instance by plugin name.
     *
     * @param name Name of plugin to find.
     * @return PluginInfo instance, or null
     */
    public PluginInfo getPluginInfoByName(final String name) {
        for (PluginInfo pluginInfo : getPluginInfos()) {
            if (pluginInfo.getName().equalsIgnoreCase(name)) {
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
        return myDir;
    }

    /**
     * Get directory where plugin files are stored.
     *
     * @return Directory where plugin files are stored.
     */
    public String getFilesDirectory() {
        final String fs = System.getProperty("file.separator");
        String filesDir = myDir + "files" + fs;
        if (IdentityManager.getGlobalConfig().hasOptionString("plugins", "filesdir")) {
            final String fdopt = IdentityManager.getGlobalConfig().getOptionString("plugins", "filesdir");
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

            for (PluginMetaData oldPlugin : new HashSet<PluginMetaData>(plugins)) {
                delPlugin(oldPlugin.getRelativeFilename());
            }

            plugins.clear();
            plugins.addAll(newPlugins);
        }

        ActionManager.getActionManager().triggerEvent(
                CoreActionType.PLUGIN_REFRESH, null, this);
    }

    /**
     * Recursively scans the plugin directory and attempts to apply any
     * available updates.
     */
    public void applyUpdates() {
        final Deque<File> dirs = new LinkedList<File>();

        dirs.add(new File(myDir));

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
     * Retrieves a list of all installed plugins.
     * Any file under the main plugin directory (~/.DMDirc/plugins or similar)
     * that matches *.jar is deemed to be a valid plugin.
     *
     * @return A list of all installed or known plugins
     */
    public Collection<PluginMetaData> getAllPlugins() {
        final Collection<PluginMetaData> res
                = new HashSet<PluginMetaData>(plugins.size());

        final Deque<File> dirs = new LinkedList<File>();
        final Collection<String> pluginPaths = new LinkedList<String>();

        dirs.add(new File(myDir));

        while (!dirs.isEmpty()) {
            final File dir = dirs.pop();
            if (dir.isDirectory()) {
                dirs.addAll(Arrays.asList(dir.listFiles()));
            } else if (dir.isFile() && dir.getName().endsWith(".jar")) {
                pluginPaths.add(dir.getPath().substring(myDir.length()));
            }
        }

        final MapList<String, String> newServices = new MapList<String, String>();
        final Map<String, PluginMetaData> newPluginsByName = new HashMap<String, PluginMetaData>();
        final Map<String, PluginMetaData> newPluginsByPath = new HashMap<String, PluginMetaData>();

        // Initialise all of our metadata objects
        for (String target : pluginPaths) {
            try {
                final PluginMetaData targetMetaData = new PluginMetaData(
                        new URL("jar:file://" + getDirectory() + target
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
            final PluginMetaDataValidator validator
                    = new PluginMetaDataValidator(target.getValue());
            final Collection<String> results
                    = validator.validate(newPluginsByName, newServices);

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
        final List<String> list = IdentityManager.getGlobalConfig().getOptionList("plugins", "autoload");
        final String path = plugin.getRelativeFilename();

        if (plugin.isLoaded() && !list.contains(path)) {
            list.add(path);
        } else if (!plugin.isLoaded() && list.contains(path)) {
            list.remove(path);
        }

        IdentityManager.getConfigIdentity().setOption("plugins", "autoload", list);
    }

    /**
     * Get Collection<PluginInfo> of known plugins.
     *
     * @return Collection<PluginInfo> of known plugins.
     */
    public Collection<PluginInfo> getPluginInfos() {
        return new ArrayList<PluginInfo>(knownPlugins.values());
    }

    /** {@inheritDoc} */
    @Override
    public void processEvent(final ActionType type, final StringBuffer format, final Object... arguments) {
        if (type.equals(CoreActionType.CLIENT_PREFS_OPENED)) {
            for (PluginInfo pi : getPluginInfos()) {
                if (!pi.isLoaded() && !pi.isTempLoaded()) {
                    pi.loadPluginTemp();
                }
                if (pi.isLoaded() || pi.isTempLoaded()) {
                    try {
                        pi.getPlugin().showConfig((PreferencesDialogModel) arguments[0]);
                    } catch (LinkageError le) {
                        Logger.userError(ErrorLevel.MEDIUM, "Error with plugin (" + pi.getNiceName() + "), unable to show config (" + le + ")", le);
                    } catch (Exception ex) {
                        Logger.userError(ErrorLevel.MEDIUM, "Error with plugin (" + pi.getNiceName() + "), unable to show config (" + ex + ")", ex);
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
