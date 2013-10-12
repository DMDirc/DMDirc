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

package com.dmdirc.plugins;

import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.config.ConfigFileBackedConfigProvider;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.InvalidIdentityFileException;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.util.SimpleInjector;
import com.dmdirc.util.resourcemanager.ResourceManager;
import com.dmdirc.util.validators.ValidationResponse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import javax.inject.Provider;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import dagger.ObjectGraph;

/**
 * Stores plugin metadata and handles loading of plugin resources.
 */
@Slf4j
public class PluginInfo implements Comparable<PluginInfo>, ServiceProvider {

    /** The metadata for this plugin. */
    @Getter
    private final PluginMetaData metaData;
    /** The initialiser to use for the injector. */
    private final Provider<PluginInjectorInitialiser> injectorInitialiser;
    /** The object graph to pass to plugins for DI purposes. */
    private final ObjectGraph objectGraph;
    /** Filename for this plugin (taken from URL). */
    @Getter
    private final String filename;
    /** The actual Plugin from this jar. */
    @Getter
    private Plugin plugin;
    /** The classloader used for this Plugin. */
    @Getter
    private PluginClassLoader pluginClassLoader;
    /** The resource manager used by this pluginInfo. */
    private ResourceManager resourceManager;
    /** Is this plugin only loaded temporarily? */
    private boolean tempLoaded;
    /** List of classes this plugin has. */
    private final List<String> myClasses = new ArrayList<>();
    /** Last Error Message. */
    @Getter
    private String lastError = "No Error";
    /** Are we trying to load? */
    private boolean isLoading;
    /** List of services we provide. */
    private final List<Service> provides = new ArrayList<>();
    /** List of children of this plugin. */
    private final List<PluginInfo> children = new ArrayList<>();
    /** Map of exports. */
    private final Map<String, ExportInfo> exports = new HashMap<>();
    /** List of configuration providers. */
    private final List<ConfigProvider> configProviders = new ArrayList<>();

    /**
     * Create a new PluginInfo.
     *
     * @param metadata The plugin's metadata information
     * @param injectorInitialiser The initialiser to use for the plugin's injector.
     * @param objectGraph The object graph to give to plugins for DI purposes.
     * @throws PluginException if there is an error loading the Plugin
     */
    public PluginInfo(
            final PluginMetaData metadata,
            final Provider<PluginInjectorInitialiser> injectorInitialiser,
            final ObjectGraph objectGraph) throws PluginException {
        this.injectorInitialiser = injectorInitialiser;
        this.objectGraph = objectGraph;
        this.filename = new File(metadata.getPluginUrl().getPath()).getName();
        this.metaData = metadata;

        ResourceManager res;

        try {
            res = getResourceManager();
        } catch (IOException ioe) {
            lastError = "Error with resourcemanager: " + ioe.getMessage();
            throw new PluginException("Plugin " + filename + " failed to load. " + lastError, ioe);
        }

        updateClassList(res);

        if (!myClasses.contains(metadata.getMainClass())) {
            lastError = "main class file (" + metadata.getMainClass() + ") not found in jar.";
            throw new PluginException("Plugin " + filename + " failed to load. " + lastError);
        }

        updateProvides();
        getDefaults();
    }

    /**
     * Updates the list of known classes within this plugin from the specified
     * resource manager.
     *
     * @param res Resource manager to use to read the plugin contents.
     */
    private void updateClassList(final ResourceManager res) {
        myClasses.clear();

        for (final String classfilename : res.getResourcesStartingWith("")) {
            final String classname = classfilename.replace('/', '.');
            if (classname.endsWith(".class")) {
                myClasses.add(classname.substring(0, classname.length() - 6));
            }
        }
    }

    /**
     * Retrieves the injector used to inject parameters into this
     * plugin's methods.
     *
     * @return The injector used for this plugin
     */
    public SimpleInjector getInjector() {
        final SimpleInjector injector;
        final SimpleInjector[] parents = new SimpleInjector[metaData.getParent() == null ? 0 : 1];
        final Plugin[] plugins = new Plugin[parents.length];

        if (metaData.getParent() != null) {
            final PluginInfo parent = metaData.getManager()
                    .getPluginInfoByName(metaData.getParent());
            parents[0] = parent.getInjector();
            plugins[0] = parent.getPlugin();
        }

        injector = new SimpleInjector(parents);

        for (Plugin parentPlugin : plugins) {
            injector.addParameter(parentPlugin);
        }

        // TODO: This should be switched to using a full DI framework.
        injector.addParameter(PluginInfo.class, this);
        injector.addParameter(PluginMetaData.class, metaData);
        injectorInitialiser.get().initialise(injector);

        return injector;
    }

    /**
     * Gets the {@link ObjectGraph} that should be used when loading this plugin.
     *
     * <p>Where this plugin has a parent which returns a non-null graph from
     * {@link Plugin#getObjectGraph()} that object graph will be used unmodified. Otherwise, the
     * global object graph will be used.
     *
     * @return An {@link ObjectGraph} to be used.
     */
    protected ObjectGraph getObjectGraph() {
        if (metaData.getParent() != null) {
            final PluginInfo parentInfo = metaData.getManager()
                    .getPluginInfoByName(metaData.getParent());
            Plugin parent = parentInfo.getPlugin();
            ObjectGraph parentGraph = parent.getObjectGraph();
            if (parentGraph != null) {
                return parentGraph;
            }
        }

        return objectGraph;
    }

    /**
     * Get the licence for this plugin if it exists.
     *
     * @return An InputStream for the licence of this plugin, or null if no
     *         licence found.
     * @throws IOException if there is an error with the ResourceManager.
     */
    public Map<String, InputStream> getLicenceStreams() throws IOException {
        final TreeMap<String, InputStream> licences = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        licences.putAll(getResourceManager().getResourcesStartingWithAsInputStreams(
                "META-INF/licences/"));
        return licences;
    }

    /**
     * Get the defaults, formatters and icons for this plugin.
     */
    private void getDefaults() {
        final ConfigProvider defaults = IdentityManager.getIdentityManager().getAddonSettings();
        final String domain = "plugin-" + metaData.getName();

        log.trace("{}: Using domain '{}'",
                new Object[]{metaData.getName(), domain});

        for (Map.Entry<String, String> entry : metaData.getDefaultSettings().entrySet()) {
            final String key = entry.getKey();
            final String value = entry.getValue();

            defaults.setOption(domain, key, value);
        }

        for (Map.Entry<String, String> entry : metaData.getFormatters().entrySet()) {
            final String key = entry.getKey();
            final String value = entry.getValue();

            defaults.setOption("formatter", key, value);
        }

        for (Map.Entry<String, String> entry : metaData.getIcons().entrySet()) {
            final String key = entry.getKey();
            final String value = entry.getValue();

            defaults.setOption("icon", key, value);
        }
    }

    /**
     * Try get the identities for this plugin.
     * This will unload any identities previously loaded by this plugin.
     */
    private void loadIdentities() {
        try {
            final Map<String, InputStream> identityStreams = getResourceManager().getResourcesStartingWithAsInputStreams("META-INF/identities/");

            unloadIdentities();

            for (Map.Entry<String, InputStream> entry : identityStreams.entrySet()) {
                final String name = entry.getKey();
                final InputStream stream = entry.getValue();

                if (name.endsWith("/") || stream == null) {
                    // Don't try to load folders or null streams
                    continue;
                }

                synchronized (configProviders) {
                    try {
                        final ConfigProvider configProvider = new ConfigFileBackedConfigProvider(stream, false);
                        IdentityManager.getIdentityManager().addConfigProvider(configProvider);
                        configProviders.add(configProvider);
                    } catch (final InvalidIdentityFileException ex) {
                        Logger.userError(ErrorLevel.MEDIUM,
                                "Error with identity file '" + name
                                + "' in plugin '" + metaData.getName() + "'", ex);
                    }
                }
            }
        } catch (final IOException ioe) {
            Logger.userError(ErrorLevel.MEDIUM, "Error finding identities in plugin '"
                    + metaData.getName() + "'", ioe);
        }
    }

    /**
     * Unload any identities loaded by this plugin.
     */
    private void unloadIdentities() {
        synchronized (configProviders) {
            for (ConfigProvider identity : configProviders) {
                IdentityManager.getIdentityManager().removeConfigProvider(identity);
            }

            configProviders.clear();
        }
    }

    /**
     * Update provides list.
     */
    private void updateProvides() {
        // Remove us from any existing provides lists.
        synchronized (provides) {
            for (Service service : provides) {
                service.delProvider(this);
            }
            provides.clear();
        }

        // Get services provided by this plugin
        final Collection<String> providesList = metaData.getServices();
        if (providesList != null) {
            for (String item : providesList) {
                final String[] bits = item.split(" ");
                final String name = bits[0];
                final String type = bits.length > 1 ? bits[1] : "misc";

                if (!name.equalsIgnoreCase("any") && !type.equalsIgnoreCase("export")) {
                    final Service service = metaData.getManager().getService(type, name, true);
                    synchronized (provides) {
                        service.addProvider(this);
                        provides.add(service);
                    }
                }
            }
        }

        updateExports();
    }

    /**
     * Called when the plugin is updated using the updater.
     * Reloads metaData and updates the list of files.
     */
    public void pluginUpdated() {
        try {
            // Force a new resourcemanager just incase.
            updateClassList(getResourceManager(true));

            updateMetaData();
            updateProvides();
            getDefaults();
        } catch (IOException ioe) {
            Logger.userError(ErrorLevel.MEDIUM, "There was an error updating "
                    + metaData.getName(), ioe);
        }
    }

    /**
     * Try to reload the metaData from the plugin.config file.
     * If this fails, the old data will be used still.
     *
     * @return true if metaData was reloaded ok, else false.
     */
    private boolean updateMetaData() {
        metaData.load();

        return !metaData.hasErrors();
    }

    /**
     * Gets a resource manager for this plugin
     *
     * @return The resource manager for this plugin
     * @throws IOException if there is any problem getting a ResourceManager for this plugin
     */
    public ResourceManager getResourceManager() throws IOException {
        return getResourceManager(false);
    }

    /**
     * Get the resource manager for this plugin
     *
     * @return The resource manager for this plugin
     * @param forceNew Force a new resource manager rather than using the old one.
     * @throws IOException if there is any problem getting a ResourceManager for this plugin
     * @since 0.6
     */
    public synchronized ResourceManager getResourceManager(final boolean forceNew) throws IOException {
        if (resourceManager == null || forceNew) {
            resourceManager = ResourceManager.getResourceManager("jar://" + metaData.getPluginUrl().getPath());

            // Clear the resourcemanager in 10 seconds to stop us holding the file open
            new Timer(filename + "-resourcemanagerTimer").schedule(new TimerTask() {

                /** {@inheritDoc} */
                @Override
                public void run() {
                    resourceManager = null;
                }

            }, 10000);
        }

        return resourceManager;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isActive() {
        return isLoaded();
    }

    /** {@inheritDoc} */
    @Override
    public void activateServices() {
        loadPlugin();
    }

    /** {@inheritDoc} */
    @Override
    public String getProviderName() {
        return "Plugin: " + metaData.getFriendlyName() + " ("
                + metaData.getName() + " / " + getFilename() + ")";
    }

    /** {@inheritDoc} */
    @Override
    public List<Service> getServices() {
        synchronized (provides) {
            return new ArrayList<>(provides);
        }
    }

    /**
     * Is this plugin loaded?
     *
     * @return True if the plugin is currently (non-temporarily) loaded, false
     * otherwise
     */
    public boolean isLoaded() {
        return plugin != null && !tempLoaded;
    }

    /**
     * Is this plugin temporarily loaded?
     *
     * @return True if this plugin is currently temporarily loaded, false
     * otherwise
     */
    public boolean isTempLoaded() {
        return plugin != null && tempLoaded;
    }

    /**
     * Try to Load the plugin files temporarily.
     */
    public void loadPluginTemp() {
        if (isLoaded() || isTempLoaded()) {
            // Already loaded, don't do anything
            return;
        }

        tempLoaded = true;
        loadPlugin();
    }

    /**
     * Load any required plugins or services.
     *
     * @return True if all requirements have been satisfied, false otherwise
     */
    protected boolean loadRequirements() {
        return loadRequiredPlugins() && loadRequiredServices();
    }

    /**
     * Attempts to load all services that are required by this plugin.
     *
     * @return True iff all required services were found and satisfied
     */
    protected boolean loadRequiredServices() {
        final ServiceManager manager = metaData.getManager();

        for (String serviceInfo : metaData.getRequiredServices()) {
            final String[] parts = serviceInfo.split(" ", 2);

            if ("any".equals(parts[0])) {
                Service best = null;
                boolean found = false;

                for (Service service : manager.getServicesByType(parts[1])) {
                    if (service.isActive()) {
                        found = true;
                        break;
                    }

                    best = service;
                }

                if (!found && best != null) {
                    found = best.activate();
                }

                if (!found) {
                    return false;
                }
            } else {
                final Service service = manager.getService(parts[1], parts[0]);

                if (service == null || !service.activate()) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Attempts to load all plugins that are required by this plugin.
     *
     * @return True if all required plugins were found and loaded
     */
    protected boolean loadRequiredPlugins() {
        final String required = metaData.getRequirements().get("plugins");

        if (required != null) {
            for (String pluginName : required.split(",")) {
                final String[] data = pluginName.split(":");
                if (!data[0].trim().isEmpty() && !loadRequiredPlugin(data[0])) {
                    return false;
                }
            }
        }

        if (metaData.getParent() != null) {
            return loadRequiredPlugin(metaData.getParent());
        }

        return true;
    }

    /**
     * Attempts to load the specified required plugin.
     *
     * @param name The name of the plugin to be loaded
     * @return True if the plugin was found and loaded, false otherwise
     */
    protected boolean loadRequiredPlugin(final String name) {
        log.info("Loading required plugin '{}' for plugin {}",
                new Object[] { name, metaData.getName() });

        final PluginInfo pi = metaData.getManager().getPluginInfoByName(name);

        if (pi == null) {
            return false;
        }

        if (tempLoaded) {
            pi.loadPluginTemp();
        } else {
            pi.loadPlugin();
        }

        return true;
    }

    /**
     * Load the plugin files.
     */
    public void loadPlugin() {
        if (isLoaded() || isLoading) {
            lastError = "Not Loading: (" + isLoaded() + "||" + isLoading + ")";
            return;
        }

        updateProvides();

        if (isTempLoaded()) {
            tempLoaded = false;

            if (!loadRequirements()) {
                tempLoaded = true;
                lastError = "Unable to satisfy dependencies for " + metaData.getName();
                return;
            }

            try {
                plugin.load(this, getObjectGraph());
                plugin.onLoad();
            } catch (LinkageError | Exception e) {
                lastError = "Error in onLoad for " + metaData.getName() + ":"
                        + e.getMessage();
                Logger.userError(ErrorLevel.MEDIUM, lastError, e);
                unloadPlugin();
            }
        } else {
            isLoading = true;

            if (!loadRequirements()) {
                isLoading = false;
                lastError = "Unable to satisfy dependencies for " + metaData.getName();
                return;
            }

            loadIdentities();

            loadClass(metaData.getMainClass());

            if (isLoaded()) {
                ActionManager.getActionManager().triggerEvent(
                        CoreActionType.PLUGIN_LOADED, null, this);
            }

            isLoading = false;
        }
    }

    /**
     * Add the given Plugin as a child of this plugin.
     *
     * @param child Child to add
     */
    public void addChild(final PluginInfo child) {
        children.add(child);
    }

    /**
     * Remove the given Plugin as a child of this plugin.
     *
     * @param child Child to remove
     */
    public void delChild(final PluginInfo child) {
        children.remove(child);
    }

    /**
     * Returns a list of this plugin's children.
     *
     * @return List of child plugins
     */
    public List<PluginInfo> getChildren() {
        return Collections.unmodifiableList(children);
    }

    /**
     * Checks if this plugin has any children.
     *
     * @return true iff this plugin has children
     */
    public boolean hasChildren() {
        return !children.isEmpty();
    }

    /**
     * Initialises this plugin's classloader.
     */
    private void initialiseClassLoader() {
        if (pluginClassLoader == null) {
            final PluginClassLoader[] loaders =
                    new PluginClassLoader[metaData.getParent() == null ? 0 : 1];

            if (metaData.getParent() != null) {
                final String parentName = metaData.getParent();
                final PluginInfo parent = metaData.getManager()
                        .getPluginInfoByName(parentName);
                parent.addChild(this);
                loaders[0] = parent.getPluginClassLoader();

                if (loaders[0] == null) {
                    // Not loaded? Try again...

                    parent.loadPlugin();
                    loaders[0] = parent.getPluginClassLoader();

                    if (loaders[0] == null) {
                        lastError = "Unable to get classloader from required parent '" + parentName + "' for " + metaData.getName();
                        return;
                    }
                }
            }

            pluginClassLoader = new PluginClassLoader(this, metaData.getManager().getGlobalClassLoader(), loaders);
        }
    }

    /**
     * Load the given classname.
     *
     * @param classname Class to load
     */
    private void loadClass(final String classname) {
        try {
            initialiseClassLoader();

            // Don't reload a class if its already loaded.
            if (pluginClassLoader.isClassLoaded(classname, true)) {
                lastError = "Classloader says we are already loaded.";
                return;
            }

            final Class<?> clazz = pluginClassLoader.loadClass(classname);
            if (clazz == null) {
                lastError = "Class '" + classname + "' was not able to load.";
                return;
            }

            // Only try and construct the main class, anything else should be constructed
            // by the plugin itself.
            if (classname.equals(metaData.getMainClass())) {
                final Object temp = getInjector().createInstance(clazz);

                if (temp instanceof Plugin) {
                    final ValidationResponse prerequisites = ((Plugin) temp).checkPrerequisites();
                    if (prerequisites.isFailure()) {
                        if (!tempLoaded) {
                            lastError = "Prerequisites for plugin not met. ('"
                                    + filename + ":" + metaData.getMainClass()
                                    + "' -> '" + prerequisites.getFailureReason() + "') ";
                            Logger.userError(ErrorLevel.LOW, lastError);
                        }
                    } else {
                        final String domain = "plugin-" + metaData.getName();
                        plugin = (Plugin) temp;

                        log.debug("{}: Setting domain '{}'",
                                new Object[]{metaData.getName(), domain});

                        plugin.setDomain(domain);
                        if (!tempLoaded) {
                            try {
                                plugin.load(this, getObjectGraph());
                                plugin.onLoad();
                            } catch (LinkageError | Exception e) {
                                lastError = "Error in onLoad for "
                                        + metaData.getName() + ":" + e.getMessage();
                                Logger.userError(ErrorLevel.MEDIUM, lastError, e);
                                unloadPlugin();
                            }
                        }
                    }
                }
            }
        } catch (ClassNotFoundException cnfe) {
            lastError = "Class not found ('" + filename + ":" + classname + ":"
                    + classname.equals(metaData.getMainClass()) + "') - "
                    + cnfe.getMessage();
            Logger.userError(ErrorLevel.LOW, lastError, cnfe);
        } catch (NoClassDefFoundError ncdf) {
            lastError = "Unable to instantiate plugin ('" + filename + ":"
                    + classname + ":"
                    + classname.equals(metaData.getMainClass())
                    + "') - Unable to find class: " + ncdf.getMessage();
            Logger.userError(ErrorLevel.LOW, lastError, ncdf);
        } catch (VerifyError ve) {
            lastError = "Unable to instantiate plugin ('" + filename + ":"
                    + classname + ":"
                    + classname.equals(metaData.getMainClass())
                    + "') - Incompatible: " + ve.getMessage();
            Logger.userError(ErrorLevel.LOW, lastError, ve);
        } catch (IllegalArgumentException ex) {
            lastError = "Unable to instantiate plugin ('" + filename + ":"
                    + classname + ":"
                    + classname.equals(metaData.getMainClass())
                    + "') - Unable to construct: " + ex.getMessage();
            Logger.userError(ErrorLevel.LOW, lastError, ex);
        }
    }

    /**
     * Unload the plugin if possible.
     */
    public void unloadPlugin() {
        unloadPlugin(false);
    }

    /**
     * Can this plugin be unloaded?
     * Will return false if:
     *   - The plugin is persistent (all its classes are loaded into the global class loader)
     *   - The plugin isn't currently loaded
     *   - The metadata key "unloadable" is set to false, no or 0
     *
     * @return true if plugin can be unloaded
     */
    public boolean isUnloadable() {
        return !isPersistent() && (isTempLoaded() || isLoaded())
                && metaData.isUnloadable();
    }

    /**
     * Unload the plugin if possible.
     *
     * @param parentUnloading is our parent already unloading? (if so, don't call delChild)
     */
    private void unloadPlugin(final boolean parentUnloading) {
        if (isUnloadable()) {
            if (!isTempLoaded()) {
                // Unload all children
                for (PluginInfo child : children) {
                    child.unloadPlugin(true);
                }

                // Delete ourself as a child of our parent.
                if (!parentUnloading) {
                    if (metaData.getParent() != null) {
                        final PluginInfo pi = metaData.getManager()
                                .getPluginInfoByName(metaData.getParent());
                        if (pi != null) {
                            pi.delChild(this);
                        }
                    }
                }

                // Now unload ourself
                try {
                    plugin.onUnload();
                } catch (Exception | LinkageError e) {
                    lastError = "Error in onUnload for " + metaData.getName()
                            + ":" + e + " - " + e.getMessage();
                    Logger.userError(ErrorLevel.MEDIUM, lastError, e);
                }

                ActionManager.getActionManager().triggerEvent(
                        CoreActionType.PLUGIN_UNLOADED, null, this);
                synchronized (provides) {
                    for (Service service : provides) {
                        service.delProvider(this);
                    }
                    provides.clear();
                }
            }
            unloadIdentities();
            tempLoaded = false;
            plugin = null;
            pluginClassLoader = null;
        }
    }

    /**
     * Get the list of Classes
     *
     * @return Classes this plugin has
     */
    public List<String> getClassList() {
        return Collections.unmodifiableList(myClasses);
    }

    /**
     * Is this a persistent plugin?
     *
     * @return true if persistent, else false
     */
    public boolean isPersistent() {
        return metaData.getPersistentClasses().contains("*");
    }

    /**
     * Does this plugin contain any persistent classes?
     *
     * @return true if this plugin contains any persistent classes, else false
     */
    public boolean hasPersistent() {
        return !metaData.getPersistentClasses().isEmpty();
    }

    /**
     * Get a list of all persistent classes in this plugin
     *
     * @return List of all persistent classes in this plugin
     */
    public Collection<String> getPersistentClasses() {
        if (isPersistent()) {
            return getClassList();
        } else {
            return metaData.getPersistentClasses();
        }
    }

    /**
     * Is this a persistent class?
     *
     * @param classname class to check persistence of
     * @return true if file (or whole plugin) is persistent, else false
     */
    public boolean isPersistent(final String classname) {
        return isPersistent() || metaData.getPersistentClasses().contains(classname);
    }

    /**
     * String Representation of this plugin
     *
     * @return String Representation of this plugin
     */
    @Override
    public String toString() {
        return metaData.getFriendlyName() + " - " + filename;
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(final PluginInfo o) {
        return toString().compareTo(o.toString());
    }

    /**
     * Update exports list.
     */
    private void updateExports() {
        exports.clear();

        // Get exports provided by this plugin
        final Collection<String> exportsList = metaData.getExports();
        for (String item : exportsList) {
            final String[] bits = item.split(" ");
            if (bits.length > 2) {
                final String methodName = bits[0];
                final String methodClass = bits[2];
                final String serviceName = bits.length > 4 ? bits[4] : bits[0];

                // Add a provides for this
                final Service service = metaData.getManager().getService("export", serviceName, true);
                synchronized (provides) {
                    service.addProvider(this);
                    provides.add(service);
                }
                // Add is as an export
                exports.put(serviceName, new ExportInfo(methodName, methodClass, this));
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public ExportedService getExportedService(final String name) {
        if (exports.containsKey(name)) {
            return exports.get(name).getExportedService();
        } else {
            return null;
        }
    }

    /**
     * Does this plugin export the specified service?
     *
     * @param name Name of the service to check
     *
     * @return true iff the plugin exports the service
     */
    public boolean hasExportedService(final String name) {
        return exports.containsKey(name);
    }

}
