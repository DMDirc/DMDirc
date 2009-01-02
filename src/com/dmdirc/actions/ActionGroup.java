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

package com.dmdirc.actions;

import com.dmdirc.config.prefs.PreferencesSetting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Represents a group of actions, along with their meta-data.
 * 
 * @author chris
 */
public class ActionGroup implements Iterable<Action> {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;    
    
    /** The actions in this group. */
    private final List<Action> actions = new ArrayList<Action>();
    
    /** The name of this action group. */
    private final String name;
    
    /** The description of this action group. */
    private String description = null;
    
    /** The author of this action group. */
    private String author = null;
    
    /** The component number of this action group (for updating). */
    private int component = -1;
    
    /** The version number of this action group. */
    private int version = -1;
    
    /** A list of settings used by this action group. */
    private final Map<String, PreferencesSetting> settings
            = new HashMap<String, PreferencesSetting>();

    /**
     * Creates a new instance of ActionGroup.
     * 
     * @param name The name of this action group
     */
    public ActionGroup(final String name) {
        super();
        
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
     * @return This action group's version number, or -1 if none is specified.
     */
    public int getVersion() {
        return version;
    }

    /**
     * Sets the version of this action group.
     * 
     * @param version This action group's new version number.
     */
    public void setVersion(final int version) {
        this.version = version;
    }

    /**
     * Retrieves the addon site component number for this action group.
     * 
     * @return The component number for this action group, or -1 if none is
     * specified.
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
     * Determines the size of this group.
     * 
     * @return The size of this group
     */
    public int size() {
        return actions.size();
    }

    /**
     * Removes the specified action from this group.
     * 
     * @param action The action to be removed
     */
    public void remove(final Action action) {
        actions.remove(action);
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<Action> iterator() {
        return actions.iterator();
    }

    /**
     * Retrieves the action at the specified index.
     * 
     * @param index The index of the action to return
     * @return The action at the specified index
     */
    public Action get(final int index) {
        return actions.get(index);
    }

    /**
     * Determines if this group contains the specified action.
     * 
     * @param action The action to search for
     * @return True if the action is contained in this list, false otherwise
     */
    public boolean contains(final Action action) {
        return actions.contains(action);
    }

    /**
     * Removes all actions from this group, and removes all meta-data.
     */
    public void clear() {
        for (Action action : new ArrayList<Action>(actions)) {
            remove(action);
        }
        
        settings.clear();
        description = null;
        author = null;
        component = -1;
        version = -1;
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
        return new ArrayList<Action>(actions);
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
