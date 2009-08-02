/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.ui.messages.ColourManager;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Defines methods to get options from a config source in various forms.
 *
 * @author chris
 */
public abstract class ConfigSource {

    /**
     * Retrieves the specified option.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     * @return The value of the option
     */
    public abstract String getOption(final String domain, final String option);

    /**
     * Determines if this manager has the specified option.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     * @return True iff the option exists, false otherwise.
     */
    protected abstract boolean hasOption(final String domain, final String option);

    /**
     * Determines if this manager has the specified String option.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     * @since 0.6.3m1
     * @return True iff the option exists and is not empty, false otherwise
     */
    public boolean hasOptionString(final String domain, final String option) {
        return hasOption(domain, option) && !getOption(domain, option).isEmpty();
    }

    /**
     * Determines if this manager has the specified integer option.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     * @since 0.6.3m1
     * @return True iff the option exists and is parsable as an integer,
     * false otherwise.
     */
    public boolean hasOptionInt(final String domain, final String option) {
        if (hasOption(domain, option)) {
            try {
                getOptionInt(domain, option);
                return true;
            } catch (NumberFormatException ex) {
                // Do nothing
            }
        }

        return false;
    }

    /**
     * Determines if this manager has the specified character option.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     * @since 0.6.3m1
     * @return True iff the option exists and is parsable as a char,
     * false otherwise.
     */
    public boolean hasOptionChar(final String domain, final String option) {
        return hasOption(domain, option) && !getOption(domain, option).isEmpty();
    }

    /**
     * Determines if this manager has the specified colour option.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     * @since 0.6.3m1
     * @return True iff the option exists and is parsable as a colour,
     * false otherwise.
     */
    public boolean hasOptionColour(final String domain, final String option) {
        return getOptionColour(domain, option) != null;
    }

    /**
     * Retrieves a boolean representation of the specified option.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     * @return The boolean representation of the option
     */
    public boolean hasOptionBool(final String domain, final String option) {
        return hasOption(domain, option);
    }

    /**
     * Retrieves the specified option as a character.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     * @return The value of the option
     */
    public char getOptionChar(final String domain, final String option) {
        return getOption(domain, option).charAt(0);
    }
    
    /**
     * Retrieves a colour representation of the specified option.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     * @param fallbacks An ordered array of further domains and options
     * (in pairs) to try if the specified domain/option isn't found
     * @return The colour representation of the option
     * @since 0.6.3m1
     */
    public Color getOptionColour(final String domain, final String option,
            final String ... fallbacks) {
        String value;

        if (!hasOption(domain, option) || (value = getOption(domain, option))
                .startsWith("false:")) {
            return fallbacks.length >= 2 ? getOptionColour(fallbacks[0], fallbacks[1],
                    Arrays.copyOfRange(fallbacks, 2, fallbacks.length)) : null;
        }
        return ColourManager.parseColour(value.startsWith("true:")
                ? value.substring(5) : value, null);
    }

    /**
     * Retrieves a boolean representation of the specified option.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     * @return The boolean representation of the option
     */
    public boolean getOptionBool(final String domain, final String option) {
        return Boolean.parseBoolean(getOption(domain, option));
    }

    /**
     * Retrieves a list representation of the specified option.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     * @param trimEmpty Whether or not to trim empty lines
     * @return The list representation of the option
     */
    public List<String> getOptionList(final String domain, final String option,
            final boolean trimEmpty) {
        final List<String> res = new ArrayList<String>();

        if (hasOption(domain, option)) {
            for (String line : getOption(domain, option).split("\n")) {
                if (!line.isEmpty() || !trimEmpty) {
                    res.add(line);
                }
            }
        }

        return res;
    }

    /**
     * Retrieves a list representation of the specified option, trimming empty
     * lines by default.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     * @return The list representation of the option
     */
    public List<String> getOptionList(final String domain, final String option) {
        return getOptionList(domain, option, true);
    }

    /**
     * Retrieves an integral representation of the specified option.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     * @throws NumberFormatException If the setting can't be parsed
     * @return The integer representation of the option
     */
    public int getOptionInt(final String domain, final String option) {
        return Integer.parseInt(getOption(domain, option).trim());
    }

}
