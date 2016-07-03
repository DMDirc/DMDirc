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

package com.dmdirc.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;

import static com.dmdirc.util.YamlReaderUtils.asList;
import static java.util.stream.Collectors.toList;

/**
 * Base class for a store that is backed by a YAML file.
 */
public abstract class BaseYamlStore<T> {

    /** The charset to use when reading and writing files. */
    private static final String CHARSET = "UTF-8";

    /** Logger to use. */
    private static final Logger LOG = LoggerFactory.getLogger(BaseYamlStore.class);

    /**
     * Reads a list of items from the YAML file at the specified path.
     *
     * <p>Each item is converted from an object into some form of model by invoking the
     * {@link #convertFromYaml} method.
     *
     * @param path The path of the file to read.
     * @return A list of successfully converted model objects.
     */
    protected List<T> read(final Path path) {
        final List<T> results = new ArrayList<>();
        if (Files.exists(path)) {
            try (final InputStream stream = Files.newInputStream(path);
                    final InputStreamReader reader = new InputStreamReader(stream, CHARSET)) {
                final YamlReader yamlReader = new YamlReader(reader);
                results.addAll(asList(yamlReader.read(), this::convertFromYaml));
                yamlReader.close();
            } catch (IOException | IllegalArgumentException ex) {
                LOG.warn("Unable to read from {}", path, ex);
            }
        }
        return results;
    }

    /**
     * Writes a collection of items to a YAML file at the specified path.
     *
     * <p>Each item is converted into a "plain" object by invoking the {@link #convertToYaml}
     * method.
     *
     * @param path The path of the file to write.
     * @param items A collection of items to write.
     */
    protected void write(final Path path, final Collection<T> items) {
        try (final OutputStream stream = Files.newOutputStream(path);
                final OutputStreamWriter writer = new OutputStreamWriter(stream, CHARSET)) {
            final YamlWriter yamlWriter = new YamlWriter(writer);
            yamlWriter.write(items.parallelStream().map(this::convertToYaml).collect(toList()));
            yamlWriter.close();
        } catch (IOException ex) {
            LOG.error(LogUtils.USER_ERROR, "Unable to write to {}", path, ex);
        }
    }

    protected abstract Optional<T> convertFromYaml(final Object object);

    protected abstract Object convertToYaml(final T object);

}
