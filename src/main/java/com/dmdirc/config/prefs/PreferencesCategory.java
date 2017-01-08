/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.config.prefs;

import com.dmdirc.util.collections.ListenerList;

import com.google.common.base.MoreObjects;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents one category of preferences. Categories can contain 0 or more subcategories, and
 * either 0 or more PreferencesSettings or exactly 1 PreferencesInterface object.
 */
public class PreferencesCategory {

    private static final Logger LOG = LoggerFactory.getLogger(PreferencesCategory.class);
    /** The title (name) of this category. */
    private final String title;
    /** A description of this category. */
    private final String description;
    /** The icon to use for this category. */
    private final String icon;
    /** The warning displayed for this category, if any. */
    private String warning;
    /** Whether or not this category is inline. */
    private boolean isInline;
    /** Whether or not to show inline categories before settings. */
    private boolean inlineBefore = true;
    /** Our parent category, if known. */
    private PreferencesCategory parent;
    /** A list of settings in this category. */
    private final List<PreferencesSetting> settings = new ArrayList<>();
    /** A list of subcategories of this category. */
    private final List<PreferencesCategory> subcats = new ArrayList<>();
    /** The replacement object to use for this category. */
    private final PreferencesInterface object;
    /** A list of listeners who are interested in this category. */
    private final ListenerList listeners = new ListenerList();

    /**
     * Creates a new preferences category that contains settings.
     *
     * @param title       The title of this preferences category
     * @param description The description of this category
     */
    public PreferencesCategory(final String title, final String description) {
        this(title, description, null, null);
    }

    /**
     * Creates a new preferences category that contains settings.
     *
     * @since 0.6.3m1
     * @param title       The title of this preferences category
     * @param description The description of this category
     * @param icon        The icon to use for this category
     */
    public PreferencesCategory(final String title, final String description,
            final String icon) {
        this(title, description, icon, null);
    }

    /**
     * Creates a new preferences category that contains an object.
     *
     * @param title       The title of this preferences category
     * @param description The description of this category
     * @param object      The replacement object for this category
     */
    public PreferencesCategory(final String title, final String description,
            final PreferencesInterface object) {
        this(title, description, null, object);
    }

    /**
     * Creates a new preferences category that contains an object.
     *
     * @since 0.6.3m1
     * @param title       The title of this preferences category
     * @param description The description of this category
     * @param icon        The icon to use for this category
     * @param object      The replacement object for this category
     */
    public PreferencesCategory(final String title, final String description,
            final String icon, final PreferencesInterface object) {
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.object = object;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
    }

    public boolean isInline() {
        return isInline;
    }

    public boolean isInlineBefore() {
        return inlineBefore;
    }

    public List<PreferencesSetting> getSettings() {
        return settings;
    }

    public List<PreferencesCategory> getSubcats() {
        return subcats;
    }

    public PreferencesInterface getObject() {
        return object;
    }

    public String getWarning() {
        return warning;
    }

    public void setWarning(final String warning) {
        this.warning = warning;
    }

    public PreferencesCategory getParent() {
        return parent;
    }

    public void setParent(final PreferencesCategory parent) {
        this.parent = parent;
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
     * Sets this category to show inline categories after settings, rather than before.
     *
     * @return A reference to this category, for convenience
     */
    public PreferencesCategory setInlineAfter() {
        inlineBefore = false;
        return this;
    }

    /**
     * Adds the specified setting to this category.
     *
     * @param setting The setting to be added
     */
    public void addSetting(final PreferencesSetting setting) {
        if (hasObject()) {
            throw new IllegalArgumentException("Can't add settings to a "
                    + "category that uses a replacement object");
        }

        settings.add(setting);
    }

    /**
     * Adds the specified subcategory to this category.
     *
     * @param subcategory The category to be added
     */
    public void addSubCategory(final PreferencesCategory subcategory) {
        if (isInline() && !subcategory.isInline()) {
            throw new IllegalArgumentException("Can't add non-inline "
                    + "subcategories to inline ones");
        }

        subcategory.setParent(this);
        subcats.add(subcategory);
    }

    /**
     * Determines if this category has a replacement object.
     *
     * @return True if the category has a replacement object, false otherwise
     */
    public boolean hasObject() {
        return object != null;
    }

    /**
     * Retrieves the full path of this category. A category's path is the name of each of its parent
     * categories, starting with the furthest up the hierarchy, separated by '→' characters.
     *
     * @return This category's path
     *
     * @since 0.6.3m1
     */
    public String getPath() {
        return (parent == null ? "" : parent.getPath() + " → ") + getTitle();
    }

    /**
     * Saves all the settings in this category.
     *
     * @return Is a restart needed after saving?
     */
    public boolean save() {
        LOG.debug("{} save method called", getTitle());

        boolean restart = false;
        for (PreferencesSetting setting : settings) {
            LOG.trace("{}: saving setting '{}'", getTitle(), setting.getTitle());
            if (setting.save() && setting.isRestartNeeded()) {
                restart = true;
            }
        }

        for (PreferencesCategory child : getSubcats()) {
            restart |= child.save();
        }

        return restart;
    }

    /**
     * Dismisses all the settings in this category.
     */
    public void dismiss() {
        settings.forEach(PreferencesSetting::dismiss);
        getSubcats().forEach(PreferencesCategory::dismiss);
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

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("title", getTitle()).toString();
    }

}
