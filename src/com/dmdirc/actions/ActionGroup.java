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

package com.dmdirc.actions;

import com.dmdirc.Precondition;
import com.dmdirc.config.prefs.PreferencesSetting;
import com.dmdirc.updater.Version;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents a group of actions, along with their meta-data.
 */
public class ActionGroup implements Iterable<Action> {

    /** The actions in this group. */
    private final List<Action> actions = new ArrayList<>();
    /** The name of this action group. */
    private final String name;
    /** The description of this action group. */
    private String description;
    /** The author of this action group. */
    private String author;
    /** The component number of this action group (for updating). */
    private int component = -1;
    /** The version of this action group. */
    private Version version;
    /** A list of settings used by this action group. */
    private final Map<String, PreferencesSetting> settings = new HashMap<>();
    /** Action manager. */
    private final ActionManager actionManager;

    /**
     * Creates a new instance of ActionGroup.
     *
     * @param actionManager The action manager used to manager this group.
     * @param name          The name of this action group
     */
    public ActionGroup(final ActionManager actionManager, final String name) {
        this.actionManager = actionManager;
        this.name = name;
    }

    /**
     * Retrieves the author of this ActionGroup.
     *
     * @return This action group's author, or null if the author isn't specified
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Sets the author of this ActionGroup.
     *
     * @param author The new author for this action group
     */
    public void setAuthor(final String author) {
        this.author = author;
    }

    /**
     * Retrieves the description of this action group.
     *
     * @return This action group's description, or null if none is specified
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description for this action group.
     *
     * @param description The new description for this action group
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Retrieves the name of this action group.
     *
     * @return This action group's name
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieves a map settings used by this action group.
     *
     * @return A map of setting names to values
     */
    public Map<String, PreferencesSetting> getSettings() {
        return settings;
    }

    /**
     * Retrieves the version number of this action group.
     *
     * @return This action group's version number, or null if none is specified.
     *
     * @since 0.6.4
     */
    public Version getVersion() {
        return version;
    }

    /**
     * Sets the version of this action group.
     *
     * @param version This action group's new version.
     *
     * @since 0.6.4
     */
    public void setVersion(final Version version) {
        this.version = version;
    }

    /**
     * Retrieves the addon site component number for this action group.
     *
     * @return The component number for this action group, or -1 if none is specified.
     */
    public int getComponent() {
        return component;
    }

    /**
     * Sets the addon site component number for this action group.
     *
     * @param component The component number for this action group
     */
    public void setComponent(final int component) {
        this.component = component;
    }

    /**
     * Removes the specified action from this group.
     *
     * @param action The action to be removed
     */
    public void remove(final Action action) {
        actions.remove(action);
    }

    @Override
    public Iterator<Action> iterator() {
        return actions.iterator();
    }

    /**
     * Removes all actions from this group, and removes all meta-data.
     */
    public void clear() {
        new ArrayList<>(actions).forEach(this::remove);

        settings.clear();
        description = null;
        author = null;
        version = null;
        component = -1;
    }

    /**
     * Adds the specified action to this group.
     *
     * @param action The action to be added
     */
    public void add(final Action action) {
        actions.add(action);
    }

    /**
     * Retrieves a copy of the list of all actions in this group.
     *
     * @return A list of actions in this group
     */
    public List<Action> getActions() {
        return new ArrayList<>(actions);
    }

    /**
     * Deletes an action from this group.
     *
     * @param action The action to be deleted
     *
     * @since 0.6.3
     */
    @Precondition({
        "The specified action is non-null",
        "The specified action exists in this group"
    })
    @SuppressWarnings("deprecation")
    public void deleteAction(final Action action) {
        checkNotNull(action);
        checkArgument(actions.contains(action));

        actionManager.removeAction(action);
        action.delete();
    }

    /**
     * Determines if this action group is delible or not.
     *
     * @return True if the group may be deleted, false if it may not.
     */
    public boolean isDelible() {
        return true;
    }

}
