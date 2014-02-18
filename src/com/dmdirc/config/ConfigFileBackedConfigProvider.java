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

package com.dmdirc.config;

import com.dmdirc.interfaces.config.ConfigChangeListener;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.util.collections.WeakList;
import com.dmdirc.util.io.ConfigFile;
import com.dmdirc.util.io.InvalidConfigFileException;
import com.dmdirc.util.validators.Validator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.LoggerFactory;

/**
 * Provides configuration settings from a {@link ConfigFile}.
 */
public class ConfigFileBackedConfigProvider extends BaseConfigProvider implements ConfigProvider {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(
            ConfigFileBackedConfigProvider.class);
    /** The domain used for identity settings. */
    private static final String DOMAIN = "identity";
    /** The domain used for profile settings. */
    private static final String PROFILE_DOMAIN = "profile";
    /** The target for this identity. */
    protected final ConfigTarget myTarget;
    /** The configuration details for this identity. */
    protected final ConfigFile file;
    /** The global config manager. */
    protected ConfigManager globalConfig;
    /** The config change listeners for this source. */
    protected final List<ConfigChangeListener> listeners = new WeakList<>();
    /** Whether this identity needs to be saved. */
    protected boolean needSave;

    /**
     * Creates a new instance of Identity.
     *
     * @param file         The file to load this identity from
     * @param forceDefault Whether to force this identity to be loaded as default identity or not
     *
     * @throws InvalidIdentityFileException Missing required properties
     * @throws IOException                  Input/output exception
     */
    public ConfigFileBackedConfigProvider(final File file, final boolean forceDefault) throws
            IOException,
            InvalidIdentityFileException {
        super();

        this.file = new ConfigFile(file);
        this.file.setAutomake(true);
        initFile(forceDefault);
        myTarget = getTarget(forceDefault);

        if (myTarget.isCustom(PROFILE_DOMAIN)) {
            migrateProfile();
        }
    }

    /**
     * Creates a new read-only identity.
     *
     * @param stream       The input stream to read the identity from
     * @param forceDefault Whether to force this identity to be loaded as default identity or not
     *
     * @throws InvalidIdentityFileException Missing required properties
     * @throws IOException                  Input/output exception
     */
    public ConfigFileBackedConfigProvider(final InputStream stream, final boolean forceDefault)
            throws IOException,
            InvalidIdentityFileException {
        super();

        this.file = new ConfigFile(stream);
        this.file.setAutomake(true);
        initFile(forceDefault);
        myTarget = getTarget(forceDefault);

        if (myTarget.isCustom(PROFILE_DOMAIN)) {
            migrateProfile();
        }
    }

    /**
     * Creates a new identity from the specified config file.
     *
     * @param configFile The config file to use
     * @param target     The target of this identity
     */
    public ConfigFileBackedConfigProvider(final ConfigFile configFile, final ConfigTarget target) {
        super();

        this.file = configFile;
        this.file.setAutomake(true);
        this.myTarget = target;

        if (myTarget.isCustom(PROFILE_DOMAIN)) {
            migrateProfile();
        }
    }

    /**
     * Determines and returns the target for this identity from its contents.
     *
     * @param forceDefault Whether to force this to be a default identity
     *
     * @return A ConfigTarget for this identity
     *
     * @throws InvalidIdentityFileException If the identity isn't valid
     */
    private ConfigTarget getTarget(final boolean forceDefault)
            throws InvalidIdentityFileException {
        final ConfigTarget target = new ConfigTarget();

        if (hasOptionString(DOMAIN, "ircd")) {
            target.setIrcd(getOption(DOMAIN, "ircd"));
        } else if (hasOptionString(DOMAIN, "protocol")) {
            target.setProtocol(getOption(DOMAIN, "protocol"));
        } else if (hasOptionString(DOMAIN, "network")) {
            target.setNetwork(getOption(DOMAIN, "network"));
        } else if (hasOptionString(DOMAIN, "server")) {
            target.setServer(getOption(DOMAIN, "server"));
        } else if (hasOptionString(DOMAIN, "channel")) {
            target.setChannel(getOption(DOMAIN, "channel"));
        } else if (hasOptionString(DOMAIN, "globaldefault")) {
            target.setGlobalDefault();
        } else if (hasOptionString(DOMAIN, "global") || (forceDefault && !isProfile())) {
            target.setGlobal();
        } else if (isProfile()) {
            target.setCustom(PROFILE_DOMAIN);
        } else if (hasOptionString(DOMAIN, "type")) {
            target.setCustom(getOption(DOMAIN, "type"));
        } else {
            throw new InvalidIdentityFileException("No target and no profile");
        }

        if (hasOptionString(DOMAIN, "order")) {
            target.setOrder(getOptionInt(DOMAIN, "order"));
        }

        return target;
    }

    /**
     * Initialises this identity from a file.
     *
     * @since 0.6.3
     * @param forceDefault Whether to force this to be a default identity
     *
     * @throws InvalidIdentityFileException if the identity file is invalid
     * @throws IOException                  On I/O exception when reading the identity
     */
    private void initFile(final boolean forceDefault)
            throws InvalidIdentityFileException, IOException {
        try {
            this.file.read();
        } catch (InvalidConfigFileException ex) {
            throw new InvalidIdentityFileException(ex);
        }

        if (!hasOptionString(DOMAIN, "name") && !forceDefault) {
            throw new InvalidIdentityFileException("No name specified");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void reload() throws IOException, InvalidConfigFileException {
        if (needSave) {
            return;
        }

        final List<String[]> changes = new LinkedList<>();

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
     * Fires the config changed listener for the specified option after this identity is reloaded.
     *
     * @param domain The domain of the option that's changed
     * @param key    The key of the option that's changed
     *
     * @since 0.6.3m1
     */
    private void fireSettingChange(final String domain, final String key) {
        for (ConfigChangeListener listener : new ArrayList<>(listeners)) {
            listener.configChanged(domain, key);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        if (hasOptionString(DOMAIN, "name")) {
            return getOption(DOMAIN, "name");
        } else {
            return "Unnamed";
        }
    }

    /**
     * Checks if this profile needs migrating from the old method of storing nicknames
     * (profile.nickname + profile.altnicks) to the new method (profile.nicknames), and performs the
     * migration if needed.
     *
     * @since 0.6.3m1
     */
    protected void migrateProfile() {
        if (hasOptionString(PROFILE_DOMAIN, "nickname")) {
            // Migrate

            setOption(PROFILE_DOMAIN, "nicknames", getOption(PROFILE_DOMAIN, "nickname")
                    + (hasOptionString(PROFILE_DOMAIN, "altnicks") ? "\n"
                    + getOption(PROFILE_DOMAIN, "altnicks") : ""));
            unsetOption(PROFILE_DOMAIN, "nickname");
            unsetOption(PROFILE_DOMAIN, "altnicks");
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isProfile() {
        return (hasOptionString(PROFILE_DOMAIN, "nicknames")
                || hasOptionString(PROFILE_DOMAIN, "nickname"))
                && hasOptionString(PROFILE_DOMAIN, "realname");
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasOption(final String domain, final String option,
            final Validator<String> validator) {
        return file.isKeyDomain(domain)
                && file.getKeyDomain(domain).containsKey(option)
                && !validator.validate(file.getKeyDomain(domain).get(option)).isFailure();
    }

    /** {@inheritDoc} */
    @Override
    public synchronized String getOption(final String domain,
            final String option, final Validator<String> validator) {
        final String value = file.getKeyDomain(domain).get(option);

        if (validator.validate(value).isFailure()) {
            return null;
        }

        return value;
    }

    /** {@inheritDoc} */
    @Override
    public void setOption(final String domain, final String option,
            final String value) {
        String oldValue;
        boolean unset = false;

        synchronized (this) {
            oldValue = getOption(domain, option);
            log.trace("{}: setting {}.{} to {} (was: {})",
                    new Object[]{getName(), domain, option, value, oldValue});

            if (myTarget.getType() == ConfigTarget.TYPE.GLOBAL) {
                // If we're the global config, don't set useless settings that are
                // covered by global defaults.

                if (globalConfig == null) {
                    globalConfig = new ConfigManager("", "", "", "");
                }

                globalConfig.removeIdentity(this);

                if (globalConfig.hasOptionString(domain, option)
                        && globalConfig.getOption(domain, option).equals(value)) {
                    // The new value is covered by a default setting
                    if (oldValue == null) {
                        // There was no old value, so we don't need to do anything
                        return;
                    } else {
                        // There was an old value, so we need to unset it so
                        // that the default shows through.
                        file.getKeyDomain(domain).remove(option);
                        needSave = true;
                        unset = true;
                    }
                }
            }

            if (!unset && ((oldValue == null && value != null)
                    || (oldValue != null && !oldValue.equals(value)))) {
                file.getKeyDomain(domain).put(option, value);
                needSave = true;
            }
        }

        // Fire any setting change listeners now we're no longer holding
        // a lock on this identity.
        if (unset || (oldValue == null && value != null)
                || (oldValue != null && !oldValue.equals(value))) {
            fireSettingChange(domain, option);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setOption(final String domain, final String option,
            final int value) {
        setOption(domain, option, String.valueOf(value));
    }

    /** {@inheritDoc} */
    @Override
    public void setOption(final String domain, final String option,
            final boolean value) {
        setOption(domain, option, String.valueOf(value));
    }

    /** {@inheritDoc} */
    @Override
    public void setOption(final String domain, final String option,
            final List<String> value) {
        final StringBuilder temp = new StringBuilder();
        for (String part : value) {
            temp.append('\n');
            temp.append(part);
        }
        setOption(domain, option, temp.length() > 0 ? temp.substring(1) : temp.toString());
    }

    /** {@inheritDoc} */
    @Override
    public void unsetOption(final String domain, final String option) {
        synchronized (this) {
            file.getKeyDomain(domain).remove(option);
            needSave = true;
        }

        fireSettingChange(domain, option);
    }

    /** {@inheritDoc} */
    @Override
    public Set<String> getDomains() {
        return new HashSet<>(file.getKeyDomains().keySet());
    }

    /** {@inheritDoc} */
    @Override
    public synchronized Map<String, String> getOptions(final String domain) {
        return new HashMap<>(file.getKeyDomain(domain));
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void save() {
        log.info("{}: saving. Needsave = {}", new Object[]{getName(), needSave});

        if (needSave && file != null && file.isWritable()) {
            if (myTarget != null && myTarget.getType() == ConfigTarget.TYPE.GLOBAL) {
                log.debug("{}: I'm a global config", getName());

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
                    globalConfig = new ConfigManager("", "", "", "");
                }

                globalConfig.removeIdentity(this);
                globalConfig.removeIdentity(IdentityManager.getIdentityManager().
                        getVersionSettings());

                if (log.isTraceEnabled()) {
                    for (ConfigProvider source : globalConfig.getSources()) {
                        log.trace("{}: source: {}",
                                new Object[]{getName(), source.getName()});
                    }
                }

                for (Map.Entry<String, Map<String, String>> entry
                        : file.getKeyDomains().entrySet()) {
                    final String domain = entry.getKey();

                    for (Map.Entry<String, String> subentry : new HashSet<>(entry.getValue().
                            entrySet())) {
                        final String key = subentry.getKey();
                        final String value = subentry.getValue();

                        if (globalConfig.hasOptionString(domain, key)
                                && globalConfig.getOption(domain, key).equals(value)) {
                            log.debug("{}: found superfluous setting: {}.{} (= {})",
                                    new Object[]{getName(), domain, key, value});
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

    /** {@inheritDoc} */
    @Override
    public synchronized void delete() {
        if (file != null) {
            file.delete();
        }

        IdentityManager.getIdentityManager().removeConfigProvider(this);
    }

    /** {@inheritDoc} */
    @Override
    public ConfigTarget getTarget() {
        return myTarget;
    }

    /**
     * Determines if this identity is loaded from the specified File.
     *
     * @param target The file to be checked
     *
     * @return True if this identity comes from the target file, false otherwise
     *
     * @since 0.6.4
     */
    public boolean isFile(final File target) {
        return file != null && file.getFile() != null && file.getFile().equals(target);
    }

    /** {@inheritDoc} */
    @Override
    public void addListener(final ConfigChangeListener listener) {
        listeners.add(listener);
    }

    /** {@inheritDoc} */
    @Override
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
        return obj instanceof ConfigFileBackedConfigProvider
                && getName().equals(((ConfigFileBackedConfigProvider) obj).getName())
                && getTarget() == ((ConfigFileBackedConfigProvider) obj).getTarget();
    }

}
