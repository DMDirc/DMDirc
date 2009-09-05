/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.config;

import com.dmdirc.Main;
import com.dmdirc.interfaces.ConfigChangeListener;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.util.ConfigFile;
import com.dmdirc.util.InvalidConfigFileException;
import com.dmdirc.util.WeakList;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * An identity is a group of settings that are applied to a connection, server,
 * network or channel. Identities may be automatically applied in certain
 * cases, or the user may manually apply them.
 * <p>
 * Note: this class has a natural ordering that is inconsistent with equals.
 *
 * @author chris
 */
public class Identity extends ConfigSource implements Serializable,
        Comparable<Identity> {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;

    /** The domain used for identity settings. */
    private static final String DOMAIN = "identity".intern();

    /** The domain used for profile settings. */
    private static final String PROFILE_DOMAIN = "profile".intern();

    /** A regular expression that will match all characters illegal in file names. */
    protected static final String ILLEGAL_CHARS = "[\\\\\"/:\\*\\?\"<>\\|]";

    /** A logger for this class. */
    private static final java.util.logging.Logger LOGGER = java.util.logging
            .Logger.getLogger(Identity.class.getName());

    /** The target for this identity. */
    protected final ConfigTarget myTarget;

    /** The configuration details for this identity. */
    protected final ConfigFile file;

    /** The global config manager. */
    protected ConfigManager globalConfig;

    /** The config change listeners for this source. */
    protected final List<ConfigChangeListener> listeners
            = new WeakList<ConfigChangeListener>();

    /** Whether this identity needs to be saved. */
    protected boolean needSave;

    /**
     * Creates a new instance of Identity.
     *
     * @param file The file to load this identity from
     * @param forceDefault Whether to force this identity to be loaded as default
     * identity or not
     * @throws InvalidIdentityFileException Missing required properties
     * @throws IOException Input/output exception
     */
    public Identity(final File file, final boolean forceDefault) throws IOException,
            InvalidIdentityFileException {
        super();

        this.file = new ConfigFile(file);
        this.file.setAutomake(true);
        initFile(forceDefault, new FileInputStream(file));
        myTarget = getTarget(forceDefault);

        if (myTarget.getType() == ConfigTarget.TYPE.PROFILE) {
            migrateProfile();
        }
    }

    /**
     * Creates a new read-only identity.
     *
     * @param stream The input stream to read the identity from
     * @param forceDefault Whether to force this identity to be loaded as default
     * identity or not
     * @throws InvalidIdentityFileException Missing required properties
     * @throws IOException Input/output exception
     */
    public Identity(final InputStream stream, final boolean forceDefault) throws IOException,
            InvalidIdentityFileException {
        super();

        this.file = new ConfigFile(stream);
        this.file.setAutomake(true);
        initFile(forceDefault, stream);
        myTarget = getTarget(forceDefault);

        if (myTarget.getType() == ConfigTarget.TYPE.PROFILE) {
            migrateProfile();
        }
    }

    /**
     * Creates a new identity from the specified config file.
     *
     * @param configFile The config file to use
     * @param target The target of this identity
     */
    public Identity(final ConfigFile configFile, final ConfigTarget target) {
        super();

        this.file = configFile;
        this.file.setAutomake(true);
        this.myTarget = target;

        if (myTarget.getType() == ConfigTarget.TYPE.PROFILE) {
            migrateProfile();
        }
    }

    /**
     * Determines and returns the target for this identity from its contents.
     *
     * @param forceDefault Whether to force this to be a default identity
     * @return A ConfigTarget for this identity
     * @throws InvalidIdentityFileException If the identity isn't valid
     */
    private ConfigTarget getTarget(final boolean forceDefault)
            throws InvalidIdentityFileException {
        final ConfigTarget target = new ConfigTarget();

        if (hasOption(DOMAIN, "ircd")) {
            target.setIrcd(getOption(DOMAIN, "ircd"));
        } else if (hasOption(DOMAIN, "network")) {
            target.setNetwork(getOption(DOMAIN, "network"));
        } else if (hasOption(DOMAIN, "server")) {
            target.setServer(getOption(DOMAIN, "server"));
        } else if (hasOption(DOMAIN, "channel")) {
            target.setChannel(getOption(DOMAIN, "channel"));
        } else if (hasOption(DOMAIN, "globaldefault")) {
            target.setGlobalDefault();
        } else if (hasOption(DOMAIN, "global") || (forceDefault && !isProfile())) {
            target.setGlobal();
        } else if (isProfile()) {
            target.setProfile();
        } else {
            throw new InvalidIdentityFileException("No target and no profile");
        }

        if (hasOption(DOMAIN, "order")) {
            target.setOrder(getOptionInt(DOMAIN, "order"));
        }

        return target;
    }

    /**
     * Initialises this identity from a file.
     *
     * @param forceDefault Whether to force this to be a default identity
     * @param stream The stream to load properties from if needed (or null)
     * @param file The file to load this identity from (or null)
     * @throws InvalidIdentityFileException if the identity file is invalid
     * @throws IOException On I/O exception when reading the identity
     */
    private void initFile(final boolean forceDefault, final InputStream stream)
            throws InvalidIdentityFileException, IOException {
        try {
            this.file.read();
        } catch (InvalidConfigFileException ex) {
            throw new InvalidIdentityFileException(ex);
        }

        if (!hasOption(DOMAIN, "name") && !forceDefault) {
            throw new InvalidIdentityFileException("No name specified");
        }
    }

    /**
     * Attempts to reload this identity from disk. If this identity has been
     * modified (i.e., {@code needSave} is true), then this method silently
     * returns straight away. All relevant ConfigChangeListeners are fired for
     * new, altered and deleted properties. The target of the identity will not
     * be changed by this method, even if it has changed on disk.
     *
     * @throws java.io.IOException On I/O exception when reading the identity
     * @throws InvalidConfigFileException if the config file is no longer valid
     */
    public void reload() throws IOException, InvalidConfigFileException {
        if (needSave) {
            return;
        }

        final List<String[]> changes = new LinkedList<String[]>();

        synchronized (this) {
            final Map<String, Map<String, String>> oldProps = file.getKeyDomains();

            file.read();

            for (Map.Entry<String, Map<String, String>> entry : file.getKeyDomains().entrySet()) {
                final String domain = entry.getKey();

                for (Map.Entry<String, String> subentry : entry.getValue().entrySet()) {
                    final String key = subentry.getKey();
                    final String value = subentry.getValue();

                    if (!oldProps.containsKey(domain)) {
                        changes.add(new String[]{domain, key});
                    } else if (!oldProps.get(domain).containsKey(key)
                            || !oldProps.get(domain).get(key).equals(value)) {
                        changes.add(new String[]{domain, key});
                        oldProps.get(domain).remove(key);
                    }
                }

                if (oldProps.containsKey(domain)) {
                    for (String key : oldProps.get(domain).keySet()) {
                        changes.add(new String[]{domain, key});
                    }
                }
            }
        }

        for (String[] change : changes) {
            fireSettingChange(change[0], change[1]);
        }
    }

    /**
     * Fires the config changed listener for the specified option after this
     * identity is reloaded.
     *
     * @param domain The domain of the option that's changed
     * @param key The key of the option that's changed
     * @since 0.6.3m1
     */
    private void fireSettingChange(final String domain, final String key) {
        for (ConfigChangeListener listener : new ArrayList<ConfigChangeListener>(listeners)) {
            listener.configChanged(domain, key);
        }
    }

    /**
     * Returns the name of this identity.
     *
     * @return The name of this identity
     */
    public String getName() {
        if (hasOption(DOMAIN, "name")) {
            return getOption(DOMAIN, "name");
        } else {
            return "Unnamed";
        }
    }

    /**
     * Checks if this profile needs migrating from the old method of
     * storing nicknames (profile.nickname + profile.altnicks) to the new
     * method (profile.nicknames), and performs the migration if needed.
     *
     * @since 0.6.3m1
     */
    protected void migrateProfile() {
        if (hasOption(PROFILE_DOMAIN, "nickname")) {
            // Migrate

            setOption(PROFILE_DOMAIN, "nicknames", getOption(PROFILE_DOMAIN, "nickname")
                    + (hasOption(PROFILE_DOMAIN, "altnicks") ? "\n"
                    + getOption(PROFILE_DOMAIN, "altnicks") : ""));
            unsetOption(PROFILE_DOMAIN, "nickname");
            unsetOption(PROFILE_DOMAIN, "altnicks");
        }
    }

    /**
     * Determines whether this identity can be used as a profile when
     * connecting to a server. Profiles are identities that can supply
     * nick, ident, real name, etc.
     *
     * @return True iff this identity can be used as a profile
     */
    public boolean isProfile() {
        return (hasOption(PROFILE_DOMAIN, "nicknames")
                || hasOption(PROFILE_DOMAIN, "nickname"))
                && hasOption(PROFILE_DOMAIN, "realname");
    }

    /** {@inheritDoc} */
    @Override
    protected boolean hasOption(final String domain, final String option) {
        return file.isKeyDomain(domain) && file.getKeyDomain(domain).containsKey(option);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized String getOption(final String domain, final String option) {
        return file.getKeyDomain(domain).get(option);
    }

    /**
     * Sets the specified option in this identity to the specified value.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     * @param value The new value for the option
     */
    public void setOption(final String domain, final String option,
            final String value) {
        String oldValue;

        synchronized (this) {
            oldValue = getOption(domain, option);
            LOGGER.finer(getName() + ": setting " + domain + "." + option + " to " + value);

            if (myTarget.getType() == ConfigTarget.TYPE.GLOBAL) {
                // If we're the global config, don't set useless settings that are
                // covered by global defaults.

                if (globalConfig == null) {
                    globalConfig = new ConfigManager("", "", "");
                }

                globalConfig.removeIdentity(this);

                if (globalConfig.hasOption(domain, option)
                        && globalConfig.getOption(domain, option).equals(value)) {
                    if (oldValue == null) {
                        return;
                    } else {
                        unsetOption(domain, option);
                        return;
                    }
                }
            }
        }

        if ((oldValue == null && value != null)
                || (oldValue != null && !oldValue.equals(value))) {
            synchronized (this) {
                file.getKeyDomain(domain).put(option, value);
                needSave = true;
            }

            fireSettingChange(domain, option);
        }
    }

    /**
     * Sets the specified option in this identity to the specified value.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     * @param value The new value for the option
     */
    public void setOption(final String domain, final String option,
            final int value) {
        setOption(domain, option, String.valueOf(value));
    }

    /**
     * Sets the specified option in this identity to the specified value.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     * @param value The new value for the option
     */
    public void setOption(final String domain, final String option,
            final boolean value) {
        setOption(domain, option, String.valueOf(value));
    }

    /**
     * Sets the specified option in this identity to the specified value.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     * @param value The new value for the option
     */
    public void setOption(final String domain, final String option,
            final List<String> value) {
        final StringBuilder temp = new StringBuilder();
        for (String part : value) {
            temp.append('\n');
            temp.append(part);
        }
        setOption(domain, option, temp.length() > 0 ? temp.substring(1) : temp.toString());
    }

    /**
     * Unsets a specified option.
     *
     * @param domain domain of the option
     * @param option name of the option
     */
    public synchronized void unsetOption(final String domain, final String option) {
        file.getKeyDomain(domain).remove(option);
        needSave = true;

        fireSettingChange(domain, option);
    }

    /**
     * Returns the set of domains available in this identity.
     *
     * @since 0.6
     * @return The set of domains used by this identity
     */
    public Set<String> getDomains() {
        return new HashSet<String>(file.getKeyDomains().keySet());
    }

    /**
     * Retrieves a map of all options within the specified domain in this
     * identity.
     *
     * @param domain The domain to retrieve
     * @since 0.6
     * @return A map of option names to values
     */
    public synchronized Map<String, String> getOptions(final String domain) {
        return new HashMap<String, String>(file.getKeyDomain(domain));
    }

    /**
     * Saves this identity to disk if it has been updated.
     */
    public synchronized void save() {
        LOGGER.fine(getName() + ": save(); needsave = " + needSave);

        if (needSave && file != null && file.isWritable()) {
            if (myTarget != null && myTarget.getType() == ConfigTarget.TYPE.GLOBAL) {
                LOGGER.finer(getName() + ": I'm a global config");

                // This branch is executed if this identity is global. In this
                // case, we build a global config (removing ourself and the
                // versions identity) and compare our values to the values
                // contained in that. Any values that are the same can be unset
                // from this identity (as they will default to their current
                // value).
                //
                // Note that the updater channel is included in the version
                // identity, and this is excluded from the global config. This
                // means that once you manually set the channel it will stay
                // like that until you manually change it again, as opposed
                // to being removed as soon as you use a build from that
                // channel.

                if (globalConfig == null) {
                    globalConfig = new ConfigManager("", "", "");
                }

                globalConfig.removeIdentity(this);
                globalConfig.removeIdentity(IdentityManager.getVersionIdentity());

                if (LOGGER.isLoggable(Level.FINEST)) {
                    for (Identity source : globalConfig.getSources()) {
                        LOGGER.finest(getName() + ": source: " + source.getName());
                    }
                }

                for (Map.Entry<String, Map<String, String>> entry
                        : file.getKeyDomains().entrySet()) {
                    final String domain = entry.getKey();

                    for (Map.Entry<String, String> subentry : 
                        new HashSet<Map.Entry<String, String>>(entry.getValue().entrySet())) {
                        final String key = subentry.getKey();
                        final String value = subentry.getValue();

                        if (globalConfig.hasOption(domain, key) &&
                                globalConfig.getOption(domain, key).equals(value)) {
                            LOGGER.finest(getName() + ": found superfluous setting: "
                                    + domain + "." + key + " (= " + value + ")");
                            file.getKeyDomain(domain).remove(key);
                        }
                    }
                }
            }

            if (file.isKeyDomain("temp")) {
                file.getKeyDomain("temp").clear();
            }

            try {
                file.write();

                needSave = false;
            } catch (IOException ex) {
                Logger.userError(ErrorLevel.MEDIUM,
                        "Unable to save identity file: " + ex.getMessage());
            }
        }
    }

    /**
     * Deletes this identity from disk.
     */
    public synchronized void delete() {
        if (file != null) {
            file.delete();
        }

        IdentityManager.removeIdentity(this);
    }

    /**
     * Retrieves this identity's target.
     *
     * @return The target of this identity
     */
    public ConfigTarget getTarget() {
        return myTarget;
    }

    /**
     * Retrieve this identity's ConfigFile.
     *
     * @return The ConfigFile object used by this identity
     * @deprecated Direct access should be avoided to prevent synchronisation
     * issues
     */
    @Deprecated
    public ConfigFile getFile() {
        return file;
    }

    /**
     * Adds a new config change listener for this identity.
     *
     * @param listener The listener to be added
     */
    public void addListener(final ConfigChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes the specific config change listener from this identity.
     *
     * @param listener The listener to be removed
     */
    public void removeListener(final ConfigChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Returns a string representation of this object (its name).
     *
     * @return A string representation of this object
     */
    @Override
    public String toString() {
        return getName();
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return getName().hashCode() + getTarget().hashCode();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Identity
                && getName().equals(((Identity) obj).getName())
                && getTarget() == ((Identity) obj).getTarget()) {
            return true;
        }
        return false;
    }

    /**
     * Compares this identity to another config source to determine which
     * is more specific.
     *
     * @param target The Identity to compare to
     * @return -1 if this config source is less specific, 0 if they're equal,
     * +1 if this config is more specific.
     */
    @Override
    public int compareTo(final Identity target) {
        return target.getTarget().compareTo(myTarget);
    }

    /**
     * Creates a new identity containing the specified properties.
     *
     * @param settings The settings to populate the identity with
     * @return A new identity containing the specified properties
     * @throws IOException If the file cannot be created
     * @throws InvalidIdentityFileException If the settings are invalid
     * @since 0.6.3m1
     */
    protected static Identity createIdentity(final Map<String, Map<String, String>> settings)
            throws IOException, InvalidIdentityFileException {
        if (!settings.containsKey(DOMAIN) || !settings.get(DOMAIN).containsKey("name")
                || settings.get(DOMAIN).get("name").isEmpty()) {
            throw new InvalidIdentityFileException("identity.name is not set");
        }

        final String fs = System.getProperty("file.separator");
        final String location = Main.getConfigDir() + "identities" + fs;
        final String name = settings.get(DOMAIN).get("name").replaceAll(ILLEGAL_CHARS, "_");

        File file = new File(location + name);
        int attempt = 0;

        while (file.exists()) {
            file = new File(location + name + "-" + ++attempt);
        }

        final ConfigFile configFile = new ConfigFile(file);

        for (Map.Entry<String, Map<String, String>> entry : settings.entrySet()) {
            configFile.addDomain(entry.getKey(), entry.getValue());
        }

        configFile.write();

        final Identity identity = new Identity(file, false);
        IdentityManager.addIdentity(identity);

        return identity;
    }

    /**
     * Generates an empty identity for the specified target.
     *
     * @param target The target for the new identity
     * @return An empty identity for the specified target
     * @throws IOException if the file can't be written
     * @see #createIdentity(java.util.Map)
     */
    public static Identity buildIdentity(final ConfigTarget target)
            throws IOException {
        final Map<String, Map<String, String>> settings
                = new HashMap<String, Map<String, String>>();
        settings.put(DOMAIN, new HashMap<String, String>(2));
        settings.get(DOMAIN).put("name", target.getData());
        settings.get(DOMAIN).put(target.getTypeName(), target.getData());

        try {
            return createIdentity(settings);
        } catch (InvalidIdentityFileException ex) {
            Logger.appError(ErrorLevel.MEDIUM, "Unable to create identity", ex);
            return null;
        }
    }

    /**
     * Generates an empty profile witht he specified name. Note the name is used
     * as a file name, so should be sanitised.
     *
     * @param name The name of the profile to create
     * @return A new profile with the specified name
     * @throws IOException If the file can't be written
     * @see #createIdentity(java.util.Map)
     */
    public static Identity buildProfile(final String name) throws IOException {
        final Map<String, Map<String, String>> settings
                = new HashMap<String, Map<String, String>>();
        settings.put(DOMAIN, new HashMap<String, String>(1));
        settings.put(PROFILE_DOMAIN, new HashMap<String, String>(2));

        final String nick = System.getProperty("user.name").replace(' ', '_');

        settings.get(DOMAIN).put("name", name);
        settings.get(PROFILE_DOMAIN).put("nicknames", nick);
        settings.get(PROFILE_DOMAIN).put("realname", nick);

        try {
            return createIdentity(settings);
        } catch (InvalidIdentityFileException ex) {
            Logger.appError(ErrorLevel.MEDIUM, "Unable to create identity", ex);
            return null;
        }
    }

}
