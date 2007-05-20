/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.identities;

import com.dmdirc.Config;

/**
 * Implements a non-static wrapper of the global Config class.
 * @author chris
 */
public final class GlobalConfig implements ConfigSource {
    
    /** The address that this config applies to. */
    private final ConfigTarget myTarget = new ConfigTarget();
    
    /** Creates a new instance of GlobalConfig. */
    public GlobalConfig() {
        myTarget.setGlobal();
    }

    /**
     * Determines whether this config source has a setting for the specified
     * option in the specified domain. 
     * @param domain The domain of the option
     * @param option The name of the option
     * @return True iff this source has the option, false otherwise
     */    
    public boolean hasOption(final String domain, final String option) {
        return Config.hasOption(domain, option);
    }

    /**
     * Retrieves the specified option from this config source.
     * @param domain The domain of the option
     * @param option The name of the option
     * @return The value of the specified option
     */    
    public String getOption(final String domain, final String option) {
        return Config.getOption(domain, option);
    }

    /**
     * Sets the specified option in this source to the specified value.
     * @param domain The domain of the option
     * @param option The name of the option
     * @param value The new value for the option
     */    
    public void setOption(final String domain, final String option, final String value) {
        Config.setOption(domain, option, value);
    }
    
    /**
     * Retrieves the target of this config source.
     * @return This source's target
     */
    public ConfigTarget getTarget() {
        return myTarget;
    }

    /**
     * Compares this config source to another.
     * @param target The object to compare to.
     * @return -1 if this object is less than the other, +1 if this object is
     * greater, 0 if they're equal.
     */
    public int compareTo(final ConfigSource target) {
        return target.getTarget().compareTo(myTarget);
    }
    
}
