/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
package com.dmdirc.config.prefs;

import com.dmdirc.config.IdentityManager;

/**
 *
 * @author chris
 */
public abstract class PreferencesSetting {
    
    protected final String domain;
    
    protected final String key;
    
    protected final String title;
    
    protected final String tooltip;
    
    protected final String fallback;
    
    protected String value;

    public PreferencesSetting(final String domain, final String key, final String title,
            final String tooltip, final String fallback) {
        this.domain = domain;
        this.key = key;
        this.title = title;
        this.tooltip = tooltip;
        this.fallback = fallback;
        
        value = IdentityManager.getGlobalConfig().getOption(domain, key, fallback);
    }

    public String getDomain() {
        return domain;
    }

    public String getKey() {
        return key;
    }

    public String getTitle() {
        return title;
    }

    public String getTooltip() {
        return tooltip;
    }

    public String getFallback() {
        return fallback;
    }
    
    public abstract boolean isValid(final String newValue);
    
    public void set(final String newValue) {
        if (isValid(newValue)) {
            value = newValue;
        } else {
            throw new IllegalArgumentException("Specified value is not valid");
        }
    }
    
    public void save() {
        IdentityManager.getConfigIdentity().setOption(domain, key, value);
    }

}
