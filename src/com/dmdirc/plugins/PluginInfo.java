/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
import com.dmdirc.config.prefs.validator.ValidationResponse;
import com.dmdirc.util.resourcemanager.ResourceManager;
import com.dmdirc.util.ConfigFile;
import com.dmdirc.util.InvalidConfigFileException;
import com.dmdirc.logger.Logger;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.updater.Version;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.net.URL;
import java.util.TreeMap;

/**
 * This class is used to store meta information
 *
 * @author shane
 */
public class PluginInfo implements Comparable<PluginInfo>, ServiceProvider {

    /** A logger for this class. */
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(PluginInfo.class.getName());

    /** Plugin Meta Data */
    private volatile ConfigFile metaData = null;

    /** URL that this plugin was loaded from */
    private final URL url;

    /** Filename for this plugin (taken from URL) */
    private final String filename;

    /** The actual Plugin from this jar */
    private Plugin plugin = null;

    /** The classloader used for this Plugin */
    private PluginClassLoader classloader = null;

    /** The resource manager used by this pluginInfo */
    private ResourceManager myResourceManager = null;

    /** Is this plugin only loaded temporarily? */
    private boolean tempLoaded = false;

    /** List of classes this plugin has */
    private List<String> myClasses = new ArrayList<String>();

    /** Requirements error message. */
    private String requirementsError = "";

    /** Last Error Message. */
    private String lastError = "No Error";

    /** Are we trying to load? */
    private boolean isLoading = false;

    /** List of services we provide. */
    private final List<Service> provides = new ArrayList<Service>();

    /** List of children of this plugin. */
    private final List<PluginInfo> children = new ArrayList<PluginInfo>();

    /** Map of exports */
    private final Map<String, ExportInfo> exports = new HashMap<String, ExportInfo>();

    /** List of identities */
    private final List<Identity> identities = new ArrayList<Identity>();

    /**
     * Create a new PluginInfo.
     *
     * @param url URL to file that this plugin is stored in.
     * @throws PluginException if there is an error loading the Plugin
     * @since 0.6
     */
    public PluginInfo(final URL url) throws PluginException {
        this(url, true);
    }

    /**
     * Create a new PluginInfo.
     *
     * @param url URL to file that this plugin is stored in.
     * @param load Should this plugin be loaded, or is this just a placeholder? (true for load, false for placeholder)
     * @throws PluginException if there is an error loading the Plugin
     * @since 0.6
     */
    public PluginInfo(final URL url, final boolean load) throws PluginException {
        this.url = url;
        this.filename = new File(url.getPath()).getName();

        ResourceManager res;

        // Check for updates.
        if (new File(getFullFilename() + ".update").exists() && new File(getFullFilename()).delete()) {
            new File(getFullFilename() + ".update").renameTo(new File(getFullFilename()));
        }

        if (!load) {
            // Load the metaData if available.
            try {
                metaData = getConfigFile();
            } catch (IOException ioe) {
                metaData = null;
            }
            return;
        }

        try {
            res = getResourceManager();
        } catch (IOException ioe) {
            lastError = "Error with resourcemanager: " + ioe.getMessage();
            throw new PluginException("Plugin " + filename + " failed to load. " + lastError, ioe);
        }

        try {
            metaData = getConfigFile();
            if (metaData == null) {
                lastError = "plugin.config doesn't exist in jar";
                throw new PluginException("Plugin " + filename + " failed to load. " + lastError);
            }
        } catch (IOException e) {
            lastError = "plugin.config IOException: " + e.getMessage();
            throw new PluginException("Plugin " + filename + " failed to load, plugin.config failed to open - " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            lastError = "plugin.config IllegalArgumentException: " + e.getMessage();
            throw new PluginException("Plugin " + filename + " failed to load, plugin.config failed to open - " + e.getMessage(), e);
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

            if (isPersistent() && loadAll()) {
                loadEntirePlugin();
            }
        } else {
            lastError = "One or more requirements not met (" + requirementsError + ")";
            throw new PluginException("Plugin " + filename + " was not loaded. " + lastError);
        }

        updateProvides();
        getDefaults();
    }

    /**
     * Get a ConfigFile object for this plugin.
     * This will load a ConfigFile
     *
     * @return the ConfigFile object for this plugin, or null if the plugin has no config
     * @throws IOException if there is an error with the ResourceManager.
     */
    private ConfigFile getConfigFile() throws IOException {
        ConfigFile file = null;
        final ResourceManager res = getResourceManager();
        if (res.resourceExists("META-INF/plugin.config")) {
            try {
                file = new ConfigFile(res.getResourceInputStream("META-INF/plugin.config"));
                file.read();
            } catch (InvalidConfigFileException icfe) {
                throw new IOException("Unable to read plugin.config", icfe);
            }
        }

        return file;
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
        if (metaData == null) {
            return;
        }

        final Identity defaults = IdentityManager.getAddonIdentity();
        final String domain = "plugin-" + getName();

        LOGGER.finer(getName() + ": Using domain '" + domain + "'");

        if (metaData.isKeyDomain("defaults")) {
            final Map<String, String> keysection = metaData.getKeyDomain("defaults");

            for (Map.Entry<String, String> entry : keysection.entrySet()) {
                final String key = entry.getKey();
                final String value = entry.getValue();

                defaults.setOption(domain, key, value);
            }
        }

        if (metaData.isKeyDomain("formatters")) {
            final Map<String, String> keysection = metaData.getKeyDomain("formatters");

            for (Map.Entry<String, String> entry : keysection.entrySet()) {
                final String key = entry.getKey();
                final String value = entry.getValue();

                defaults.setOption("formatter", key, value);
            }
        }

        if (metaData.isKeyDomain("icons")) {
            final Map<String, String> keysection = metaData.getKeyDomain("icons");

            for (Map.Entry<String, String> entry : keysection.entrySet()) {
                final String key = entry.getKey();
                final String value = entry.getValue();

                defaults.setOption("icon", key, value);
            }
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

                try {
                    final Identity thisIdentity = new Identity(stream, false);
                    identities.add(thisIdentity);
                    IdentityManager.addIdentity(thisIdentity);
                } catch (final InvalidIdentityFileException ex) {
                    Logger.userError(ErrorLevel.MEDIUM, "Error with identity file '" + name + "' in plugin '" + getName() + "'", ex);
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
        for (Identity identity : identities) {
            IdentityManager.removeIdentity(identity);
        }

        identities.clear();
    }

    /**
     * Update provides list.
     */
    private void updateProvides() {
        // Remove us from any existing provides lists.
        for (Service service : provides) {
            service.delProvider(this);
        }
        provides.clear();

        // Get services provided by this plugin
        final List<String> providesList = metaData.getFlatDomain("provides");
        if (providesList != null) {
            for (String item : providesList) {
                final String[] bits = item.split(" ");
                final String name = bits[0];
                final String type = (bits.length > 1) ? bits[1] : "misc";

                if (!name.equalsIgnoreCase("any") && !type.equalsIgnoreCase("export")) {
                    final Service service = PluginManager.getPluginManager().getService(type, name, true);
                    service.addProvider(this);
                    provides.add(service);
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
        // Force a new resourcemanager just incase.
        try {
            getResourceManager(true);
            final ConfigFile newMetaData = getConfigFile();
            if (newMetaData != null) {
                metaData = newMetaData;
                return true;
            }
        } catch (IOException ioe) {
            Logger.userError(ErrorLevel.MEDIUM, "There was an error updating the metadata for "+getName(), ioe);
        }

        return false;
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
    public synchronized ResourceManager getResourceManager() throws IOException {
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
        int idesired;

        if (desired.isEmpty()) {
            return true;
        }

        try {
            idesired = Integer.parseInt(desired);
        } catch (NumberFormatException ex) {
            requirementsError = "'minversion' is a non-integer";
            return false;
        }

        if (actual > 0 && idesired > 0 && actual < idesired) {
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
        int idesired;

        if (desired.isEmpty()) {
            return true;
        }

        try {
            idesired = Integer.parseInt(desired);
        } catch (NumberFormatException ex) {
            requirementsError = "'maxversion' is a non-integer";
            return false;
        }

        if (actual > 0 && idesired > 0 && actual > idesired) {
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
        if (desired.isEmpty()) {
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
     * Checks to see if the UI requirements of the plugin are satisfied.
     * If the desired string is empty, the test passes.
     * Otherwise it is used as a regular expressions against the package of the
     * UIController to test what UI is currently in use.
     * On failure, the requirementsError field will contain a user-friendly
     * error message.
     *
     * @param desired The desired UI requirements
     * @param actual The package of the current UI in use.
     * @return True if the test passes, false otherwise
     */
    protected boolean checkUI(final String desired, final String actual) {
        if (desired.isEmpty()) {
            return true;
        }

        if (!actual.toLowerCase().matches(desired)) {
            requirementsError = "Invalid UI. (Wanted: '" + desired + "', actual: '" + actual + "')";
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
        if (desired.isEmpty()) {
            return true;
        }

        for (String files : desired.split(",")) {
            final String[] filelist = files.split("\\|");
            boolean foundFile = false;
            for (String file : filelist) {
                if ((new File(file)).exists()) {
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
        if (desired.isEmpty()) {
            return true;
        }

        for (String pluginName : desired.split(",")) {
            final String[] data = pluginName.split(":");
            final PluginInfo pi = PluginManager.getPluginManager().getPluginInfoByName(data[0]);
            if (pi == null) {
                requirementsError = "Required plugin '" + data[0] + "' was not found";
                return false;
            } else {
                if (data.length > 1) {
                    // Check plugin minimum version matches.
                    if (pi.getVersion().compareTo(new Version(data[1])) < 0) {
                        requirementsError = "Plugin '" + data[0] + "' is too old (Required Version: " + data[1] + ", Actual Version: " + pi.getVersion() + ")";
                        return false;
                    } else {
                        if (data.length > 2) {
                            // Check plugin maximum version matches.
                            if (pi.getVersion().compareTo(new Version(data[2])) > 0) {
                                requirementsError = "Plugin '" + data[0] + "' is too new (Required Version: " + data[2] + ", Actual Version: " + pi.getVersion() + ")";
                                return false;
                            }
                        }
                    }
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
        if (metaData == null) {
            // No meta-data, so no requirements.
            return true;
        }

        if (!checkOS(getKeyValue("requires", "os", ""), System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch")) ||
                !checkFiles(getKeyValue("requires", "files", "")) ||
                (!preliminary && !checkPlugins(getKeyValue("requires", "plugins", ""))) ||
                (!preliminary && !checkServices(metaData.getFlatDomain("required-services")))) {
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
    private boolean checkServices(final List<String> services) {
        if (services == null || services.size() < 1) {
            return true;
        }

        for (String requirement : services) {
            boolean available = false;
            final String[] bits = requirement.split(" ");
            final String name = bits[0];
            final String type = (bits.length > 1) ? bits[1] : "misc";

            // System.out.println(toString()+" Looking for: "+requirement);
            Service service = null;
            if (name.equalsIgnoreCase("any")) {
                final List<Service> serviceList = PluginManager.getPluginManager().getServicesByType(type);
                if (serviceList.size() > 0) {
                    // Default to the first Service in the list
                    service = serviceList.get(0);
                    if (serviceList.size() > 1) {
                        // Check to see if any of the others are already active
                        for (Service serv : serviceList) {
                            if (service.isActive()) {
                                // Already active, abort.
                                available = true;
                            }
                        }
                    }
                }
            } else {
                service = PluginManager.getPluginManager().getService(type, name, false);
            }
            // System.out.println("\tSatisfied by: "+service+" "+(PluginInfo)service.getActiveProvider());
            if (service != null) {
                available = service.activate();
            }

            if (!available) {
                return false;
            }
        }

        return true;
    }

    /**
     * Is this provider active at this time.
     *
     * @return true if the provider is able to provide its services
     */
    @Override
    public final boolean isActive() {
        return isLoaded();
    }

    /** Activate the services. */
    @Override
    public void activateServices() {
        loadPlugin();
    }

    /** {@inheritDoc} */
    @Override
    public String getProviderName() {
        return "Plugin: " + getNiceName() + " (" + getName() + " / " + getFilename() + ")";
    }

    /**
     * Get a list of services provided by this provider.
     *
     * @return A list of services provided by this provider.
     */
    @Override
    public List<Service> getServices() {
        return new ArrayList<Service>(provides);
    }

    /**
     * Is this plugin loaded?
     *
     * @return True if the plugin is currently (non-temporarily) loaded, false
     * otherwise
     */
    public boolean isLoaded() {
        return (plugin != null) && !tempLoaded;
    }

    /**
     * Is this plugin temporarily loaded?
     *
     * @return True if this plugin is currently temporarily loaded, false
     * otherwise
     */
    public boolean isTempLoaded() {
        return (plugin != null) && tempLoaded;
    }

    /**
     * Load entire plugin.
     * This loads all files in the jar immediately.
     *
     * @throws PluginException if there is an error with the resourcemanager
     */
    private void loadEntirePlugin() throws PluginException {
        // Load the main "Plugin" from the jar
        loadPlugin();

        // Now load all the rest.
        for (String classname : myClasses) {
            loadClass(classname);
        }
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
        final String required = getKeyValue("requires", "plugins", "");
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
        updateProvides();
        if (!checkRequirements(isTempLoaded() || tempLoaded)) {
            lastError = "Unable to loadPlugin, all requirements not met. (" + requirementsError + ")";
            return;
        }
        loadIdentities();
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
            if (isLoaded() || metaData == null || isLoading) {
                lastError = "Not Loading: (" + isLoaded() + "||" + (metaData == null) + "||" + isLoading + ")";
                unloadIdentities();
                return;
            }
            isLoading = true;
            loadRequired();
            loadClass(getMainClass());
            if (isLoaded()) {
                ActionManager.processEvent(CoreActionType.PLUGIN_LOADED, null, this);
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
     * Load the given classname.
     *
     * @param classname Class to load
     */
    private void loadClass(final String classname) {
        try {
            if (classloader == null) {
                if (getKeyValue("requires", "parent", "").isEmpty()) {
                    classloader = new PluginClassLoader(this);
                } else {
                    final String parentName = getKeyValue("requires", "parent", "");
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
            final Constructor<?> constructor = c.getConstructor(new Class[]{});

            // Only try and construct the main class, anything else should be constructed
            // by the plugin itself.
            if (classname.equals(getMainClass())) {
                final Object temp = constructor.newInstance(new Object[]{});

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
        if (isPersistent() || (!isLoaded() && !isTempLoaded())) {
            return false;
        } else {
            final String unloadable = getKeyValue("metadata", "unloadable", "true");
            return (unloadable.equalsIgnoreCase("yes") || unloadable.equalsIgnoreCase("true") || unloadable.equalsIgnoreCase("1"));
        }
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
                if (!parentUnloading && !getKeyValue("requires", "parent", "").isEmpty()) {
                    final String parentName = getKeyValue("requires", "parent", "");
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
                }
                ActionManager.processEvent(CoreActionType.PLUGIN_UNLOADED, null, this);
                for (Service service : provides) {
                    service.delProvider(this);
                }
                provides.clear();
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
     * Get the value of the given key from the given keysection, or fallback.
     *
     * @param section Section to look in
     * @param key Key to check
     * @param fallback Value to use if key doesn't exist.
     * @return Value of the key in the keysection, or the fallback if not present
     */
    public String getKeyValue(final String section, final String key, final String fallback) {
        if (metaData != null && metaData.isKeyDomain(section)) {
            final Map<String, String> keysection = metaData.getKeyDomain(section);
            return keysection.containsKey(key) ? keysection.get(key) : fallback;
        }

        return fallback;
    }

    /**
     * Get the main Class
     *
     * @return Main Class to begin loading.
     */
    public String getMainClass() {
        return getKeyValue("metadata", "mainclass", "");
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
     */
    public String getFriendlyVersion() {
        return getKeyValue("version", "friendly", String.valueOf(getVersion()));
    }

    /**
     * Get the plugin version
     *
     * @return Plugin Version
     */
    public Version getVersion() {
        return new Version(getKeyValue("version", "number", "0"));
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
     */
    public int getAddonID() {
        try {
            return Integer.parseInt(getKeyValue("updates", "id", "-1"));
        } catch (NumberFormatException nfe) {
            return -2;
        }
    }

    /**
     * Is this a persistent plugin?
     *
     * @return true if persistent, else false
     */
    public boolean isPersistent() {
        if (metaData != null && metaData.isFlatDomain("persistent")) {
            final List<String> items = metaData.getFlatDomain("persistent");
            return items.contains("*");
        }

        return false;
    }

    /**
     * Does this plugin contain any persistent classes?
     *
     * @return true if this plugin contains any persistent classes, else false
     */
    public boolean hasPersistent() {
        if (metaData != null && metaData.isFlatDomain("persistent")) {
            final List<String> items = metaData.getFlatDomain("persistent");
            return !items.isEmpty();
        }

        return false;
    }

    /**
     * Get a list of all persistent classes in this plugin
     *
     * @return List of all persistent classes in this plugin
     */
    public List<String> getPersistentClasses() {
        final List<String> result = new ArrayList<String>();

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
        } else if (metaData != null && metaData.isFlatDomain("persistent")) {
            return metaData.getFlatDomain("persistent");
        }

        return result;
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
        } else if (metaData != null && metaData.isFlatDomain("persistent")) {
            final List<String> items = metaData.getFlatDomain("persistent");
            return items.contains(classname);
        } else {
            return false;
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
     */
    public String getAuthor() {
        return getKeyValue("metadata", "author", "");
    }

    /**
     * Get the plugin Description.
     *
     * @return Description of plugin
     */
    public String getDescription() {
        return getKeyValue("metadata", "description", "");
    }

    /**
     * Get the minimum dmdirc version required to run the plugin.
     *
     * @return minimum dmdirc version required to run the plugin.
     */
    public String getMinVersion() {
        final String requiredVersion = getKeyValue("requires", "dmdirc", "");
        if (!requiredVersion.isEmpty()) {
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
        final String requiredVersion = getKeyValue("requires", "dmdirc", "");
        if (!requiredVersion.isEmpty()) {
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
     */
    public String getName() {
        return getKeyValue("metadata", "name", "");
    }

    /**
     * Get the nice name of the plugin. (Displayed to users)
     *
     * @return Nice Name of plugin
     */
    public String getNiceName() {
        return getKeyValue("metadata", "nicename", getName());
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

    /**
     * Does this plugin want all its classes loaded?
     *
     * @return true/false if loadall=true || loadall=yes
     */
    public boolean loadAll() {
        final String loadAll = getKeyValue("metadata", "loadall", "no");
        return loadAll.equalsIgnoreCase("true") || loadAll.equalsIgnoreCase("yes");
    }

    /**
     * Get misc meta-information.
     *
     * @param metainfo The metainfo to return
     * @deprecated Use {@link #getKeyValue(String, String, String) instead
     * @return Misc Meta Info (or "" if not found);
     */
    @Deprecated
    public String getMetaInfo(final String metainfo) {
        return getMetaInfo(metainfo, "");
    }

    /**
     * Get misc meta-information.
     *
     * @param metainfo The metainfo to return
     * @param fallback Fallback value if requested value is not found
     * @deprecated Use {@link #getKeyValue(String, String, String) instead
     * @return Misc Meta Info (or fallback if not found);
     */
    @Deprecated
    public String getMetaInfo(final String metainfo, final String fallback) {
        return getKeyValue("misc", metainfo, fallback);
    }

    /**
     * Get misc meta-information.
     *
     * @param metainfo The metainfos to look for in order. If the first item in
     *                 the array is not found, the next will be looked for, and
     *                 so on until either one is found, or none are found.
     * @deprecated Use {@link #getKeyValue(String, String, String) instead
     * @return Misc Meta Info (or "" if none are found);
     */
    @Deprecated
    public String getMetaInfo(final String[] metainfo) {
        return getMetaInfo(metainfo, "");
    }

    /**
     * Get misc meta-information.
     *
     * @param metainfo The metainfos to look for in order. If the first item in
     *                 the array is not found, the next will be looked for, and
     *                 so on until either one is found, or none are found.
     * @param fallback Fallback value if requested values are not found
     * @deprecated Use {@link #getKeyValue(String, String, String) instead
     * @return Misc Meta Info (or "" if none are found);
     */
    @Deprecated
    public String getMetaInfo(final String[] metainfo, final String fallback) {
        for (String meta : metainfo) {
            final String result = getKeyValue("misc", meta, null);
            if (result != null) {
                return result;
            }
        }
        return fallback;
    }

    /**
     * Compares this object with the specified object for order.
     * Returns a negative integer, zero, or a positive integer as per String.compareTo();
     *
     * @param o Object to compare to
     * @return a negative integer, zero, or a positive integer.
     */
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
        final List<String> exportsList = metaData.getFlatDomain("exports");
        if (exportsList != null) {
            for (String item : exportsList) {
                final String[] bits = item.split(" ");
                if (bits.length > 2) {
                    final String methodName = bits[0];
                    final String methodClass = bits[2];
                    final String serviceName = (bits.length > 4) ? bits[4] : bits[0];

                    // Add a provides for this
                    final Service service = PluginManager.getPluginManager().getService("export", serviceName, true);
                    service.addProvider(this);
                    provides.add(service);
                    // Add is as an export
                    exports.put(serviceName, new ExportInfo(methodName, methodClass, this));
                }
            }
        }
    }

    /**
     * Get an ExportedService object from this provider.
     *
     * @param name Service name
     * @return ExportedService object. If no such service exists, the execute
     *         method of this ExportedService will always return null.
     */
    @Override
    public ExportedService getExportedService(final String name) {
        if (exports.containsKey(name)) {
            return exports.get(name).getExportedService();
        } else {
            return new ExportedService(null, null);
        }
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
