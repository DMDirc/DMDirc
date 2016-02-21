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

package com.dmdirc.config.prefs;

import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.interfaces.config.ReadOnlyConfigProvider;
import com.dmdirc.util.validators.PermissiveValidator;
import com.dmdirc.util.validators.Validator;

import com.google.common.collect.Table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a single setting.
 */
public class PreferencesSetting {

    /** The type of this setting. */
    protected final PreferencesType type;
    /** The possible options for a multichoice setting. */
    protected final Map<String, String> comboOptions;
    /** The headers in the table. */
    private final Iterable<String> tableHeaders;
    /** The possible options for a table setting. */
    protected final Table<Integer, Integer, String> tableOptions;
    /** The validator to use to validate this setting. */
    protected final Validator<String> validator;
    /** The domain of the setting. */
    protected final String domain;
    /** The option name of the setting. */
    protected final String option;
    /** The title of this setting. */
    protected final String title;
    /** Text to inform the user what the setting is for. */
    protected final String helptext;
    /** The current value of the setting. */
    protected String value;
    /** Whether or not we need a restart. */
    protected boolean restartNeeded;
    /** The original value of this setting. */
    private String original;
    /** A list of change listeners. */
    private final Collection<SettingChangeListener> listeners = new ArrayList<>();
    /** Identity to save settings to. */
    private final ConfigProvider identity;

    /**
     * Creates a new preferences setting for any type except multi-choice.
     *
     * @param type          The type of the setting to create
     * @param validator     A validator to validate the setting's value
     * @param domain        The domain of the setting
     * @param option        The option name of the setting
     * @param title         The title of this setting
     * @param helptext      Text to display to help the user
     * @param configManager Config Manager
     * @param identity      Identity to save setting to
     */
    public PreferencesSetting(final PreferencesType type,
            final Validator<String> validator, final String domain,
            final String option, final String title, final String helptext,
            final ReadOnlyConfigProvider configManager, final ConfigProvider identity) {
        if (PreferencesType.MULTICHOICE == type) {
            throw new IllegalArgumentException("Multi-choice preferences must "
                    + "have their options specified.");
        }
        if (PreferencesType.TABLE == type) {
            throw new IllegalArgumentException("Table preferences must "
                    + "have their table options specified.");
        }

        this.type = type;
        this.comboOptions = null;
        this.tableHeaders = null;
        this.tableOptions = null;
        this.validator = validator;
        this.domain = domain;
        this.option = option;
        this.title = title;
        this.helptext = helptext;
        this.identity = identity;

        value = configManager.getOption(domain, option);
        original = value;
    }

    /**
     * Creates a new preferences setting for any type except multi-choice, with a default permissive
     * validator.
     *
     * @param type          The type of the setting to create
     * @param domain        The domain of the setting
     * @param option        The option name of the setting
     * @param title         The title of this setting
     * @param helptext      Text to display to help the user
     * @param configManager Config Manager
     * @param identity      Identity to save setting to
     */
    public PreferencesSetting(final PreferencesType type, final String domain,
            final String option, final String title, final String helptext,
            final ReadOnlyConfigProvider configManager, final ConfigProvider identity) {
        if (PreferencesType.MULTICHOICE == type) {
            throw new IllegalArgumentException("Multi-choice preferences must "
                    + "have their options specified.");
        }
        if (PreferencesType.TABLE == type) {
            throw new IllegalArgumentException("Table preferences must "
                    + "have their table options specified.");
        }

        this.type = type;
        this.comboOptions = null;
        this.tableHeaders = null;
        this.tableOptions = null;
        this.validator = new PermissiveValidator<>();
        this.domain = domain;
        this.option = option;
        this.title = title;
        this.helptext = helptext;
        this.identity = identity;

        value = configManager.getOption(domain, option);
        original = value;
    }

    /**
     * Creates a new preferences setting for multi-choice preferences.
     *
     * @param domain        The domain of the setting
     * @param option        The option name of the setting
     * @param options       A map of setting values to display names for this setting
     * @param title         The title of this setting
     * @param helptext      Text to display to help the user
     * @param configManager Config Manager
     * @param identity      Identity to save setting to
     */
    public PreferencesSetting(final String domain, final String option,
            final String title, final String helptext,
            final Map<String, String> options,
            final ReadOnlyConfigProvider configManager, final ConfigProvider identity) {
        this.type = PreferencesType.MULTICHOICE;
        this.comboOptions = new HashMap<>(options);
        this.tableHeaders = null;
        this.tableOptions = null;
        this.validator = new PermissiveValidator<>();
        this.domain = domain;
        this.option = option;
        this.title = title;
        this.helptext = helptext;
        this.identity = identity;

        value = configManager.getOption(domain, option);
        original = value;

        if (!comboOptions.containsKey(value)) {
            comboOptions.put(value, "Current (" + value + ')');
        }
    }

    /**
     * Creates a new preferences setting for table preferences.
     *
     * @param domain        The domain of the setting
     * @param option        The option name of the setting
     * @param options       A map of setting values to display names for this setting
     * @param title         The title of this setting
     * @param helptext      Text to display to help the user
     * @param configManager Config Manager
     * @param identity      Identity to save setting to
     */
    public PreferencesSetting(final String domain, final String option,
            final String title, final String helptext,
            final Iterable<String> tableHeaders,
            final Table<Integer, Integer, String> options,
            final ReadOnlyConfigProvider configManager,
            final ConfigProvider identity) {
        this.type = PreferencesType.TABLE;
        this.comboOptions = null;
        this.tableHeaders = tableHeaders;
        this.tableOptions = options;
        this.validator = new PermissiveValidator<>();
        this.domain = domain;
        this.option = option;
        this.title = title;
        this.helptext = helptext;
        this.identity = identity;

        value = configManager.getOption(domain, option);
        original = value;
    }

    public PreferencesType getType() {
        return type;
    }

    public Map<String, String> getComboOptions() {
        return comboOptions;
    }

    public Iterable<String> getTableHeaders() {
        return tableHeaders;
    }

    public Table<Integer, Integer, String> getTableOptions() {
        return tableOptions;
    }

    public Validator<String> getValidator() {
        return validator;
    }

    public String getTitle() {
        return title;
    }

    public String getHelptext() {
        return helptext;
    }

    public String getValue() {
        return value;
    }

    /**
     * Sets the current value of this setting. Note that the setting is not saved to the
     * configuration file until the save method is called.
     *
     * @param newValue The new value of the setting
     */
    public void setValue(final String newValue) {
        value = newValue;

        for (SettingChangeListener listener : listeners) {
            listener.settingChanged(this);
        }
    }

    /**
     * Determines whether or not this setting needs a restart when it's changed.
     *
     * @return True if this setting needs a restart, false otherwise
     */
    public boolean isRestartNeeded() {
        return restartNeeded;
    }

    /**
     * Sets the "restart needed" flag for this setting, indicating a client restart is needed before
     * the setting takes effect.
     *
     * @return A reference to this setting, for convenience
     */
    public PreferencesSetting setRestartNeeded() {
        restartNeeded = true;
        return this;
    }

    /**
     * Registers the specified setting change listener.
     *
     * @param listener The listener to be registered
     *
     * @return A reference to this setting, for convenience
     */
    public PreferencesSetting registerChangeListener(final SettingChangeListener listener) {
        listeners.add(listener);
        return this;
    }

    /**
     * Saves the current value of this setting to the global configuration.
     *
     * @return True if the setting has changed, false otherwise
     */
    public boolean save() {
        if (!needsSaving()) {
            return false;
        }

        if (value == null) {
            identity.unsetOption(domain, option);
        } else {
            identity.setOption(domain, option, value);
        }

        original = value;
        return true;
    }

    /**
     * Dismisses changes to this setting.
     */
    public void dismiss() {
        if (Objects.equals(original, value)) {
            return;
        }

        value = original;

        for (SettingChangeListener listener : listeners) {
            listener.settingChanged(this);
        }
    }

    /**
     * Does the setting need saving?
     *
     * @return true iif the setting will be changed if saved
     */
    public boolean needsSaving() {
        return (!Objects.equals(value, original))
                && (value != null || original != null)
                && (validator == null || !validator.validate(value).isFailure());
    }

    /**
     * Does this setting's identity have this setting?
     *
     * @return true iif the setting is present
     */
    public boolean isSet() {
        return identity.hasOptionString(domain, option, validator);
    }

}
