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

package com.dmdirc.config.profiles;

import com.dmdirc.interfaces.SystemLifecycleComponent;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Life cycle manager for the profile manager.
 */
@Singleton
public class ProfileManagerLifeCycleManager implements SystemLifecycleComponent {

    private final ProfileManager profileManager;
    private final ProfileStore profileStore;

    @Inject
    public ProfileManagerLifeCycleManager(final ProfileManager profileManager,
            final ProfileStore profileStore) {
        this.profileManager = profileManager;
        this.profileStore = profileStore;
    }

    @Override
    public void startUp() {
        profileStore.readProfiles().forEach(profileManager::addProfile);
    }

    @Override
    public void save() {
        profileStore.writeProfiles(profileManager.getProfiles());
    }

}
