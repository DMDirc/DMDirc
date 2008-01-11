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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author chris
 */
public class PreferencesCategory {
        
    private final String title;
    
    private final String description;
    
    private final List<PreferencesSetting> settings = new ArrayList<PreferencesSetting>();
    
    private final List<PreferencesCategory> subcats = new ArrayList<PreferencesCategory>();

    public PreferencesCategory(final String title, final String description) {
        this.title = title;
        this.description = description;
    }
    
    public void addSetting(final PreferencesSetting setting) {
        settings.add(setting);
    }
    
    public void addSubCategory(final PreferencesCategory subcategory) {
        subcats.add(subcategory);
    }

    public String getDescription() {
        return description;
    }

    public List<PreferencesSetting> getSettings() {
        return settings;
    }

    public List<PreferencesCategory> getSubcats() {
        return subcats;
    }

    public String getTitle() {
        return title;
    }

}
