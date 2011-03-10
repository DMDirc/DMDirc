/*
 * Copyright (c) 2006-2011 DMDirc Developers
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
import com.dmdirc.config.Identity;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.InvalidIdentityFileException;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.updater.Version;
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
import java.util.logging.Level;

/**
 * Stores plugin metadata and handles loading of plugin resources.
 */
public class PluginInfo implements Comparable<PluginInfo>, ServiceProvider {

    /** A logger for this class. */
    private static final java.util.logging.Logger LOGGER
            = java.util.logging.Logger.getLogger(PluginInfo.class.getName());

    /** The metadata for this plugin. */
    private final PluginMetaData metadata;
    /** Filename for this plugin (taken from URL). */
    private final String filename;
    /** The actual Plugin from this jar. */
    private Plugin plugin;
    /** The classloader used for this Plugin. */
    private PluginClassLoader classloader;
    /** The resource manager used by this pluginInfo. */
    private ResourceManager myResourceManager;
    /** Is this plugin only loaded temporarily? */
    private boolean tempLoaded;
    /** List of classes this plugin has. */
    private final List<String> myClasses = new ArrayList<String>();
    /** Last Error Message. */
    private String lastError = "No Error";
    /** Are we trying to load? */
    private boolean isLoading;
    /** List of services we provide. */
    private final List<Service> provides = new ArrayList<Service>();
    /** List of children of this plugin. */
    private final List<PluginInfo> children = new ArrayList<PluginInfo>();
    /** Map of exports. */
    private final Map<String, ExportInfo> exports = new HashMap<String, ExportInfo>();
    /** List of identities. */
    private final List<Identity> identities = new ArrayList<Identity>();

    /**
     * Create a new PluginInfo.
     *
     * @param metadata The plugin's metadata information
     * @throws PluginException if there is an error loading the Plugin
     * @since 0.6.6
     */
    public PluginInfo(final PluginMetaData metadata) throws PluginException {
        this.filename = new File(metadata.getPluginUrl().getPath()).getName();
        this.metadata = metadata;

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
     * Gets this plugin's meta data object.
     *
     * @return Plugin meta data object
     */
    public PluginMetaData getMetaData() {
        return metadata;
    }

    /**
     * Updates the list of known classes within this plugin from the specified
     * resource manager.
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
        if (metadata.getParent() == null || metadata.getParent().isEmpty()) {
            injector = new SimpleInjector();
            injector.addParameter(PluginManager.class,
                    PluginManager.getPluginManager());
            injector.addParameter(ActionManager.class,
                    ActionManager.getActionManager());
        } else {
            final PluginInfo parent = PluginManager.getPluginManager()
                    .getPluginInfoByName(metadata.getParent());
            injector = new SimpleInjector(parent.getInjector());
            injector.addParameter(parent.getPlugin());
        }

        injector.addParameter(PluginInfo.class, this);

        return injector;
    }

    /**
     * Get the licence for this plugin if it exists.
     *
     * @return An InputStream for the licence of this plugin, or null if no
     *         licence found.
     * @throws IOException if there is an error with the ResourceManager.
     */
    public Map<String, InputStream> getLicenceStreams() throws IOException {
        final TreeMap<String, InputStream> licences =
                new TreeMap<String, InputStream>(String.CASE_INSENSITIVE_ORDER);
        licences.putAll(getResourceManager().getResourcesStartingWithAsInputStreams(
                "META-INF/licences/"));
        return licences;
    }

    /**
     * Get the defaults, formatters and icons for this plugin.
     */
    private void getDefaults() {
        final Identity defaults = IdentityManager.getAddonIdentity();
        final String domain = "plugin-" + metadata.getName();

        LOGGER.log(Level.FINER, "{0}: Using domain ''{1}''",
                new Object[]{metadata.getName(), domain});

        for (Map.Entry<String, String> entry : metadata.getDefaultSettings().entrySet()) {
            final String key = entry.getKey();
            final String value = entry.getValue();

            defaults.setOption(domain, key, value);
        }

        for (Map.Entry<String, String> entry : metadata.getFormatters().entrySet()) {
            final String key = entry.getKey();
            final String value = entry.getValue();

            defaults.setOption("formatter", key, value);
        }

        for (Map.Entry<String, String> entry : metadata.getIcons().entrySet()) {
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

                synchronized (identities) {
                    try {
                        final Identity thisIdentity = new Identity(stream, false);
                        IdentityManager.addIdentity(thisIdentity);
                        identities.add(thisIdentity);
                    } catch (final InvalidIdentityFileException ex) {
                        Logger.userError(ErrorLevel.MEDIUM,
                                "Error with identity file '" + name
                                + "' in plugin '" + metadata.getName() + "'", ex);
                    }
                }
            }
        } catch (final IOException ioe) {
            Logger.userError(ErrorLevel.MEDIUM, "Error finding identities in plugin '"
                    + metadata.getName() + "'", ioe);
        }
    }

    /**
     * Unload any identities loaded by this plugin.
     */
    private void unloadIdentities() {
        synchronized (identities) {
            for (Identity identity : identities) {
                IdentityManager.removeIdentity(identity);
            }

            identities.clear();
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
        final Collection<String> providesList = metadata.getServices();
        if (providesList != null) {
            for (String item : providesList) {
                final String[] bits = item.split(" ");
                final String name = bits[0];
                final String type = (bits.length > 1) ? bits[1] : "misc";

                if (!name.equalsIgnoreCase("any") && !type.equalsIgnoreCase("export")) {
                    final Service service = PluginManager.getPluginManager().getService(type, name, true);
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
                    + metadata.getName(), ioe);
        }
    }

    /**
     * Try to reload the metaData from the plugin.config file.
     * If this fails, the old data will be used still.
     *
     * @return true if metaData was reloaded ok, else false.
     */
    private boolean updateMetaData() {
        metadata.load();

        return !metadata.hasErrors();
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
        if (myResourceManager == null || forceNew) {
            myResourceManager = ResourceManager.getResourceManager("jar://" + getFullFilename());

            // Clear the resourcemanager in 10 seconds to stop us holding the file open
            new Timer(filename + "-resourcemanagerTimer").schedule(new TimerTask() {

                /** {@inheritDoc} */
                @Override
                public void run() {
                    myResourceManager = null;
                }

            }, 10000);
        }

        return myResourceManager;
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
        return "Plugin: " + metadata.getFriendlyName() + " ("
                + metadata.getName() + " / " + getFilename() + ")";
    }

    /** {@inheritDoc} */
    @Override
    public List<Service> getServices() {
        synchronized (provides) {
            return new ArrayList<Service>(provides);
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
        tempLoaded = true;
        loadPlugin();
    }

    /**
     * Load any required plugins.
     */
    public void loadRequired() {
        final String required = metadata.getRequirements().get("plugins");

        if (required == null) {
            return;
        }

        for (String pluginName : required.split(",")) {
            final String[] data = pluginName.split(":");
            if (!data[0].trim().isEmpty()) {
                final PluginInfo pi = PluginManager.getPluginManager().getPluginInfoByName(data[0]);

                if (pi == null) {
                    return;
                }
                if (tempLoaded) {
                    pi.loadPluginTemp();
                } else {
                    pi.loadPlugin();
                }
            }
        }
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
            loadRequired();

            try {
                plugin.onLoad();
            } catch (LinkageError e) {
                lastError = "Error in onLoad for " + metadata.getName() + ":"
                        + e.getMessage();
                Logger.userError(ErrorLevel.MEDIUM, lastError, e);
                unloadPlugin();
            } catch (Exception e) {
                lastError = "Error in onLoad for " + metadata.getName() + ":"
                        + e.getMessage();
                Logger.userError(ErrorLevel.MEDIUM, lastError, e);
                unloadPlugin();
            }
        } else {
            isLoading = true;
            loadIdentities();
            loadRequired();
            loadClass(metadata.getMainClass());

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
        return new ArrayList<PluginInfo>(children);
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
        if (classloader == null) {
            if (metadata.getParent() == null) {
                classloader = new PluginClassLoader(this);
            } else {
                final String parentName = metadata.getParent();
                final PluginInfo pi = PluginManager.getPluginManager().getPluginInfoByName(parentName);
                if (pi == null) {
                    lastError = "Required parent '" + parentName + "' was not found";
                    return;
                } else {
                    pi.addChild(this);
                    PluginClassLoader parentCL = pi.getPluginClassLoader();
                    if (parentCL == null) {
                        // Parent appears not to be loaded.
                        pi.loadPlugin();
                        parentCL = pi.getPluginClassLoader();
                        if (parentCL == null) {
                            lastError = "Unable to get classloader from required parent '" + parentName + "' for "+getName();
                            return;
                        }
                    }
                    classloader = parentCL.getSubClassLoader(this);
                }
            }
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
            if (classloader.isClassLoaded(classname, true)) {
                lastError = "Classloader says we are already loaded.";
                return;
            }

            final Class<?> clazz = classloader.loadClass(classname);
            if (clazz == null) {
                lastError = "Class '" + classname + "' was not able to load.";
                return;
            }

            // Only try and construct the main class, anything else should be constructed
            // by the plugin itself.
            if (classname.equals(metadata.getMainClass())) {
                final Object temp = getInjector().createInstance(clazz);

                if (temp instanceof Plugin) {
                    final ValidationResponse prerequisites = ((Plugin) temp).checkPrerequisites();
                    if (prerequisites.isFailure()) {
                        if (!tempLoaded) {
                            lastError = "Prerequisites for plugin not met. ('"
                                    + filename + ":" + metadata.getMainClass()
                                    + "' -> '" + prerequisites.getFailureReason() + "') ";
                            Logger.userError(ErrorLevel.LOW, lastError);
                        }
                    } else {
                        plugin = (Plugin) temp;
                        LOGGER.log(Level.FINER, "{0}: Setting domain ''plugin-{0}''",
                                new Object[]{metadata.getName()});
                        plugin.setPluginInfo(this);
                        plugin.setDomain("plugin-" + metadata.getName());
                        if (!tempLoaded) {
                            try {
                                plugin.onLoad();
                            } catch (LinkageError e) {
                                lastError = "Error in onLoad for "
                                        + metadata.getName() + ":" + e.getMessage();
                                Logger.userError(ErrorLevel.MEDIUM, lastError, e);
                                unloadPlugin();
                            } catch (Exception e) {
                                lastError = "Error in onLoad for "
                                        + metadata.getName() + ":" + e.getMessage();
                                Logger.userError(ErrorLevel.MEDIUM, lastError, e);
                                unloadPlugin();
                            }
                        }
                    }
                }
            }
        } catch (ClassNotFoundException cnfe) {
            lastError = "Class not found ('" + filename + ":" + classname + ":"
                    + classname.equals(metadata.getMainClass()) + "') - "
                    + cnfe.getMessage();
            Logger.userError(ErrorLevel.LOW, lastError, cnfe);
        } catch (NoClassDefFoundError ncdf) {
            lastError = "Unable to instantiate plugin ('" + filename + ":"
                    + classname + ":"
                    + classname.equals(metadata.getMainClass())
                    + "') - Unable to find class: " + ncdf.getMessage();
            Logger.userError(ErrorLevel.LOW, lastError, ncdf);
        } catch (VerifyError ve) {
            lastError = "Unable to instantiate plugin ('" + filename + ":"
                    + classname + ":"
                    + classname.equals(metadata.getMainClass())
                    + "') - Incompatible: " + ve.getMessage();
            Logger.userError(ErrorLevel.LOW, lastError, ve);
        } catch (IllegalArgumentException ex) {
            lastError = "Unable to instantiate plugin ('" + filename + ":"
                    + classname + ":"
                    + classname.equals(metadata.getMainClass())
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
                && metadata.isUnloadable();
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
                final String parentName = metadata.getParent();
                if (!parentUnloading && parentName != null) {
                    final PluginInfo pi = PluginManager.getPluginManager().getPluginInfoByName(parentName);
                    if (pi != null) {
                        pi.delChild(this);
                        classloader = pi.getPluginClassLoader().getSubClassLoader(this);
                    }
                }

                // Now unload ourself
                try {
                    plugin.onUnload();
                } catch (Exception e) {
                    lastError = "Error in onUnload for " + metadata.getName()
                            + ":" + e + " - " + e.getMessage();
                    Logger.userError(ErrorLevel.MEDIUM, lastError, e);
                } catch (LinkageError e) {
                    lastError = "Error in onUnload for " + metadata.getName()
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
            classloader = null;
        }
    }

    /**
     * Get the last Error
     *
     * @return last Error
     * @since 0.6
     */
    public String getLastError() {
        return lastError;
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
     * Get the main Class
     *
     * @return Main Class to begin loading.
     * @deprecated Retrieve this from {@link PluginMetaData} directly
     */
    @Deprecated
    public String getMainClass() {
        return metadata.getMainClass();
    }

    /**
     * Get the Plugin for this plugin.
     *
     * @return Plugin
     */
    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * Get the PluginClassLoader for this plugin.
     *
     * @return PluginClassLoader
     */
    protected PluginClassLoader getPluginClassLoader() {
        return classloader;
    }

    /**
     * Get the plugin friendly version
     *
     * @return Plugin friendly Version
     * @deprecated Retrieve this from {@link PluginMetaData} directly
     */
    @Deprecated
    public String getFriendlyVersion() {
        return metadata.getFriendlyVersion();
    }

    /**
     * Get the plugin version
     *
     * @return Plugin Version
     * @deprecated Retrieve this from {@link PluginMetaData} directly
     */
    @Deprecated
    public Version getVersion() {
        return metadata.getVersion();
    }

    /**
     * Get the id for this plugin on the addons site.
     * If a plugin has been submitted to addons.dmdirc.com, and plugin.config
     * contains a property addonid then this will return it.
     * This is used along with the version property to allow the auto-updater to
     * update the addon if the author submits a new version to the addons site.
     *
     * @return Addon Site ID number
     *         -1 If not present
     *         -2 If non-integer
     * @deprecated Retrieve this from {@link PluginMetaData} directly
     */
    @Deprecated
    public int getAddonID() {
        return metadata.getUpdaterId();
    }

    /**
     * Is this a persistent plugin?
     *
     * @return true if persistent, else false
     */
    public boolean isPersistent() {
        return metadata.getPersistentClasses().contains("*");
    }

    /**
     * Does this plugin contain any persistent classes?
     *
     * @return true if this plugin contains any persistent classes, else false
     */
    public boolean hasPersistent() {
        return !metadata.getPersistentClasses().isEmpty();
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
            return metadata.getPersistentClasses();
        }
    }

    /**
     * Is this a persistent class?
     *
     * @param classname class to check persistence of
     * @return true if file (or whole plugin) is persistent, else false
     */
    public boolean isPersistent(final String classname) {
        return isPersistent() || metadata.getPersistentClasses().contains(classname);
    }

    /**
     * Get the plugin Filename.
     *
     * @return Filename of plugin
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Get the full plugin Filename (inc dirname)
     *
     * @return Filename of plugin
     * @deprecated Retrieve this from {@link PluginMetaData} directly
     */
    @Deprecated
    public String getFullFilename() {
        return metadata.getPluginUrl().getPath();
    }

    /**
     * Retrieves the path to this plugin relative to the main plugin directory,
     * if appropriate.
     *
     * @return A relative path to the plugin if it is situated under the main
     * plugin directory, or an absolute path otherwise.
     * @deprecated Retrieve this from {@link PluginMetaData} directly
     */
    @Deprecated
    public String getRelativeFilename() {
        return metadata.getRelativeFilename();
    }

    /**
     * Get the plugin Author.
     *
     * @return Author of plugin
     * @deprecated Retrieve this from {@link PluginMetaData} directly
     */
    @Deprecated
    public String getAuthor() {
        return metadata.getAuthor();
    }

    /**
     * Get the plugin Description.
     *
     * @return Description of plugin
     * @deprecated Retrieve this from {@link PluginMetaData} directly
     */
    @Deprecated
    public String getDescription() {
        return metadata.getDescription();
    }

    /**
     * Get the name of the plugin. (Used to identify the plugin)
     *
     * @return Name of plugin
     * @deprecated Retrieve this from {@link PluginMetaData} directly
     */
    @Deprecated
    public String getName() {
        return metadata.getName();
    }

    /**
     * Get the nice name of the plugin. (Displayed to users)
     *
     * @return Nice Name of plugin
     * @deprecated Retrieve this from {@link PluginMetaData} directly
     */
    @Deprecated
    public String getNiceName() {
        return metadata.getFriendlyName();
    }

    /**
     * String Representation of this plugin
     *
     * @return String Representation of this plugin
     */
    @Override
    public String toString() {
        return metadata.getFriendlyName() + " - " + filename;
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
        final Collection<String> exportsList = metadata.getExports();
        for (String item : exportsList) {
            final String[] bits = item.split(" ");
            if (bits.length > 2) {
                final String methodName = bits[0];
                final String methodClass = bits[2];
                final String serviceName = bits.length > 4 ? bits[4] : bits[0];

                // Add a provides for this
                final Service service = PluginManager.getPluginManager().getService("export", serviceName, true);
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

    /**
     * Get the Plugin object for this plugin.
     *
     * @return Plugin object for the plugin
     */
    protected Plugin getPluginObject() {
        return plugin;
    }

}
