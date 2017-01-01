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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.WillClose;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.yamlbeans.YamlReader;

import static com.dmdirc.util.YamlReaderUtils.asList;
import static com.dmdirc.util.YamlReaderUtils.asMap;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Reads preferences information from a meta-data file.
 * <p>
 * Meta-data files are expected to be valid YAML with UTF-8 encoding, containing a dictionary with a
 * single entry with the key {@link #CATEGORIES_KEY}. This key contains a list of preferences
 * categories.
 * <p>
 * The outer level of a preferences meta-data file should look like:
 * <pre><code>
 * ----
 * categories:
 *   - [category data]
 *   - [category data]
 * </code></pre>
 *
 * @see CategoryReader
 */
public class PreferencesReader {

    /** The charset to use when reading files. */
    private static final String CHARSET = "UTF-8";
    /** The top-level key that contains a list of categories. */
    private static final String CATEGORIES_KEY = "categories";
    private static final Logger LOG = LoggerFactory.getLogger(PreferencesReader.class);

    private final InputStream input;
    private final List<PreferencesCategory> categories = new ArrayList<>();

    /**
     * Creates a new instance of {@link PreferencesReader} that will read from the given stream.
     *
     * @param input The input stream to read preferences meta-data from. Will be closed once read.
     */
    public PreferencesReader(@WillClose final InputStream input) {
        this.input = checkNotNull(input);
    }

    /**
     * Gets a list of all categories that were defined in the given input.
     *
     * @return A list of all categories given in the input.
     */
    public List<PreferencesCategory> getCategories() {
        return Collections.unmodifiableList(categories);
    }

    /**
     * Reads the preferences data.
     *
     * @throws PreferencesReaderException If the stream cannot be read, or if preferences cannot be
     *                                    parsed.
     */
    public void read() throws PreferencesReaderException {
        try (final InputStreamReader reader = new InputStreamReader(input, CHARSET)) {
            final YamlReader yamlReader = new YamlReader(reader);
            final Object top = yamlReader.read();
            LOG.debug("Found top-level {} element", top.getClass().getName());
            readMap(asMap(top));
        } catch (IOException ex) {
            throw new PreferencesReaderException("Unable to read input stream", ex);
        } catch (IllegalArgumentException ex) {
            throw new PreferencesReaderException(ex.getMessage(), ex);
        }
    }

    private void readMap(final Map<Object, Object> map) throws PreferencesReaderException {
        if (!map.containsKey(CATEGORIES_KEY)) {
            throw new PreferencesReaderException("No categories element found");
        }

        final Object value = map.get(CATEGORIES_KEY);
        LOG.debug("Found categories value: {}", value.getClass().getName());
        readCategoriesList(asList(value));
    }

    private void readCategoriesList(final List<Object> list) {
        for (Object category : list) {
            LOG.debug("Found category entry: {}", category.getClass().getName());
            final CategoryReader reader = new CategoryReader(asMap(category));
            reader.read();
            categories.add(reader.getCategory());
        }
    }

}
