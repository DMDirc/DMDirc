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

package com.dmdirc.interfaces.ui;

import com.dmdirc.commandparser.auto.AutoCommandType;
import com.dmdirc.config.profiles.Profile;
import com.dmdirc.interfaces.Connection;

import java.util.Collection;
import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * Dialog to edit both auto commands, either global, connection or both.
 */
public interface AutoCommandsModel {

    /**
     * Adds a listener to this model.
     *
     * @param listener Listener to add
     */
    void addListener(@Nonnull AutoCommandsModelListener listener);

    /**
     * Removes a listener from this model.
     *
     * @param listener Listener to remove
     */
    void removeListener(@Nonnull AutoCommandsModelListener listener);

    /**
     * Loads the model, initialising things as required.  Must be called before using the model.
     * Must not be called again.
     *
     * @param connection Connection to use
     */
    void loadModel(@Nonnull Connection connection);

    /**
     * Returns the available profiles to the model.
     *
     * @return Available profiles
     */
    @Nonnull
    Collection<Profile> getProfiles();

    /**
     * Gets ths selected profile in this model.
     *
     * @return Select profile
     */
    @Nonnull
    Optional<Profile> getSelectedProfile();

    /**
     * Sets the selected profile in this model.
     *
     * @param selectedProfile New selected profile
     */
    void setSelectedProfile(@Nonnull Optional<Profile> selectedProfile);

    /**
     * Returns the global auto command.
     *
     * @return Global auto command
     */
    @Nonnull
    String getGlobalAutoCommandResponse();

    /**
     * Sets the response for the global command. This should not be empty, and should not contain
     * command characters.
     *
     * @param response The new response
     */
    void setGlobalAutoCommandResponse(@Nonnull String response);

    /**
     * Returns the selected command's server name.
     *
     * @return Selected command's server name, or empty
     */
    Optional<String> getSelectedCommandServer();

    /**
     * Returns the selected command's network name.
     *
     * @return Selected command's network name, or empty
     */
    Optional<String> getSelectedCommandNetwork();

    /**
     * Returns the selected command's type.
     *
     * @return Selected command's type, or an empty optional
     */
    @Nonnull
    Optional<AutoCommandType> getSelectedCommandType();

    /**
     * Sets the command type of the selected command. This is a NOOP if there is no selected
     * command.
     */
    void setSelectedCommandType(@Nonnull AutoCommandType type);

    /**
     * Returns the selected command's response.
     *
     * @return Selected command's response, or an empty string
     */
    @Nonnull
    String getSelectedCommandResponse();

    /**
     * Sets the selected command's response. This is a NOOP if there is no selected command.
     *
     * @param response New response to set.  Cannot be empty.
     */
    void setSelectedCommandResponse(@Nonnull String response);

    /**
     * Saves the state of this model to the core manager.
     */
    void save();
}
