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

package com.dmdirc.config;

import com.dmdirc.interfaces.config.ReadOnlyConfigProvider;

import java.util.List;

/**
 * Provides methods to retrieve a value of a certain type from a config provider.
 */
public class ConfigValueRetriever {

    private final ReadOnlyConfigProvider configProvider;

    public ConfigValueRetriever(final ReadOnlyConfigProvider configProvider) {
        this.configProvider = configProvider;
    }

    /**
     * Gets a value from the configuration manager, and attempts to coerce it into the given class.
     *
     * @param targetClass The desired class.
     * @param domain The domain of the option to retrieve.
     * @param key The key of the option to retrieve.
     * @param required Whether the option is required or not (only used for strings).
     * @param fallbacks Ordered collection of domain/key pairs to try if the value is not set.
     *
     * @return An object representing the current value of the configuration key(s) given, of the
     *         desired target class, or null if the type conversion couldn't be performed.
     */
    public Object getValue(final Class<?> targetClass, final String domain, final String key,
            final boolean required, final String ... fallbacks) {
        if (targetClass.equals(String.class)) {
            return configProvider.getOptionString(
                    domain, key, required, ReadOnlyConfigProvider.PERMISSIVE_VALIDATOR, fallbacks);
        }

        if (targetClass.equals(Boolean.class) || targetClass.equals(Boolean.TYPE)) {
            return configProvider.getOptionBool(domain, key);
        }

        if (targetClass.equals(Character.class) || targetClass.equals(Character.TYPE)) {
            return configProvider.getOptionChar(domain, key);
        }

        if (targetClass.equals(Integer.class) || targetClass.equals(Integer.TYPE)) {
            return configProvider.getOptionInt(domain, key, fallbacks);
        }

        if (targetClass.equals(List.class)) {
            return configProvider.getOptionList(domain, key);
        }

        return null;
    }

    /**
     * Gets a value from the configuration manager, and attempts to coerce it into the given class.
     *
     * @param targetClass The desired class.
     * @param domain The domain of the option to retrieve.
     * @param key The key of the option to retrieve.
     *
     * @return An object representing the current value of the configuration key(s) given, of the
     *         desired target class, or null if the type conversion couldn't be performed.
     */
    public Object getValue(final Class<?> targetClass, final String domain, final String key) {
        return getValue(targetClass, domain, key, true);
    }

}
