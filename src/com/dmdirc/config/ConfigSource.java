/*
 * Copyright (c) 2006-2012 DMDirc Developers
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

import com.dmdirc.ui.Colour;
import com.dmdirc.ui.messages.ColourManager;
import com.dmdirc.util.validators.ColourValidator;
import com.dmdirc.util.validators.DisabledOptionValidator;
import com.dmdirc.util.validators.NumericalValidator;
import com.dmdirc.util.validators.OptionalValidator;
import com.dmdirc.util.validators.PermissiveValidator;
import com.dmdirc.util.validators.Validator;
import com.dmdirc.util.validators.ValidatorChain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Defines methods to get options from a config source in various forms.
 * <p>
 * This implementation supports the idea of optional/disabled settings.
 * This is where values may be prefixed with the strings <code>true:</code>
 * or <code>false:</code>, where the former has no effect on the value of
 * the setting, and the latter indicates that the user wishes to disable
 * the setting.
 * <p>
 * Disabled settings allow for optional settings to have default values; a
 * value can be added with the <code>false:</code> prefix which effectively
 * disables the default value and makes the setting act as though it does not
 * have a value. Note that for this sort of behaviour to make sense, it requires
 * an implementation that takes values from multiple sources, such as a
 * {@link ConfigManager}.
 */
public abstract class ConfigSource {

    /** A permissive validator to use when callers don't specify one. */
    private static final Validator<String> PERMISSIVE_VALIDATOR
            = new PermissiveValidator<String>();

    /** A validator for integer settings. */
    private static final Validator<String> INT_VALIDATOR
            = new OptionalValidator(new NumericalValidator(-1, -1));

    /** A validator for colour settings. */
    private static final Validator<String> COLOUR_VALIDATOR
            = new OptionalValidator(new ColourValidator());

    /**
     * Retrieves the specified option from this config source.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     * @return The value of the option, or null if it doesn't exist
     */
    public String getOption(final String domain, final String option) {
        return getOption(domain, option, PERMISSIVE_VALIDATOR);
    }

    /**
     * Retrieves the first value for the specified option that matches the
     * specified validator.
     *
     * @since 0.6.5
     * @param domain The domain of the option
     * @param option The name of the option
     * @param validator The validator to use to check legal values
     * @return The value of the option, or null if no matching values exist
     */
    protected abstract String getOption(final String domain,
            final String option, final Validator<String> validator);

    /**
     * Determines if this source has a value for the specified option which
     * matches the specified validator.
     *
     * @since 0.6.5
     * @param domain The domain of the option
     * @param option The name of the option
     * @param validator The validator to use to check legal values
     * @return True iff a matching option exists, false otherwise.
     */
    protected abstract boolean hasOption(final String domain,
            final String option, final Validator<String> validator);

    /**
     * Determines if this source has the specified String option.
     * <p>
     * A String option is considered to exist if:
     * <ul>
     *  <li>there is a value for the specified option,
     *  <li>that value is not empty, and
     *  <li>that value is not disabled (i.e., it doesn't begin with "false:")
     * </ul>
     *
     * @param domain The domain of the option
     * @param option The name of the option
     * @since 0.6.3m1
     * @return True iff the option exists and is not empty, false otherwise
     */
    public boolean hasOptionString(final String domain, final String option) {
        return hasOptionString(domain, option, PERMISSIVE_VALIDATOR);
    }

    /**
     * Determines if this source has the specified String option.
     * <p>
     * A String option is considered to exist if:
     * <ul>
     *  <li>there is a value for the specified option that
     *      satisfies the specified validator,
     *  <li>that value is not empty, and
     *  <li>that value is not disabled (i.e., it doesn't begin with "false:")
     * </ul>
     *
     * @param domain The domain of the option
     * @param option The name of the option
     * @param validator The validator to use to check legal values
     * @since 0.6.5
     * @return True iff the option exists and is not empty, false otherwise
     */
    public boolean hasOptionString(final String domain, final String option,
            final Validator<String> validator) {
        String value;

        return hasOption(domain, option, validator)
                && !(value = getOption(domain, option, validator)).isEmpty()
                && !value.startsWith("false:");
    }

    /**
     * Determines if this source has the specified integer option.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     * @since 0.6.3m1
     * @return True iff the option exists and is parsable as an integer,
     * false otherwise.
     */
    public boolean hasOptionInt(final String domain, final String option) {
        return hasOptionString(domain, option, INT_VALIDATOR);
    }

    /**
     * Determines if this source has the specified character option.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     * @since 0.6.3m1
     * @return True iff the option exists and is parsable as a char,
     * false otherwise.
     */
    public boolean hasOptionChar(final String domain, final String option) {
        return hasOptionString(domain, option);
    }

    /**
     * Determines if this source has the specified colour option.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     * @since 0.6.3m1
     * @return True iff the option exists and is parsable as a colour,
     * false otherwise.
     */
    public boolean hasOptionColour(final String domain, final String option) {
        return hasOptionString(domain, option, COLOUR_VALIDATOR);
    }

    /**
     * Retrieves a boolean representation of the specified option.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     * @return The boolean representation of the option
     */
    public boolean hasOptionBool(final String domain, final String option) {
        return hasOption(domain, option, PERMISSIVE_VALIDATOR);
    }

    /**
     * Retrieves the specified option as a String, if it exists and satisfies
     * the specified validator.
     * <p>
     * If no fallback settings are supplied (or if fallback settings are
     * supplied and execution reaches the last fallback setting) AND if the
     * 'required' parameter is true, then all disabled values (i.e., those
     * starting with <code>false:</code>) will be ignored. To check if a valid
     * non-disabled value exists, call
     * {@link #hasOptionString(java.lang.String, java.lang.String, com.dmdirc.util.validators.Validator)}.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     * @param required Whether the specified option is required or not
     * @param validator The validator to use to check the value
     * @param fallbacks An ordered array of further domains and options
     * (in pairs) to try if the specified domain/option isn't found
     * @return The string representation of the option or null if optional
     * setting is not specified
     * @since 0.6.5
     */
    public String getOptionString(final String domain, final String option,
            final boolean required, final Validator<String> validator,
            final String ... fallbacks) {
        String value;

        @SuppressWarnings("unchecked")
        final Validator<String> newValidator = required && fallbacks.length == 0
                ? new ValidatorChain<String>(new DisabledOptionValidator(), validator)
                : validator;

        if (!hasOption(domain, option, newValidator)
                || (value = getOption(domain, option, newValidator)).startsWith("false:")) {
            return fallbacks.length >= 2 ? getOptionString(fallbacks[0],
                    fallbacks[1], required, validator,
                    Arrays.copyOfRange(fallbacks, 2, fallbacks.length)) : null;
        } else if (value.startsWith("true:")) {
            return value.substring(5);
        } else {
            return value;
        }
    }

    /**
     * Retrieves the specified option as a String, if it exists.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     * @param fallbacks An ordered array of further domains and options
     * (in pairs) to try if the specified domain/option isn't found
     * @return The string representation of the option or null if optional
     * setting is not specified
     * @since 0.6.3
     */
    public String getOptionString(final String domain, final String option,
            final String ... fallbacks) {
        return getOptionString(domain, option, true, PERMISSIVE_VALIDATOR, fallbacks);
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
    public Colour getOptionColour(final String domain, final String option,
            final String ... fallbacks) {
        final String value = getOptionString(domain, option, true, COLOUR_VALIDATOR, fallbacks);

        return value == null ? null : ColourManager.parseColour(value, null);
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

        if (hasOption(domain, option, PERMISSIVE_VALIDATOR)) {
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
     * @param fallbacks An ordered array of further domains and options
     * (in pairs) to try if the specified domain/option isn't found
     * @return The integer representation of the option
     */
    public Integer getOptionInt(final String domain, final String option,
            final String ... fallbacks) {
        return getOptionInt(domain, option, true, fallbacks);
    }

    /**
     * Retrieves an integral representation of the specified option.
     *
     * @since 0.6.5
     * @param domain The domain of the option
     * @param option The name of the option
     * @param required Whether this option is required or optional
     * @param fallbacks An ordered array of further domains and options
     * (in pairs) to try if the specified domain/option isn't found
     * @return The integer representation of the option
     */
    public Integer getOptionInt(final String domain, final String option,
            final boolean required, final String ... fallbacks) {
        final String value = getOptionString(domain, option, required, INT_VALIDATOR, fallbacks);

        return value == null ? null : Integer.parseInt(value.trim());
    }

}
