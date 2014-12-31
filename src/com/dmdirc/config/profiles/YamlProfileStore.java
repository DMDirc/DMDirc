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

package com.dmdirc.config.profiles;

import com.dmdirc.util.BaseYamlStore;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.dmdirc.util.YamlReaderUtils.asList;
import static com.dmdirc.util.YamlReaderUtils.asMap;
import static com.dmdirc.util.YamlReaderUtils.optionalString;
import static com.dmdirc.util.YamlReaderUtils.requiredString;

/**
 * Store that reads and writes profiles from a YAML file on disk.
 */
public class YamlProfileStore extends BaseYamlStore<Profile> implements ProfileStore {

    private static final Logger LOG = LoggerFactory.getLogger(YamlProfileStore.class);

    /** The path to the file to read and write auto commands in. */
    private final Path path;

    /**
     * Creates a new YAML profiles store.
     *
     * @param path The path to the YAML file to read and write profiles in.
     */
    public YamlProfileStore(final Path path) {
        this.path = path;
    }

    @Override
    public Collection<Profile> readProfiles() {
        return read(path);
    }

    @Override
    public void writeProfiles(final Collection<Profile> profiles) {
        write(path, profiles);
    }

    @Override
    protected Optional<Profile> convertFromYaml(final Object object) {
        try {
            final Map<Object, Object> map = asMap(object);
            final String name = requiredString(map, "name");
            final String realname = requiredString(map, "realname");
            final Optional<String> ident = Optional.ofNullable(optionalString(map, "ident"));
            final List<String> nicknames = asList(map.get("nicknames"), s -> Optional.of(s.toString()));
            return Optional.of(new Profile(name, realname, ident, nicknames));
        } catch (IllegalArgumentException ex) {
            LOG.info("Unable to read profile", ex);
            return Optional.empty();
        }
    }

    @Override
    protected Object convertToYaml(final Profile object) {
        final Map<Object, Object> map = new HashMap<>();
        map.put("name", object.getName());
        map.put("realname", object.getRealname());
        map.put("nicknames", object.getNicknames().toArray());
        object.getIdent().ifPresent(v -> map.put("ident", v));
        return map;
    }

}
