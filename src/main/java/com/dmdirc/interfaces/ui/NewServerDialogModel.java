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

import com.dmdirc.config.profiles.Profile;
import com.dmdirc.util.validators.Validator;

import java.util.List;
import java.util.Optional;

/**
 * Model representing a dialog containing the details required to open a new connection.
 */
public interface NewServerDialogModel {

    /**
     * Loads the default and required settings and adds the appropriate listeners.
     */
    void loadModel();

    /**
     * Returns the list of profiles available to the dialog.
     *
     * @return List of available profiles
     */
    List<Profile> getProfileList();

    /**
     * Gets the selected profile in the dialog.
     *
     * @return Selected profile
     */
    Optional<Profile> getSelectedProfile();

    /**
     * Sets the selected profile in the dialog.
     *
     * @param selectedProfile New selected profile
     */
    void setSelectedProfile(Optional<Profile> selectedProfile);

    /**
     * Is the profiles list valid?
     *
     * @return true if value
     */
    boolean isProfileListValid();

    /**
     * Returns the validator for the profiles list.
     *
     * @return Profile validator
     */
    Validator<List<Profile>> getProfileListValidator();

    /**
     * Returns the hostname of the server.
     *
     * @return Server hostname
     */
    Optional<String> getHostname();

    /**
     * Sets the server hostname.
     *
     * @param hostname New server hostname
     */
    void setHostname(Optional<String> hostname);

    /**
     * Is the server hostname valid.
     *
     * @return true if valid
     */
    boolean isHostnameValid();

    /**
     * Returns the validator for the server hostname.
     *
     * @return Server hostname validator
     */
    Validator<String> getHostnameValidator();

    /**
     * Returns the servers port.
     *
     * @return Servers port
     */
    Optional<Integer> getPort();

    /**
     * Sets the servers port.
     *
     * @param port New server port
     */
    void setPort(Optional<Integer> port);

    /**
     * Is the port valid?
     *
     * @return true if valid
     */
    boolean isPortValid();

    /**
     * Returns the validator for servers port.
     *
     * @return Server port validator
     */
    Validator<Integer> getPortValidator();

    /**
     * Gets the servers password.
     *
     * @return Servers password
     */
    Optional<String> getPassword();

    /**
     * Sets the password for the server.
     *
     * @param password Servers password
     */
    void setPassword(Optional<String> password);

    /**
     * Is the password valid?
     *
     * @return true if valid
     */
    boolean isPasswordValid();

    /**
     * Returns the validator for the server password.
     *
     * @return Server port validator
     */
    Validator<String> getPasswordValidator();

    /**
     * Should be use SSL to connect to the server?
     *
     * @return true if yes
     */
    boolean getSSL();

    /**
     * Sets whether we should connect to the server over SSL.
     *
     * @param ssl New SSL state
     */
    void setSSL(boolean ssl);

    /**
     * Should we save these settings as the default?
     *
     * @return true if yes
     */
    boolean getSaveAsDefault();

    /**
     * Sets whether we should save these settings as the default.
     *
     * @param saveAsDefault true if yes
     */
    void setSaveAsDefault(boolean saveAsDefault);

    /**
     * Saves the defaults as appropriate and connects to the server.
     */
    void save();

    /**
     * Are we allowed to save the dialog and connect to the server?
     *
     * @return true if all validations pass
     */
    boolean isSaveAllowed();

    /**
     * Adds a listener for changes on this model.
     *
     * @param listener Listener to add
     */
    void addListener(NewServerDialogModelListener listener);

    /**
     * Removes a listener for changes on this model.
     *
     * @param listener Listener to remove
     */
    void removeListener(NewServerDialogModelListener listener);

}
