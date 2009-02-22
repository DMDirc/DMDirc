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

import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.messages.ColourManager;

import java.awt.Color;
import java.util.ArrayList;
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
     * @deprecated Should only be used internally by ConfigSource
     */
    @Deprecated
    public abstract boolean hasOption(final String domain, final String option);

    /**
     * Determines if this manager has the specified String option.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     * @since 0.6.3
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
     * @since 0.6.3
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
     * @since 0.6.3
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
     * @since 0.6.3
     * @return True iff the option exists and is parsable as a colour,
     * false otherwise.
     */
    public boolean hasOptionColour(final String domain, final String option) {
        return getOptionColour(domain, option) != null;
    }

    /**
     * Retrieves the specified option.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     * @param fallback Value to use if the option isn't set
     * @return The value of the option
     * @deprecated Fallback should be specified in configs
     */
    @Deprecated
    public String getOption(final String domain, final String option,
            final String fallback) {
        return hasOption(domain, option) ? getOption(domain, option) : fallback;
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
     * Retrieves the specified option as a character.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     * @param fallback Value to use if the option isn't set
     * @return The value of the option
     * @deprecated Fallback should be specified in configs
     */
    @Deprecated
    public char getOptionChar(final String domain, final String option,
            final char fallback) {
        
        if (hasOption(domain, option)) {
            final String value = getOption(domain, option);
            
            if (!value.isEmpty()) {
                return value.charAt(0);
            }
        }
        
        return fallback;
    }

    /**
     * Retrieves a colour representation of the specified option.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     * @return The colour representation of the option
     * @since 0.6.3
     */
    public Color getOptionColour(final String domain, final String option) {
        if (!hasOption(domain, option)) {
            return null;
        }

        String value = getOption(domain, option);

        if (value.startsWith("true:")) {
            value = value.substring(5);
        } else if (value.startsWith("false:")) {
            return null;
        }

        return ColourManager.parseColour(value, null);
    }

    /**
     * Retrieves a colour representation of the specified option.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     * @param fallback The colour that should be used in case of error
     * @return The colour representation of the option
     * @deprecated Fallback should be specified in configs
     */
    @Deprecated
    public Color getOptionColour(final String domain, final String option,
            final Color fallback) {
        if (!hasOption(domain, option)) {
            return fallback;
        }

        String value = getOption(domain, option);
        
        if (value.startsWith("true:")) {
            value = value.substring(5);
        } else if (value.startsWith("false:")) {
            return fallback;
        }
        
        return ColourManager.parseColour(value, fallback);
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
     * Retrieves a boolean representation of the specified option.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     * @param fallback The value to use if the config isn't value
     * @return The boolean representation of the option
     * @deprecated Fallback should be specified in configs
     */
    @Deprecated
    public boolean getOptionBool(final String domain, final String option,
            final boolean fallback) {
        return hasOption(domain, option) ?
            Boolean.parseBoolean(getOption(domain, option)) : fallback;
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
     * @param fallback The value to use if the config isn't valud
     * @return The integer representation of the option
     * @deprecated Fallback should be specified in configs
     */
    @Deprecated
    public int getOptionInt(final String domain, final String option,
            final int fallback) {
        if (!hasOption(domain, option)) {
            return fallback;
        }

        int res;

        try {
            res = Integer.parseInt(getOption(domain, option).trim());
        } catch (NumberFormatException ex) {
            Logger.userError(ErrorLevel.MEDIUM,
                    "Invalid number format for " + domain + "." + option);
            res = fallback;
        }

        return res;
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
