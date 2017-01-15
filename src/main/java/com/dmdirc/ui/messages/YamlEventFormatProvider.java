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

package com.dmdirc.ui.messages;

import com.dmdirc.events.DisplayProperty;
import com.dmdirc.events.DisplayPropertyMap;
import com.dmdirc.events.DisplayableEvent;
import com.dmdirc.util.colours.Colour;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.yamlbeans.YamlReader;

import static com.dmdirc.util.LogUtils.FATAL_APP_ERROR;
import static com.dmdirc.util.LogUtils.USER_ERROR;
import static com.dmdirc.util.io.yaml.YamlReaderUtils.asMap;

/**
 * YAML-backed supplier of event formats.
 */
public class YamlEventFormatProvider implements EventFormatProvider {

    private static final Logger LOG = LoggerFactory.getLogger(YamlEventFormatProvider.class);

    /** The charset to use when reading and writing files. */
    private static final String CHARSET = "UTF-8";

    private final Path path;
    private final ColourManager colourManager;
    private final DisplayLocationManager displayLocationManager;
    private final Map<String, EventFormat> formats = new HashMap<>();

    public YamlEventFormatProvider(final Path path, final ColourManager colourManager,
                                   final DisplayLocationManager displayLocationManager) {
        this.path = path;
        this.colourManager = colourManager;
        this.displayLocationManager = displayLocationManager;
    }

    public void load() {
        formats.clear();

        try (final InputStream stream = getClass().getResourceAsStream("format.yml")) {
            load(stream);
        } catch (IOException e) {
            LOG.error(FATAL_APP_ERROR, "Unable to load default event templates", e);
        }

        if (Files.exists(path)) {
            try (final InputStream stream = Files.newInputStream(path)) {
                load(stream);
            } catch (IOException e) {
                LOG.info(USER_ERROR, "Unable to load event templates from {}", path, e);
            }
        }
    }

    private void load(final InputStream stream) throws IOException {
        try (final InputStreamReader reader = new InputStreamReader(stream, CHARSET)) {
            final YamlReader yamlReader = new YamlReader(reader);
            final Object root = yamlReader.read();
            final Map<Object, Object> entries = asMap(root);
            entries.forEach((k, v) -> formats.put(k.toString(), readFormat(v)));
            yamlReader.close();
        }
    }

    private EventFormat readFormat(final Object format) {
        final Map<Object, Object> info = asMap(format);
        final String template = info.get("format").toString();
        final Optional<String> beforeTemplate = info.containsKey("before")
                ? Optional.of(info.get("before").toString())
                : Optional.empty();
        final Optional<String> afterTemplate = info.containsKey("after")
                ? Optional.of(info.get("after").toString())
                : Optional.empty();
        final Optional<String> iterateProperty = info.containsKey("iterate")
                ? Optional.of(info.get("iterate").toString())
                : Optional.empty();
        return EventFormat.create(
                template, beforeTemplate, afterTemplate, iterateProperty,
                getDisplayProperties(info));
    }

    private DisplayPropertyMap getDisplayProperties(final Map<Object, Object> info) {
        final DisplayPropertyMap map = new DisplayPropertyMap();
        if (info.containsKey("colour")) {
            map.put(DisplayProperty.FOREGROUND_COLOUR,
                    colourManager.getColourFromString(
                            info.get("colour").toString(), Colour.BLACK));
        }
        if (info.containsKey("background")) {
            map.put(DisplayProperty.BACKGROUND_COLOUR,
                    colourManager.getColourFromString(
                            info.get("background").toString(), Colour.BLACK));
        }
        if (info.containsKey("timestamp")) {
            map.put(DisplayProperty.NO_TIMESTAMPS,
                    !info.get("timestamp").toString().toLowerCase().matches("y|yes|true|1|on"));
        }
        if (info.containsKey("displaywindow")) {
            try {
                map.put(DisplayProperty.DISPLAY_LOCATION, displayLocationManager.getDisplayLocation(
                        info.get("displaywindow").toString()).orElseThrow(() -> new IllegalArgumentException()));
            } catch (final IllegalArgumentException iae) {
                LOG.info(USER_ERROR, "Invalid displaywindow specified for: {}.\nValid values are: {}",
                        info.get("displaywindow").toString(),
                        displayLocationManager.getDisplayLocations().toString());
            }
        }
        return map;
    }

    @Override
    public Optional<EventFormat> getFormat(final Class<? extends DisplayableEvent> eventType) {
        return Optional.ofNullable(formats.get(eventType.getSimpleName()));
    }

    @Override
    public void reload() {
        load();
    }
}
