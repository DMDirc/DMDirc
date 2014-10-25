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

package com.dmdirc.ui.core.profiles;

import com.dmdirc.config.profiles.Profile;
import com.dmdirc.interfaces.ui.ProfilesDialogModelListener;

import java.util.List;
import java.util.Optional;


public class ProfilesDialogModelAdapter implements ProfilesDialogModelListener {

    @Override
    public void profileAdded(final Profile profile) {
    }

    @Override
    public void profileRemoved(final Profile profile) {
    }

    @Override
    public void profileEdited(final Profile oldProfile, final Profile newProfile) {
    }

    @Override
    public void profileRenamed(final Profile oldProfile, final Profile newProfile) {
    }

    @Override
    public void profileSelectionChanged(final Optional<Profile> profile) {
    }

    @Override
    public void selectedProfileEdited(final Optional<String> name, final Optional<String> realname,
            final Optional<String> ident, final Optional<List<String>> nicknames) {
    }

    @Override
    public void selectedNicknameChanged(final Optional<String> nickname) {
    }

    @Override
    public void selectedProfileNicknameEdited(final String oldNickname, final String newNickname) {
    }

    @Override
    public void selectedProfileNicknameAdded(final String nickname) {
    }

    @Override
    public void selectedProfileNicknameRemoved(final String nickname) {
    }

}
