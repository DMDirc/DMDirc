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

package com.dmdirc.commandparser.aliases;

import com.dmdirc.util.BaseYamlStore;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.dmdirc.util.YamlReaderUtils.asMap;
import static com.dmdirc.util.YamlReaderUtils.requiredString;

/**
 * Store that reads and writes aliases from a YAML file on disk.
 * <p>
 * Aliases are written as a list of maps:
 * <pre><code>
 * ---
 * - name: quit
 *   min_arguments: 0
 *   substitution: exit $1-
 * </code></pre>
 */
public class YamlAliasStore extends BaseYamlStore<Alias> implements AliasStore {

    private static final Logger LOG = LoggerFactory.getLogger(YamlAliasStore.class);

    /** The path to the file to read and write aliases in. */
    private final Path path;
    /** The factory to use to create aliases. */
    private final AliasFactory factory;

    /**
     * Creates a new YAML alias store.
     *
     * @param path    The path to the YAML file to read and write aliases in.
     * @param factory The factory to use to create aliases.
     */
    public YamlAliasStore(final Path path, final AliasFactory factory) {
        this.path = path;
        this.factory = factory;
    }

    @Override
    public Set<Alias> readAliases() {
        return new HashSet<>(read(path));
    }

    @Override
    public void writeAliases(final Set<Alias> aliases) {
        write(path, aliases);
    }

    @Override
    protected Optional<Alias> convertFromYaml(final Object object) {
        try {
            final Map<Object, Object> map = asMap(object);
            final String name = requiredString(map, "name");
            final int minArguments = Integer.parseInt(requiredString(map, "min_arguments"));
            final String substitution = requiredString(map, "substitution");
            return Optional.of(factory.createAlias(name, minArguments, substitution));
        } catch (IllegalArgumentException ex) {
            LOG.info("Unable to read alias", ex);
            return Optional.empty();
        }
    }

    @Override
    protected Object convertToYaml(final Alias object) {
        final Map<Object, Object> map = new HashMap<>();
        map.put("name", object.getName());
        map.put("min_arguments", String.valueOf(object.getMinArguments()));
        map.put("substitution", object.getSubstitution());
        return map;
    }

}
