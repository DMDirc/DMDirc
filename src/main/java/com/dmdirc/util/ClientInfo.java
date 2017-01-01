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

package com.dmdirc.util;

import com.dmdirc.util.io.ConfigFile;
import com.dmdirc.util.io.InvalidConfigFileException;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides static utility methods to access information about the client and its environment.
 */
@Singleton
public class ClientInfo {

    private static final Logger LOG = LoggerFactory.getLogger(ClientInfo.class);
    /** Domain to read version settings from. */
    private static final String VERSION_DOMAIN = "version";
    /** Key within the version domain that holds the actual version. */
    private static final String VERSION_KEY = "version";
    /** Domain to read updater settings from. */
    private static final String UPDATER_DOMAIN = "updater";
    /** Key within the updater domain that holds the channel. */
    private static final String UPDATER_CHANNEL_KEY = "channel";
    /** Fallback value to use if a system property isn't found. */
    private static final String PROPERTY_FALLBACK = "unknown";
    /** Lock used to guard access to {@link #versionConfig}. */
    private final Object VERSION_CONFIG_LOCK = new Object();
    /** Cached config file containing the client's version information. */
    private ConfigFile versionConfig;

    @Inject
    public ClientInfo() {
        // Shouldn't be initialised.
    }

    /**
     * @return The version of the client.
     */
    public String getVersion() {
        return getVersionConfigSetting(VERSION_DOMAIN, VERSION_KEY);
    }

    /**
     * @return The major version of the client.
     */
    public String getMajorVersion() {
        return getVersion().replaceAll("(-|rc|a|b|m).*", "");
    }

    /**
     * @return The channel that this client was built for.
     */
    public String getUpdaterChannel() {
        return getVersionConfigSetting(UPDATER_DOMAIN, UPDATER_CHANNEL_KEY);
    }

    /**
     * @return The name of the operating system the client is running on.
     */
    public String getOperatingSystemName() {
        return System.getProperty("os.name", PROPERTY_FALLBACK);
    }

    /**
     * @return The version of the operating system the client is running on.
     */
    public String getOperatingSystemVersion() {
        return System.getProperty("os.version", PROPERTY_FALLBACK);
    }

    /**
     * @return The name of the architecture that the operating system is built for.
     */
    public String getOperatingSystemArchitecture() {
        return System.getProperty("os.arch", PROPERTY_FALLBACK);
    }

    /**
     * @return The default file encoding used by the system.
     */
    public String getSystemFileEncoding() {
        return System.getProperty("file.encoding", PROPERTY_FALLBACK);
    }

    /**
     * @return The default locale used by the system.
     */
    public String getSystemDefaultLocale() {
        return Locale.getDefault().toString();
    }

    /**
     * @return The name of the JVM running the client.
     */
    public String getJavaName() {
        return System.getProperty("java.vm.name", PROPERTY_FALLBACK);
    }

    /**
     * @return The name of the vendor of the JVM running the client.
     */
    public String getJavaVendor() {
        return System.getProperty("java.vm.vendor", PROPERTY_FALLBACK);
    }

    /**
     * @return The version of the JVM running the client.
     */
    public String getJavaVersion() {
        return System.getProperty("java.version", PROPERTY_FALLBACK);
    }

    /**
     * @return The major version of the JVM running the client.
     */
    public String getJavaMajorVersion() {
        return getJavaVersion().replaceAll("_.*", "");
    }

    /**
     * @return The uptime for the client in milliseconds.
     */
    public long getUptime() {
        return ManagementFactory.getRuntimeMXBean().getUptime();
    }

    /**
     * @return A string containing the DMDirc version and updater channel.
     */
    public String getVersionInformation() {
        return getVersion() + " (" + getUpdaterChannel() + ')';
    }

    /**
     * @return A string containing the JVM name, version, and vendor.
     */
    public String getJavaInformation() {
        return getJavaName() + ' ' + getJavaVersion() + " [" + getJavaVendor() + ']';
    }

    /**
     * @return A string containing the OS name, version and architecture, and the default file
     *         encoding and locale.
     */
    public String getOperatingSystemInformation() {
        return getOperatingSystemName() + ' '
                + getOperatingSystemVersion() + ' '
                + getOperatingSystemArchitecture() + "; "
                + getSystemFileEncoding() + "; "
                + getSystemDefaultLocale();
    }

    /**
     * Retrieves a single setting from the version configuration file bundled with the client.
     *
     * @param domain The domain of the setting to retrieve.
     * @param key    The key of the setting to retrieve.
     *
     * @return The value of the key within the version config, or {@code null} if it doesn't exist.
     */
    public String getVersionConfigSetting(final String domain, final String key) {
        return getVersionConfig().getKeyDomain(domain).get(key);
    }

    /**
     * Utility method to get the config containing the client's version information.
     *
     * <p>
     * This will be cached where possible.
     *
     * @return The client's bundled version config file.
     */
    private ConfigFile getVersionConfig() {
        synchronized (VERSION_CONFIG_LOCK) {
            if (versionConfig == null) {
                LOG.debug("No previous version config cached, creating a new one...");

                final ConfigFile config = new ConfigFile(
                        ClientInfo.class.getResourceAsStream("/com/dmdirc/version.config"));

                try {
                    config.read();
                } catch (IOException | InvalidConfigFileException ex) {
                    LOG.error("Unable to read version information", ex);
                }

                versionConfig = config;
            }
        }

        return versionConfig;
    }

}
