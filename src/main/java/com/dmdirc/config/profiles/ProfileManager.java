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

package com.dmdirc.config.profiles;

import com.dmdirc.events.ProfileAddedEvent;
import com.dmdirc.events.ProfileDeletedEvent;
import com.dmdirc.events.eventbus.EventBus;
import com.dmdirc.util.SystemInfo;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Manager for {@link Profile}s.
 */
@Singleton
public class ProfileManager {

    private final EventBus eventBus;
    private final List<Profile> profiles;
    private final Profile defaultProfile;

    @Inject
    public ProfileManager(final EventBus eventBus, final SystemInfo systemInfo) {
        this.eventBus = eventBus;
        profiles = new ArrayList<>();
        final String nick = systemInfo.getProperty("user.name").replace(' ', '_');
        defaultProfile = Profile.create(nick, nick, Optional.empty(), Lists.newArrayList(nick));
    }

    /**
     * Adds a profile to the manager.
     *
     * @param profile New profile to add
     */
    public void addProfile(final Profile profile) {
        profiles.add(profile);
        eventBus.publishAsync(new ProfileAddedEvent(profile));
    }

    /**
     * Removes a profile from the manager.
     *
     * @param profile Profile to be removed
     */
    public void deleteProfile(final Profile profile) {
        profiles.remove(profile);
        eventBus.publishAsync(new ProfileDeletedEvent(profile));
    }

    /**
     * Returns the available profiles in the client.
     *
     * @return Unmodifiable collection of profiles
     */
    public Collection<Profile> getProfiles() {
        return Collections.unmodifiableCollection(profiles);
    }

    /**
     * Returns the default profile if no custom profiles have been set.
     *
     * @return Default profile
     */
    public Profile getDefault() {
        return Iterables.getFirst(profiles, defaultProfile);
    }
}
