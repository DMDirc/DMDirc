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

package com.dmdirc.config;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.events.AppErrorEvent;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigChangeListener;
import com.dmdirc.interfaces.config.ReadOnlyConfigProvider;
import com.dmdirc.logger.ErrorLevel;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * Facilitates automatically binding fields or methods annotated with a {@link ConfigBinding}
 * element to a configuration value.
 */
public class ConfigBinder {

    /** A map of instances to created listeners. */
    private final Multimap<Object, ConfigChangeListener> listeners = ArrayListMultimap.create();
    /** The default domain to use. */
    private final Optional<String> defaultDomain;
    /** The configuration manager to use to retrieve settings. */
    private final AggregateConfigProvider manager;
    /** The event but used to raise error events. */
    private final DMDircMBassador eventBus;

    ConfigBinder(final AggregateConfigProvider manager, final DMDircMBassador eventBus) {
        this.manager = manager;
        this.eventBus = eventBus;
        this.defaultDomain = Optional.empty();
    }

    ConfigBinder(final AggregateConfigProvider manager, final DMDircMBassador eventBus,
            @Nonnull final String domain) {
        this.manager = manager;
        this.eventBus = eventBus;
        this.defaultDomain = Optional.of(domain);
    }

    /**
     * Binds all annotated methods and fields of the given instance to this binder's configuration
     * manager.
     *
     * @param instance The instance to be bound
     * @param clazz    The class to read bindings from
     */
    public void bind(final Object instance, final Class<?> clazz) {
        final Collection<ConfigChangeListener> newListeners = new ArrayList<>();
        final Collection<AccessibleObject> elements = new ArrayList<>();

        elements.addAll(Arrays.asList(clazz.getDeclaredMethods()));
        elements.addAll(Arrays.asList(clazz.getDeclaredFields()));

        for (AccessibleObject element : elements) {
            final ConfigBinding binding = element.getAnnotation(ConfigBinding.class);
            if (binding != null) {
                final ConfigChangeListener listener = getListener(instance, element, binding);
                newListeners.add(listener);

                manager.addChangeListener(getDomain(binding.domain()), binding.key(), listener);

                for (int i = 0; i < binding.fallbacks().length - 1; i += 2) {
                    manager.addChangeListener(getDomain(binding.fallbacks()[i]),
                            binding.fallbacks()[i + 1], listener);
                }

                if (binding.applyInitially()) {
                    updateBoundMember(instance, element, binding);
                }
            }
        }

        addListeners(instance, newListeners);
    }

    /**
     * Returns the default domain for this binder if the given annotation-specified domain is empty.
     */
    private String getDomain(final String annotationDomain) {
        return annotationDomain.isEmpty() ? defaultDomain.get() : annotationDomain;
    }

    /**
     * Creates a new listener which will call
     * {@link #updateBoundMember(Object, AccessibleObject, ConfigBinding)} with the given arguments.
     *
     * @param instance The instance to create a listener for
     * @param element  The element to create a listener for
     * @param binding  The binding annotation on the above element
     *
     * @return An appropriate config change listener
     */
    private ConfigChangeListener getListener(final Object instance,
            final AccessibleObject element, final ConfigBinding binding) {
        return (domain, key) -> updateBoundMember(instance, element, binding);
    }

    /**
     * Updates the specified element of the given instance with the current value of the
     * configuration key(s) specified by its binding.
     *
     * @param instance The instance to be updated
     * @param element  The element to be updated
     * @param binding  The binding which defines the configuration properties
     */
    private void updateBoundMember(final Object instance,
            final AccessibleObject element, final ConfigBinding binding) {
        if (!element.isAccessible()) {
            element.setAccessible(true);
        }

        final Object value = getValue(binding, getTargetClass(element));

        try {
            binding.invocation().newInstance().invoke(element, instance, value);
        } catch (ReflectiveOperationException ex) {
            eventBus.publish(new AppErrorEvent(ErrorLevel.HIGH, ex,
                    "Exception when updating bound setting", ""));
        }
    }

    /**
     * Gets a value from the configuration manager for use with the given binding, and attempts to
     * coerce it into the given class.
     *
     * @param binding     The binding defining configuration parameters
     * @param targetClass The desired class
     *
     * @return An object representing the current value of the configuration key(s) associated with
     *         the binding, of the desired target class, or null if the type conversion couldn't be
     *         performed.
     */
    private Object getValue(final ConfigBinding binding,
            final Class<?> targetClass) {
        if (targetClass.equals(String.class)) {
            return manager.getOptionString(getDomain(binding.domain()), binding.key(),
                    binding.required(), ReadOnlyConfigProvider.PERMISSIVE_VALIDATOR,
                    binding.fallbacks());
        }

        if (targetClass.equals(Boolean.class) || targetClass.equals(Boolean.TYPE)) {
            return manager.getOptionBool(getDomain(binding.domain()), binding.key());
        }

        if (targetClass.equals(Character.class) || targetClass.equals(Character.TYPE)) {
            return manager.getOptionChar(getDomain(binding.domain()), binding.key());
        }

        if (targetClass.equals(Integer.class) || targetClass.equals(Integer.TYPE)) {
            return manager.getOptionInt(getDomain(binding.domain()), binding.key(),
                    binding.fallbacks());
        }

        if (targetClass.equals(List.class)) {
            return manager.getOptionList(getDomain(binding.domain()), binding.key());
        }

        return null;
    }

    /**
     * Gets the type required for setting the given element.
     *
     * @param element The element to determine a type for
     *
     * @return If the given element is a field, then the type of that field; if the element is a
     *         method then the type of the first parameter; otherwise, <code>String.class</code>.
     */
    private Class<?> getTargetClass(final AccessibleObject element) {
        if (element instanceof Field) {
            return ((Field) element).getType();
        }

        if (element instanceof Executable) {
            return ((Executable) element).getParameterTypes()[0];
        }

        return String.class;
    }

    /**
     * Adds the given listeners to the given instance's collection.
     *
     * @param instance     The instance to add listeners for
     * @param newListeners The listeners to be added
     */
    private void addListeners(final Object instance,
            final Collection<ConfigChangeListener> newListeners) {
        synchronized (listeners) {
            listeners.putAll(instance, newListeners);
        }
    }

    /**
     * Unbinds all elements of the given instance that have been bound using this ConfigBinder.
     *
     * @param instance The instance to be unbound
     */
    public void unbind(final Object instance) {
        synchronized (listeners) {
            listeners.get(instance).forEach(manager::removeListener);
            listeners.removeAll(instance);
        }
    }

    /**
     * Returns a new config binder with the specified default domain.
     *
     * @param domain The default domain to use if one is not specified
     * @return A config binder with the specified default domain.
     */
    public ConfigBinder withDefaultDomain(@Nonnull final String domain) {
        return new ConfigBinder(manager, eventBus, domain);
    }

}
