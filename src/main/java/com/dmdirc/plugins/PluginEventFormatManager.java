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

package com.dmdirc.plugins;

import com.dmdirc.config.GlobalConfig;
import com.dmdirc.events.PluginLoadedEvent;
import com.dmdirc.events.PluginUnloadedEvent;
import com.dmdirc.events.eventbus.EventBus;
import com.dmdirc.ui.messages.*;
import com.dmdirc.util.system.SystemLifecycleComponent;
import net.engio.mbassy.listener.Handler;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads default event formats from plugins.
 *
 * <p>Plugins can bundle a format file in {@code /META-INF/format.yml}, and it will be
 * automatically added to the client when the plugin is loaded.
 */
@Singleton
public class PluginEventFormatManager implements SystemLifecycleComponent {

    private final EventBus eventbus;
    private final MultiEventFormatProvider multiEventFormatProvider;
    private final Map<PluginInfo, EventFormatProvider> providers = new HashMap<>();
    private final ColourManager colourManager;
    private final DisplayLocationManager displayLocationManager;

    @Inject
    public PluginEventFormatManager(
            final EventBus eventbus,
            final MultiEventFormatProvider multiEventFormatProvider,
            @GlobalConfig final ColourManager colourManager,
            final DisplayLocationManager displayLocationManager) {
        this.eventbus = eventbus;
        this.multiEventFormatProvider = multiEventFormatProvider;
        this.colourManager = colourManager;
        this.displayLocationManager = displayLocationManager;
    }

    @Override
    public void startUp() {
        eventbus.subscribe(this);
    }

    @Override
    public void shutDown() {
        eventbus.unsubscribe(this);
    }

    @Handler
    public void handlePluginLoaded(final PluginLoadedEvent event) {
        final Path path = event.getPlugin().getPath("/META-INF/format.yml");
        if (Files.exists(path)) {
            final YamlEventFormatProvider provider =
                    new YamlEventFormatProvider(path, colourManager, displayLocationManager);
            provider.load();
            providers.put(event.getPlugin(), provider);
            multiEventFormatProvider.addProvider(provider);
        }
    }

    @Handler
    public void handlePluginUnloaded(final PluginUnloadedEvent event) {
        if (providers.containsKey(event.getPlugin())) {
            multiEventFormatProvider.removeProvider(providers.remove(event.getPlugin()));
        }
    }

}
