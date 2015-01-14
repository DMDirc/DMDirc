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
    public void removeProfile(final String name) {
        checkNotNull(name, "Name cannot be null");
        checkArgument(profiles.containsKey(name), "profile must exist in list");
        final MutableProfile profile = profiles.remove(name);
        if (getSelectedProfile().isPresent() && getSelectedProfile().get().equals(profile)) {
            setSelectedProfile(Optional.ofNullable(Iterables.getFirst(profiles.values(), null)));
            setSelectedProfileIdent(Optional.empty());
            setSelectedProfileRealname(Optional.empty());
            setSelectedProfileSelectedHighlight(Optional.empty());
            setSelectedProfileName(Optional.empty());
            setSelectedProfileSelectedNickname(Optional.empty());
            setSelectedProfileHighlights(Optional.empty());
            setSelectedProfileNicknames(Optional.empty());
        }
        listeners.getCallable(ProfilesDialogModelListener.class).profileRemoved(profile);
    }

    @Override
    public void save() {
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
            listeners.getCallable(ProfilesDialogModelListener.class).profileSelectionChanged(profile);
        }
    }

    @Override
    public Optional<MutableProfile> getSelectedProfile() {
        return selectedProfile;
    }

    @Override
    public Optional<String> getSelectedProfileName() {
        return selectedProfile.map(MutableProfile::getName);
    }

    @Override
    public void setSelectedProfileName(final Optional<String> name) {
        checkNotNull(name, "Name cannot be null");
        selectedProfile.ifPresent(p -> {
            p.setName(name.orElse(""));
            listeners.getCallable(ProfilesDialogModelListener.class).profileEdited(p);
        });
    }

    @Override
    public Optional<String> getSelectedProfileRealname() {
        return selectedProfile.map(MutableProfile::getRealname);
    }

    @Override
    public void setSelectedProfileRealname(final Optional<String> realname) {
        checkNotNull(realname, "Realname cannot be null");
        selectedProfile.ifPresent(p -> {
            p.setRealname(realname.orElse(""));
            listeners.getCallable(ProfilesDialogModelListener.class).profileEdited(p);
        });
    }

    @Override
    public Optional<String> getSelectedProfileIdent() {
        return selectedProfile.flatMap(MutableProfile::getIdent);
    }

    @Override
    public void setSelectedProfileIdent(final Optional<String> ident) {
        checkNotNull(ident, "Ident cannot be null");
        selectedProfile.ifPresent(p -> {
            p.setIdent(ident);
            listeners.getCallable(ProfilesDialogModelListener.class).profileEdited(p);
        });
    }

    @Override
    public Optional<List<String>> getSelectedProfileNicknames() {
        return selectedProfile.map(MutableProfile::getNicknames);
    }

    @Override
    public void setSelectedProfileNicknames(final Optional<List<String>> nicknames) {
        checkNotNull(nicknames, "nicknames cannot be null");
        selectedProfile.ifPresent(p -> {
            p.setNicknames(nicknames.orElse(Lists.newArrayList()));
            listeners.getCallable(ProfilesDialogModelListener.class).profileEdited(p);
        });
    }

    @Override
    public void setSelectedProfileHighlights(final Optional<List<String>> highlights) {
        checkNotNull(highlights, "highlights cannot be null");
        selectedProfile.ifPresent(p -> {
            p.setHighlights(highlights.orElse(Lists.newArrayList()));
            listeners.getCallable(ProfilesDialogModelListener.class).profileEdited(p);
        });
    }

    @Override
    public void addSelectedProfileNickname(final String nickname) {
        checkNotNull(nickname, "Nickname cannot be null");
        selectedProfile.ifPresent(p -> {
            checkArgument(!p.getNicknames().contains(nickname), "New nickname must not exist");
            p.addNickname(nickname);
            listeners.getCallable(ProfilesDialogModelListener.class)
                    .selectedProfileNicknameAdded(nickname);
        });
    }

    @Override
    public void removeSelectedProfileNickname(final String nickname) {
        checkNotNull(nickname, "Nickname cannot be null");
        selectedProfile.ifPresent(p -> {
            checkArgument(p.getNicknames().contains(nickname), "Nickname must exist");
            p.removeNickname(nickname);
            listeners.getCallable(ProfilesDialogModelListener.class)
                    .selectedProfileNicknameRemoved(nickname);
        });
    }

    @Override
    public void editSelectedProfileNickname(final String oldName, final String newName) {
        checkNotNull(oldName, "Nickname cannot be null");
        checkNotNull(newName, "Nickname cannot be null");
        selectedProfile.ifPresent(p -> {
            checkArgument(p.getNicknames().contains(oldName), "Old nickname must exist");
            checkArgument(!p.getNicknames().contains(newName), "New nickname must not exist");
            final int index = p.getNicknames().indexOf(oldName);
            p.setNickname(index, newName);
            selectedNickname = Optional.of(newName);
            listeners.getCallable(ProfilesDialogModelListener.class)
                    .selectedProfileNicknameEdited(oldName, newName);
        });
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
        selectedProfile.ifPresent(p -> {
            if (selectedNickname.isPresent()) {
                checkArgument(p.getNicknames().contains(selectedNickname.get()),
                    "Nickname must exist in nicknames list");
            }
            if (this.selectedNickname != selectedNickname) {
                this.selectedNickname = selectedNickname;
                listeners.getCallable(ProfilesDialogModelListener.class)
                        .selectedNicknameChanged(selectedNickname);
            }
        });
    }

    @Override
    public void setSelectedProfileSelectedHighlight(final Optional<String> selectedHighlight) {
        checkNotNull(selectedHighlight, "Highlight cannot be null");
        selectedProfile.ifPresent(p -> {
            if (selectedHighlight.isPresent()) {
                checkArgument(p.getHighlights().contains(selectedHighlight.get()),
                        "Nickname must exist in nicknames list");
            }
            if (this.selectedHighlight != selectedHighlight) {
                this.selectedHighlight = selectedHighlight;
                listeners.getCallable(ProfilesDialogModelListener.class)
                        .selectedHighlightChanged(selectedNickname);
            }
        });
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
        return ValidatorChain.<String>builder()
                .addValidator(new NotEmptyValidator())
                .addValidator(new AddNicknameValidator(this))
                .build();
    }

    @Override
    public Validator<String> getSelectedProfileEditNicknameValidator() {
        return ValidatorChain.<String>builder()
                .addValidator(new NotEmptyValidator())
                .addValidator(new EditSelectedNicknameValidator(this))
                .build();
    }

    @Override
    public void addSelectedProfileHighlight(final String highlight) {
        checkNotNull(highlight, "highlight cannot be null");
        selectedProfile.ifPresent(p -> {
            if (!p.getHighlights().contains(highlight)) {
                p.addHighlight(highlight);
                listeners.getCallable(ProfilesDialogModelListener.class)
                        .selectedProfileHighlightAdded(highlight);
            }
        });
    }

    @Override
    public void removeSelectedProfileHighlight(final String highlight) {
        checkNotNull(highlight, "highlight cannot be null");
        selectedProfile.ifPresent(p -> {
            if (p.getHighlights().contains(highlight)) {
                p.removeHighlight(highlight);
                listeners.getCallable(ProfilesDialogModelListener.class)
                        .selectedProfileHighlightRemoved(highlight);
            }
        });
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
        selectedProfile.ifPresent(p -> {
            checkArgument(p.getHighlights().contains(oldHighlight), "Old highlight must exist");
            checkArgument(!p.getHighlights().contains(newHighlight), "New highlight must not exist");
            final int index = p.getHighlights().indexOf(oldHighlight);
            p.setHighlight(index, newHighlight);
            selectedHighlight = Optional.of(newHighlight);
            listeners.getCallable(ProfilesDialogModelListener.class)
                    .selectedProfileHighlightEdited(oldHighlight, newHighlight);
        });
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
