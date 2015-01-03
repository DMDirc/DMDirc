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

package com.dmdirc.ui.messages;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.events.AppErrorEvent;
import com.dmdirc.events.DisplayableEvent;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.util.colours.Colour;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.esotericsoftware.yamlbeans.YamlReader;

import static com.dmdirc.util.YamlReaderUtils.asMap;

/**
 * YAML-backed supplier of event formats.
 */
public class YamlEventFormatProvider implements EventFormatProvider {

    /** The charset to use when reading and writing files. */
    private static final String CHARSET = "UTF-8";

    private final Path path;
    private final DMDircMBassador eventBus;
    private final ColourManager colourManager;
    private final Map<String, EventFormat> formats = new HashMap<>();

    public YamlEventFormatProvider(final Path path, final DMDircMBassador eventBus,
            final ColourManager colourManager) {
        this.path = path;
        this.eventBus = eventBus;
        this.colourManager = colourManager;
    }

    public void load() {
        formats.clear();

        try (final InputStream stream = getClass().getResourceAsStream("format.yml")) {
            load(stream);
        } catch (IOException e) {
            eventBus.publishAsync(new AppErrorEvent(ErrorLevel.FATAL, e,
                    "Unable to load default event templates", ""));
        }

        if (Files.exists(path)) {
            try (final InputStream stream = Files.newInputStream(path)) {
                load(stream);
            } catch (IOException e) {
                eventBus.publishAsync(new AppErrorEvent(ErrorLevel.LOW, e,
                        "Unable to load event templates", ""));
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
        final Optional<Colour> foregroundColour = info.containsKey("colour")
                ? Optional.of(colourManager.getColourFromIrcCode(
                        Integer.parseInt(info.get("colour").toString())))
                : Optional.empty();
        return EventFormat.create(template, foregroundColour);
    }

    @Override
    public Optional<EventFormat> getFormat(final Class<? extends DisplayableEvent> eventType) {
        return Optional.ofNullable(formats.get(eventType.getSimpleName()));
    }

}
