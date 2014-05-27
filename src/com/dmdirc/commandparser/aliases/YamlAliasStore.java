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

import com.dmdirc.commandline.CommandLineOptionsModule.Directory;
import com.dmdirc.commandline.CommandLineOptionsModule.DirectoryType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

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

    /** The directory to find the aliases file in. */
    private final String directory;
    /** The factory to use to create aliases. */
    private final AliasFactory factory;

    @Inject
    public YamlAliasStore(
            @Directory(DirectoryType.BASE) final String directory,
            final AliasFactory factory) {
        this.directory = directory;
        this.factory = factory;
    }

    @Override
    public Set<Alias> readAliases() {
        final File file = new File(directory, "aliases.yml");
        final Set<Alias> aliases = new HashSet<>();

        if (file.exists()) {
            try (final InputStream stream = new FileInputStream(file);
                    final InputStreamReader reader = new InputStreamReader(stream, CHARSET)) {
                final YamlReader yamlReader = new YamlReader(reader);
                final Object root = yamlReader.read();
                aliases.addAll(getAliases(asList(root)));
                yamlReader.close();
            } catch (IOException | IllegalArgumentException ex) {
                LOG.warn("Unable to read aliases", ex);
            }
        }

        return aliases;
    }

    @Override
    public void writeAliases(final Set<Alias> aliases) {
        final File file = new File(directory, "aliases.yml");
        final List<Object> list = getAliases(aliases);

        try (final OutputStream stream = new FileOutputStream(file);
                final OutputStreamWriter writer = new OutputStreamWriter(stream, CHARSET)) {
            final YamlWriter yamlWriter = new YamlWriter(writer);
            yamlWriter.write(list);
            yamlWriter.close();
        } catch (IOException ex) {
            LOG.error("Unable to write aliases", ex);
        }
    }

    /**
     * Builds a set of aliases from the given YAML representations.
     *
     * @param objects The raw objects read from the YAML file.
     *
     * @return A list of corresponding aliases.
     */
    private List<Alias> getAliases(final List<Object> objects) {
        final List<Alias> res = new ArrayList<>(objects.size());
        for (Object alias : objects) {
            try {
                final Map<Object, Object> map = asMap(alias);
                final String name = requiredString(map, "name");
                final int minArguments = Integer.parseInt(requiredString(map, "min_arguments"));
                final String substitution = requiredString(map, "substitution");
                res.add(factory.createAlias(name, minArguments, substitution));
            } catch (IllegalArgumentException ex) {
                LOG.info("Unable to read alias", ex);
            }
        }
        return res;
    }

    /**
     * Gets a set of maps that can be written to a YAML file.
     *
     * @param aliases The aliases to be stored.
     *
     * @return A list of corresponding maps.
     */
    private List<Object> getAliases(final Set<Alias> aliases) {
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
