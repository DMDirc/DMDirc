/*
 * Copyright (c) 2006-2014 DMDirc Developers
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;

import static com.dmdirc.util.YamlReaderUtils.asList;
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
public class YamlAliasStore implements AliasStore {

    /** The charset to use when reading and writing files. */
    private static final String CHARSET = "UTF-8";

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
        final Set<Alias> aliases = new HashSet<>();

        if (Files.exists(path)) {
            try (final InputStream stream = Files.newInputStream(path);
                    final InputStreamReader reader = new InputStreamReader(stream, CHARSET)) {
                final YamlReader yamlReader = new YamlReader(reader);
                aliases.addAll(asList(yamlReader.read(), this::getAlias));
                yamlReader.close();
            } catch (IOException | IllegalArgumentException ex) {
                LOG.warn("Unable to read aliases", ex);
            }
        }

        return aliases;
    }

    @Override
    public void writeAliases(final Set<Alias> aliases) {
        final List<Object> list = getAliases(aliases);

        try (final OutputStream stream = Files.newOutputStream(path);
                final OutputStreamWriter writer = new OutputStreamWriter(stream, CHARSET)) {
            final YamlWriter yamlWriter = new YamlWriter(writer);
            yamlWriter.write(list);
            yamlWriter.close();
        } catch (IOException ex) {
            LOG.error("Unable to write aliases", ex);
        }
    }

    /**
     * Builds an alias from the given YAML representations.
     *
     * @param alias The raw object read from the YAML file.
     *
     * @return The corresponding aliases.
     */
    private Optional<Alias> getAlias(final Object alias) {
        try {
            final Map<Object, Object> map = asMap(alias);
            final String name = requiredString(map, "name");
            final int minArguments = Integer.parseInt(requiredString(map, "min_arguments"));
            final String substitution = requiredString(map, "substitution");
            return Optional.of(factory.createAlias(name, minArguments, substitution));
        } catch (IllegalArgumentException ex) {
            LOG.info("Unable to read alias", ex);
            return Optional.empty();
        }
    }

    /**
     * Gets a set of maps that can be written to a YAML file.
     *
     * @param aliases The aliases to be stored.
     *
     * @return A list of corresponding maps.
     */
    private List<Object> getAliases(final Collection<Alias> aliases) {
        final List<Object> res = new ArrayList<>(aliases.size());
        for (Alias alias : aliases) {
            final Map<Object, Object> map = new HashMap<>();
            map.put("name", alias.getName());
            map.put("min_arguments", String.valueOf(alias.getMinArguments()));
            map.put("substitution", alias.getSubstitution());
            res.add(map);
        }
        return res;
    }

}
