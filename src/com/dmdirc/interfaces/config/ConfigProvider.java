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

import com.dmdirc.config.ConfigTarget;
import com.dmdirc.util.io.InvalidConfigFileException;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Provides methods to read and write from a single configuration source, such as a file on disk.
 */
public interface ConfigProvider extends ReadOnlyConfigProvider {

    /**
     * Adds a new config change listener for this identity.
     *
     * @param listener The listener to be added
     */
    void addListener(ConfigChangeListener listener);

    /**
     * Deletes this identity from disk.
     *
     * @throws IOException if the file fails to delete
     */
    void delete() throws IOException;

    /**
     * Returns the set of domains available in this identity.
     *
     * @since 0.6
     * @return The set of domains used by this identity
     */
    Set<String> getDomains();

    /**
     * Returns the name of this identity.
     *
     * @return The name of this identity
     */
    String getName();

    /**
     * Retrieves this identity's target.
     *
     * @return The target of this identity
     */
    ConfigTarget getTarget();

    /**
     * Determines whether this identity can be used as a profile when connecting to a server.
     * Profiles are identities that can supply nick, ident, real name, etc.
     *
     * @return True iff this identity can be used as a profile
     */
    boolean isProfile();

    /**
     * Attempts to reload this identity from disk. If this identity has been modified (i.e.,
     * {@code needSave} is true), then this method silently returns straight away. All relevant
     * ConfigChangeListeners are fired for new, altered and deleted properties. The target of the
     * identity will not be changed by this method, even if it has changed on disk.
     *
     * @throws java.io.IOException        On I/O exception when reading the identity
     * @throws InvalidConfigFileException if the config file is no longer valid
     */
    void reload() throws IOException, InvalidConfigFileException;

    /**
     * Removes the specific config change listener from this identity.
     *
     * @param listener The listener to be removed
     */
    void removeListener(ConfigChangeListener listener);

    /**
     * Saves this identity to disk if it has been updated.
     */
    void save();

    /**
     * Sets the specified option in this identity to the specified value.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     * @param value  The new value for the option
     */
    void setOption(String domain, String option, String value);

    /**
     * Sets the specified option in this identity to the specified value.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     * @param value  The new value for the option
     */
    void setOption(String domain, String option, int value);

    /**
     * Sets the specified option in this identity to the specified value.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     * @param value  The new value for the option
     */
    void setOption(String domain, String option, boolean value);

    /**
     * Sets the specified option in this identity to the specified value.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     * @param value  The new value for the option
     */
    void setOption(String domain, String option, List<String> value);

    /**
     * Unsets a specified option.
     *
     * @param domain domain of the option
     * @param option name of the option
     */
    void unsetOption(String domain, String option);

}
