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

import com.dmdirc.actions.wrappers.Profile;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.interfaces.config.IdentityFactory;
import com.dmdirc.interfaces.ui.ProfilesDialogModel;
import com.dmdirc.interfaces.ui.ProfilesDialogModelListener;
import com.dmdirc.util.collections.ListenerList;
import com.dmdirc.util.validators.ListNotEmptyValidator;
import com.dmdirc.util.validators.NotEmptyValidator;
import com.dmdirc.util.validators.PermissiveValidator;
import com.dmdirc.util.validators.Validator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class CoreProfilesDialogModel implements ProfilesDialogModel {

    private final ListenerList listeners;
    private final IdentityFactory identityFactory;
    private final IdentityController identityController;
    private final Map<String, Profile> profiles;
    private Optional<Profile> selectedProfile = Optional.empty();
    private Optional<String> name = Optional.empty();
    private Optional<List<String>> nicknames = Optional.empty();
    private Optional<String> selectedNickname = Optional.empty();
    private Optional<String> realname = Optional.empty();
    private Optional<String> ident = Optional.empty();

    @Inject
    public CoreProfilesDialogModel(final IdentityController identityController,
            final IdentityFactory identityFactory) {
        this.identityFactory = identityFactory;
        this.identityController = identityController;
        listeners = new ListenerList();
        profiles = new HashMap<>(0);
        selectedProfile = Optional.empty();
    }

    @Override
    public void loadModel() {
        profiles.clear();
        final List<ConfigProvider> identities = identityController.getProvidersByType("profile");
        for (ConfigProvider identity : identities) {
            final Profile profile = getProfile(identity);
            profiles.put(identity.getName(), profile);
            listeners.getCallable(ProfilesDialogModelListener.class).profileAdded(profile);
        }
        setSelectedProfile(Optional.ofNullable(Iterables.getFirst(profiles.values(), null)));
    }

    private Profile getProfile(final ConfigProvider configProvider) {
        final Profile newProfile = new Profile(configProvider.getName(), identityFactory);
        newProfile.setName(configProvider.getOption("identity", "name"));
        newProfile.setRealname(configProvider.getOption("profile", "realname"));
        newProfile.setIdent(configProvider.getOption("profile", "ident"));
        newProfile.setNicknames(configProvider.getOptionList("profile", "nicknames"));
        this.name = Optional.ofNullable(configProvider.getOption("identity", "name"));
        this.nicknames = Optional.ofNullable(configProvider.getOptionList("profile", "nicknames"));
        this.realname = Optional.ofNullable(configProvider.getOption("profile", "realname"));
        this.ident = Optional.ofNullable(configProvider.getOption("profile", "ident"));
        return newProfile;
    }

    @Override
    public List<Profile> getProfileList() {
        return ImmutableList.copyOf(profiles.values());
    }

    @Override
    public Optional<Profile> getProfile(final String name) {
        checkNotNull(name, "Name cannot be null");
        return Optional.ofNullable(profiles.get(name));
    }

    @Override
    public boolean isProfileListValid() {
        return !getProfileListValidator().validate(getProfileList()).isFailure();
    }

    @Override
    public Validator<List<Profile>> getProfileListValidator() {
        return new ListNotEmptyValidator<>();
    }

    @Override
    public void addProfile(final String name, final String realname, final String ident,
            final List<String> nicknames) {
        checkNotNull(name, "Name cannot be null");
        checkArgument(!profiles.containsKey(name), "Name cannot already exist");
        final Profile profile = new Profile(name, identityFactory);
        profile.setRealname(realname);
        profile.setIdent(ident);
        profile.setNicknames(Lists.newArrayList(nicknames));
        profiles.put(name, profile);
        listeners.getCallable(ProfilesDialogModelListener.class).profileAdded(profile);
    }

    @Override
    public void editProfile(final String name, final String realname, final String ident,
            final List<String> nicknames) {
        checkNotNull(name, "Name cannot be null");
        checkArgument(profiles.containsKey(name), "Name must already exist");
        final Profile profile = profiles.get(name);
        profile.setRealname(realname);
        profile.setIdent(ident);
        profile.setNicknames(Lists.newArrayList(nicknames));
        listeners.getCallable(ProfilesDialogModelListener.class).profileEdited(profile, profile);
    }

    @Override
    public void renameProfile(final String oldName, final String newName) {
        renameProfile(oldName, newName, false);
    }

    public void renameProfile(final String oldName, final String newName, final boolean selection) {
        checkNotNull(oldName, "Oldname cannot be null");
        checkNotNull(newName, "Newname cannot be null");
        checkArgument(profiles.containsKey(oldName), "Old name must exist");
        checkArgument(!profiles.containsKey(newName), "New name must not exist");
        final Profile profile = profiles.get(oldName);
        final Profile newProfile = new Profile(newName, identityFactory);
        profile.setRealname(profile.getRealname());
        profile.setIdent(profile.getIdent());
        profile.setNicknames(Lists.newArrayList(profile.getNicknames()));
        final Profile oldProfile = profiles.remove(oldName);
        profiles.put(newName, newProfile);
        listeners.getCallable(ProfilesDialogModelListener.class).profileRenamed(oldProfile,
                newProfile);
    }

    @Override
    public void removeProfile(final String name) {
        checkNotNull(name, "Name cannot be null");
        checkArgument(profiles.containsKey(name), "profile must exist in list");
        final Profile profile = profiles.remove(name);
        if (getSelectedProfile().isPresent() && getSelectedProfile().get().equals(profile)) {
            setSelectedProfile(Optional.<Profile>empty());
        }
        listeners.getCallable(ProfilesDialogModelListener.class).profileRemoved(profile);
    }

    @Override
    public void save() {
        setSelectedProfile(Optional.<Profile>empty());
        final List<ConfigProvider> identities = Lists.newArrayList(
                identityController.getProvidersByType("profile"));
        for (ConfigProvider identity : identities) {
            try {
                identity.delete();
            } catch (IOException ex) {
                //Can't handle and will be dealt with when profiles are redone.
            }
        }
        for (Profile profile : profiles.values()) {
            profile.save();
        }
    }

    @Override
    public void setSelectedProfile(final Optional<Profile> profile) {
        checkNotNull(profile, "profile cannot be null");
        if (profile.isPresent()) {
            checkArgument(profiles.containsValue(profile.get()), "Profile must exist in list");
        }
        if (selectedProfile.isPresent()) {
            if (!Optional.ofNullable(selectedProfile.get().getRealname()).equals(realname)
                    || !Optional.ofNullable(selectedProfile.get().getIdent()).equals(ident)
                    || !Optional.ofNullable(selectedProfile.get().getNicknames()).equals
                    (nicknames)) {
                editProfile(selectedProfile.get().getName(), realname.get(),
                        ident.get(), nicknames.get());
            }
            if (!Optional.ofNullable(selectedProfile.get().getName()).equals(name)) {
                renameProfile(selectedProfile.get().getName(), name.get(), true);
            }
        }
        selectedProfile = profile;
        if (selectedProfile.isPresent()) {
            name = Optional.ofNullable(selectedProfile.get().getName());
            realname = Optional.ofNullable(selectedProfile.get().getRealname());
            ident = Optional.ofNullable(selectedProfile.get().getIdent());
            nicknames = Optional.ofNullable(selectedProfile.get().getNicknames());
        } else {
            name = Optional.empty();
            realname = Optional.empty();
            ident = Optional.empty();
            nicknames = Optional.empty();
        }
        listeners.getCallable(ProfilesDialogModelListener.class).profileSelectionChanged(profile);
    }

    @Override
    public Optional<Profile> getSelectedProfile() {
        return selectedProfile;
    }

    @Override
    public Optional<String> getSelectedProfileName() {
        if (selectedProfile.isPresent()) {
            return name;
        }
        return Optional.empty();
    }

    @Override
    public void setSelectedProfileName(final Optional<String> name) {
        checkNotNull(name, "Name cannot be null");
        checkState(selectedProfile.isPresent(), "There must be a profile selected");
        this.name = name;
        listeners.getCallable(ProfilesDialogModelListener.class)
                .selectedProfileEdited(name, realname, ident, nicknames);
    }

    @Override
    public Validator<String> getSelectedProfileNameValidator() {
        return new EditSelectedProfileNameValidator(this);
    }

    @Override
    public Validator<String> getNewProfileNameValidator() {
        return new NewProfileNameValidator(this);
    }

    @Override
    public boolean isSelectedProfileNameValid() {
        return !getSelectedProfileName().isPresent() ||
                !getSelectedProfileNameValidator().validate(getSelectedProfileName().get())
                        .isFailure();
    }

    @Override
    public Optional<String> getSelectedProfileRealname() {
        if (selectedProfile.isPresent()) {
            return realname;
        }
        return Optional.empty();
    }

    @Override
    public void setSelectedProfileRealname(final Optional<String> realname) {
        checkNotNull(realname, "Realname cannot be null");
        checkState(selectedProfile.isPresent(), "There must be a profile selected");
        this.realname = realname;
        listeners.getCallable(ProfilesDialogModelListener.class)
                .selectedProfileEdited(name, realname, ident, nicknames);
    }

    @Override
    public Validator<String> getSelectedProfileRealnameValidator() {
        return new NotEmptyValidator();
    }

    @Override
    public boolean isSelectedProfileRealnameValid() {
        return !getSelectedProfileRealname().isPresent() ||
                !getSelectedProfileRealnameValidator().validate(getSelectedProfileRealname().get())
                        .isFailure();
    }

    @Override
    public Optional<String> getSelectedProfileIdent() {
        if (selectedProfile.isPresent()) {
            return ident;
        }
        return Optional.empty();
    }

    @Override
    public void setSelectedProfileIdent(final Optional<String> ident) {
        checkNotNull(ident, "Ident cannot be null");
        checkState(selectedProfile.isPresent(), "There must be a profile selected");
        this.ident = ident;
        listeners.getCallable(ProfilesDialogModelListener.class)
                .selectedProfileEdited(name, realname, ident, nicknames);
    }

    @Override
    public Validator<String> getSelectedProfileIdentValidator() {
        return new PermissiveValidator<>();
    }

    @Override
    public boolean isSelectedProfileIdentValid() {
        return !getSelectedProfileIdent().isPresent() ||
                !getSelectedProfileIdentValidator().validate(getSelectedProfileIdent().get())
                        .isFailure();
    }

    @Override
    public Optional<List<String>> getSelectedProfileNicknames() {
        if (selectedProfile.isPresent()) {
            return nicknames;
        }
        return Optional.empty();
    }

    @Override
    public void setSelectedProfileNicknames(final Optional<List<String>> nicknames) {
        checkNotNull(nicknames, "nicknames cannot be null");
        checkState(selectedProfile.isPresent(), "There must be a profile selected");
        if (nicknames.isPresent()) {
            this.nicknames = nicknames;
        } else {
            this.nicknames = Optional.ofNullable((List<String>) Lists.
                    newArrayList(nicknames.get()));
        }
        listeners.getCallable(ProfilesDialogModelListener.class)
                .selectedProfileEdited(name, realname, ident, nicknames);
    }

    @Override
    public Validator<List<String>> getSelectedProfileNicknamesValidator() {
        return new ListNotEmptyValidator<>();
    }

    @Override
    public boolean isSelectedProfileNicknamesValid() {
        return !getSelectedProfileNicknames().isPresent() || !getSelectedProfileNicknamesValidator()
                .validate(getSelectedProfileNicknames().get()).isFailure();
    }

    @Override
    public void addSelectedProfileNickname(final String nickname) {
        checkNotNull(nickname, "Nickname cannot be null");
        checkState(selectedProfile.isPresent(), "There must be a profile selected");
        checkState(nicknames.isPresent(), "There must be nicknames present");
        checkArgument(!nicknames.get().contains(nickname), "New nickname must not exist");
        nicknames.get().add(nickname);
        listeners.getCallable(ProfilesDialogModelListener.class).selectedProfileNicknameAdded(
                nickname);
    }

    @Override
    public void removeSelectedProfileNickname(final String nickname) {
        checkNotNull(nickname, "Nickname cannot be null");
        checkState(selectedProfile.isPresent(), "There must be a profile selected");
        checkState(nicknames.isPresent(), "There must be nicknames present");
        checkArgument(nicknames.get().contains(nickname), "Nickname must exist");
        nicknames.get().remove(nickname);
        listeners.getCallable(ProfilesDialogModelListener.class).selectedProfileNicknameRemoved(
                nickname);
    }

    @Override
    public Validator<String> getSelectedProfileAddNicknameValidator() {
        return new AddNicknameValidator(this);
    }

    @Override
    public void editSelectedProfileNickname(final String oldName, final String newName) {
        checkNotNull(oldName, "Nickname cannot be null");
        checkNotNull(newName, "Nickname cannot be null");
        checkState(selectedProfile.isPresent(), "There must be a profile selected");
        checkState(nicknames.isPresent(), "There must be nicknames present");
        checkArgument(nicknames.get().contains(oldName), "Old nickname must exist");
        checkArgument(!nicknames.get().contains(newName), "New nickname must not exist");
        final int index = nicknames.get().indexOf(oldName);
        nicknames.get().set(index, newName);
        listeners.getCallable(ProfilesDialogModelListener.class).selectedProfileNicknameEdited(
                oldName, newName);
    }

    @Override
    public Validator<String> getSelectedProfileEditNicknameValidator() {
        return new EditSelectedNicknameValidator(this);
    }

    @Override
    public Optional<String> getSelectedProfileSelectedNickname() {
        if (selectedProfile.isPresent()) {
            return selectedNickname;
        }
        return Optional.empty();
    }

    @Override
    public void setSelectedProfileSelectedNickname(final Optional<String> selectedNickname) {
        checkNotNull(selectedNickname, "Nickname cannot be null");
        checkState(selectedProfile.isPresent(), "There must be a profile selected");
        checkState(nicknames.isPresent(), "There must be nicknames present");
        if (selectedNickname.isPresent()) {
            checkArgument(nicknames.get().contains(selectedNickname.get()),
                    "Nickname must exist in nicknames list");
        }
        this.selectedNickname = selectedNickname;
        listeners.getCallable(ProfilesDialogModelListener.class)
                .selectedNicknameChanged(selectedNickname);
    }

    @Override
    public void addListener(final ProfilesDialogModelListener listener) {
        checkNotNull(listener, "Listener must not be null");
        listeners.add(ProfilesDialogModelListener.class, listener);
    }

    @Override
    public void removeListener(final ProfilesDialogModelListener listener) {
        checkNotNull(listener, "Listener must not be null");
        listeners.remove(ProfilesDialogModelListener.class, listener);
    }

    @Override
    public boolean canSwitchProfiles() {
        return !selectedProfile.isPresent() ||
                isSelectedProfileIdentValid() && isSelectedProfileNameValid() &&
                        isSelectedProfileNicknamesValid() && isSelectedProfileRealnameValid();
    }

    @Override
    public boolean isSaveAllowed() {
        return isProfileListValid()
                && isSelectedProfileIdentValid()
                && isSelectedProfileNameValid()
                && isSelectedProfileNicknamesValid()
                && isSelectedProfileRealnameValid();
    }

}
