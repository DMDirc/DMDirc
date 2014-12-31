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
import com.dmdirc.config.ConfigFileBackedConfigProvider;
import com.dmdirc.config.InvalidIdentityFileException;
import com.dmdirc.events.AppErrorEvent;
import com.dmdirc.events.PluginLoadedEvent;
import com.dmdirc.events.PluginUnloadedEvent;
import com.dmdirc.events.UserErrorEvent;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.util.SimpleInjector;
import com.dmdirc.util.validators.ValidationResponse;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Nonnull;
import javax.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dagger.ObjectGraph;

/**
 * Stores plugin metadata and handles loading of plugin resources.
 */
public class PluginInfo implements Comparable<PluginInfo>, ServiceProvider {

    private static final Logger LOG = LoggerFactory.getLogger(PluginInfo.class);
    /** The metadata for this plugin. */
    private final PluginMetaData metaData;
    /** The manager to use to look up other plugins. */
    private final PluginManager pluginManager;
    /** The initialiser to use for the injector. */
    private final Provider<PluginInjectorInitialiser> injectorInitialiser;
    /** The object graph to pass to plugins for DI purposes. */
    private final ObjectGraph objectGraph;
    /** The controller to add and remove settings from. */
    private final IdentityController identityController;
    /** Filename for this plugin (taken from URL). */
    private final String filename;
    /** The actual Plugin from this jar. */
    private Plugin plugin;
    /** The classloader used for this Plugin. */
    private PluginClassLoader pluginClassLoader;
    /** Is this plugin only loaded temporarily? */
    private boolean tempLoaded;
    /** List of classes this plugin has. */
    private final List<String> myClasses = new ArrayList<>();
    /** Last Error Message. */
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
    private final Collection<ConfigProvider> configProviders = new ArrayList<>();
    /** Event bus to post plugin loaded events to. */
    private final DMDircMBassador eventBus;
    /** File system for the plugin's jar. */
    private final FileSystem pluginFilesystem;

    /**
     * Create a new PluginInfo.
     *
     * @param metadata            The plugin's metadata information
     * @param injectorInitialiser The initialiser to use for the plugin's injector.
     * @param eventBus            Event bus to post event loaded events on.
     * @param identityController  The identity controller to add and remove settings from.
     * @param objectGraph         The object graph to give to plugins for DI purposes.
     *
     * @throws PluginException if there is an error loading the Plugin
     */
    public PluginInfo(
            final PluginManager pluginManager,
            final String pluginDirectory,
            final PluginMetaData metadata,
            final Provider<PluginInjectorInitialiser> injectorInitialiser,
            final DMDircMBassador eventBus,
            final IdentityController identityController,
            final ObjectGraph objectGraph) throws PluginException {
        this.pluginManager = pluginManager;
        this.injectorInitialiser = injectorInitialiser;
        this.objectGraph = objectGraph;
        this.eventBus = eventBus;
        this.identityController = identityController;
        this.filename = metadata.getPluginPath().getFileName().toString();
        this.metaData = metadata;

        try {
            pluginFilesystem = FileSystems.newFileSystem(Paths.get(pluginDirectory, filename), null);
        } catch (IOException ex) {
            lastError = "Error loading filesystem: " + ex.getMessage();
            throw new PluginException("Plugin " + filename + " failed to load. " + lastError, ex);
        }
        updateClassList();

        if (!myClasses.contains(metadata.getMainClass())) {
            lastError = "main class file (" + metadata.getMainClass() + ") not found in jar.";
            throw new PluginException("Plugin " + filename + " failed to load. " + lastError);
        }

        updateProvides();
        getDefaults();
    }

    public PluginMetaData getMetaData() {
        return metaData;
    }

    public String getFilename() {
        return filename;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public PluginClassLoader getPluginClassLoader() {
        return pluginClassLoader;
    }

    public String getLastError() {
        return lastError;
    }

    /**
     * Updates the list of known classes within this plugin from the specified resource manager.
     */
    private void updateClassList() throws PluginException {
        myClasses.clear();
        try {
            Files.walkFileTree(pluginFilesystem.getPath("/"), new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {
                    if (file.getFileName().toString().endsWith(".class")) {
                        final String classname = file.toAbsolutePath().toString().replace('/',
                                '.');
                        myClasses.add(classname.substring(1, classname.length() - 6));
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ex) {
            lastError = "Error loading classes: " + ex.getMessage();
            throw new PluginException("Plugin " + filename + " failed to load. " + lastError, ex);
        }
    }

    /**
     * Retrieves the injector used to inject parameters into this plugin's methods.
     *
     * @return The injector used for this plugin
     */
    public SimpleInjector getInjector() {
        final SimpleInjector[] parents = new SimpleInjector[metaData.getParent() == null ? 0 : 1];
        final Plugin[] plugins = new Plugin[parents.length];

        if (metaData.getParent() != null) {
            final PluginInfo parent = pluginManager.getPluginInfoByName(metaData.getParent());
            parents[0] = parent.getInjector();
            plugins[0] = parent.getPlugin();
        }

        final SimpleInjector injector = new SimpleInjector(parents);

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
     * <p>
     * Where this plugin has a parent which returns a non-null graph from
     * {@link Plugin#getObjectGraph()} that object graph will be used unmodified. Otherwise, the
     * global object graph will be used.
     *
     * @return An {@link ObjectGraph} to be used.
     */
    protected ObjectGraph getObjectGraph() {
        if (metaData.getParent() != null) {
            final PluginInfo parentInfo = pluginManager.getPluginInfoByName(metaData.getParent());
            final Plugin parent = parentInfo.getPlugin();
            final ObjectGraph parentGraph = parent.getObjectGraph();
            if (parentGraph != null) {
                return parentGraph;
            }
        }

        return objectGraph;
    }

    /**
     * Get the licence for this plugin if it exists.
     *
     * @return An InputStream for the licence of this plugin, or null if no licence found.
     *
     * @throws IOException if there is an error with the ResourceManager.
     */
    public Map<String, InputStream> getLicenceStreams() throws IOException {
        final Map<String, InputStream> licences = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        if (!Files.exists(pluginFilesystem.getPath("/META-INF/licenses/"))) {
            return licences;
        }
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(
                pluginFilesystem.getPath("/META-INF/licenses/"))) {
            for (Path path : directoryStream) {
                licences.put(path.getFileName().toString(), Files.newInputStream(path));
            }
        }
        return licences;
    }

    /**
     * Get the defaults, formatters and icons for this plugin.
     */
    private void getDefaults() {
        final ConfigProvider defaults = identityController.getAddonSettings();

        LOG.trace("{}: Using domain '{}'", new Object[]{metaData.getName(), getDomain()});

        for (Map.Entry<String, String> entry : metaData.getDefaultSettings().entrySet()) {
            final String key = entry.getKey();
            final String value = entry.getValue();

            defaults.setOption(getDomain(), key, value);
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
     * Try get the identities for this plugin. This will unload any identities previously loaded by
     * this plugin.
     */
    private void loadIdentities() {
        if (!Files.exists(pluginFilesystem.getPath("/META-INF/identities/"))) {
            return;
        }
        try {
            final Map<String, InputStream> identityStreams = new HashMap<>();
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(
                    pluginFilesystem.getPath("/META-INF/identities/"))) {
                for (Path path : directoryStream) {
                    identityStreams.put(path.getFileName().toString(), Files.newInputStream(path));
                }
            }

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
                        final ConfigProvider configProvider = new ConfigFileBackedConfigProvider(
                                eventBus, stream, false);
                        identityController.addConfigProvider(configProvider);
                        configProviders.add(configProvider);
                    } catch (final InvalidIdentityFileException ex) {
                        eventBus.publish(new UserErrorEvent(ErrorLevel.MEDIUM, ex,
                                "Error with identity file '" + name + "' in plugin '"
                                + metaData.getName() + '\'', ""));
                    }
                }
            }
        } catch (final IOException ioe) {
            eventBus.publish(new UserErrorEvent(ErrorLevel.MEDIUM, ioe,
                    "Error finding identities in plugin '" + metaData.getName() + '\'', ""));
        }
    }

    /**
     * Unload any identities loaded by this plugin.
     */
    private void unloadIdentities() {
        synchronized (configProviders) {
            configProviders.forEach(identityController::removeConfigProvider);

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

                if (!"any".equalsIgnoreCase(name) && !"export".equalsIgnoreCase(type)) {
                    final Service service = pluginManager.getService(type, name, true);
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
     * Called when the plugin is updated using the updater. Reloads metaData and updates the list of
     * files.
     */
    public void pluginUpdated() throws PluginException {
            updateClassList();
            updateMetaData();
            updateProvides();
            getDefaults();
    }

    /**
     * Try to reload the metaData from the plugin.config file. If this fails, the old data will be
     * used still.
     *
     * @return true if metaData was reloaded ok, else false.
     */
    private boolean updateMetaData() {
        metaData.load();

        return !metaData.hasErrors();
    }

    /**
     * Returns the file system for this plugin's jar.
     *
     * @return Filesystem
     */
    public FileSystem getFileSystem() {
        return pluginFilesystem;
    }

    /**
     * Returns a path inside the plugin's jar for the given name.
     *
     * @param   first The path string or initial part of the path string
     * @param   more Additional strings to be joined to form the full path
     *
     * @return The resulting path inside the plugin's jar
     */
    public Path getPath(final String first, final String... more) {
        return pluginFilesystem.getPath(first, more);
    }

    @Override
    public final boolean isActive() {
        return isLoaded();
    }

    @Override
    public void activateServices() {
        loadPlugin();
    }

    @Override
    public String getProviderName() {
        return "Plugin: " + metaData.getFriendlyName() + " ("
                + metaData.getName() + " / " + getFilename() + ')';
    }

    @Override
    public List<Service> getServices() {
        synchronized (provides) {
            return new ArrayList<>(provides);
        }
    }

    /**
     * Is this plugin loaded?
     *
     * @return True if the plugin is currently (non-temporarily) loaded, false otherwise
     */
    public boolean isLoaded() {
        return plugin != null && !tempLoaded;
    }

    /**
     * Is this plugin temporarily loaded?
     *
     * @return True if this plugin is currently temporarily loaded, false otherwise
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
        for (String serviceInfo : metaData.getRequiredServices()) {
            final String[] parts = serviceInfo.split(" ", 2);

            if ("any".equals(parts[0])) {
                Service best = null;
                boolean found = false;

                for (Service service : pluginManager.getServicesByType(parts[1])) {
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
                final Service service = pluginManager.getService(parts[1], parts[0]);

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

        return metaData.getParent() == null || loadRequiredPlugin(metaData.getParent());
    }

    /**
     * Attempts to load the specified required plugin.
     *
     * @param name The name of the plugin to be loaded
     *
     * @return True if the plugin was found and loaded, false otherwise
     */
    protected boolean loadRequiredPlugin(final String name) {
        LOG.info("Loading required plugin '{}' for plugin {}",
                new Object[]{name, metaData.getName()});

        final PluginInfo pi = pluginManager.getPluginInfoByName(name);

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
            lastError = "Not Loading: (" + isLoaded() + "||" + isLoading + ')';
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
                lastError = "Error in onLoad for " + metaData.getName() + ':'
                        + e.getMessage();
                eventBus.publishAsync(new AppErrorEvent(ErrorLevel.MEDIUM, e, lastError, ""));
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
                //TODO plugin loading shouldn't be done from here, event bus shouldn't be here.
                eventBus.publishAsync(new PluginLoadedEvent(this));
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
            final PluginClassLoader[] loaders = new PluginClassLoader[metaData.getParent() == null
                    ? 0 : 1];

            if (metaData.getParent() != null) {
                final String parentName = metaData.getParent();
                final PluginInfo parent = pluginManager.getPluginInfoByName(parentName);
                parent.addChild(this);
                loaders[0] = parent.getPluginClassLoader();

                if (loaders[0] == null) {
                    // Not loaded? Try again...

                    parent.loadPlugin();
                    loaders[0] = parent.getPluginClassLoader();

                    if (loaders[0] == null) {
                        lastError = "Unable to get classloader from required parent '" + parentName
                                + "' for " + metaData.getName();
                        return;
                    }
                }
            }

            pluginClassLoader = new PluginClassLoader(this, pluginManager.getGlobalClassLoader(),
                    loaders);
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
                                    + filename + ':' + metaData.getMainClass()
                                    + "' -> '" + prerequisites.getFailureReason() + "') ";
                            eventBus.publish(new UserErrorEvent(ErrorLevel.LOW, null, lastError, ""));
                        }
                    } else {
                        plugin = (Plugin) temp;
                        if (!tempLoaded) {
                            try {
                                plugin.load(this, getObjectGraph());
                                plugin.onLoad();
                            } catch (LinkageError | Exception e) {
                                lastError = "Error in onLoad for "
                                        + metaData.getName() + ':' + e.getMessage();
                                eventBus.publishAsync(new AppErrorEvent(ErrorLevel.MEDIUM,
                                        e, lastError, ""));
                                unloadPlugin();
                            }
                        }
                    }
                }
            }
        } catch (ClassNotFoundException cnfe) {
            lastError = "Class not found ('" + filename + ':' + classname + ':'
                    + classname.equals(metaData.getMainClass()) + "') - "
                    + cnfe.getMessage();
            eventBus.publishAsync(new UserErrorEvent(ErrorLevel.LOW, cnfe, lastError, ""));
        } catch (NoClassDefFoundError ncdf) {
            lastError = "Unable to instantiate plugin ('" + filename + ':'
                    + classname + ':'
                    + classname.equals(metaData.getMainClass())
                    + "') - Unable to find class: " + ncdf.getMessage();
            eventBus.publishAsync(new UserErrorEvent(ErrorLevel.LOW, ncdf, lastError, ""));
        } catch (VerifyError ve) {
            lastError = "Unable to instantiate plugin ('" + filename + ':'
                    + classname + ':'
                    + classname.equals(metaData.getMainClass())
                    + "') - Incompatible: " + ve.getMessage();
            eventBus.publishAsync(new UserErrorEvent(ErrorLevel.LOW, ve, lastError, ""));
        } catch (IllegalArgumentException ex) {
            lastError = "Unable to instantiate class for plugin " + metaData.getName()
                    + ": " + classname;
            eventBus.publishAsync(new AppErrorEvent(ErrorLevel.LOW, ex, lastError, ""));
        }
    }

    /**
     * Gets the configuration domain that should be used by this plugin.
     *
     * @return The configuration domain to use.
     */
    public String getDomain() {
        return "plugin-" + metaData.getName();
    }

    /**
     * Unload the plugin if possible.
     */
    public void unloadPlugin() {
        unloadPlugin(false);
    }

    /**
     * Can this plugin be unloaded? Will return false if: - The plugin is persistent (all its
     * classes are loaded into the global class loader) - The plugin isn't currently loaded - The
     * metadata key "unloadable" is set to false, no or 0
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
                if (!parentUnloading && metaData.getParent() != null) {
                    final PluginInfo pi = pluginManager.getPluginInfoByName(metaData.getParent());
                    if (pi != null) {
                        pi.delChild(this);
                    }
                }

                // Now unload ourself
                try {
                    plugin.onUnload();
                } catch (Exception | LinkageError e) {
                    lastError = "Error in onUnload for " + metaData.getName()
                            + ':' + e + " - " + e.getMessage();
                    eventBus.publishAsync(new AppErrorEvent(ErrorLevel.MEDIUM, e, lastError, ""));
                }

                //TODO plugin unloading shouldn't be done from here, event bus shouldn't be here.
                eventBus.publishAsync(new PluginUnloadedEvent(this));
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
     *
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

    @Override
    public int compareTo(@Nonnull final PluginInfo o) {
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
                final Service service = pluginManager.getService("export", serviceName, true);
                synchronized (provides) {
                    service.addProvider(this);
                    provides.add(service);
                }
                // Add is as an export
                exports.put(serviceName, new ExportInfo(methodName, methodClass, this));
            }
        }
    }

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
