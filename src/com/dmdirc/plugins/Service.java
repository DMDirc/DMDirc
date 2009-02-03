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

import java.util.Map;
import java.util.HashMap;
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
	
	/** Map of services. */
	private final static Map<String, Map<String, Service>> services = new HashMap<String, Map<String, Service>>();
	
	/** List of ServiceProviders that implement this service. */
	private List<ServiceProvider> serviceproviders = new ArrayList<ServiceProvider>();
	
	/**
	 * Create a new Service
	 *
	 * @param type Type of this service
	 * @param name Name of this service
	 */
	private Service(final String type, final String name) {
		this.type = type;
		this.name = name;
	}
	
	/**
	 * Get a service object for the given name/type.
	 *
	 * @param type Type of this service
	 * @param name Name of this service
	 * @param create If the requested service doesn't exist, should it be created?
	 * @return The service requested, or null if service wasn't found and create wasn't specifed
	 */
	public static Service getService(final String type, final String name, final boolean create) {
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
			final Map<String, Service> map = new HashMap<String, Service>();
			final Service service = new Service(type, name);
			map.put(name, service);
			services.put(type, map);
			return service;
		}
		
		return null;
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
	 * Get this service as a String
	 *
	 * @return String representation of this service
	 */
	public String toString() {
		return "Service: " + type + "->" + name;
	}
}
