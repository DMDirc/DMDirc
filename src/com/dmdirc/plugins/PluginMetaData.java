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

import com.dmdirc.updater.Version;
import com.dmdirc.util.io.ConfigFile;
import com.dmdirc.util.io.InvalidConfigFileException;
import com.dmdirc.util.io.StreamUtils;
import com.dmdirc.util.resourcemanager.ResourceManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads metadata for a plugin. Plugin metadata is defined in a DMDirc
 * {@link ConfigFile} which is typically found inside plugin jars as
 * <code>META-INF/plugin.config</code>. The following sections are read from
 * the config file:
 * <ul>
 *  <li><code>metadata</code> - generic plugin information. See {@link #readMetaData(java.util.Map)}.
 *  <li><code>version</code> - versioning information. See {@link #readVersion(java.util.Map)}.
 *  <li><code>defaults</code> - optional default settings to add to the configuration
 *  <li><code>formatters</code> - optional default formatters to add to the configuration
 *  <li><code>icons</code> - optional default icons to add to the configuration
 *  <li><code>requires</code> - optional generic requirements. See {@link #readRequirements(java.util.Map, java.util.List)}.
 *  <li><code>required-services</code> - optional service requirements. See {@link #readRequirements(java.util.Map, java.util.List)}.
 *  <li><code>updates</code> - optional information for automated updates. See {@link #readUpdates(java.util.Map)}.
 *  <li><code>provides</code> - optional list of services provided by the plugin. See {@link #readProvides(java.util.List)}.
 *  <li><code>exports</code> - optional list of methods exported by the plugin. See {@link #readExports(java.util.List)}.
 *  <li><code>persistent</code> - optional list of persistent classes within the plugin. See {@link #readPersistent(java.util.List)}.
 * </ul>
 * A collection of errors that occurred when attempting to read the metadata
 * is available via the {@link #getErrors()} method.
 *
 * @since 0.6.6
 */
public class PluginMetaData {

    /** A collection of errors that occurred when reading or validating the data. */
    private final Collection<String> errors = new ArrayList<>();

    /** Default settings defined in the plugin metadata. */
    private final Map<String, String> defaultSettings = new HashMap<>();

    /** Formatters defined in the plugin metadata. */
    private final Map<String, String> formatters = new HashMap<>();

    /** Icons defined in the plugin metadata. */
    private final Map<String, String> icons = new HashMap<>();

    /** A set of requirements made by the plugin. */
    private final Map<String, String> requirements = new HashMap<>();

    /** A list of required services. */
    private final Collection<String> requiredServices = new ArrayList<>();

    /** Services provided by this plugin. */
    private final Collection<String> services = new ArrayList<>();

    /** Methods exported by this plugin. */
    private final Collection<String> exports = new ArrayList<>();

    /** Persistent classes in this plugin. */
    private final Collection<String> persistentClasses = new ArrayList<>();

    /** The name of the parent plugin, if any. */
    private String parent;

    /** The name of the main class, if any. */
    private String mainClass;

    /** The friendly version name/number. */
    private String friendlyVersion;

    /** The version of the plugin. */
    private Version version;

    /** The ID to use with the updater system, if any. */
    private int updaterId;

    /** The author-supplied friendly name of the plugin. */
    private String friendlyName;

    /** The internal name of the plugin. */
    private String name;

    /** The name of the author of the plugin. */
    private String author;

    /** The author-supplied description of the plugin. */
    private String description;

    /** Whether or not the plugin is marked as unloadable. */
    private boolean unloadable;

    /** The URL to the plugin. */
    private final URL pluginUrl;

    /** The URL to load the metadata from. */
    private final URL url;

    /** The parent plugin manager. */
    private PluginManager manager;

    /**
     * Creates a new meta data reader for a config file at the specified URL.
     *
     * @param manager Plugin manager
     * @param url The URL to load the config file from
     * @param pluginUrl The URL to the plugin that this data corresponds to
     */
    public PluginMetaData(final PluginManager manager, final URL url, final URL pluginUrl) {
        this.manager = manager;
        this.pluginUrl = pluginUrl;
        this.url = url;
    }

    /**
     * Loads plugin metadata from a config file.
     */
    public void load() {
        errors.clear();
        InputStream stream = null;

        try {
            stream = getStream();
            final ConfigFile configFile = new ConfigFile(stream);
            configFile.read();
            readMetaData(configFile.getKeyDomain("metadata"));
            readVersion(configFile.getKeyDomain("version"));
            readUpdates(configFile.getKeyDomain("updates"));
            readSettings(defaultSettings, configFile.getKeyDomain("defaults"));
            readSettings(formatters, configFile.getKeyDomain("formatters"));
            readSettings(icons, configFile.getKeyDomain("icons"));
            readRequirements(configFile.getKeyDomain("requires"),
                    configFile.getFlatDomain("required-services"));
            readProvides(configFile.getFlatDomain("provides"));
            readPersistent(configFile.getFlatDomain("persistent"));
            readExports(configFile.getFlatDomain("exports"));
        } catch (IOException | InvalidConfigFileException ex) {
            errors.add("Unable to read config file: " + ex.getMessage());
            StreamUtils.close(stream);
        }
    }

    /**
     * Get the InputStream for this PluginMetaData
     *
     * @return The InputStream for this PluginMetaData
     */
    private InputStream getStream() throws IOException {
        // Sometimes url.openStream sucks, and this breaks reloading, so try
        // not to use it if we have a jar file.

        if (url.toString().startsWith("jar:file:")) {
            final String[] bits = url.toString().replaceFirst("^jar:file:", "jar://").split("!", 2);

            if (bits.length > 1) {
                final String file = bits[1].replaceFirst("^/", "");
                final ResourceManager rm = ResourceManager.getResourceManager(bits[0]);
                final InputStream s = rm.getResourceInputStream(file);
                if (s == null) {
                    throw new IOException("Unable to find " + file + " in " + bits[0]);
                }
                return s;
            }
        }

        return url.openStream();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final PluginMetaData other = (PluginMetaData) obj;
        return pluginUrl == other.getPluginUrl() || (pluginUrl != null
                && pluginUrl.equals(other.getPluginUrl()));
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return pluginUrl == null ? 0 : pluginUrl.hashCode();
    }

    // <editor-fold desc="Read methods">

    /**
     * Reads information from the metadata section of the config file.
     * The following entries are read from the metadata section:
     * <ul>
     *  <li><code>mainclass</code> - full classname of the main class to load
     *  <li><code>author</code> - name of the author
     *  <li><code>description</code> - user-friendly description
     *  <li><code>name</code> - internal short name
     *  <li><code>nicename</code> - user-friendly name
     *  <li><code>unloadable</code> - boolean indicating if the plugin can
     *  be unloaded. Defaults to true if not specified.
     * </ul>
     * It is recommended that the <code>author</code> field should take the
     * form of "<code>name &lt;email@address&gt;</code>", although this is not
     * enforced.
     * <p>
     * The short name must be a single word (i.e., no spaces) that uniquely
     * identifies the plugin. This is typically the same as the plugin's jar
     * name.
     *
     * @param data A map of config entry names to values
     */
    protected void readMetaData(final Map<String, String> data) {
        if (data == null || data.isEmpty()) {
            errors.add("'metadata' section not specified or empty");
            return;
        }

        mainClass = data.get("mainclass");
        author = data.get("author");
        description = data.get("description");
        name = data.get("name");
        friendlyName = data.get("nicename");
        unloadable = !data.containsKey("unloadable")
                || !data.get("unloadable").matches("(?i)^true|1|yes$");
    }

    /**
     * Reads information from the version section of the config file.
     * The following entries are read from the version section:
     * <ul>
     *  <li><code>friendly</code> - a user-friendly version string
     *  <li><code>number</code> - a DMDirc {@link Version} 'number'
     * </ul>
     * If the 'number' field is not specified it will be defaulted to a value
     * of "0".
     *
     * @param data A map of config entry names to values
     */
    protected void readVersion(final Map<String, String> data) {
        if (data == null || data.isEmpty()) {
            errors.add("'version' section not specified or empty");
            return;
        }

        friendlyVersion = data.get("friendly");
        version = new Version(data.containsKey("number") ? data.get("number") : "0");
    }

    /**
     * Reads information from the updates section of the config file.
     * The following entries are read from the updates section:
     * <ul>
     *  <li><code>id</code> - a numeric ID for the plugin's entry in the DMDirc
     *  updater system
     * </ul>
     *
     * @param data A map of config entry names to values
     */
    protected void readUpdates(final Map<String, String> data) {
        updaterId = -1;

        if (data != null && data.containsKey("id")) {
            try {
                updaterId = Integer.parseInt(data.get("id"));
            } catch (NumberFormatException ex) {
                errors.add("Invalid updater id specified: " + data.get("id"));
            }
        }
    }

    /**
     * Copies the specified settings (defaults, formatters, icons, etc) into
     * the specified property.
     *
     * @param target The map to add settings to
     * @param data The settings specified in the metadata
     */
    protected void readSettings(final Map<String, String> target,
            final Map<String, String> data) {
        target.clear();

        if (data != null) {
            target.putAll(data);
        }
    }

    /**
     * Reads the requirements and required-services sections of the config file.
     * The requires section is a mapping of a requirement type to a constraint.
     * No validation is performed on this section, but the following types
     * are standard:
     * <ul>
     *  <li><code>parent</code> - internal name of a single parent plugin,
     *  e.g. 'ui_swing'.
     *  <li><code>plugins</code> - a comma-separated list of plugins that are
     *  required. Plugins are specified by their internal names, and each entry
     *  may include an optional minimum and maximum version separated by colons,
     *  e.g. 'ui_swing' or 'plugin1,plugin2:0.6.3,plugin3:0.1:0.2'.
     *  <li><code>os</code> - one to three colon-separated regular expressions
     *  to match the OS name, version and architecture, e.g. '.*win.*'.
     *  <li><code>files</code> - a comma-separated list of files that must
     *  exist. Each comma-separated value may contain multiple alternatives
     *  separated by a pipe, e.g. '/bin/bash' or '/bin/bash|/bin/dash,/bin/foo'.
     *  <li><code>dmdirc</code> - the minimum and maximum DMDirc versions,
     *  separated by a '-', e.g. '0.6.3', '0.6.3-0.6.4', '-0.6.4'.
     * </ul>
     * The required-services section is a flat domain which lists services that
     * the plugin requires. No validation is performed on these values, but
     * each service should consist of a name and a type. The magic string
     * <code>any</code> may be specified in place of a name (e.g. 'any ui'
     * instead of 'swing ui').
     *
     * @param data The specified requirements
     * @param requiredServices The specified required services
     */
    protected void readRequirements(final Map<String, String> data,
            final List<String> requiredServices) {
        readSettings(requirements, data);

        if (requirements.containsKey("parent")) {
            parent = requirements.get("parent");
        } else {
            parent = null;
        }

        this.requiredServices.clear();

        if (requiredServices != null) {
            this.requiredServices.addAll(requiredServices);
        }
    }

    /**
     * Reads the provides section of the config file. This is a flat domain
     * containing a list of services provided by the plugin. Services are
     * specified as a space-separated name and type pair, e.g. 'swing ui' or
     * 'logging command'.
     *
     * @param services The services to be added
     */
    protected void readProvides(final List<String> services) {
        this.services.clear();

        if (services != null) {
            this.services.addAll(services);
        }
    }

    /**
     * Reads the persistent section of the config file. This is a flat domain
     * containing a list of classes which should be made persistent (i.e.,
     * loaded globally and not unloaded).
     *
     * @param classes The services to be added
     */
    protected void readPersistent(final List<String> classes) {
        this.persistentClasses.clear();

        if (classes != null) {
            this.persistentClasses.addAll(classes);
        }
    }

    /**
     * Reads the exports section of the config file. This is a flat domain
     * containing a list of exported methods in the format
     * <code>&lt;methodName&gt; in &lt;className&gt; [as &lt;methodAlias&gt;]</code>,
     * e.g. 'getFoo in my.class.name as getParser' or 'getParser in my.class.name'.
     *
     * @param exports The exported methods for this plugin
     */
    protected void readExports(final List<String> exports) {
        this.exports.clear();

        if (exports != null) {
            this.exports.addAll(exports);
        }
    }

    // </editor-fold>

    /**
     * Calculates the relative path of this plugin in relation to the main
     * plugin directory.
     *
     * @return The plugin's relative path, or absolute path if not within the
     * plugins directory
     */
    public String getRelativeFilename() {
        // Yuck...
        final String filename = getPluginUrl().getPath();
        final String dir = new File(manager.getDirectory())
                .getAbsolutePath() + File.separator;
        final String file = new File(filename).getAbsolutePath();

        return file.startsWith(dir) ? filename.substring(dir.length()) : filename;
    }

    // <editor-fold defaultstate="collapsed" desc="Getters">

    /**
     * What plugin manager owns this metadata?
     *
     * @return The pluginmanager that created this metadata.
     */
    public PluginManager getManager() {
        return manager;
    }

    /**
     * Retrieves the URL to the plugin that this metadata corresponds to.
     *
     * @return The plugin's URL
     */
    public URL getPluginUrl() {
        return pluginUrl;
    }

    /**
     * Retrieves a collection of errors that occurred while trying to read
     * the metadata.
     *
     * @return A (possibly empty) collection of errors that occurred.
     */
    public Collection<String> getErrors() {
        return Collections.unmodifiableCollection(errors);
    }

    /**
     * Determines whether or not there were errors encountered while trying to
     * read the metadata.
     *
     * @return True if there are errors, false otherwise
     * @see #getErrors()
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Retrieves the author information for the plugin.
     *
     * @return The plugin's specified author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Retrieves a user-friendly description of the plugin.
     *
     * @return The plugin's user-friendly description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Retrieves a user-friendly name of the plugin.
     *
     * @return This plugin's user-friendly name
     */
    public String getFriendlyName() {
        return friendlyName;
    }

    /**
     * Retrieves the specified main class of the plugin.
     *
     * @return This plugin's main class
     */
    public String getMainClass() {
        return mainClass;
    }

    /**
     * Retrieves the internal short name of this plugin.
     *
     * @return This plugin's internal short name
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieves the user-friendly version of this plugin.
     *
     * @return This plugin's user-friendly version
     */
    public String getFriendlyVersion() {
        return friendlyVersion;
    }

    /**
     * Determines whether the plugin is specified as unloadable or not.
     *
     * @return True if the plugin is unloadable, false otherwise
     */
    public boolean isUnloadable() {
        return unloadable;
    }

    /**
     * Retrieves the version of this plugin
     *
     * @return This plugin's version
     */
    public Version getVersion() {
        return version;
    }

    /**
     * Retrieves the updater ID of this plugin.
     *
     * @return This plugin's updater ID, or -1 if not specified
     */
    public int getUpdaterId() {
        return updaterId;
    }

    /**
     * Returns a map of default settings supplied by this plugin.
     *
     * @return A map of setting keys to values
     */
    public Map<String, String> getDefaultSettings() {
        return Collections.unmodifiableMap(defaultSettings);
    }

    /**
     * Returns a map of default formatters supplied by this plugin.
     *
     * @return A map of formatter names to values
     */
    public Map<String, String> getFormatters() {
        return Collections.unmodifiableMap(formatters);
    }

    /**
     * Returns a map of default icons supplied by this plugin.
     *
     * @return A map of icon names to values
     */
    public Map<String, String> getIcons() {
        return Collections.unmodifiableMap(icons);
    }

    /**
     * Retrieves this plugin's parent, if specified.
     *
     * @return The desired parent plugin
     */
    public String getParent() {
        return parent;
    }

    /**
     * Retrieves the collection of required services.
     *
     * @see #readRequirements(java.util.Map, java.util.List)
     * @return A collection of plugin-defined service requirements
     */
    public Collection<String> getRequiredServices() {
        return Collections.unmodifiableCollection(requiredServices);
    }

    /**
     * Retrieves the collection of plugin requirements.
     *
     * @see #readRequirements(java.util.Map, java.util.List)
     * @return A collection of plugin-defined requirements
     */
    public Map<String, String> getRequirements() {
        return Collections.unmodifiableMap(requirements);
    }

    /**
     * Retrieves the collection of services exported by the plugin.
     *
     * @return The services which the plugin specifies it provides
     */
    public Collection<String> getServices() {
        return Collections.unmodifiableCollection(services);
    }

    /**
     * Retrieves the collection of methods exported by the plugin.
     *
     * @return The methods exported by the plugin
     */
    public Collection<String> getExports() {
        return Collections.unmodifiableCollection(exports);
    }

    /**
     * Retrieves the set of classes marked as persistent for this plugin.
     *
     * @return The classes of this plugin marked as persistent
     */
    public Collection<String> getPersistentClasses() {
        return Collections.unmodifiableCollection(persistentClasses);
    }

    // </editor-fold>

}
