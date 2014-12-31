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

import com.dmdirc.util.validators.Validator;

import java.util.List;
import java.util.Map;

/**
 * A source of configuration settings that can only be read from.
 */
public interface ReadOnlyConfigProvider {

    /**
     * Retrieves the specified option from this config source.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     *
     * @return The value of the option, or null if it doesn't exist
     */
    String getOption(String domain, String option);

    /**
     * Retrieves the first value for the specified option that matches the specified validator.
     *
     * @since 0.6.5
     * @param domain    The domain of the option
     * @param option    The name of the option
     * @param validator The validator to use to check legal values
     *
     * @return The value of the option, or null if no matching values exist
     */
    String getOption(String domain, String option, Validator<String> validator);

    /**
     * Determines if this source has a value for the specified option which matches the specified
     * validator.
     *
     * @since 0.6.5
     * @param domain    The domain of the option
     * @param option    The name of the option
     * @param validator The validator to use to check legal values
     *
     * @return True iff a matching option exists, false otherwise.
     */
    boolean hasOption(String domain, String option, Validator<String> validator);

    /**
     * Retrieves a boolean representation of the specified option.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     *
     * @return The boolean representation of the option
     */
    boolean getOptionBool(String domain, String option);

    /**
     * Retrieves the specified option as a character.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     *
     * @return The value of the option
     */
    char getOptionChar(String domain, String option);

    /**
     * Retrieves an integral representation of the specified option.
     *
     * @param domain    The domain of the option
     * @param option    The name of the option
     * @param fallbacks An ordered array of further domains and options (in pairs) to try if the
     *                  specified domain/option isn't found
     *
     * @return The integer representation of the option
     */
    Integer getOptionInt(String domain, String option, String... fallbacks);

    /**
     * Retrieves an integral representation of the specified option.
     *
     * @since 0.6.5
     * @param domain    The domain of the option
     * @param option    The name of the option
     * @param required  Whether this option is required or optional
     * @param fallbacks An ordered array of further domains and options (in pairs) to try if the
     *                  specified domain/option isn't found
     *
     * @return The integer representation of the option
     */
    Integer getOptionInt(String domain, String option, boolean required, String... fallbacks);

    /**
     * Retrieves a list representation of the specified option.
     *
     * @param domain    The domain of the option
     * @param option    The name of the option
     * @param trimEmpty Whether or not to trim empty lines
     *
     * @return The list representation of the option
     */
    List<String> getOptionList(String domain, String option, boolean trimEmpty);

    /**
     * Retrieves a list representation of the specified option, trimming empty lines by default.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     *
     * @return The list representation of the option
     */
    List<String> getOptionList(String domain, String option);

    /**
     * Retrieves the specified option as a String, if it exists and satisfies the specified
     * validator.
     * <p>
     * If no fallback settings are supplied (or if fallback settings are supplied and execution
     * reaches the last fallback setting) AND if the 'required' parameter is true, then all disabled
     * values (i.e., those starting with <code>false:</code>) will be ignored. To check if a valid
     * non-disabled value exists, call
     * {@link #hasOptionString(java.lang.String, java.lang.String, com.dmdirc.util.validators.Validator)}.
     *
     * @param domain    The domain of the option
     * @param option    The name of the option
     * @param required  Whether the specified option is required or not
     * @param validator The validator to use to check the value
     * @param fallbacks An ordered array of further domains and options (in pairs) to try if the
     *                  specified domain/option isn't found
     *
     * @return The string representation of the option or null if optional setting is not specified
     *
     * @since 0.6.5
     */
    String getOptionString(String domain, String option, boolean required,
            Validator<String> validator, String... fallbacks);

    /**
     * Retrieves the specified option as a String, if it exists.
     *
     * @param domain    The domain of the option
     * @param option    The name of the option
     * @param fallbacks An ordered array of further domains and options (in pairs) to try if the
     *                  specified domain/option isn't found
     *
     * @return The string representation of the option or null if optional setting is not specified
     *
     * @since 0.6.3
     */
    String getOptionString(String domain, String option, String... fallbacks);

    /**
     * Retrieves a boolean representation of the specified option.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     *
     * @return The boolean representation of the option
     */
    boolean hasOptionBool(String domain, String option);

    /**
     * Determines if this source has the specified character option.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     *
     * @since 0.6.3m1
     * @return True iff the option exists and is parsable as a char, false otherwise.
     */
    boolean hasOptionChar(String domain, String option);

    /**
     * Determines if this source has the specified integer option.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     *
     * @since 0.6.3m1
     * @return True iff the option exists and is parsable as an integer, false otherwise.
     */
    boolean hasOptionInt(String domain, String option);

    /**
     * Determines if this source has the specified String option.
     * <p>
     * A String option is considered to exist if:
     * <ul>
     * <li>there is a value for the specified option,
     * <li>that value is not empty, and
     * <li>that value is not disabled (i.e., it doesn't begin with "false:")
     * </ul>
     *
     * @param domain The domain of the option
     * @param option The name of the option
     *
     * @since 0.6.3m1
     * @return True iff the option exists and is not empty, false otherwise
     */
    boolean hasOptionString(String domain, String option);

    /**
     * Determines if this source has the specified String option.
     * <p>
     * A String option is considered to exist if:
     * <ul>
     * <li>there is a value for the specified option that satisfies the specified validator,
     * <li>that value is not empty, and
     * <li>that value is not disabled (i.e., it doesn't begin with "false:")
     * </ul>
     *
     * @param domain    The domain of the option
     * @param option    The name of the option
     * @param validator The validator to use to check legal values
     *
     * @since 0.6.5
     * @return True iff the option exists and is not empty, false otherwise
     */
    boolean hasOptionString(String domain, String option, Validator<String> validator);

    /**
     * Returns the name of all the options in the specified domain. If the domain doesn't exist, an
     * empty list is returned.
     *
     * @param domain The domain to search
     *
     * @return A list of options in the specified domain
     */
    Map<String, String> getOptions(String domain);

}
