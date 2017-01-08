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

package com.dmdirc.updater.checking;

import com.dmdirc.config.binding.ConfigBinding;
import com.dmdirc.config.GlobalConfig;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.updater.UpdateChannel;
import com.dmdirc.updater.UpdateComponent;
import com.dmdirc.updater.Version;
import com.dmdirc.util.LogUtils;
import com.dmdirc.util.io.Downloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A strategy which sends a request to the DMDirc update service for information.
 */
public class DMDircCheckStrategy implements UpdateCheckStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(DMDircCheckStrategy.class);
    /** The URL to request to check for updates. */
    private static final String UPDATE_URL = "https://updates.dmdirc.com/";
    /** The update channel to check for updates on. */
    private UpdateChannel channel;
    /** Downloader to download files. */
    private final Downloader downloader;

    /**
     * Creates a new instance of {@link DMDircCheckStrategy}.
     *
     * @param configProvider The provider to use to retrieve update channel information.
     * @param downloader     Used to download files
     */
    @Inject
    public DMDircCheckStrategy(@GlobalConfig final AggregateConfigProvider configProvider,
            final Downloader downloader) {
        configProvider.getBinder().bind(this, DMDircCheckStrategy.class);
        this.downloader = downloader;
    }

    /**
     * Sets the channel which will be used by the {@link DMDircCheckStrategy}.
     *
     * @param channel The new channel to use
     */
    @ConfigBinding(domain = "updater", key = "channel")
    public void setChannel(final String channel) {
        LOG.info("Changing channel to {}", channel);

        try {
            this.channel = UpdateChannel.valueOf(channel.toUpperCase());
        } catch (IllegalArgumentException ex) {
            LOG.warn("Unknown channel {}", channel, ex);
        }
    }

    @Override
    public Map<UpdateComponent, UpdateCheckResult> checkForUpdates(
            final Collection<UpdateComponent> components) {
        final Map<UpdateComponent, UpdateCheckResult> res = new HashMap<>();
        final Map<String, UpdateComponent> names = getComponentsByName(components);

        try {
            final List<String> response = downloader.getPage(UPDATE_URL, getPayload(components));
            LOG.trace("Response from update server: {}", response);

            for (String line : response) {
                final UpdateComponent component = names.get(getComponent(line));

                if (component == null) {
                    LOG.warn("Unable to extract component from line: {}", line);
                    continue;
                }

                final UpdateCheckResult result = parseResponse(component, line);

                if (result != null) {
                    res.put(component, result);
                }
            }
        } catch (IOException ex) {
            LOG.warn("I/O exception when checking for updates", ex);
        }

        return res;
    }

    /**
     * Builds the data payload which will be sent to the update server. Specifically, iterates over
     * each component and appends their name, the channel name, and the component's version number.
     *
     * @param components The components to be added to the payload
     *
     * @return A string which can be posted to the DMDirc update server
     */
    private String getPayload(final Collection<UpdateComponent> components) {
        final StringBuilder data = new StringBuilder("data=");

        for (UpdateComponent component : components) {
            LOG.trace("Adding payload info for component {} (version {})", component.getName(),
                    component.getVersion());

            data.append(component.getName());
            data.append(',');
            data.append(channel.name());
            data.append(',');
            data.append(component.getVersion());
            data.append(';');
        }

        LOG.debug("Constructed update payload: {}", data);

        return data.toString();
    }

    /**
     * Extracts the name of the component a given response line contains.
     *
     * @param line The line to be parsed
     *
     * @return The name of the component extracted from the given line
     */
    private String getComponent(final String line) {
        final String[] parts = line.split(" ");

        if (parts.length >= 2 && "outofdate".equals(parts[0])) {
            return parts[1];
        }

        return parts.length >= 3 ? parts[2] : null;
    }

    /**
     * Checks the specified line to determine the message from the update server.
     *
     * @param component The component the line refers to
     * @param line      The line to be checked
     */
    private UpdateCheckResult parseResponse(final UpdateComponent component,
            final String line) {
        final String[] parts = line.split(" ");
        switch (parts[0]) {
            case "outofdate":
                return parseOutOfDateResponse(component, parts);
            case "uptodate":
                return new BaseCheckResult(component);
            case "error":
                LOG.warn("Error received from update server: {}", line);
                break;
            default:
                LOG.error("Unknown update line received from server: {}", line);
                break;
        }

        return null;
    }

    /**
     * Parses an "outofdate" response from the server. Extracts the URL, remote version and remote
     * friendly version into a {@link BaseDownloadableResult}.
     *
     * @param parts The tokenised parts of the response line
     *
     * @return A corresponding {@link UpdateCheckResult} or null on failure
     */
    private UpdateCheckResult parseOutOfDateResponse(
            final UpdateComponent component, final String[] parts) {
        try {
            return new BaseDownloadableResult(component, new URL(parts[5]),
                    parts[4], new Version(parts[3]));
        } catch (MalformedURLException ex) {
            LOG.error(LogUtils.APP_ERROR, "Unable to construct URL for update. Parts: {}", parts,
                    ex);
            return null;
        }
    }

    /**
     * Builds a mapping of components' names to their actual component objects.
     *
     * @param components A collection of components to be mapped
     *
     * @return A corresponding Map containing a single entry for each component, which the
     *         component's name as a key and the component itself as a value.
     */
    private Map<String, UpdateComponent> getComponentsByName(
            final Collection<UpdateComponent> components) {
        final Map<String, UpdateComponent> res = new HashMap<>();

        for (UpdateComponent component : components) {
            res.put(component.getName(), component);
        }

        return res;
    }

}
