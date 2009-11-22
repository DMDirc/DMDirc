/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import java.util.List;
import java.util.ArrayList;

/**
 * Defines a service provided by a ServiceProvider.
 */
public class Service {

    /** Service Type. */
    private final String type;

    /** Service Name. */
    private final String name;

    /** List of ServiceProviders that implement this service. */
    private List<ServiceProvider> serviceproviders = new ArrayList<ServiceProvider>();

    /**
     * Create a new Service
     *
     * @param type Type of this service
     * @param name Name of this service
     */
    protected Service(final String type, final String name) {
        this.type = type;
        this.name = name;
    }

    /**
     * Get the name of this service
     *
     * @return The name of this service
     */
    public String getName() {
        return name;
    }

    /**
     * Get the type of this service
     *
     * @return The type of this service
     */
    public String getType() {
        return type;
    }

    /**
     * Add the given ServiceProvider as a provider for this service.
     *
     * @param provider ServiceProvider that provides this service
     */
    public void addProvider(final ServiceProvider provider) {
        serviceproviders.add(provider);
    }

    /**
     * Remove the given ServiceProvider as a provider for this service.
     *
     * @param provider ServiceProvider that no longer provides this service
     */
    public void delProvider(final ServiceProvider provider) {
        serviceproviders.remove(provider);
    }

    /**
     * Get a list of ServiceProviders for this service
     *
     * @return List of ServiceProvider that provide this service
     */
    public List<ServiceProvider> getProviders() {
        return new ArrayList<ServiceProvider>(serviceproviders);
    }

    /**
     * Get the first active ServiceProvider for this service, or null.
     *
     * @return First active ServiceProvider for this service, or null.
     */
    public ServiceProvider getActiveProvider() {
        for (ServiceProvider provider : getProviders()) {
            if (provider.isActive()) {
                return provider;
            }
        }

        return null;
    }

    /**
     * Check if this service is active.
     *
     * @return True if this service is currently active.
     */
    public boolean isActive() {
        // Service is known, check that at least 1 plugin that provides it is loaded
        for (ServiceProvider provider : getProviders()) {
            if (provider.isActive()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Activate this service.
     *
     * @return True if this service is already active, or if it was activated.
     */
    public boolean activate() {
        if (isActive()) {
            return true;
        }

        // If none of the plugins that provide the service are loaded, load the
        // first one that registered itself as the provider
        for (ServiceProvider provider : getProviders()) {
            if (!provider.isActive()) {
                provider.activateServices();
                if (provider.isActive()) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Get this service as a String
     *
     * @return String representation of this service
     */
    public String toString() {
        return "Service: " + type + "->" + name + " (Providers: " + serviceproviders.size() + ")";
    }

}
