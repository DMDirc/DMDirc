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

package com.dmdirc.interfaces.ui;

import com.dmdirc.ui.core.profiles.MutableProfile;

import java.util.Optional;

/**
 * Listener for various events in a profiles dialog.
 */
public interface ProfilesDialogModelListener {

    /**
     * Called when a profile is added to the model.
     *
     * @param profile New profile
     */
    void profileAdded(MutableProfile profile);

    /**
     * Called when a profile is removed from the model.
     *
     * @param profile Old profile
     */
    void profileRemoved(MutableProfile profile);

    /**
     * Called when a profile in the model is edited.
     *
     * @param profile Profile that was edited
     */
    void profileEdited(MutableProfile profile);

    /**
     * Called when the selected profile in the model changes.
     *
     * @param profile New selected profile
     */
    void profileSelectionChanged(Optional<MutableProfile> profile);

    /**
     * Called when the selected profile's selected nickname is changed
     *
     * @param nickname Optional selected profile's selected nickname
     */
    void selectedNicknameChanged(Optional<String> nickname);

    /**
     * Called when a nickname in the selected profile is changed.
     *
     * @param oldNickname Old nickname
     * @param newNickname New nickname
     */
    void selectedProfileNicknameEdited(String oldNickname, String newNickname);

    /**
     * Called when a nickname is added to the selected profile.
     *
     * @param nickname New nickname
     */
    void selectedProfileNicknameAdded(String nickname);

    /**
     * Called when a nickname is removed from the selected profile.
     *
     * @param nickname Old nickname
     */
    void selectedProfileNicknameRemoved(String nickname);

    /**
     * Called when the selected profile's selected highlight is changed
     *
     * @param highlight Optional selected profile's selected highlight
     */
    void selectedHighlightChanged(Optional<String> highlight);

    /**
     * Called when a highlight in the selected profile is changed.
     *
     * @param oldHighlight Old highlight
     * @param newHighlight New highlight
     */
    void selectedProfileHighlightEdited(String oldHighlight, String newHighlight);

    /**
     * Called when a highlight is added to the selected profile.
     *
     * @param highlight New highlight
     */
    void selectedProfileHighlightAdded(String highlight);

    /**
     * Called when a highlight is removed from the selected profile.
     *
     * @param highlight Old highlight
     */
    void selectedProfileHighlightRemoved(String highlight);

}
