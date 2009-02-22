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
package com.dmdirc.config.prefs;

import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.prefs.validator.PermissiveValidator;
import com.dmdirc.config.prefs.validator.Validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a single setting.
 * 
 * @author chris
 */
public class PreferencesSetting {
    
    /** The type of this setting. */
    protected final PreferencesType type;
    
    /** The possible options for a multichoice setting. */
    protected final Map<String, String> combooptions;

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
    protected String current;
    
    /** The original value of this vsetting. */
    private String original;
    
    /** Whether or not we need a restart. */
    protected boolean restartNeeded;
    
    /** A list of change listeners. */
    private final List<SettingChangeListener> listeners = new ArrayList<SettingChangeListener>();

    /**
     * Creates a new preferences setting for any type except multi-choice.
     * 
     * @param type The type of the setting to create
     * @param validator A validator to validate the setting's value
     * @param domain The domain of the setting
     * @param option The option name of the setting
     * @param title The title of this setting
     * @param helptext Text to display to help the user
     */
    public PreferencesSetting(final PreferencesType type,
            final Validator<String> validator, final String domain,
            final String option, final String title, final String helptext) {
        if (PreferencesType.MULTICHOICE.equals(type)) {
            throw new IllegalArgumentException("Multi-choice preferences must " +
                    "have their options specified.");
        }
        
        this.type = type;
        this.combooptions = null;
        this.validator = validator;
        this.domain = domain;
        this.option = option;
        this.title = title;
        this.helptext = helptext;
        
        current = IdentityManager.getGlobalConfig().getOption(domain, option);
        original = current;
    }
    
    /**
     * Creates a new preferences setting for any type except multi-choice, with
     * a default permissive validator.
     * 
     * @param type The type of the setting to create
     * @param domain The domain of the setting
     * @param option The option name of the setting
     * @param title The title of this setting
     * @param helptext Text to display to help the user
     */
    public PreferencesSetting(final PreferencesType type, final String domain,
            final String option, final String title, final String helptext) {
        if (PreferencesType.MULTICHOICE.equals(type)) {
            throw new IllegalArgumentException("Multi-choice preferences must " +
                    "have their options specified.");
        }
        
        this.type = type;
        this.combooptions = null;
        this.validator = new PermissiveValidator<String>();
        this.domain = domain;
        this.option = option;
        this.title = title;
        this.helptext = helptext;
        
        current = IdentityManager.getGlobalConfig().getOption(domain, option);
        original = current;
    }    
    
    /**
     * Creates a new preferences setting for multi-choice preferences.
     * 
     * @param domain The domain of the setting
     * @param option The option name of the setting
     * @param options A map of setting values to display names for this setting
     * @param title The title of this setting
     * @param helptext Text to display to help the user
     */
    public PreferencesSetting(final String domain, final String option,
            final String title, final String helptext,
            final Map<String, String> options) {
        this.type = PreferencesType.MULTICHOICE;
        this.combooptions = new HashMap<String, String>(options);
        this.validator = new PermissiveValidator<String>();
        this.domain = domain;
        this.option = option;
        this.title = title;
        this.helptext = helptext;
        
        current = IdentityManager.getGlobalConfig().getOption(domain, option);
        original = current;
        
        if (!combooptions.containsKey(current)) {
            combooptions.put(current, "Current (" + current + ")");
        }
    }    

    /**
     * Retrieves the possible options for use in a multi-choice preference.
     * 
     * @return A map of setting values to display names, representing the
     * possible options for this setting.
     */
    public Map<String, String> getComboOptions() {
        return combooptions;
    }

    /**
     * Retrieves the help text for this setting.
     * 
     * @return This setting's help text.
     */
    public String getHelptext() {
        return helptext;
    }

    /**
     * Retrieves the title of this setting.
     * 
     * @return This setting's title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Retrieves the current value of this setting.
     * 
     * @return The current value of this setting.
     */
    public String getValue() {
        return current;
    }

    /**
     * Retieves the type of this setting.
     * 
     * @return This setting's type.
     */
    public PreferencesType getType() {
        return type;
    }

    /**
     * Returns a validator that can validate this setting.
     * 
     * @return This setting's validator.
     */
    public Validator<String> getValidator() {
        return validator;
    }
    
    /**
     * Sets the current value of this setting. Note that the setting is not
     * saved to the configuration file until the save method is called.
     * 
     * @param newValue The new value of the setting
     */
    public void setValue(final String newValue) {
        current = newValue;
        
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
     * Sets the "restart needed" flag for this setting, indicating a client
     * restart is needed before the setting takes effect.
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
        
        if (current == null) {
            IdentityManager.getConfigIdentity().unsetOption(domain, option);
        } else {
            IdentityManager.getConfigIdentity().setOption(domain, option, current);
        }

        original = current;
        return true;
    }
    
    /**
     * Dismisses changes to this setting.
     */
    public void dismiss() {
        if ((original != null && original.equals(current))
                || (original == null && current == null)) {
            return;
        }
        
        current = original;
        
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
        return (current == null || !current.equals(original))
                && (current != null || original != null)
                && (validator == null || !validator.validate(current).isFailure());
    }

}
