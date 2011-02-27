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

import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.config.Identity;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.InvalidIdentityFileException;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.updater.Version;
import com.dmdirc.util.resourcemanager.ResourceManager;
import com.dmdirc.util.validators.ValidationResponse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

/**
 * Stores plugin metadata and handles loading of plugin resources.
 */
public class PluginInfo implements Comparable<PluginInfo>, ServiceProvider {

    /** A logger for this class. */
    private static final java.util.logging.Logger LOGGER
            = java.util.logging.Logger.getLogger(PluginInfo.class.getName());

    /** The metadata for this plugin. */
    private final PluginMetaData metadata;
    /** URL that this plugin was loaded from. */
    private final URL url;
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
    /** Requirements error message. */
    private String requirementsError = "";
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
     * @param url URL to file that this plugin is stored in.
     * @throws PluginException if there is an error loading the Plugin
     * @since 0.6.6
     */
    public PluginInfo(final PluginMetaData metadata, final URL url) throws PluginException {
        this(metadata, url, true);
    }

    /**
     * Create a new PluginInfo.
     *
     * @param metadata The plugin's metadata information
     * @param url URL to file that this plugin is stored in.
     * @param load Should this plugin be loaded, or is this just a placeholder? (true for load, false for placeholder)
     * @throws PluginException if there is an error loading the Plugin
     * @since 0.6.6
     */
    public PluginInfo(final PluginMetaData metadata, final URL url, final boolean load) throws PluginException {
        this.url = url;
        this.filename = new File(url.getPath()).getName();
        this.metadata = metadata;

        ResourceManager res;

        // Check for updates.
        if (new File(getFullFilename() + ".update").exists() && new File(getFullFilename()).delete()) {
            new File(getFullFilename() + ".update").renameTo(new File(getFullFilename()));

            updateMetaData();
        }

        if (!load) {
            // TODO: This is pointless now
            return;
        }

        if (metadata.hasErrors()) {
            throw new PluginException("Plugin " + filename + " has metadata "
                    + "errors: " + metadata.getErrors());
        }

        try {
            res = getResourceManager();
        } catch (IOException ioe) {
            lastError = "Error with resourcemanager: " + ioe.getMessage();
            throw new PluginException("Plugin " + filename + " failed to load. " + lastError, ioe);
        }

        if (!getVersion().isValid()) {
            lastError = "Incomplete plugin.config (Missing or invalid 'version')";
            throw new PluginException("Plugin " + filename + " failed to load. " + lastError);
        } else if (getAuthor().isEmpty()) {
            lastError = "Incomplete plugin.config (Missing or invalid 'author')";
            throw new PluginException("Plugin " + filename + " failed to load. " + lastError);
        } else if (getName().isEmpty() || getName().indexOf(' ') != -1) {
            lastError = "Incomplete plugin.config (Missing or invalid 'name')";
            throw new PluginException("Plugin " + filename + " failed to load. " + lastError);
        } else if (getMainClass().isEmpty()) {
            lastError = "Incomplete plugin.config (Missing or invalid 'mainclass')";
            throw new PluginException("Plugin " + filename + " failed to load. " + lastError);
        }

        if (checkRequirements(true)) {
            final String mainClass = getMainClass().replace('.', '/') + ".class";
            if (!res.resourceExists(mainClass)) {
                lastError = "main class file (" + mainClass + ") not found in jar.";
                throw new PluginException("Plugin " + filename + " failed to load. " + lastError);
            }

            for (final String classfilename : res.getResourcesStartingWith("")) {
                String classname = classfilename.replace('/', '.');
                if (classname.matches("^.*\\.class$")) {
                    classname = classname.replaceAll("\\.class$", "");
                    myClasses.add(classname);
                }
            }
        } else {
            lastError = "One or more requirements not met (" + requirementsError + ")";
            throw new PluginException("Plugin " + filename + " was not loaded. " + lastError);
        }

        updateProvides();
        getDefaults();
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
        final String domain = "plugin-" + getName();

        LOGGER.finer(getName() + ": Using domain '" + domain + "'");

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

                if (name.endsWith("/")) {
                    // Don't try to load folders as identities
                    continue;
                }

                if (stream == null) {
                    //Don't add null streams
                    continue;
                }

                synchronized (identities) {
                    try {
                        final Identity thisIdentity = new Identity(stream, false);
                        IdentityManager.addIdentity(thisIdentity);
                        identities.add(thisIdentity);
                    } catch (final InvalidIdentityFileException ex) {
                        Logger.userError(ErrorLevel.MEDIUM, "Error with identity file '" + name + "' in plugin '" + getName() + "'", ex);
                    }
                }
            }
        } catch (final IOException ioe) {
            Logger.userError(ErrorLevel.MEDIUM, "Error finding identities in plugin '" + getName() + "'", ioe);
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
            final ResourceManager res = getResourceManager(true);

            myClasses.clear();
            for (final String classfilename : res.getResourcesStartingWith("")) {
                String classname = classfilename.replace('/', '.');
                if (classname.matches("^.*\\.class$")) {
                    classname = classname.replaceAll("\\.class$", "");
                    myClasses.add(classname);
                }
            }
            updateMetaData();
            updateProvides();
            getDefaults();
        } catch (IOException ioe) {
            Logger.userError(ErrorLevel.MEDIUM, "There was an error updating "+getName(), ioe);
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
     * Get the contents of requirementsError
     *
     * @return requirementsError
     */
    public String getRequirementsError() {
        return requirementsError;
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

    /**
     * Checks to see if the minimum version requirement of the plugin is
     * satisfied.
     * If either version is non-positive, the test passes.
     * On failure, the requirementsError field will contain a user-friendly
     * error message.
     *
     * @param desired The desired minimum version of DMDirc.
     * @param actual The actual current version of DMDirc.
     * @return True if the test passed, false otherwise
     */
    protected boolean checkMinimumVersion(final String desired, final int actual) {
        if (desired.isEmpty()) {
            return true;
        }

        int minversion;
        try {
            minversion = Integer.parseInt(desired);
        } catch (NumberFormatException ex) {
            requirementsError = "'minversion' is a non-integer";
            return false;
        }

        if (actual > 0 && minversion > 0 && actual < minversion) {
            requirementsError = "Plugin is for a newer version of DMDirc";
            return false;
        } else {
            return true;
        }
    }

    /**
     * Checks to see if the maximum version requirement of the plugin is
     * satisfied.
     * If either version is non-positive, the test passes.
     * If the desired version is empty, the test passes.
     * On failure, the requirementsError field will contain a user-friendly
     * error message.
     *
     * @param desired The desired maximum version of DMDirc.
     * @param actual The actual current version of DMDirc.
     * @return True if the test passed, false otherwise
     */
    protected boolean checkMaximumVersion(final String desired, final int actual) {
        if (desired.isEmpty()) {
            return true;
        }

        int maxversion;
        try {
            maxversion = Integer.parseInt(desired);
        } catch (NumberFormatException ex) {
            requirementsError = "'maxversion' is a non-integer";
            return false;
        }

        if (actual > 0 && maxversion > 0 && actual > maxversion) {
            requirementsError = "Plugin is for an older version of DMDirc";
            return false;
        } else {
            return true;
        }
    }

    /**
     * Checks to see if the OS requirements of the plugin are satisfied.
     * If the desired string is empty, the test passes.
     * Otherwise it is used as one to three colon-delimited regular expressions,
     * to test the name, version and architecture of the OS, respectively.
     * On failure, the requirementsError field will contain a user-friendly
     * error message.
     *
     * @param desired The desired OS requirements
     * @param actualName The actual name of the OS
     * @param actualVersion The actual version of the OS
     * @param actualArch The actual architecture of the OS
     * @return True if the test passes, false otherwise
     */
    protected boolean checkOS(final String desired, final String actualName, final String actualVersion, final String actualArch) {
        if (desired == null || desired.isEmpty()) {
            return true;
        }

        final String[] desiredParts = desired.split(":");

        if (!actualName.toLowerCase().matches(desiredParts[0])) {
            requirementsError = "Invalid OS. (Wanted: '" + desiredParts[0] + "', actual: '" + actualName + "')";
            return false;
        } else if (desiredParts.length > 1 && !actualVersion.toLowerCase().matches(desiredParts[1])) {
            requirementsError = "Invalid OS version. (Wanted: '" + desiredParts[1] + "', actual: '" + actualVersion + "')";
            return false;
        } else if (desiredParts.length > 2 && !actualArch.toLowerCase().matches(desiredParts[2])) {
            requirementsError = "Invalid OS architecture. (Wanted: '" + desiredParts[2] + "', actual: '" + actualArch + "')";
            return false;
        }

        return true;
    }

    /**
     * Checks to see if the file requirements of the plugin are satisfied.
     * If the desired string is empty, the test passes.
     * Otherwise it is passed to File.exists() to see if the file is valid.
     * Multiple files can be specified by using a "," to separate. And either/or
     * files can be specified using a "|" (eg /usr/bin/bash|/bin/bash)
     * If the test fails, the requirementsError field will contain a
     * user-friendly error message.
     *
     * @param desired The desired file requirements
     * @return True if the test passes, false otherwise
     */
    protected boolean checkFiles(final String desired) {
        if (desired == null || desired.isEmpty()) {
            return true;
        }

        for (String files : desired.split(",")) {
            final String[] filelist = files.split("\\|");
            boolean foundFile = false;
            for (String file : filelist) {
                if (new File(file).exists()) {
                    foundFile = true;
                    break;
                }
            }
            if (!foundFile) {
                requirementsError = "Required file '" + files + "' not found";
                return false;
            }
        }
        return true;
    }

    /**
     * Checks to see if the plugin requirements of the plugin are satisfied.
     * If the desired string is empty, the test passes.
     * Plugins should be specified as:
     * plugin1[:minversion[:maxversion]],plugin2[:minversion[:maxversion]]
     * Plugins will be attempted to be loaded if not loaded, else the test will
     * fail if the versions don't match, or the plugin isn't known.
     * If the test fails, the requirementsError field will contain a
     * user-friendly error message.
     *
     * @param desired The desired file requirements
     * @return True if the test passes, false otherwise
     */
    protected boolean checkPlugins(final String desired) {
        if (desired == null || desired.isEmpty()) {
            return true;
        }

        for (String pluginName : desired.split(",")) {
            final String[] data = pluginName.split(":");
            final PluginInfo pi = PluginManager.getPluginManager()
                    .getPluginInfoByName(data[0]);
            if (pi == null) {
                requirementsError = "Required plugin '" + data[0]
                        + "' was not found";
                return false;
            }
            if (data.length > 1) {
                // Check plugin minimum version matches.
                if (pi.getVersion().compareTo(new Version(data[1])) < 0) {
                    requirementsError = "Plugin '" + data[0]
                            + "' is too old (Required Version: " + data[1]
                            + ", Actual Version: " + pi.getVersion() + ")";
                    return false;
                }
                // Check plugin maximum version matches.
                if (data.length > 2 && pi.getVersion().compareTo(
                        new Version(data[2])) > 0) {
                    requirementsError = "Plugin '" + data[0]
                            + "' is too new (Required Version: " + data[2]
                            + ", Actual Version: " + pi.getVersion() + ")";
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Are the requirements for this plugin met?
     *
     * @param preliminary Is this a preliminary check?
     * @return true/false (Actual error if false is in the requirementsError field)
     */
    public boolean checkRequirements(final boolean preliminary) {
        if (!checkOS(metadata.getRequirements().get("os"), System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch"))
                || !checkFiles(metadata.getRequirements().get("files"))
                || (!preliminary && !checkPlugins(metadata.getRequirements().get("plugins")))
                || (!preliminary && !checkServices(metadata.getRequiredServices()))) {
            return false;
        }

        // All requirements passed, woo \o
        return true;
    }

    /**
     * Check if the services required by this plugin are available.
     *
     * @param services Required services
     * @return true if all services are available
     */
    private boolean checkServices(final Collection<String> services) {
        if (services == null || services.isEmpty()) {
            return true;
        }

        for (String requirement : services) {
            boolean available = false;
            final String[] bits = requirement.split(" ", 2);
            final String name = bits[0];
            final String type = bits.length > 1 ? bits[1] : "misc";

            Service best = null;
            if (name.equalsIgnoreCase("any")) {
                final List<Service> serviceList = PluginManager.getPluginManager().getServicesByType(type);
                for (Service service : serviceList) {
                    if (service.isActive()) {
                        // There's a service already active, let's use that.
                        best = service;
                        available = true;
                        break;
                    }
                }

                if (!available && !serviceList.isEmpty()) {
                    // If none are already active, default to the first one
                    best = serviceList.get(0);
                }
            } else {
                best = PluginManager.getPluginManager().getService(type, name, false);
            }

            if (best != null) {
                available = best.activate();
            }

            if (!available) {
                return false;
            }
        }

        return true;
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
        return "Plugin: " + getNiceName() + " (" + getName() + " / " + getFilename() + ")";
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
     * Load any required plugins
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

        if (!checkRequirements(isTempLoaded() || tempLoaded)) {
            lastError = "Unable to loadPlugin, all requirements not met. (" + requirementsError + ")";
            return;
        }

        updateProvides();

        if (isTempLoaded()) {
            tempLoaded = false;
            loadRequired();

            try {
                plugin.onLoad();
            } catch (LinkageError e) {
                lastError = "Error in onLoad for " + getName() + ":" + e.getMessage();
                Logger.userError(ErrorLevel.MEDIUM, lastError, e);
                unloadPlugin();
            } catch (Exception e) {
                lastError = "Error in onLoad for " + getName() + ":" + e.getMessage();
                Logger.userError(ErrorLevel.MEDIUM, lastError, e);
                unloadPlugin();
            }
        } else {
            isLoading = true;
            loadIdentities();
            loadRequired();
            loadClass(getMainClass());

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
     * @return true iif this plugin has children
     */
    public boolean hasChildren() {
        return !children.isEmpty();
    }

    /**
     * Load the given classname.
     *
     * @param classname Class to load
     */
    private void loadClass(final String classname) {
        try {
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

            // Don't reload a class if its already loaded.
            if (classloader.isClassLoaded(classname, true)) {
                lastError = "Classloader says we are already loaded.";
                return;
            }

            final Class<?> c = classloader.loadClass(classname);
            if (c == null) {
                lastError = "Class '"+classname+"' was not able to load.";
                return;
            }

            // Only try and construct the main class, anything else should be constructed
            // by the plugin itself.
            if (classname.equals(getMainClass())) {
                final Constructor<?> constructor = c.getConstructor();
                final Object temp = constructor.newInstance();

                if (temp instanceof Plugin) {
                    final ValidationResponse prerequisites = ((Plugin) temp).checkPrerequisites();
                    if (prerequisites.isFailure()) {
                        if (!tempLoaded) {
                            lastError = "Prerequisites for plugin not met. ('" + filename + ":" + getMainClass() + "' -> '" + prerequisites.getFailureReason() + "') ";
                            Logger.userError(ErrorLevel.LOW, lastError);
                        }
                    } else {
                        plugin = (Plugin) temp;
                        LOGGER.finer(getName() + ": Setting domain 'plugin-" + getName() + "'");
                        plugin.setPluginInfo(this);
                        plugin.setDomain("plugin-" + getName());
                        if (!tempLoaded) {
                            try {
                                plugin.onLoad();
                            } catch (LinkageError e) {
                                lastError = "Error in onLoad for " + getName() + ":" + e.getMessage();
                                Logger.userError(ErrorLevel.MEDIUM, lastError, e);
                                unloadPlugin();
                            } catch (Exception e) {
                                lastError = "Error in onLoad for " + getName() + ":" + e.getMessage();
                                Logger.userError(ErrorLevel.MEDIUM, lastError, e);
                                unloadPlugin();
                            }
                        }
                    }
                }
            }
        } catch (ClassNotFoundException cnfe) {
            lastError = "Class not found ('" + filename + ":" + classname + ":" + classname.equals(getMainClass()) + "') - " + cnfe.getMessage();
            Logger.userError(ErrorLevel.LOW, lastError, cnfe);
        } catch (NoSuchMethodException nsme) {
            // Don't moan about missing constructors for any class thats not the main Class
            lastError = "Constructor missing ('" + filename + ":" + classname + ":" + classname.equals(getMainClass()) + "') - " + nsme.getMessage();
            if (classname.equals(getMainClass())) {
                Logger.userError(ErrorLevel.LOW, lastError, nsme);
            }
        } catch (IllegalAccessException iae) {
            lastError = "Unable to access constructor ('" + filename + ":" + classname + ":" + classname.equals(getMainClass()) + "') - " + iae.getMessage();
            Logger.userError(ErrorLevel.LOW, lastError, iae);
        } catch (InvocationTargetException ite) {
            lastError = "Unable to invoke target ('" + filename + ":" + classname + ":" + classname.equals(getMainClass()) + "') - " + ite.getMessage();
            Logger.userError(ErrorLevel.LOW, lastError, ite);
        } catch (InstantiationException ie) {
            lastError = "Unable to instantiate plugin ('" + filename + ":" + classname + ":" + classname.equals(getMainClass()) + "') - " + ie.getMessage();
            Logger.userError(ErrorLevel.LOW, lastError, ie);
        } catch (NoClassDefFoundError ncdf) {
            lastError = "Unable to instantiate plugin ('" + filename + ":" + classname + ":" + classname.equals(getMainClass()) + "') - Unable to find class: " + ncdf.getMessage();
            Logger.userError(ErrorLevel.LOW, lastError, ncdf);
        } catch (VerifyError ve) {
            lastError = "Unable to instantiate plugin ('" + filename + ":" + classname + ":" + classname.equals(getMainClass()) + "') - Incompatible: " + ve.getMessage();
            Logger.userError(ErrorLevel.LOW, lastError, ve);
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
                    lastError = "Error in onUnload for " + getName() + ":" + e + " - " + e.getMessage();
                    Logger.userError(ErrorLevel.MEDIUM, lastError, e);
                } catch (LinkageError e) {
                    lastError = "Error in onUnload for " + getName() + ":" + e + " - " + e.getMessage();
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
        return myClasses;
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
        final Collection<String> result = new ArrayList<String>();

        if (isPersistent()) {
            try {
                final ResourceManager res = getResourceManager();

                for (final String resourceFilename : res.getResourcesStartingWith("")) {
                    if (resourceFilename.matches("^.*\\.class$")) {
                        result.add(resourceFilename.replaceAll("\\.class$", "").replace('/', '.'));
                    }
                }
            } catch (IOException e) {
                // Jar no longer exists?
            }
        }

        return metadata.getPersistentClasses();
    }

    /**
     * Is this a persistent class?
     *
     * @param classname class to check persistence of
     * @return true if file (or whole plugin) is persistent, else false
     */
    public boolean isPersistent(final String classname) {
        if (isPersistent()) {
            return true;
        } else {
            return metadata.getPersistentClasses().contains(classname);
        }
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
     */
    public String getFullFilename() {
        return url.getPath();
    }

    /**
     * Retrieves the path to this plugin relative to the main plugin directory,
     * if appropriate.
     *
     * @return A relative path to the plugin if it is situated under the main
     * plugin directory, or an absolute path otherwise.
     */
    public String getRelativeFilename() {
        final String dir = new File(PluginManager.getPluginManager().getDirectory())
                .getAbsolutePath() + File.separator;
        final String file = new File(getFullFilename()).getAbsolutePath();

        return file.startsWith(dir) ? getFullFilename().substring(dir.length()) : getFullFilename();
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
     * Get the minimum dmdirc version required to run the plugin.
     *
     * @return minimum dmdirc version required to run the plugin.
     */
    public String getMinVersion() {
        final String requiredVersion = metadata.getRequirements().get("dmdirc");

        if (requiredVersion != null) {
            final String[] bits = requiredVersion.split("-");
            return bits[0];
        }

        return "";
    }

    /**
     * Get the (optional) maximum dmdirc version on which this plugin can run
     *
     * @return optional maximum dmdirc version on which this plugin can run
     */
    public String getMaxVersion() {
        final String requiredVersion = metadata.getRequirements().get("dmdirc");

        if (requiredVersion != null) {
            final String[] bits = requiredVersion.split("-");
            if (bits.length > 1) {
                return bits[1];
            }
        }

        return "";
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
        return getNiceName() + " - " + filename;
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
                final String serviceName = (bits.length > 4) ? bits[4] : bits[0];

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
     * @return true iif the plugin exports the service
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
