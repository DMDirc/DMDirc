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

package com.dmdirc.interfaces.ui;

import com.dmdirc.ui.core.profiles.MutableProfile;
import com.dmdirc.util.validators.Validator;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Dialog to edit the user profiles.
 */
public interface ProfilesDialogModel {

    /**
     * Adds a listener to the model.
     *
     * @param listener Listener to add
     */
    void addListener(ProfilesDialogModelListener listener);

    /**
     * Adds a profile to the model.
     *
     * @param name      Name of the profile to add
     * @param realname  Realname of the profile
     * @param ident     Optional ident of the profile
     * @param nicknames Nicknames to add
     */
    void addProfile(String name, String realname, String ident, List<String> nicknames);

    /**
     * Edits a profile in the model.
     *
     * @param name      Name of the profile to edit
     * @param realname  New realname
     * @param ident     New ident
     * @param nicknames New nicknames
     */
    void editProfile(MutableProfile profile, String name, String realname, String ident,
            List<String> nicknames);

    /**
     * Retrieves a profile from the model.
     *
     * @param name Name of the profile to get
     *
     * @return Optional profile from the model
     */
    Optional<MutableProfile> getProfile(String name);

    /**
     * Gets the list of profiles from the model.
     *
     * @return Collection of profiles
     */
    Collection<MutableProfile> getProfileList();

    /**
     * Gets the profile list validator.
     *
     * @return Profile list validator
     */
    Validator<List<MutableProfile>> getProfileListValidator();

    /**
     * Gets the selected profile in the model.
     *
     * @return Optional selected profile
     */
    Optional<MutableProfile> getSelectedProfile();

    /**
     * Gets the selected profile's ident
     *
     * @return Optional profile's ident
     */
    Optional<String> getSelectedProfileIdent();

    /**
     * Gets the selected profile's ident validator.
     *
     * @return Selected profile ident validator
     */
    Validator<String> getSelectedProfileIdentValidator();

    /**
     * Gets the selected profile's name.
     *
     * @return Optional selected profile name
     */
    Optional<String> getSelectedProfileName();

    /**
     * Gets the selected profile's name validator.
     *
     * @return Selected profile name validator
     */
    Validator<String> getSelectedProfileNameValidator();

    /**
     * Gets the new profile name validator.
     *
     * @return New profile name validator
     */
    Validator<String> getNewProfileNameValidator();

    /**
     * Gets the selected profile's list of nicknames.
     *
     * @return Optional selected profile's list of nicknames
     */
    Optional<List<String>> getSelectedProfileNicknames();

    /**
     * Gets the selected profile's list of nicknames validator.
     *
     * @return The selected profile's nicknames validator
     */
    Validator<List<String>> getSelectedProfileNicknamesValidator();

    /**
     * Gets the selected profile's realname.
     *
     * @return Optional selected profile's realname
     */
    Optional<String> getSelectedProfileRealname();

    /**
     * Gets the selected profile's realname validator.
     *
     * @return Selected profile's realname validator
     */
    Validator<String> getSelectedProfileRealnameValidator();

    /**
     * Gets the selected profile's selected nickname.
     *
     * @return Optional selected profile's selected nickname
     */
    Optional<String> getSelectedProfileSelectedNickname();

    /**
     * Is the profile list valid?
     *
     * @return true or false
     */
    boolean isProfileListValid();

    /**
     * Is the selected profile's ident valid?
     *
     * @return true or false
     */
    boolean isSelectedProfileIdentValid();

    /**
     * Is the selected profile's name valid?
     *
     * @return true or false
     */
    boolean isSelectedProfileNameValid();

    /**
     * Is the selected profile's list of nicknames valid?
     *
     * @return true of false
     */
    boolean isSelectedProfileNicknamesValid();

    /**
     * Is the selected profile's realname valid.
     *
     * @return true or false
     */
    boolean isSelectedProfileRealnameValid();

    /**
     * Removes a listener from this model.
     *
     * @param listener Listener to remove
     */
    void removeListener(ProfilesDialogModelListener listener);

    /**
     * Removes a profile from this model.
     *
     * @param name Name of the profile to remove
     */
    void removeProfile(String name);

    /**
     * Saves the profiles in the model, deleting as appropriate.
     */
    void save();

    /**
     * Sets the selected profile in the model.
     *
     * @param profile New selected profile
     */
    void setSelectedProfile(Optional<MutableProfile> profile);

    /**
     * Sets the selected profile's ident.
     *
     * @param ident New selected profile's ident
     */
    void setSelectedProfileIdent(Optional<String> ident);

    /**
     * Sets the selected profile's name.
     *
     * @param name New selected profile's name
     */
    void setSelectedProfileName(Optional<String> name);

    /**
     * Sets the selected profile's list of nicknames.
     *
     * @param nicknames New selected profile's list of nicknames
     */
    void setSelectedProfileNicknames(Optional<List<String>> nicknames);

    /**
     * Sets the selected profile's realname.
     *
     * @param realname New selected profile's realname
     */
    void setSelectedProfileRealname(Optional<String> realname);

    /**
     * Sets the selected profile's selected nickname.
     *
     * @param selectedNickname New selected profile's selected nickname
     */
    void setSelectedProfileSelectedNickname(Optional<String> selectedNickname);

    /**
     * Adds a nickname to the selected profile.
     *
     * @param nickname Nickname to add
     */
    void addSelectedProfileNickname(final String nickname);

    /**
     * Removes a nickname from the selected profile.
     *
     * @param nickname Nickname to remove
     */
    void removeSelectedProfileNickname(final String nickname);

    /**
     * Gets the selected profile's add nickname validator.
     *
     * @return Selected profile's add nickname validator
     */
    Validator<String> getSelectedProfileAddNicknameValidator();

    /**
     * Gets the selected profile's edit nickname validator.
     *
     * @param oldName Old nickname
     * @param newName New nickname
     */
    void editSelectedProfileNickname(final String oldName, final String newName);

    /**
     * Gets the selected profile's edit nickname validator.
     *
     * @return Selected profile's edit nickname validator
     */
    Validator<String> getSelectedProfileEditNicknameValidator();

    /**
     * Are we allowed to change profile?
     *
     * @return true or false
     */
    boolean canSwitchProfiles();

    /**
     * Loads the model.
     */
    void loadModel();

    /**
     * Are we allowed to save the dialog?
     *
     * @return true or false
     */
    boolean isSaveAllowed();

    Validator<String> getNameValidator();

    Validator<List<String>> getNicknamesValidator();

    Validator<String> getRealnameValidator();

    Validator<String> getIdentValidator();
}
