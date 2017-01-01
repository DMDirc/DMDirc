/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.config.prefs.reader;

import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesSetting;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.dmdirc.util.YamlReaderUtils.optionalString;
import static com.dmdirc.util.YamlReaderUtils.requiredString;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Reads a single category.
 * <p>
 * Categories must be specified as a map containing some information about the category itself, and
 * a list of items.
 * <p>
 * Each category must have both a title (specified with the key {@link #TITLE_KEY}) and a list of
 * items (specified with {@link #ITEMS_KEY}). It may optionally have a description
 * ({@link #DESCRIPTION_KEY}), parent ({@link #PARENT_KEY}), icon ({@link #ICON_KEY}) and a default
 * domain for its items ({@link #DOMAIN_KEY}).
 * <p>
 * A category expressed in YAML will resemble:
 * <pre><code>
 *   - name: FooBar settings
 *     description: Settings that control how the Foo interacts with Bar
 *     icon: foo-bar-settings
 *     items:
 *       - [item]
 *       - [item]
 * </code></pre>
 */
public class CategoryReader {

    private static final String TITLE_KEY = "name";
    private static final String DESCRIPTION_KEY = "description";
    private static final String PARENT_KEY = "parent";
    private static final String ICON_KEY = "icon";
    private static final String DOMAIN_KEY = "domain";
    private static final String ITEMS_KEY = "items"; // NOPMD

    private final Map<Object, Object> data;
    private final List<PreferencesSetting> items = new LinkedList<>();
    private String title;
    private String description;
    private String icon;
    private String parent;  // NOPMD
    private String domain;  // NOPMD

    public CategoryReader(final Map<Object, Object> data) {
        this.data = checkNotNull(data);
    }

    public void read() {
        title = requiredString(data, TITLE_KEY);
        description = optionalString(data, DESCRIPTION_KEY);
        icon = optionalString(data, ICON_KEY);
        parent = optionalString(data, PARENT_KEY);
        domain = optionalString(data, DOMAIN_KEY);
    }

    public PreferencesCategory getCategory() {
        final PreferencesCategory category = new PreferencesCategory(title, description, icon);

        items.forEach(category::addSetting);

        return category;
    }

}
