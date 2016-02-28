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

package com.dmdirc.updater.checking;

import com.dmdirc.config.ConfigBinding;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.updater.UpdateChannel;
import com.dmdirc.updater.UpdateComponent;
import com.dmdirc.updater.Version;
import com.dmdirc.util.io.Downloader;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.dmdirc.ClientModule.GlobalConfig;

/**
 * Nightly update checker.
 */
public class NightlyChecker implements UpdateCheckStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(NightlyChecker.class);
    /** Name matching regex. */
    private final Pattern pattern = Pattern.compile(
            "^(.*?)-([^-]+(-[0-9]+-g[0-9a-f]+)?)(-SNAPSHOT).jar?$");
    /** The URL to request to check for updates. */
    private static final String UPDATE_URL = "https://nightlies.dmdirc.com/json/latest";
    /** The update channel to check for updates on. */
    private UpdateChannel channel;
    /** Downloader to download files. */
    private final Downloader downloader;

    /**
     * Creates a new instance of {@link NightlyChecker}.
     *
     * @param configProvider The provider to use to retrieve update channel information.
     * @param downloader     Used to download files
     */
    @Inject
    public NightlyChecker(@GlobalConfig final AggregateConfigProvider configProvider,
            final Downloader downloader) {
        configProvider.getBinder().bind(this, NightlyChecker.class);
        this.downloader = downloader;
    }

    /**
     * Sets the channel which will be used by the {@link NightlyChecker}.
     *
     * @param channel The new channel to use
     */
    @ConfigBinding(domain = "updater", key = "channel")
    public void setChannel(final String channel) {
        LOG.info("Changing channel to {}", channel);
        try {
            this.channel = UpdateChannel.valueOf(channel.toUpperCase());
        } catch (IllegalArgumentException ex) {
            this.channel = null;
            LOG.warn("Unknown channel {}", channel, ex);
        }
    }

    @Override
    public Map<UpdateComponent, UpdateCheckResult> checkForUpdates(
            final Collection<UpdateComponent> components) {
        if (channel != UpdateChannel.NIGHTLY) {
            LOG.info("Channel {} is not nightly, aborting", channel);
            return Collections.emptyMap();
        }
        LOG.info("Retrieving latest versions.");
        final List<NightlyResult> resultsList = new Gson().fromJson(getJson(),
                new TypeToken<List<NightlyResult>>(){}.getType());
        if (resultsList == null) {
            return Collections.emptyMap();
        }
        resultsList.stream()
                .filter(Objects::nonNull) //This is incase the JSON is broken
                .forEach(e -> {
            final Matcher matcher = pattern.matcher(e.getName());
            if (matcher.matches()) {
                e.setOtherName(matcher.group(1));
                e.setVersion(new Version(matcher.group(2)));
                e.setUrl(UPDATE_URL + '/' + e.getName());
            }
        });
        final Map<String, NightlyResult> resultsMap = resultsList.stream()
                .collect(Collectors.toMap(NightlyResult::getOtherName, Function.identity()));
        final Map<UpdateComponent, UpdateCheckResult> returns = new HashMap<>();
        components.forEach(e -> {
            if (resultsMap.containsKey(e.getName())) {
                if (resultsMap.get(e.getName()).getVersion().compareTo(e.getVersion()) > 0) {
                    final String name = e.getName();
                    final NightlyResult result = resultsMap.get(e.getName());
                    try {
                        returns.put(e, new BaseDownloadableResult(e, getURL(result),
                                result.getOtherName(), result.getVersion()));
                    } catch (MalformedURLException e1) {
                        LOG.error("Unable to create a URL for {}", name);
                    }
                }
            }
        });
        return returns;
    }

    private URL getURL(final NightlyResult result) throws MalformedURLException {
        return new URL(result.getUrl());
    }

    private String getJson() {
        try {
            return downloader.getPage(UPDATE_URL).stream().map(String::toString)
                    .collect(Collectors.joining("\r\n"));
        } catch (IOException e) {
            LOG.warn("Error when getting update page: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Wrapper class for GSON to deserialise the JSON.
     */
    private static class NightlyResult {
        private final String name;
        private final String type;
        private final String mtime;
        private final int size;
        private String otherName;
        private Version version;
        private String url;

        NightlyResult(final String name, final String type, final String mtime,
                final int size) {
            this.name = name;
            this.type = type;
            this.mtime = mtime;
            this.size = size;
        }

        String getName() {
            return name;
        }

        String getUrl() {
            return url;
        }

        String getOtherName() {
            return otherName;
        }

        void setOtherName(final String otherName) {
            this.otherName = otherName;
        }

        Version getVersion() {
            return version;
        }

        void setVersion(final Version version) {
            this.version = version;
        }

        void setUrl(final String url) {
            this.url = url;
        }
    }
}
