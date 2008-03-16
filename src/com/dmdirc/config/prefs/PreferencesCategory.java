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

import com.dmdirc.util.ListenerList;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents one category of preferences. Categories can contain 0 or more
 * subcategories, and either 0 or more PreferencesSettings or exactly 1
 * PreferencesInterface object.
 * 
 * @author chris
 */
public class PreferencesCategory {
        
    /** The title (name) of this category. */
    private final String title;
    
    /** A description of this category. */
    private final String description;
    
    /** Whether or not this category is inline. */
    private boolean isInline = false;
    
    /** Whether or not to show inline categories before settings. */
    private boolean inlineBefore = true;
    
    /** A list of settings in this category. */
    private final List<PreferencesSetting> settings = new ArrayList<PreferencesSetting>();
    
    /** A list of subcategories of this category. */
    private final List<PreferencesCategory> subcats = new ArrayList<PreferencesCategory>();
    
    /** The replacement object to use for this category. */
    private final PreferencesInterface object;
    
    /** A list of listeners who are interested in this category. */
    private final ListenerList listeners = new ListenerList();

    /**
     * Creates a new preferences category that contains settings.
     * 
     * @param title The title of this preferences category
     * @param description The description of this category
     */
    public PreferencesCategory(final String title, final String description) {
        this(title, description, null);
    }
    
    /**
     * Creates a new preferences category that contains an object.
     * 
     * @param title The title of this preferences category
     * @param description The description of this category
     * @param object The replacement object for this category
     */
    public PreferencesCategory(final String title, final String description,
            final PreferencesInterface object) {
        this.title = title;
        this.description = description;
        this.object = object;
    }
    
    /**
     * Sets this as an inline category.
     * 
     * @return A reference to this category, for convenience
     */
    public PreferencesCategory setInline() {
        isInline = true;
        return this;
    }
    
    /**
     * Sets this category to show inline categories after settings, rather than
     * before.
     * 
     * @return A reference to this category, for convenience
     */
    public PreferencesCategory setInlineAfter() {
        inlineBefore = false;
        return this;
    }
    
    /**
     * Determines if this category is meant to be displayed inline or not.
     * 
     * @return True if this category should be shown inline, false otherwise
     */
    public boolean isInline() {
        return isInline;
    }
    
    /**
     * Determines whether this category wants inline subcats to be displayed
     * before the settings, or after.
     * 
     * @return True if subcats should be displayed first, false otherwise.
     */
    public boolean isInlineBefore() {
        return inlineBefore;
    }
    
    /**
     * Adds the specified setting to this category.
     * 
     * @param setting The setting to be added
     */
    public void addSetting(final PreferencesSetting setting) {
        if (hasObject()) {
            throw new IllegalArgumentException("Can't add settings to a " +
                    "category that uses a replacement object");
        }
        
        settings.add(setting);
    }
    
    /**
     * Adds the specified subcategory to this category.
     * 
     * @param subcategory The category to be asdded
     */
    public void addSubCategory(final PreferencesCategory subcategory) {
        if (isInline() && !subcategory.isInline()) {
            throw new IllegalArgumentException("Can't add non-inline " +
                    "subcategories to inline ones");
        }
        
        subcats.add(subcategory);
    }

    /**
     * Retrieves the description of this category.
     * 
     * @return This category's description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Retrieves the settings in this category.
     * 
     * @return This category's settings
     */
    public List<PreferencesSetting> getSettings() {
        return settings;
    }

    /**
     * Retrieves the subcategories of this category.
     * 
     * @return This category's subcategories
     */
    public List<PreferencesCategory> getSubcats() {
        return subcats;
    }

    /**
     * Retrieves the title of this category.
     * 
     * @return This category's title
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Determines if this category has a replacement object.
     * 
     * @return True if the category has a replacement object, false otherwise
     * @see #getObject()
     */
    public boolean hasObject() {
        return object != null;
    }
    
    /**
     * Retrieves this category's replacement object.
     * 
     * @return This category's replacement object.
     * @see #hasObject()
     */
    public PreferencesInterface getObject() {
        return object;
    }
    
    /**
     * Registers a change listener for this category.
     * 
     * @param listener The listener to be added
     */
    public void addChangeListener(final CategoryChangeListener listener) {
        listeners.add(CategoryChangeListener.class, listener);
    }
    
    /**
     * Removes a change listener from this category.
     * 
     * @param listener The listener to be added
     */
    public void removeChangeListener(final CategoryChangeListener listener) {
        listeners.remove(CategoryChangeListener.class, listener);
    }
    
    /**
     * Informs all registered listeners that this category has been selected.
     */
    public void fireCategorySelected() {
        for (CategoryChangeListener listener : listeners.get(CategoryChangeListener.class)) {
            listener.categorySelected(this);
        }
    }
    
    /**
     * Informs all registered listeners that this category has been deselected.
     */
    public void fireCategoryDeselected() {
        for (CategoryChangeListener listener : listeners.get(CategoryChangeListener.class)) {
            listener.categoryDeselected(this);
        }
    }    

}
