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

import com.dmdirc.updater.Version;

import com.google.common.collect.Multimap;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Validates that the values specified in a plugin's meta data are correct and all requirements are
 * met.
 *
 * @since 0.6.6
 */
public class PluginMetaDataValidator {

    /** The metadata being validated. */
    private final PluginMetaData metadata;
    /** A collection of errors which have been identified. */
    private final Collection<String> errors = new ArrayList<>();

    /**
     * Creates a new metadata validator.
     *
     * @param metadata The metadata to be validated
     */
    public PluginMetaDataValidator(final PluginMetaData metadata) {
        this.metadata = metadata;
    }

    /**
     * Validates the metadata file.
     *
     * @param plugins  A map of known plugins to their short names
     * @param services A map of known services
     *
     * @return A collection of errors that occurred (if any)
     */
    public Collection<String> validate(final Map<String, PluginMetaData> plugins,
            final Multimap<String, String> services) {
        errors.clear();

        checkMetaData();
        checkRequirements(plugins, services);

        return Collections.unmodifiableCollection(errors);
    }

    /**
     * Checks that the metadata values are correct.
     */
    protected void checkMetaData() {
        if (!metadata.getVersion().isValid()) {
            errors.add("Missing or invalid 'version'");
        }

        if (metadata.getAuthor() == null || metadata.getAuthor().isEmpty()) {
            errors.add("Missing or invalid 'author'");
        }

        if (metadata.getName() == null || metadata.getName().isEmpty() || metadata.getName().
                indexOf(' ') != -1) {
            errors.add("Missing or invalid 'name'");
        }

        if (metadata.getMainClass() == null || metadata.getMainClass().isEmpty()) {
            errors.add("Missing or invalid 'mainclass'");
        }
    }

    /**
     * Checks that the plugin's requirements are met.
     *
     * @param plugins  A map of known plugins by their short names
     * @param services A map of known services
     */
    protected void checkRequirements(final Map<String, PluginMetaData> plugins,
            final Multimap<String, String> services) {
        checkOS(metadata.getRequirements().get("os"), System.getProperty("os.name"), System.
                getProperty("os.version"), System.getProperty("os.arch"));
        checkFiles(metadata.getRequirements().get("files"));
        checkPlugins(plugins, metadata.getRequirements().get("plugins"));
        checkServices(services, metadata.getRequiredServices());
    }

    /**
     * Check if the services required by this plugin are available.
     *
     * @param knownServices A map of known services
     * @param services      Required services
     */
    private void checkServices(final Multimap<String, String> knownServices,
            final Collection<String> services) {
        if (services == null || services.isEmpty()) {
            return;
        }

        for (String requirement : services) {
            final String[] bits = requirement.split(" ", 2);
            final String name = bits[0];
            final String type = bits[1];

            if (!knownServices.containsKey(type)
                    || !"any".equalsIgnoreCase(name)
                    && !knownServices.containsEntry(type, name)) {
                errors.add("Service " + name + " of type " + type
                        + " not available");
            }
        }
    }

    /**
     * Checks to see if the OS requirements of the plugin are satisfied. If the desired string is
     * empty, the test passes. Otherwise it is used as one to three colon-delimited regular
     * expressions, to test the name, version and architecture of the OS, respectively. On failure,
     * the requirementsError field will contain a user-friendly error message.
     *
     * @param desired       The desired OS requirements
     * @param actualName    The actual name of the OS
     * @param actualVersion The actual version of the OS
     * @param actualArch    The actual architecture of the OS
     */
    protected void checkOS(final String desired, final String actualName, final String actualVersion,
            final String actualArch) {
        if (desired == null || desired.isEmpty()) {
            return;
        }

        final String[] desiredParts = desired.split(":");

        if (!actualName.toLowerCase().matches(desiredParts[0])) {
            errors.add("Invalid OS. (Wanted: '" + desiredParts[0] + "', actual: '" + actualName
                    + "')");
        } else if (desiredParts.length > 1 && !actualVersion.toLowerCase().matches(desiredParts[1])) {
            errors.add("Invalid OS version. (Wanted: '" + desiredParts[1] + "', actual: '"
                    + actualVersion + "')");
        } else if (desiredParts.length > 2 && !actualArch.toLowerCase().matches(desiredParts[2])) {
            errors.add("Invalid OS architecture. (Wanted: '" + desiredParts[2] + "', actual: '"
                    + actualArch + "')");
        }
    }

    /**
     * Checks to see if the file requirements of the plugin are satisfied. If the desired string is
     * empty, the test passes. Otherwise it is passed to File.exists() to see if the file is valid.
     * Multiple files can be specified by using a "," to separate. And either/or files can be
     * specified using a "|" (eg /usr/bin/bash|/bin/bash) If the test fails, the requirementsError
     * field will contain a user-friendly error message.
     *
     * @param desired The desired file requirements
     */
    protected void checkFiles(final String desired) {
        if (desired == null || desired.isEmpty()) {
            return;
        }

        for (String files : desired.split(",")) {
            final String[] filelist = files.split("\\|");
            boolean foundFile = false;
            for (String file : filelist) {
                if (new File(file).exists()) {
                    foundFile = true;
                    break;
                }
            }
            if (!foundFile) {
                errors.add("Required file '" + files + "' not found");
            }
        }
    }

    /**
     * Checks to see if the plugin requirements of the plugin are satisfied. If the desired string
     * is empty, the test passes. Plugins should be specified as:
     * plugin1[:minversion[:maxversion]],plugin2[:minversion[:maxversion]] Plugins will be attempted
     * to be loaded if not loaded, else the test will fail if the versions don't match, or the
     * plugin isn't known. If the test fails, the requirementsError field will contain a
     * user-friendly error message.
     *
     * @param plugins A map of known plugins by their short names
     * @param desired The desired file requirements
     */
    protected void checkPlugins(final Map<String, PluginMetaData> plugins,
            final String desired) {
        if (desired == null || desired.isEmpty()) {
            return;
        }

        for (String pluginName : desired.split(",")) {
            final String[] data = pluginName.split(":");
            final PluginMetaData target = plugins.get(data[0]);

            if (target == null) {
                errors.add("Required plugin '" + data[0] + "' was not found");
            }

            if (data.length > 1) {
                // Check plugin minimum version matches.
                if (target.getVersion().compareTo(new Version(data[1])) < 0) {
                    errors.add("Plugin '" + data[0]
                            + "' is too old (required version: " + data[1]
                            + ", actual version: " + target.getVersion() + ")");
                }

                // Check plugin maximum version matches.
                if (data.length > 2 && target.getVersion().compareTo(
                        new Version(data[2])) > 0) {
                    errors.add("Plugin '" + data[0]
                            + "' is too new (required version: " + data[2]
                            + ", actual version: " + target.getVersion() + ")");
                }
            }
        }
    }

}
