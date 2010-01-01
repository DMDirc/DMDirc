/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.ui_swing.dialogs.profiles;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultListModel;

/** Profile list model. */
public class ProfileListModel extends DefaultListModel implements Iterable<Profile> {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Profile list. */
    private final List<Profile> profiles;

    /** Creates a new profile list model. */
    public ProfileListModel() {
        this(new ArrayList<Profile>());
    }

    /**
     * Creates a new profile list model.
     *
     * @param profiles Profile list to use
     */
    public ProfileListModel(final List<Profile> profiles) {
        this.profiles = profiles;
    }

    /** {@inheritDoc} */
    @Override
    public int getSize() {
        return profiles.size();
    }

    /** {@inheritDoc} */
    @Override
    public Profile getElementAt(int index) {
        return profiles.get(index);
    }

    /**
     * Removes the index from the model.
     *
     * @param index Index to remove
     *
     * @return the profile that was removed
     */
    @Override
    public Profile remove(int index) {
        final Profile returnValue = profiles.remove(index);

        fireIntervalRemoved(this, index, index);

        return returnValue;
    }

    /**
     * Removes the object from the model.
     *
     * @param p object to remove from the model
     *
     * @return true if the object was removed
     */
    public boolean remove(Profile p) {
        final int index = profiles.indexOf(p);
        final boolean returnValue = profiles.remove(p);

        fireIntervalRemoved(this, index, index);

        return returnValue;
    }

    /**
     * Checks if the model is empty.
     *
     * @return true if the model is empty
     */
    @Override
    public boolean isEmpty() {
        return profiles.isEmpty();
    }

    /**
     * Returns the index of the object.
     *
     * @param p object to find the index of
     *
     * @return index of the specified object
     */
    public int indexOf(Profile p) {
        return profiles.indexOf(p);
    }

    /**
     * Returns the profile at the index.
     *
     * @param index index to retrieve
     *
     * @return the profile that was removed
     */
    @Override
    public Profile get(int index) {
        return profiles.get(index);
    }
    
    /**
     * Returns a list of all profiles
     * 
     * @return Profile list
     */
    public List<Profile> getProfiles() {
        return new ArrayList<Profile>(profiles);
    }

    /**
     *
     * Checks if the model contains the profile
     *
     * @param p profile to check for
     *
     * @return true if the model contains the profile
     */
    public boolean contains(Profile p) {
        return profiles.contains(p);
    }

    /**
     *
     * Checks if the model contains a profile with the specified name.
     *
     * @param name name to match against
     *
     * @return true if the model contains a profile with the specified name
     */
    public boolean contains(String name) {
        synchronized (profiles) {
            for (Profile profile : profiles) {
                if (profile.getName().equals(name)) {
                    return true;
                }
            }

            return false;
        }
    }

    /**
     * Clears the model.
     */
    @Override
    public void clear() {
        final int size = profiles.size();
        profiles.clear();
        fireIntervalRemoved(this, 0, size);
    }

    /**
     * Adds a profile at the index
     *
     * @param index index to add the profile
     * @param element profile to add
     */
    public void add(int index, Profile element) {
        profiles.add(index, element);

        fireIntervalAdded(this, index, index);
    }

    /**
     * Adds the profile to the model
     *
     * @param p profile to add
     *
     * @return true if the item was added
     */
    public boolean add(Profile p) {
        final boolean returnValue = profiles.add(p);
        final int index = profiles.indexOf(p);

        fireIntervalAdded(this, index, index);

        return returnValue;
    }

    /**
     * Returns an iterator for this model.
     *
     * @return Iterator for the model
     */
    @Override
    public Iterator<Profile> iterator() {
        return profiles.iterator();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        return profiles.equals(obj);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return profiles.hashCode();
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return profiles.toString();
    }
}
