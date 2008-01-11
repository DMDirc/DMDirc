/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.config.prefs.validator.NumericalValidator;
import com.dmdirc.config.prefs.validator.StringLengthValidator;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author chris
 */
public class PreferencesManager {

    private final Map<String, PreferencesCategory> categories
            = new HashMap<String, PreferencesCategory>();

    public PreferencesManager() {
        addDefaultCategories();
    }

    public void addCategory(final PreferencesCategory category) {
        categories.put(category.getTitle(), category);
    }

    public Map<String, PreferencesCategory> getCategories() {
        return categories;
    }

    /**
     * Adds the default categories to this preferences manager.
     */
    private void addDefaultCategories() {
        addGeneralCategory();
        addConnectionCategory();
        addMessagesCategory();
        addGuiCategory();
        addUrlHandlerCategory();
        addAdvancedCategory();
    }

    /**
     * Creates and adds the "General" category.
     */
    private void addGeneralCategory() {
        final PreferencesCategory category = new PreferencesCategory("General", "");

        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "ui", "confirmquit", "false", "Confirm quit",
                "Do you want to confirm closing the client?"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "channel", "splitusermodes", "false", "Split user modes",
                "Show individual mode lines for each mode change that affects" +
                " a user (e.g. op, devoice)"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "channel", "sendwho", "false", "Send channel WHOs",
                "Request information (away state, hostname, etc) on channel " +
                "users automatically"));
        category.addSetting(new PreferencesSetting(PreferencesType.DURATION,
                "general", "whotime", "600", "Who request interval",
                "How often to send WHO requests for a channel"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "channel", "showmodeprefix", "false", "Show mode prefix",
                "Prefix users' names with their mode in channels"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "server", "friendlymodes", "false", "Friendly modes",
                "Show friendly mode names"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "general", "hidequeries", "false", "Hide queries",
                "Initially hide queries so that they don't steal focus"));
        category.addSetting(new PreferencesSetting(PreferencesType.TEXT,
                new StringLengthValidator(1, 1), "general", "commandchar", "/",
                "Command character", "Character used to prefix a command"));
        category.addSetting(new PreferencesSetting(PreferencesType.TEXT,
                new StringLengthValidator(1, 1), "general", "silencechar", ".",
                "Silence character", "Character used to silence commands"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "ui", "awayindicator", "false", "Away indicator",
                "Shows an indicator in windows when you are marked as away"));
        category.addSetting(new PreferencesSetting(PreferencesType.INTEGER,
                new NumericalValidator(0, 100), "ui", "pasteProtectionLimit", "1",
                "Paste protection trigger", "Confirm pasting of text that " +
                "contains more than this many lines."));
        
        addCategory(category);
    }
    
    private void addAdvancedCategory() {
        // XXX: Not implemented
    }

    private void addConnectionCategory() {
        // XXX: Not implemented
    }    

    private void addGuiCategory() {
        // XXX: Not implemented
    }

    private void addMessagesCategory() {
        // XXX: Not implemented
    }

    private void addUrlHandlerCategory() {
        // XXX: Not implemented
    }

}
