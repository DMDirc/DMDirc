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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Implementation of {@link ServiceManager}.
 */
@Singleton
public class ServiceManagerImpl implements ServiceManager {

    /** Map of services. */
    private final Map<String, Map<String, Service>> services = new HashMap<>();

    @Inject
    public ServiceManagerImpl() {
    }

    @Override
    public Service getService(final String type, final String name) {
        return getService(type, name, false);
    }

    @Override
    public Service getService(final String type, final String name, final boolean create) {
        // Find the type first
        if (services.containsKey(type)) {
            final Map<String, Service> map = services.get(type);
            // Now the name
            if (map.containsKey(name)) {
                return map.get(name);
            } else if (create) {
                final Service service = new Service(type, name);
                map.put(name, service);
                return service;
            }
        } else if (create) {
            final Map<String, Service> map = new HashMap<>();
            final Service service = new Service(type, name);
            map.put(name, service);
            services.put(type, map);
            return service;
        }

        return null;
    }

    @Override
    public ServiceProvider getServiceProvider(final String type, final String name) throws
            NoSuchProviderException {
        final Service service = getService(type, name);
        if (service != null) {
            final ServiceProvider provider = service.getActiveProvider();
            if (provider == null) {
                // Try to activate the service then try again.
                service.activate();
                final ServiceProvider activeProvider = service.getActiveProvider();
                if (activeProvider != null) {
                    return activeProvider;
                }
            } else {
                return provider;
            }
        }

        throw new NoSuchProviderException("No provider found for: " + type + "->" + name);
    }

    @Override
    public ServiceProvider getServiceProvider(final String type, final List<String> names,
            final boolean fallback) throws NoSuchProviderException {
        for (final String name : names) {
            final ServiceProvider provider = getServiceProvider(type, name);
            if (provider != null) {
                return provider;
            }
        }

        if (fallback) {
            final List<Service> servicesType = getServicesByType(type);
            if (!servicesType.isEmpty()) {
                final Service service = servicesType.get(0);
                return getServiceProvider(type, service.getName());
            }
        }

        throw new NoSuchProviderException("No provider found for " + type + "from the given list");
    }

    @Override
    public ExportedService getExportedService(final String name) {
        return getServiceProvider("export", name).getExportedService(name);
    }

    @Override
    public List<Service> getServicesByType(final String type) {
        // Find the type first
        if (services.containsKey(type)) {
            final Map<String, Service> map = services.get(type);
            return new ArrayList<>(map.values());
        }

        return new ArrayList<>();
    }

    @Override
    public List<Service> getAllServices() {
        // Find the type first
        final List<Service> allServices = new ArrayList<>();
        for (Map<String, Service> map : services.values()) {
            allServices.addAll(map.values());
        }

        return allServices;
    }

}
