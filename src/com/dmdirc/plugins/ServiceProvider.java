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

/**
 * Defines a ServiceProvider.
 */
public interface ServiceProvider {

    /**
     * Is this provider active at this time.
     *
     * @return true if the provider is able to provide its services
     */
    boolean isActive();

    /** Activate the services. */
    void activateServices();

    /**
     * Get a list of services provided by this provider.
     *
     * @return A list of services provided by this provider.
     */
    List<Service> getServices();

    /**
     * Get the name of this provider.
     * 
     * @return This provider's name
     */
    String getProviderName();

    /**
     * Get an ExportedService object from this provider.
     *
     * @param name Service name
     * @return ExportedService object. If no such service exists, the execute
     *         method of this ExportedService will always return null.
     */
    ExportedService getExportedService(final String name);

}
