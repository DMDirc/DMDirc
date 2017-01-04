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

package com.dmdirc.commandparser.auto;

import com.dmdirc.util.io.yaml.BaseYamlStore;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.dmdirc.util.io.yaml.YamlReaderUtils.asMap;
import static com.dmdirc.util.io.yaml.YamlReaderUtils.optionalString;
import static com.dmdirc.util.io.yaml.YamlReaderUtils.requiredString;

/**
 * Store that reads and writes auto-commands from a YAML file on disk.
 * <p>
 * Auto-commands are written as a list of maps:
 * <pre><code>
 * ---
 * - server: irc.foo.bar
 *   network: quakenet
 *   profile: bob
 *   command: msg Q@Cserve.quakenet.org ...
 * </code></pre>
 */
public class YamlAutoCommandStore extends BaseYamlStore<AutoCommand> implements AutoCommandStore {

    private static final Logger LOG = LoggerFactory.getLogger(YamlAutoCommandStore.class);

    /** The path to the file to read and write auto commands in. */
    private final Path path;

    /**
     * Creates a new YAML auto command store.
     *
     * @param path    The path to the YAML file to read and write auto commands in.
     */
    public YamlAutoCommandStore(final Path path) {
        this.path = path;
    }

    @Override
    public Set<AutoCommand> readAutoCommands() {
        return new HashSet<>(read(path));
    }

    @Override
    public void writeAutoCommands(final Set<AutoCommand> commands) {
        write(path, commands);
    }

    @Override
    protected Optional<AutoCommand> convertFromYaml(final Object object) {
        try {
            final Map<Object, Object> map = asMap(object);
            final Optional<String> server =
                    Optional.ofNullable(optionalString(map, "server"));
            final Optional<String> network =
                    Optional.ofNullable(optionalString(map, "network"));
            final Optional<String> profile =
                    Optional.ofNullable(optionalString(map, "profile"));
            final String command = requiredString(map, "command");
            return Optional.of(AutoCommand.create(server, network, profile, command));
        } catch (IllegalArgumentException ex) {
            LOG.info("Unable to read auto command", ex);
            return Optional.empty();
        }
    }

    @Override
    protected Object convertToYaml(final AutoCommand object) {
        final Map<Object, Object> map = new HashMap<>();
        object.getServer().ifPresent(v -> map.put("server", v));
        object.getNetwork().ifPresent(v -> map.put("network", v));
        object.getProfile().ifPresent(v -> map.put("profile", v));
        map.put("command", object.getResponse());
        return map;
    }

}
