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

import com.dmdirc.config.IdentityManager;
import com.dmdirc.interfaces.Migrator;
import com.dmdirc.interfaces.config.ConfigProvider;

import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

/**
 * Migrates identity based performs to config based performs.
 */
public class IdentitiesProfileMigrator implements Migrator {

    private static final String DOMAIN_PROFILE = "profile";
    private final IdentityManager identityManager;
    private final ProfileManager profileManager;

    @Inject
    public IdentitiesProfileMigrator(final IdentityManager identityManager,
            final ProfileManager profileManager) {
        this.identityManager = identityManager;
        this.profileManager = profileManager;
    }

    @Override
    public boolean needsMigration() {
        return !identityManager.getProvidersByType("profile").isEmpty();
    }

    @Override
    public void migrate() {
        final List<ConfigProvider> profiles = Lists.newArrayList(
                identityManager.getProvidersByType("profile"));
        for(ConfigProvider p : profiles) {
            final Optional<String> ident;
            if (p.hasOptionString("profile", "ident")) {
                ident = Optional.of(p.getOption(DOMAIN_PROFILE, "ident"));
            } else {
                ident = Optional.empty();
            }
            profileManager.addProfile(
                    new Profile(p.getName(), p.getOption(DOMAIN_PROFILE, "realname"), ident,
                            p.getOptionList(DOMAIN_PROFILE, "nicknames")));
            try {
                p.delete();
            } catch (IOException e) {
                //Can't do anything, ignore
            }
        }
    }
}
