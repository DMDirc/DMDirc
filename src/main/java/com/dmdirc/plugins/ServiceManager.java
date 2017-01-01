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

import java.util.List;

/**
 * A service manager handles registration and searches for {@link Service}s.
 */
public interface ServiceManager {

    /**
     * Get a List of all services
     *
     * @return The list of all services.
     */
    List<Service> getAllServices();

    /**
     * Get an ExportedService object of the given name from any provider that provides it. This is
     * the same as doing getServiceProvider("export", name).getExportedService(name)
     *
     * @param name Name of this service
     *
     * @return An ExportedService object.
     *
     * @throws NoSuchProviderException If no provider exists for the requested service.
     */
    ExportedService getExportedService(final String name);

    /**
     * Get a service object for the given name/type if one exists.
     *
     * @param type Type of this service
     * @param name Name of this service
     *
     * @return The service requested, or null if service wasn't found and create wasn't specified
     */
    Service getService(final String type, final String name);

    /**
     * Get a service object for the given name/type.
     *
     * @param type   Type of this service
     * @param name   Name of this service
     * @param create If the requested service doesn't exist, should it be created?
     *
     * @return The service requested, or null if service wasn't found and create wasn't specified
     */
    Service getService(final String type, final String name, final boolean create);

    /**
     * Get a ServiceProvider object for the given name/type if one exists.
     *
     * @param type Type of this service
     * @param name Name of this service
     *
     * @return A ServiceProvider that provides the requested service.
     *
     * @throws NoSuchProviderException If no provider exists for the requested service
     */
    ServiceProvider getServiceProvider(final String type, final String name) throws
            NoSuchProviderException;

    /**
     * Get a ServiceProvider object for the given tpye, prioritising those in the list of names.
     *
     * @param type     Type to look for
     * @param names    Names to look for
     * @param fallback Fallback to the first provider of type that exists if one from the list is
     *                 not found.
     *
     * @return A ServiceProvider that provides the requested service.
     *
     * @throws NoSuchProviderException If no provider exists for the requested service and fallback
     *                                 is false, or no providers exist at all.
     */
    ServiceProvider getServiceProvider(final String type, final List<String> names,
            final boolean fallback) throws NoSuchProviderException;

    /**
     * Get a List of all services of a specified type.
     *
     * @param type Type of service
     *
     * @return The list of services requested.
     */
    List<Service> getServicesByType(final String type);

}
