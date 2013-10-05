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

package com.dmdirc.config;

import com.dmdirc.interfaces.config.ReadOnlyConfigProvider;
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

import javax.inject.Provider;

/**
 * Defines methods to get options from a config provider in various forms.
 *
 * <p>This implementation supports the idea of optional/disabled settings.
 * This is where values may be prefixed with the strings <code>true:</code>
 * or <code>false:</code>, where the former has no effect on the value of
 * the setting, and the latter indicates that the user wishes to disable
 * the setting.
 *
 * <p>Disabled settings allow for optional settings to have default values; a
 * value can be added with the <code>false:</code> prefix which effectively
 * disables the default value and makes the setting act as though it does not
 * have a value. Note that for this sort of behaviour to make sense, it requires
 * an implementation that takes values from multiple sources, such as a
 * {@link ConfigManager}.
 */
public abstract class BaseConfigProvider implements ReadOnlyConfigProvider {

    /** A permissive validator to use when callers don't specify one. */
    private static final Validator<String> PERMISSIVE_VALIDATOR
            = new PermissiveValidator<>();

    /** A validator for integer settings. */
    private static final Validator<String> INT_VALIDATOR
            = new OptionalValidator(new NumericalValidator(-1, -1));

    /** A validator for colour settings. */
    private static final Validator<String> COLOUR_VALIDATOR
            = new OptionalValidator(new ColourValidator());

    /** Manager to use to convert colours. */
    private final Provider<ColourManager> colourManager;

    /**
     * Creates a new instance of {@link ConfigSource}.
     *
     * @param colourManager The colour manager to use to convert colours.
     */
    public BaseConfigProvider(final Provider<ColourManager> colourManager) {
        this.colourManager = colourManager;
    }

    /**
     * Creates a new instance of {@link ConfigSource} using the singleton
     * colour manager.
     *
     * @deprecated Should pass in a {@link ColourManager}.
     */
    @Deprecated
    public BaseConfigProvider() {
        this(new Provider<ColourManager>() {
            /** {@inheritDoc} */
            @Override
            public ColourManager get() {
                return new ColourManager(IdentityManager.getIdentityManager().getGlobalConfiguration());
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public String getOption(final String domain, final String option) {
        return getOption(domain, option, PERMISSIVE_VALIDATOR);
    }

    /** {@inheritDoc} */
    @Override
    public abstract String getOption(final String domain,
            final String option, final Validator<String> validator);

    /** {@inheritDoc} */
    @Override
    public boolean hasOptionString(final String domain, final String option) {
        return hasOptionString(domain, option, PERMISSIVE_VALIDATOR);
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasOptionString(final String domain, final String option,
            final Validator<String> validator) {
        String value;

        return hasOption(domain, option, validator)
                && !(value = getOption(domain, option, validator)).isEmpty()
                && !value.startsWith("false:");
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasOptionInt(final String domain, final String option) {
        return hasOptionString(domain, option, INT_VALIDATOR);
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasOptionChar(final String domain, final String option) {
        return hasOptionString(domain, option);
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasOptionColour(final String domain, final String option) {
        return hasOptionString(domain, option, COLOUR_VALIDATOR);
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasOptionBool(final String domain, final String option) {
        return hasOption(domain, option, PERMISSIVE_VALIDATOR);
    }

    /** {@inheritDoc} */
    @Override
    public String getOptionString(final String domain, final String option,
            final boolean required, final Validator<String> validator,
            final String ... fallbacks) {
        String value;

        @SuppressWarnings("unchecked")
        final Validator<String> newValidator = required && fallbacks.length == 0
                ? new ValidatorChain<>(new DisabledOptionValidator(), validator)
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

    /** {@inheritDoc} */
    @Override
    public String getOptionString(final String domain, final String option,
            final String ... fallbacks) {
        return getOptionString(domain, option, true, PERMISSIVE_VALIDATOR, fallbacks);
    }

    /** {@inheritDoc} */
    @Override
    public char getOptionChar(final String domain, final String option) {
        return getOption(domain, option).charAt(0);
    }

    /** {@inheritDoc} */
    @Override
    public Colour getOptionColour(final String domain, final String option,
            final String ... fallbacks) {
        final String value = getOptionString(domain, option, true, COLOUR_VALIDATOR, fallbacks);

        return value == null ? null : colourManager.get().getColourFromString(value, null);
    }

    /** {@inheritDoc} */
    @Override
    public boolean getOptionBool(final String domain, final String option) {
        return Boolean.parseBoolean(getOption(domain, option));
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getOptionList(final String domain, final String option,
            final boolean trimEmpty) {
        final List<String> res = new ArrayList<>();

        if (hasOption(domain, option, PERMISSIVE_VALIDATOR)) {
            for (String line : getOption(domain, option).split("\n")) {
                if (!line.isEmpty() || !trimEmpty) {
                    res.add(line);
                }
            }
        }

        return res;
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getOptionList(final String domain, final String option) {
        return getOptionList(domain, option, true);
    }

    /** {@inheritDoc} */
    @Override
    public Integer getOptionInt(final String domain, final String option,
            final String ... fallbacks) {
        return getOptionInt(domain, option, true, fallbacks);
    }

    /** {@inheritDoc} */
    @Override
    public Integer getOptionInt(final String domain, final String option,
            final boolean required, final String ... fallbacks) {
        final String value = getOptionString(domain, option, required, INT_VALIDATOR, fallbacks);

        return value == null ? null : Integer.parseInt(value.trim());
    }

}
