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

import com.dmdirc.commandline.CommandLineOptionsModule.Directory;
import com.dmdirc.commandline.CommandLineOptionsModule.DirectoryType;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.dmdirc.util.LogUtils.USER_ERROR;

/**
 * Locates and tracks plugin files on disk.
 */
@Singleton
public class PluginFileHandler {

    private static final Logger LOG = LoggerFactory.getLogger(PluginFileHandler.class);
    private final Path directory;

    private final Collection<PluginMetaData> knownPlugins = new CopyOnWriteArrayList<>();

    @Inject
    public PluginFileHandler(
            @Directory(DirectoryType.PLUGINS) final Path directory) {
        this.directory = directory;
    }

    /**
     * Finds all plugin files on disk, loads their metadata, and validates them.
     *
     * @param manager The plugin manager to pass to new metadata instances.
     * @return Collection of valid plugins.
     */
    public Set<PluginMetaData> refresh(final PluginManager manager) {
        applyUpdates();

        final Set<PluginMetaData> metadata = findAllPlugins(manager);

        // Deal with plugins that had errors
        metadata.stream().filter(PluginMetaData::hasErrors).forEach(this::reportErrors);
        metadata.removeIf(PluginMetaData::hasErrors);

        final Set<PluginMetaData> newPlugins = getValidPlugins(metadata);

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
     * Recursively scans the plugin directory and attempts to apply any available updates.
     */
    private void applyUpdates() {
        try {
            Files.walk(directory, FileVisitOption.FOLLOW_LINKS)
                    .filter(p -> p.getFileName().toString().endsWith(".jar.update"))
                    .forEach(this::applyUpdate);
        } catch (IOException ex) {
            LOG.warn(USER_ERROR, "Unable to apply plugin updates", ex);
        }
    }

    /**
     * Attempts to apply an update located at the specified path. Pending updates named
     * '*.jar.update' will be renamed to '*.jar' (unless the original couldn't be deleted).
     *
     * @param path The path of the pending update to apply.
     */
    private void applyUpdate(final Path path) {
        final Path target = path.getParent()
                .resolve(path.getFileName().toString().replaceAll("\\.update$", ""));
        try {
            Files.deleteIfExists(target);
            Files.move(path, target);
        } catch (IOException ex) {
            LOG.warn(USER_ERROR, "Unable to apply update to {}", path, ex);
        }
    }

    /**
     * Finds all plugins on disk with loadable metadata.
     *
     * @param manager The plugin manager to pass to new metadata instances.
     * @return Collection of all plugins with loadable metadata.
     */
    private Set<PluginMetaData> findAllPlugins(final PluginManager manager) {
        try {
            return Files.walk(directory, FileVisitOption.FOLLOW_LINKS)
                    .filter(p -> p.getFileName().toString().endsWith(".jar"))
                    .map(Path::toAbsolutePath)
                    .map(path -> getMetaData(path, manager))
                    .collect(Collectors.toSet());
        } catch (IOException ex) {
            LOG.error(USER_ERROR, "Unable to read plugin directory.", ex);
            return Collections.emptySet();
        }
    }

    /**
     * Attempts to get the metadata for a plugin at the specified path.
     *
     * @param path The path of the plugin to get metadata from.
     * @param manager The plugin manager to pass to new metadata instances.
     * @return The metadata for the given plugin.
     */
    private PluginMetaData getMetaData(final Path path, final PluginManager manager) {
        final PluginMetaData metaData = new PluginMetaData(manager, path);
        metaData.load();
        return metaData;
    }

    /**
     * Reports any errors present in the metadata to the event bus.
     *
     * @param pluginMetaData The metadata to read errors from.
     */
    private void reportErrors(final PluginMetaData pluginMetaData) {
        LOG.warn(USER_ERROR, "Error reading plugin metadata for plugin {}: {}",
                pluginMetaData.getPluginPath(), pluginMetaData.getErrors());
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
    private Set<PluginMetaData> getValidPlugins(final Collection<PluginMetaData> metadata) {
        // Collect a map by name
        final Map<String, PluginMetaData> metaDataByName = metadata.stream()
                .collect(Collectors.toMap(PluginMetaData::getName, Function.identity()));

        // Collect up services
        final Multimap<String, String> services = getServices(metadata);

        // Validate each in turn
        final Set<PluginMetaData> res = new HashSet<>();
        for (PluginMetaData target : metadata) {
            final PluginMetaDataValidator validator = new PluginMetaDataValidator(target);
            final Collection<String> results = validator.validate(metaDataByName, services);

            if (results.isEmpty()) {
                res.add(target);
            } else {
                LOG.warn(USER_ERROR, "Plugin validation failed for {}: {}",
                        target.getPluginPath(), results);
            }
        }

        return res;
    }

}
