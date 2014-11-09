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

import com.dmdirc.ui.core.autocommands.AutoCommandType;
import com.dmdirc.ui.core.autocommands.MutableAutoCommand;

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
     * Sets the type of auto command to display in the model.  Must be called before
     * {@see #loadModel()}.  Must not be called again.
     *
     * @param type Type to use
     */
    void setType(@Nonnull AutoCommandType type);

    /**
     * Loads the model, initialising things as required.  Must be called before using the model.
     * Must not be called again.
     */
    void loadModel();

    /**
     * Returns the auto commands for this model.
     *
     * @return List of commands in this model
     */
    @Nonnull Collection<MutableAutoCommand> getAutoCommands();

    /**
     * Sets the list of auto commands available in the model.
     *
     * @param commands New commands
     */
    void setAutoCommands(@Nonnull Collection<MutableAutoCommand> commands);

    /**
     * Gets ths selected command in this model.
     *
     * @return Select command
     */
    @Nonnull Optional<MutableAutoCommand> getSelectedCommand();

    /**
     * Sets the selected command in this model.
     *
     * @param selectedCommand New selected command
     */
    void setSelectedCommand(@Nonnull Optional<MutableAutoCommand> selectedCommand);

    /**
     * Returns the selected command's server.
     *
     * @return Selected command's server, or an empty optional
     */
    @Nonnull Optional<String> getSelectedCommandServer();

    /**
     * Returns the selected command's network.
     *
     * @return Selected command's network, or an empty optional
     */
    @Nonnull Optional<String> getSelectedCommandNetwork();

    /**
     * Returns the selected command's profile.
     *
     * @return Selected command's profile, or an empty optional
     */
    @Nonnull Optional<String> getSelectedCommandProfile();

    /**
     * Returns the selected command's response.
     *
     * @return Selected command's response, or an empty string
     */
    @Nonnull String getSelectedCommandResponse();

    /**
     * Sets the selected command's server. This is a NOOP if there is no selected command.
     *
     * @param server New server to set
     */
    void setSelectedCommandServer(@Nonnull Optional<String> server);

    /**
     * Sets the selected command's network. This is a NOOP if there is no selected command.
     *
     * @param network New network to set
     */
    void setSelectedCommandNetwork(@Nonnull Optional<String> network);

    /**
     * Sets the selected command's profile. This is a NOOP if there is no selected command.
     *
     * @param profile New profile to set
     */
    void setSelectedCommandProfile(@Nonnull Optional<String> profile);

    /**
     * Sets the selected command's response. This is a NOOP if there is no selected command.
     *
     * @param response New response to set.  Cannot be empty.
     */
    void setSelectedCommandResponse(@Nonnull String response);

    /**
     * Adds a new command to this model.
     *
     * @param command New command
     */
    void addCommand(@Nonnull MutableAutoCommand command);

    /**
     * Removes a command from this model.
     *
     * @param command Command to remove
     */
    void removeCommand(@Nonnull MutableAutoCommand command);

    /**
     * Saves the state of this model to the core manager.
     */
    void save();
}
