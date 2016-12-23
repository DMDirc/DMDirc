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

package com.dmdirc.updater.manager;

import com.dmdirc.config.GlobalConfig;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.updater.UpdateComponent;

import javax.inject.Inject;

/**
 * An {@link UpdateComponentPolicy} which checks if the component is enabled in DMDirc's
 * configuration.
 */
public class ConfigComponentPolicy implements UpdateComponentPolicy {

    /** The configuration manager to check. */
    private final AggregateConfigProvider manager;

    /**
     * Creates a new instance of {@link ConfigComponentPolicy}.
     *
     * @param manager The config provider to use to check if components are enabled.
     */
    @Inject
    public ConfigComponentPolicy(@GlobalConfig final AggregateConfigProvider manager) {
        this.manager = manager;
    }

    @Override
    public boolean canCheck(final UpdateComponent component) {
        return !manager.hasOptionBool("updater", "enable-" + component.getName())
                || manager.getOptionBool("updater", "enable-" + component.getName());
    }

}
