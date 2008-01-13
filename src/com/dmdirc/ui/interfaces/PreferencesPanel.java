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

package com.dmdirc.ui.interfaces;

import com.dmdirc.config.prefs.PreferencesCategory;
import java.util.Collection;
import javax.swing.JPanel;

/**
 * Preferences panel interface.
 */
public interface PreferencesPanel {
       
    /**
     * Adds a named category to the preferences panel.
     *
     * @param name Category name
     * @param blurb category blurb
     * @deprecated Use addCategory(PreferencesCategory) instead
     */
    @Deprecated
    void addCategory(final String name, final String blurb);
    
    /**
     * Adds the specified PreferencesCategory to the panel.
     * 
     * @param category The category to be added
     */
    void addCategory(final PreferencesCategory category);
    
    /**
     * Adds the specified PreferencesCategories to the panel.
     * 
     * @param categories The categories to be added
     */
    void addCategories(final Collection<? extends PreferencesCategory> categories);    
    
    /**
     * Adds a named category to the preferences panel.
     *
     * @param parentCategory parent category
     * @param name Category name
     * @param blurb category blurb
     * @deprecated Use addCategory(PreferencesCategory) instead
     */
    @Deprecated
    void addCategory(final String parentCategory, final String name,
            final String blurb);
    
    /**
     * Replaces the option panel in a category with the specified panel, this
     * panel will be be automatically laid out.
     *
     * @param category category to replace the options panel in
     * @param panel panel to replace options panel with
     * @deprecated Should be done via core PreferencesSettings instead
     */
    @Deprecated
    void replaceOptionPanel(final String category, final JPanel panel);
    
    /**
     * Adds an option to the specified category.
     *
     * @param category category option is to be added to
     * @param name config name for the option
     * @param displayName displayable name for the option
     * @param helpText Help text to be displayed for the option
     * @param defaultValue default value
     * @deprecated Should be done via core PreferencesSettings instead
     */
    @Deprecated
    void addTextfieldOption(final String category, final String name,
            final String displayName, final String helpText,
            final String defaultValue);
    
    /**
     * Adds an option to the specified category.
     *
     * @param category category option is to be added to
     * @param name config name for the option
     * @param displayName displayable name for the option
     * @param helpText Help text to be displayed for the option
     * @param defaultValue default value
     * @deprecated Should be done via core PreferencesSettings instead
     */
    @Deprecated
    void addCheckboxOption(final String category, final String name,
            final String displayName, final String helpText,
            final boolean defaultValue);
    
    /**
     * Adds an option to the specified category.
     *
     * @param category category option is to be added to
     * @param name config name for the option
     * @param displayName displayable name for the option
     * @param helpText Help text to be displayed for the option
     * @param options Default combo box options
     * @param defaultValue default value
     * @param editable editable combo box
     * @deprecated Should be done via core PreferencesSettings instead
     */
    @Deprecated
    void addComboboxOption(final String category, final String name,
            final String displayName, final String helpText,
            final String[] options, final String defaultValue,
            final boolean editable);
    
    /**
     * Adds an option to the specified category.
     *
     * @param category category option is to be added to
     * @param name config name for the option
     * @param displayName displayable name for the option
     * @param helpText Help text to be displayed for the option
     * @param defaultValue default value
     * @deprecated Should be done via core PreferencesSettings instead
     */
    @Deprecated
    void addSpinnerOption(final String category, final String name,
            final String displayName, final String helpText,
            final int defaultValue);
    
    /**
     * Adds an option to the specified category.
     *
     * @param category category option is to be added to
     * @param name config name for the option
     * @param displayName displayable name for the option
     * @param helpText Help text to be displayed for the option
     * @param defaultValue default value
     * @param minimum minimum value
     * @param maximum maximum value
     * @param stepSize step size interval
     * @deprecated Should be done via core PreferencesSettings instead
     */
    @Deprecated
    void addSpinnerOption(final String category, final String name,
            final String displayName, final String helpText,
            final int defaultValue, final int minimum, final int maximum,
            final int stepSize);
    
    /**
     * Adds an option to the specified category.
     *
     * @param category category option is to be added to
     * @param name config name for the option
     * @param displayName displayable name for the option
     * @param helpText Help text to be displayed for the option
     * @param defaultValue default value
     * @param showIrcColours show irc colours in the colour picker
     * @param showHexColours show hex colours in the colour picker
     * @deprecated Should be done via core PreferencesSettings instead
     */
    @Deprecated
    void addColourOption(final String category, final String name,
            final String displayName, final String helpText,
            final String defaultValue, final boolean showIrcColours,
            final boolean showHexColours);
    
    /**
     * Adds an option to the specified category.
     *
     * @param category category option is to be added to
     * @param name config name for the option
     * @param displayName displayable name for the option
     * @param helpText Help text to be displayed for the option
     * @param defaultValue default value
     * @param initialState initial state
     * @param showIrcColours show irc colours in the colour picker
     * @param showHexColours show hex colours in the colour picker
     * @deprecated Should be done via core PreferencesSettings instead
     */
    @Deprecated
    void addOptionalColourOption(final String category, final String name,
            final String displayName, final String helpText,
            final String defaultValue, final boolean initialState,
            final boolean showIrcColours, final boolean showHexColours);
    
    /**
     * Saves the options in the dialog to the config.
     */
    void saveOptions();
    
    /**
     * Lays out the preferences panel and displays it.
     */
    void display();
}
