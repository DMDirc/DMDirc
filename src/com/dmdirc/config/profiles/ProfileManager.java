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

package com.dmdirc.config.profiles;

import com.dmdirc.util.validators.Validator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Manager for {@link Profile}s.
 */
@Singleton
public class ProfileManager {

    private final Collection<Profile> profiles;

    @Inject
    public ProfileManager() {
        profiles = new ArrayList<>();
    }

    /**
     * Adds a profile to the manager.
     *
     * @param profile New profile to add
     */
    public void addProfile(final Profile profile) {
        profiles.add(profile);
    }

    /**
     * Removes a profile from the manager.
     *
     * @param profile Profile to be removed
     */
    public void deleteProfile(final Profile profile) {
        profiles.remove(profile);
    }

    /**
     * Returns the available profiles in the client.
     *
     * @return Unmodifiable collection of profiles
     */
    public Collection<Profile> getProfiles() {
        return Collections.unmodifiableCollection(profiles);
    }
}
