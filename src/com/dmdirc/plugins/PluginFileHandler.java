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

package com.dmdirc.plugins;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.commandline.CommandLineOptionsModule.Directory;
import com.dmdirc.commandline.CommandLineOptionsModule.DirectoryType;
import com.dmdirc.events.UserErrorEvent;
import com.dmdirc.logger.ErrorLevel;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Locates and tracks plugin files on disk.
 */
@Singleton
public class PluginFileHandler {

    private final Path directory;
    private final DMDircMBassador eventBus;

    private final Collection<PluginMetaData> knownPlugins = new CopyOnWriteArrayList<>();

    @Inject
    public PluginFileHandler(
            @Directory(DirectoryType.PLUGINS) final Path directory,
            final DMDircMBassador eventBus) {
        this.directory = directory;
        this.eventBus = eventBus;
    }

    /**
     * Finds all plugin files on disk, loads their metadata, and validates them.
     *
     * @param manager The plugin manager to pass to new metadata instances.
     * @return Collection of valid plugins.
     */
    public Collection<PluginMetaData> refresh(final PluginManager manager) {
        final Collection<PluginMetaData> metadata = findAllPlugins(manager);

        // Deal with plugins that had errors
        metadata.stream().filter(PluginMetaData::hasErrors).forEach(this::reportErrors);
        metadata.removeIf(PluginMetaData::hasErrors);

        final Collection<PluginMetaData> newPlugins = getValidPlugins(metadata);

        knownPlugins.clear();
        knownPlugins.addAll(newPlugins);

        return newPlugins;
    }

    /**
     * Gets the collection of known plugins.
     *
     * @return A cached collection of plugins (no disk I/O will be performed).
     */
    public Collection<PluginMetaData> getKnownPlugins() {
        return Collections.unmodifiableCollection(knownPlugins);
    }

    /**
     * Finds all plugins on disk with loadable metadata.
     *
     * @param manager The plugin manager to pass to new metadata instances.
     * @return Collection of all plugins with loadable metadata.
     */
    private Collection<PluginMetaData> findAllPlugins(final PluginManager manager) {
        try {
            return Files.walk(directory, FileVisitOption.FOLLOW_LINKS)
                    .filter(p -> p.getFileName().toString().endsWith(".jar"))
                    .map(Path::toAbsolutePath)
                    .map(path -> getMetaData(path, manager))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (IOException ex) {
            eventBus.publish(new UserErrorEvent(ErrorLevel.HIGH, ex,
                    "Unable to read plugin directory", ""));
            return Collections.emptyList();
        }
    }

    /**
     * Attempts to get the metadata for a plugin at the specified path.
     *
     * @param path The path of the plugin to get metadata from.
     * @param manager The plugin manager to pass to new metadata instances.
     * @return The metadata if it could be created, {@code null} otherwise.
     */
    @Nullable
    private PluginMetaData getMetaData(final Path path, final PluginManager manager) {
        try {
            final PluginMetaData metaData = new PluginMetaData(
                    manager, new URL("jar:file:" + path + "!/META-INF/plugin.config"), path);
            metaData.load();
            return metaData;
        } catch (MalformedURLException ex) {
            eventBus.publish(new UserErrorEvent(ErrorLevel.MEDIUM, ex,
                    "Error creating URL for plugin " + path + ": " + ex.getMessage(), ""));
            return null;
        }
    }

    /**
     * Reports any errors present in the metadata to the event bus.
     *
     * @param pluginMetaData The metadata to read errors from.
     */
    private void reportErrors(final PluginMetaData pluginMetaData) {
        eventBus.publish(new UserErrorEvent(ErrorLevel.MEDIUM, null,
                "Error reading plugin metadata for plugin " + pluginMetaData.getPluginPath() + ": "
                        + pluginMetaData.getErrors(), ""));
    }

    /**
     * Gets all services defined for the given plugins.
     *
     * @param metadata The metadata to retrieve services from.
     * @return A multimap of service types to their implementations.
     */
    private Multimap<String, String> getServices(final Collection<PluginMetaData> metadata) {
        final Multimap<String, String> services = ArrayListMultimap.create();

        // Normal services
        metadata.stream()
                .map(PluginMetaData::getServices)
                .flatMap(Collection::stream)
                .map(service -> service.split(" ", 2))
                .forEach(pair -> services.put(pair[1], pair[0]));

        // Exports
        metadata.stream()
                .map(PluginMetaData::getExports)
                .flatMap(Collection::stream)
                .map(exportName -> exportName.split(" "))
                .map(parts -> parts.length > 4 ? parts[4] : parts[0])
                .forEach(name -> services.put("export", name));

        return services;
    }

    /**
     * Validates each plugin using a {@link PluginMetaDataValidator}, and returns those found to
     * be valid.
     *
     * @param metadata The collection of metadata to validate.
     * @return The collection of metadata that passed validation.
     */
    private Collection<PluginMetaData> getValidPlugins(
            final Collection<PluginMetaData> metadata) {
        // Collect a map by name
        final Map<String, PluginMetaData> metaDataByName = metadata.stream()
                .collect(Collectors.toMap(PluginMetaData::getName, Function.identity()));

        // Collect up services
        final Multimap<String, String> services = getServices(metadata);

        // Validate each in turn
        final Collection<PluginMetaData> res = new ArrayList<>();
        for (PluginMetaData target : metadata) {
            final PluginMetaDataValidator validator = new PluginMetaDataValidator(target);
            final Collection<String> results = validator.validate(metaDataByName, services);

            if (results.isEmpty()) {
                res.add(target);
            } else {
                eventBus.publish(new UserErrorEvent(ErrorLevel.MEDIUM, null,
                        "Plugin validation failed for " + target.getPluginPath() + ": " + results,
                        ""));
            }
        }

        return res;
    }

}
