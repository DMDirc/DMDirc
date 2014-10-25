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

import com.dmdirc.actions.wrappers.Profile;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.interfaces.config.ConfigProviderListener;
import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.interfaces.config.IdentityFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

/**
 * Manager for {@link Profile}s.
 */
public class ProfileManager implements ConfigProviderListener {

    private final IdentityFactory identityFactory;
    private final IdentityController identityController;
    private final Collection<Profile> profiles;

    @Inject
    public ProfileManager(final IdentityController identityController,
            final IdentityFactory identityFactory) {
        this.identityFactory = identityFactory;
        this.identityController = identityController;
        profiles = new ArrayList<>();
        final List<ConfigProvider> identities = identityController.getProvidersByType("profile");
        profiles.addAll(identities.stream()
                .map(identity -> new Profile(identityFactory, identity))
                .collect(Collectors.toList()));
    }

    /**
     * Starts this manager, registering listeners.
     */
    public void start() {
        identityController.registerIdentityListener(this);
    }

    /**
     * Stops this manager, unregistering listeners.
     */
    public void stop() {
        identityController.unregisterIdentityListener(this);
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

    @Override
    public void configProviderAdded(final ConfigProvider configProvider) {
        if (configProvider.isProfile()) {
            profiles.removeIf(p-> p.equalsConfigProvider(configProvider));
        }
    }

    @Override
    public void configProviderRemoved(final ConfigProvider configProvider) {
        if (configProvider.isProfile()) {
            profiles.add(new Profile(identityFactory, configProvider));
        }
    }
}
