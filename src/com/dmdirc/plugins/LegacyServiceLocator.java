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
import com.dmdirc.events.UserErrorEvent;
import com.dmdirc.logger.ErrorLevel;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A service locator backed by a {@link PluginManager} and exported methods.
 */
@Singleton
public class LegacyServiceLocator implements ServiceLocator {

    /** The plugin manager to use to find services. */
    private final PluginManager pluginManager;
    /** The event bus to post errors . */
    private final DMDircMBassador eventBus;

    @Inject
    public LegacyServiceLocator(final PluginManager pluginManager, final DMDircMBassador eventBus) {
        this.pluginManager = checkNotNull(pluginManager);
        this.eventBus = checkNotNull(eventBus);
    }

    @Override
    public <T> Collection<T> getAllServices(final Class<T> serviceType) {
        return getServices(serviceType, null);
    }

    @Override
    public <T> T getService(final Class<T> serviceType, final String implementation) {
        final Collection<T> services = getServices(serviceType, implementation);
        return services.isEmpty() ? null : services.iterator().next();
    }

    @SuppressWarnings("unchecked")
    private <T> Collection<T> getServices(final Class<T> serviceType,
            @Nullable final String implementation) {
        checkNotNull(serviceType);
        final Collection<T> services = new HashSet<>();
        pluginManager.getPluginInfos().stream().filter(PluginInfo::isLoaded)
                .forEach(pluginInfo -> {
                    final Plugin plugin = pluginInfo.getPlugin();
                    for (Method method : plugin.getClass().getMethods()) {
                        if (method.isAnnotationPresent(Exported.class) &&
                                method.getParameterTypes().length == 0 &&
                                serviceType.isAssignableFrom(method.getReturnType())) {
                            try {
                                method.setAccessible(true);
                                final Object object = method.invoke(plugin);
                                if (object != null && (implementation == null ||
                                        implementation.equals(object.getClass().getName()))) {
                                    services.add((T) object);
                                }
                            } catch (ReflectiveOperationException ex) {
                                eventBus.publish(new UserErrorEvent(ErrorLevel.LOW, ex,
                                        "Unable to execute exported method " + method.getName() +
                                                " in plugin " + pluginInfo.getMetaData().getName(),
                                        ""));
                            }
                        }
                    }
                });

        return services;
    }

}
