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

package com.dmdirc.ui.core.profiles;

import com.dmdirc.config.profiles.Profile;
import com.dmdirc.config.profiles.ProfileManager;
import com.dmdirc.interfaces.ui.ProfilesDialogModel;
import com.dmdirc.interfaces.ui.ProfilesDialogModelListener;
import com.dmdirc.util.collections.ListenerList;
import com.dmdirc.util.validators.FileNameValidator;
import com.dmdirc.util.validators.IdentValidator;
import com.dmdirc.util.validators.ListNotEmptyValidator;
import com.dmdirc.util.validators.NotEmptyValidator;
import com.dmdirc.util.validators.PermissiveValidator;
import com.dmdirc.util.validators.Validator;
import com.dmdirc.util.validators.ValidatorChain;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class CoreProfilesDialogModel implements ProfilesDialogModel {

    private final ListenerList listeners;
    private final ProfileManager profileManager;
    private final SortedMap<String, MutableProfile> profiles;
    private Optional<MutableProfile> selectedProfile = Optional.empty();
    private Optional<String> selectedNickname = Optional.empty();
    private Optional<String> selectedHighlight = Optional.empty();

    @Inject
    public CoreProfilesDialogModel(final ProfileManager profileManager) {
        this.profileManager = profileManager;
        listeners = new ListenerList();
        profiles = new ConcurrentSkipListMap<>(Comparator.naturalOrder());
    }

    @Override
    public void loadModel() {
        profiles.clear();
        profileManager.getProfiles().stream().map(MutableProfile::new).forEach(mp -> {
            profiles.put(mp.getName(), mp);
            listeners.getCallable(ProfilesDialogModelListener.class).profileAdded(mp);
        });
        setSelectedProfile(Optional.ofNullable(Iterables.getFirst(profiles.values(), null)));
    }

    @Override
    public List<MutableProfile> getProfileList() {
        return ImmutableList.copyOf(profiles.values());
    }

    @Override
    public Optional<MutableProfile> getProfile(final String name) {
        checkNotNull(name, "Name cannot be null");
        return Optional.ofNullable(profiles.get(name));
    }

    @Override
    public void addProfile(final String name, final String realname, final String ident,
            final List<String> nicknames) {
        checkNotNull(name, "Name cannot be null");
        checkArgument(!profiles.containsKey(name), "Name cannot already exist");
        final MutableProfile profile =
                new MutableProfile(name, realname, Optional.of(ident), nicknames);
        profiles.put(name, profile);
        listeners.getCallable(ProfilesDialogModelListener.class).profileAdded(profile);
        setSelectedProfile(Optional.of(profile));
    }

    @Override
    public void editProfile(final MutableProfile profile, final String name, final String realname,
            final String ident, final List<String> nicknames) {
        checkNotNull(name, "Name cannot be null");
        checkArgument(profiles.containsValue(profile), "Name must already exist");
        if (!profile.getName().equals(name)) {
            profiles.remove(profile.getName());
            profiles.put(name, profile);
        }
        profile.setName(name);
        profile.setRealname(realname);
        profile.setIdent(Optional.of(ident));
        profile.setNicknames(Lists.newArrayList(nicknames));
        listeners.getCallable(ProfilesDialogModelListener.class).profileEdited(profile);
    }

    @Override
    public void removeProfile(final String name) {
        checkNotNull(name, "Name cannot be null");
        checkArgument(profiles.containsKey(name), "profile must exist in list");
        final MutableProfile profile = profiles.remove(name);
        if (getSelectedProfile().isPresent() && getSelectedProfile().get().equals(profile)) {
            setSelectedProfile(Optional.ofNullable(Iterables.getFirst(profiles.values(), null)));
        }
        listeners.getCallable(ProfilesDialogModelListener.class).profileRemoved(profile);
    }

    @Override
    public void save() {
        setSelectedProfile(Optional.empty());
        final List<Profile> profileList = Lists.newArrayList(profileManager.getProfiles());
        profileList.forEach(profileManager::deleteProfile);
        profiles.values().forEach(p -> profileManager.addProfile(
                Profile.create(p.getName(), p.getRealname(), p.getIdent(),
                        p.getNicknames(), p.getHighlights())));
    }

    @Override
    public void setSelectedProfile(final Optional<MutableProfile> profile) {
        checkNotNull(profile, "profile cannot be null");
        if (profile.isPresent()) {
            checkArgument(profiles.containsValue(profile.get()), "Profile must exist in list");
        }
        if (!selectedProfile.equals(profile)) {
            selectedProfile = profile;
            listeners.getCallable(ProfilesDialogModelListener.class)
                    .profileSelectionChanged(profile);
        }
    }

    @Override
    public Optional<MutableProfile> getSelectedProfile() {
        return selectedProfile;
    }

    @Override
    public Optional<String> getSelectedProfileName() {
        if (selectedProfile.isPresent()) {
            return Optional.of(selectedProfile.get().getName());
        }
        return Optional.empty();
    }

    @Override
    public void setSelectedProfileName(final Optional<String> name) {
        // TODO: should probably handle name being empty
        checkNotNull(name, "Name cannot be null");
        checkState(selectedProfile.isPresent(), "There must be a profile selected");
        selectedProfile.get().setName(name.orElse(""));
        listeners.getCallable(ProfilesDialogModelListener.class)
                .profileEdited(selectedProfile.get());
    }

    @Override
    public Optional<String> getSelectedProfileRealname() {
        if (selectedProfile.isPresent()) {
            return Optional.of(selectedProfile.get().getRealname());
        }
        return Optional.empty();
    }

    @Override
    public void setSelectedProfileRealname(final Optional<String> realname) {
        checkNotNull(realname, "Realname cannot be null");
        checkState(selectedProfile.isPresent(), "There must be a profile selected");
        selectedProfile.get().setRealname(realname.orElse(""));
        listeners.getCallable(ProfilesDialogModelListener.class)
                .profileEdited(selectedProfile.get());
    }

    @Override
    public Optional<String> getSelectedProfileIdent() {
        if (selectedProfile.isPresent()) {
            return selectedProfile.get().getIdent();
        }
        return Optional.empty();
    }

    @Override
    public void setSelectedProfileIdent(final Optional<String> ident) {
        checkNotNull(ident, "Ident cannot be null");
        checkState(selectedProfile.isPresent(), "There must be a profile selected");
        selectedProfile.get().setIdent(ident);
        listeners.getCallable(ProfilesDialogModelListener.class)
                .profileEdited(selectedProfile.get());
    }

    @Override
    public Optional<List<String>> getSelectedProfileNicknames() {
        return selectedProfile.map(MutableProfile::getNicknames);
    }

    @Override
    public void setSelectedProfileNicknames(final Optional<List<String>> nicknames) {
        checkNotNull(nicknames, "nicknames cannot be null");
        checkState(selectedProfile.isPresent(), "There must be a profile selected");
        if (nicknames.isPresent()) {
            selectedProfile.get().setNicknames(nicknames.get());
        } else {
            selectedProfile.get().setNicknames(Lists.newArrayList());
        }
        listeners.getCallable(ProfilesDialogModelListener.class)
                .profileEdited(selectedProfile.get());
    }

    @Override
    public void setSelectedProfileHighlights(final Optional<List<String>> highlights) {
        checkNotNull(highlights, "highlights cannot be null");
        checkState(selectedProfile.isPresent(), "There must be a profile selected");
        if (highlights.isPresent()) {
            selectedProfile.get().setHighlights(highlights.get());
        } else {
            selectedProfile.get().setHighlights(Lists.newArrayList());
        }
        listeners.getCallable(ProfilesDialogModelListener.class)
                .profileEdited(selectedProfile.get());
    }

    @Override
    public void addSelectedProfileNickname(final String nickname) {
        checkNotNull(nickname, "Nickname cannot be null");
        checkState(selectedProfile.isPresent(), "There must be a profile selected");
        checkArgument(!selectedProfile.get().getNicknames().contains(nickname),
                "New nickname must not exist");
        selectedProfile.get().addNickname(nickname);
        listeners.getCallable(ProfilesDialogModelListener.class)
                .selectedProfileNicknameAdded(nickname);
    }

    @Override
    public void removeSelectedProfileNickname(final String nickname) {
        checkNotNull(nickname, "Nickname cannot be null");
        checkState(selectedProfile.isPresent(), "There must be a profile selected");
        checkArgument(selectedProfile.get().getNicknames().contains(nickname), "Nickname must exist");
        selectedProfile.get().removeNickname(nickname);
        listeners.getCallable(ProfilesDialogModelListener.class)
                .selectedProfileNicknameRemoved(nickname);
    }

    @Override
    public void editSelectedProfileNickname(final String oldName, final String newName) {
        checkNotNull(oldName, "Nickname cannot be null");
        checkNotNull(newName, "Nickname cannot be null");
        checkState(selectedProfile.isPresent(), "There must be a profile selected");
        checkArgument(selectedProfile.get().getNicknames().contains(oldName),
                "Old nickname must exist");
        checkArgument(!selectedProfile.get().getNicknames().contains(newName),
                "New nickname must not exist");
        final int index = selectedProfile.get().getNicknames().indexOf(oldName);
        selectedProfile.get().setNickname(index, newName);
        listeners.getCallable(ProfilesDialogModelListener.class)
                .selectedProfileNicknameEdited(oldName, newName);
    }

    @Override
    public Optional<String> getSelectedProfileSelectedNickname() {
        return selectedNickname;
    }

    @Override
    public Optional<String> getSelectedProfileSelectedHighlight() {
        return selectedHighlight;
    }

    @Override
    public void setSelectedProfileSelectedNickname(final Optional<String> selectedNickname) {
        checkNotNull(selectedNickname, "Nickname cannot be null");
        checkState(selectedProfile.isPresent(), "There must be a profile selected");
        if (selectedNickname.isPresent()) {
            checkArgument(selectedProfile.get().getNicknames().contains(selectedNickname.get()),
                    "Nickname must exist in nicknames list");
        }
        this.selectedNickname = selectedNickname;
        listeners.getCallable(ProfilesDialogModelListener.class)
                .selectedNicknameChanged(selectedNickname);
    }

    @Override
    public void setSelectedProfileSelectedHighlight(final Optional<String> selectedHighlight) {
        checkNotNull(selectedHighlight, "Highlight cannot be null");
        checkState(selectedProfile.isPresent(), "There must be a profile selected");
        if (selectedHighlight.isPresent()) {
            checkArgument(selectedProfile.get().getHighlights().contains(selectedHighlight.get()),
                    "Nickname must exist in nicknames list");
        }
        this.selectedHighlight = selectedHighlight;
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
        return isProfileListValid() && isSelectedProfileIdentValid() &&
                isSelectedProfileNameValid() && isSelectedProfileNicknamesValid() &&
                isSelectedProfileRealnameValid();
    }

    @Override
    public boolean isSelectedProfileNicknamesValid() {
        return !getSelectedProfileNicknames().isPresent() || !getSelectedProfileNicknamesValidator()
                .validate(getSelectedProfileNicknames().get()).isFailure();
    }

    @Override
    public boolean isSelectedProfileHighlightsValid() {
        return !getSelectedProfileHighlights().isPresent() ||
                !getSelectedProfileHighlightsValidator().validate(
                        getSelectedProfileHighlights().get()).isFailure();
    }

    @Override
    public boolean isSelectedProfileIdentValid() {
        return !getSelectedProfileIdent().isPresent() ||
                !getSelectedProfileIdentValidator().validate(getSelectedProfileIdent().get())
                        .isFailure();
    }

    @Override
    public boolean isSelectedProfileRealnameValid() {
        return getSelectedProfileRealname().isPresent() ||
                !getSelectedProfileRealnameValidator().validate(getSelectedProfileRealname().get())
                        .isFailure();
    }

    @Override
    public boolean isSelectedProfileNameValid() {
        return getSelectedProfileName().isPresent() &&
                !getSelectedProfileNameValidator().validate(getSelectedProfileName().get())
                        .isFailure();
    }

    @Override
    public boolean isProfileListValid() {
        return !getProfileListValidator().validate(getProfileList()).isFailure();
    }

    @Override
    public Validator<List<MutableProfile>> getProfileListValidator() {
        return new ListNotEmptyValidator<>();
    }

    @Override
    public Validator<String> getSelectedProfileNameValidator() {
        return ValidatorChain.<String>builder()
                .addValidator(new EditSelectedProfileNameValidator(this))
                .addValidator(new FileNameValidator())
                .build();
    }

    @Override
    public Validator<String> getNewProfileNameValidator() {
        return ValidatorChain.<String>builder()
                .addValidator(new NewProfileNameValidator(this))
                .addValidator(new FileNameValidator())
                .build();
    }

    @Override
    public Validator<String> getSelectedProfileIdentValidator() {
        return new PermissiveValidator<>();
    }

    @Override
    public Validator<String> getSelectedProfileRealnameValidator() {
        return new NotEmptyValidator();
    }

    @Override
    public Validator<List<String>> getSelectedProfileNicknamesValidator() {
        return new ListNotEmptyValidator<>();
    }

    @Override
    public Optional<List<String>> getSelectedProfileHighlights() {
        return selectedProfile.map(MutableProfile::getHighlights);
    }

    @Override
    public Validator<List<String>> getSelectedProfileHighlightsValidator() {
        return new PermissiveValidator<>();
    }

    @Override
    public Validator<String> getSelectedProfileAddNicknameValidator() {
        return new AddNicknameValidator(this);
    }

    @Override
    public Validator<String> getSelectedProfileEditNicknameValidator() {
        return new EditSelectedNicknameValidator(this);
    }

    @Override
    public void addSelectedProfileHighlight(final String highlight) {
        checkNotNull(highlight, "highlight cannot be null");
        checkState(selectedProfile.isPresent(), "There must be a profile selected");
        if (!selectedProfile.get().getHighlights().contains(highlight)) {
            selectedProfile.get().addHighlight(highlight);
            listeners.getCallable(ProfilesDialogModelListener.class)
                    .selectedProfileHighlightAdded(highlight);
        }
    }

    @Override
    public void removeSelectedProfileHighlight(final String highlight) {
        checkNotNull(highlight, "highlight cannot be null");
        checkState(selectedProfile.isPresent(), "There must be a profile selected");
        if (selectedProfile.get().getHighlights().contains(highlight)) {
            selectedProfile.get().removeHighlight(highlight);
            listeners.getCallable(ProfilesDialogModelListener.class)
                    .selectedProfileHighlightRemoved(highlight);
        }
    }

    @Override
    public Validator<String> getSelectedProfileAddHighlightValidator() {
        return new NotEmptyValidator();
    }

    @Override
    public void editSelectedProfileHighlight(final String oldHighlight, final String newHighlight) {
        checkNotNull(oldHighlight, "Highlight cannot be null");
        checkNotNull(newHighlight, "Highlight cannot be null");
        checkState(selectedProfile.isPresent(), "There must be a profile selected");
        checkArgument(selectedProfile.get().getHighlights().contains(oldHighlight),
                "Old highlight must exist");
        checkArgument(!selectedProfile.get().getHighlights().contains(newHighlight),
                "New highlight must not exist");
        final int index = selectedProfile.get().getNicknames().indexOf(oldHighlight);
        selectedProfile.get().setHighlight(index, newHighlight);
        listeners.getCallable(ProfilesDialogModelListener.class)
                .selectedProfileHighlightEdited(oldHighlight, newHighlight);
    }

    @Override
    public Validator<String> getSelectedProfileEditHighlightValidator() {
        return new EditSelectedHighlightValidator(this);
    }

    @Override
    public Validator<List<String>> getNicknamesValidator() {
        return new ListNotEmptyValidator<>();
    }

    @Override
    public Validator<List<String>> getHighlightsValidator() {
        return new PermissiveValidator<>();
    }

    @Override
    public Validator<String> getRealnameValidator() {
        return new NotEmptyValidator();
    }

    @Override
    public Validator<String> getIdentValidator() {
        return new IdentValidator();
    }

}
