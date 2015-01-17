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

package com.dmdirc.interfaces.config;

import java.util.Collection;

/**
 * Defines the interface implemented by the object in charge of DMDirc's identities.
 */
public interface IdentityController {

    /**
     * Retrieves the identity used for addons defaults.
     *
     * @return The addons defaults identity
     */
    ConfigProvider getAddonSettings();

    /**
     * Retrieves the identity used for the global config.
     *
     * @return The global config identity
     */
    ConfigProvider getUserSettings();

    /**
     * Retrieves the global config manager.
     *
     * @return The global config manager
     */
    AggregateConfigProvider getGlobalConfiguration();

    /**
     * Retrieves the identity bundled with the DMDirc client containing version information.
     *
     * @return The version identity
     *
     * @since 0.6.3m2
     */
    ReadOnlyConfigProvider getVersionSettings();

    /**
     * Retrieves a list of identities that belong to the specified custom type.
     *
     * @param type The type of identity to search for
     *
     * @return A list of matching identities
     *
     * @since 0.6.4
     */
    Collection<ConfigProvider> getProvidersByType(String type);

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
     *
     * @param identity The identity to be added
     */
    void addConfigProvider(ConfigProvider identity);

    /**
     * Adds a new identity listener which will be informed of all settings identities which are
     * added to this manager.
     *
     * @param listener The listener to be added
     *
     * @since 0.6.4
     */
    void registerIdentityListener(ConfigProviderListener listener);

    /**
     * Adds a new identity listener which will be informed of all identities of the specified custom
     * type which are added to this manager.
     *
     * @param type     The type of identities to listen for
     * @param listener The listener to be added
     *
     * @since 0.6.4
     */
    void registerIdentityListener(String type, ConfigProviderListener listener);

    /**
     * Unregisters the given identity listener.
     *
     * @param listener The listener to be removed
     */
    void unregisterIdentityListener(ConfigProviderListener listener);

    /**
     * Saves all modified identity files to disk.
     */
    void saveAll();

    /**
     * Removes an identity from this manager.
     *
     * @param identity The identity to be removed
     */
    void removeConfigProvider(ConfigProvider identity);

}
