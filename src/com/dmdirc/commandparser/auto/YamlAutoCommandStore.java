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

package com.dmdirc.commandparser.auto;

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
import static com.dmdirc.util.YamlReaderUtils.optionalString;
import static com.dmdirc.util.YamlReaderUtils.requiredString;

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
public class YamlAutoCommandStore implements AutoCommandStore {

    /** The charset to use when reading and writing files. */
    private static final String CHARSET = "UTF-8";

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
        final Set<AutoCommand> commands = new HashSet<>();

        if (Files.exists(path)) {
            try (final InputStream stream = Files.newInputStream(path);
                    final InputStreamReader reader = new InputStreamReader(stream, CHARSET)) {
                final YamlReader yamlReader = new YamlReader(reader);
                commands.addAll(asList(yamlReader.read(), this::getCommand));
                yamlReader.close();
            } catch (IOException | IllegalArgumentException ex) {
                LOG.warn("Unable to read auto commands", ex);
            }
        }

        return commands;
    }

    @Override
    public void writeAutoCommands(final Set<AutoCommand> commands) {
        final List<Object> list = getCommands(commands);

        try (final OutputStream stream = Files.newOutputStream(path);
                final OutputStreamWriter writer = new OutputStreamWriter(stream, CHARSET)) {
            final YamlWriter yamlWriter = new YamlWriter(writer);
            yamlWriter.write(list);
            yamlWriter.close();
        } catch (IOException ex) {
            LOG.error("Unable to write auto commands", ex);
        }
    }

    /**
     * Builds an of auto command from the given YAML representation.
     *
     * @param autoCommand The raw object read from the YAML file.
     *
     * @return The corresponding auto command.
     */
    private Optional<AutoCommand> getCommand(final Object autoCommand) {
        try {
            final Map<Object, Object> map = asMap(autoCommand);
            final Optional<String> server =
                    Optional.ofNullable(optionalString(map, "server"));
            final Optional<String> network =
                    Optional.ofNullable(optionalString(map, "network"));
            final Optional<String> profile =
                    Optional.ofNullable(optionalString(map, "profile"));
            final String command = requiredString(map, "command");
            return Optional.of(new AutoCommand(server, network, profile, command));
        } catch (IllegalArgumentException ex) {
            LOG.info("Unable to read auto command", ex);
            return Optional.empty();
        }
    }

    /**
     * Gets a set of maps that can be written to a YAML file.
     *
     * @param commands The auto commands to be stored.
     *
     * @return A list of corresponding maps.
     */
    private List<Object> getCommands(final Collection<AutoCommand> commands) {
        final List<Object> res = new ArrayList<>(commands.size());
        for (AutoCommand command : commands) {
            final Map<Object, Object> map = new HashMap<>();

            if (command.getServer().isPresent()) {
                map.put("server", command.getServer().get());
            }

            if (command.getNetwork().isPresent()) {
                map.put("network", command.getNetwork().get());
            }

            if (command.getProfile().isPresent()) {
                map.put("profile", command.getProfile().get());
            }

            map.put("command", command.getResponse());
            res.add(map);
        }
        return res;
    }

}
