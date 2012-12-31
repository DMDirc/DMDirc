/*
 * Copyright (c) 2006-2013 DMDirc Developers
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

package com.dmdirc.interfaces;

import com.dmdirc.Precondition;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.Identity;
import com.dmdirc.config.IdentityListener;
import com.dmdirc.config.InvalidIdentityFileException;

import java.util.List;

/**
 * Defines the interface implemented by the object in charge of DMDirc's
 * identities.
 */
public interface IdentityController {

    /**
     * Retrieves the identity used for addons defaults.
     *
     * @return The addons defaults identity
     */
    Identity getGlobalAddonIdentity();

    /**
     * Retrieves the identity used for the global config.
     *
     * @return The global config identity
     */
    Identity getGlobalConfigIdentity();

    /**
     * Retrieves the global config manager.
     *
     * @return The global config manager
     */
    ConfigManager getGlobalConfiguration();

    /**
     * Retrieves the identity bundled with the DMDirc client containing
     * version information.
     *
     * @return The version identity
     * @since 0.6.3m2
     */
    Identity getGlobalVersionIdentity();

    /**
     * Retrieves a list of identities that belong to the specified custom type.
     *
     * @param type The type of identity to search for
     * @return A list of matching identities
     * @since 0.6.4
     */
    List<Identity> getIdentitiesByType(final String type);

    /**
     * Retrieves a list of all config sources that should be applied to the
     * specified config manager.
     *
     * @param manager The manager requesting sources
     * @return A list of all matching config sources
     */
    List<Identity> getIdentitiesForManager(final ConfigManager manager);

    /**
     * Retrieves the directory used to store identities in.
     *
     * @return The identity directory path
     */
    String getIdentityDirectory();

    /**
     * Loads all identity files.
     *
     * @param configDirectory Config Directory.
     * @throws InvalidIdentityFileException If there is an error with the config
     * file.
     */
    void initialise(final String configDirectory) throws InvalidIdentityFileException;

    /**
     * Get the config directory used by this identity controller.
     *
     * @return The config directory.
     */
    String getConfigDir();

    /**
     * Loads user-defined identity files.
     */
    void loadUserIdentities();

    /**
     * Loads the version information.
     */
    void loadVersionIdentity();

    /**
     * Adds the specific identity to this manager.
     * @param identity The identity to be added
     */
    @Precondition(value = "The specified Identity is not null")
    void registerIdentity(final Identity identity);

    /**
     * Adds a new identity listener which will be informed of all settings
     * identities which are added to this manager.
     *
     * @param listener The listener to be added
     * @since 0.6.4
     */
    @Precondition(value = "The specified listener is not null")
    void registerIdentityListener(final IdentityListener listener);

    /**
     * Adds a new identity listener which will be informed of all identities
     * of the specified custom type which are added to this manager.
     *
     * @param type The type of identities to listen for
     * @param listener The listener to be added
     * @since 0.6.4
     */
    @Precondition(value = "The specified listener is not null")
    void registerIdentityListener(final String type, final IdentityListener listener);

    /**
     * Unregisters the given identity listener.
     *
     * @param listener The listener to be removed
     */
    void unregisterIdentityListener(final IdentityListener listener);

    /**
     * Saves all modified identity files to disk.
     */
    void saveAll();

    /**
     * Removes an identity from this manager.
     * @param identity The identity to be removed
     */
    @Precondition(value = {"The specified Identity is not null", "The specified Identity has previously been added and not removed"})
    void unregisterIdentity(final Identity identity);

}
