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

package com.dmdirc.ui.messages;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.events.AppErrorEvent;
import com.dmdirc.events.DisplayableEvent;
import com.dmdirc.logger.ErrorLevel;

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
 * YAML-backed supplier of event templates.
 */
public class YamlEventTemplateProvider implements EventTemplateProvider {

    /** The charset to use when reading and writing files. */
    private static final String CHARSET = "UTF-8";

    private final Path path;
    private final DMDircMBassador eventBus;
    private final Map<String, String> formats = new HashMap<>();

    public YamlEventTemplateProvider(final Path path, final DMDircMBassador eventBus) {
        this.path = path;
        this.eventBus = eventBus;
    }

    public void load() {
        formats.clear();

        try (final InputStream stream = getClass().getResourceAsStream("format.yml")) {
            load(stream);
        } catch (IOException e) {
            eventBus.publishAsync(new AppErrorEvent(ErrorLevel.LOW, e,
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

    private void load(final InputStream stream) {
        try (final InputStreamReader reader = new InputStreamReader(stream, CHARSET)) {
            final YamlReader yamlReader = new YamlReader(reader);
            final Object root = yamlReader.read();
            final Map<Object, Object> entries = asMap(root);
            entries.forEach((k, v) -> formats.put(k.toString(), v.toString()));
            yamlReader.close();
        } catch (IOException e) {
            eventBus.publishAsync(new AppErrorEvent(ErrorLevel.LOW, e,
                    "Unable to load event templates", ""));
        }
    }

    @Override
    public Optional<String> getTemplate(final Class<? extends DisplayableEvent> eventType) {
        return Optional.ofNullable(formats.get(eventType.getSimpleName()));
    }

}
