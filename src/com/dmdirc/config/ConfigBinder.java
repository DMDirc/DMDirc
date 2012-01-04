/*
 * Copyright (c) 2006-2012 DMDirc Developers
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

import com.dmdirc.interfaces.ConfigChangeListener;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.Colour;
import com.dmdirc.util.collections.MapList;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Synchronized;

/**
 * Facilitates automatically binding fields or methods annotated with a
 * {@link ConfigBinding} element to a configuration value.
 */
@AllArgsConstructor
public class ConfigBinder {

    /** A map of instances to created listeners. */
    private final MapList<Object, ConfigChangeListener> listeners
            = new MapList<Object, ConfigChangeListener>();

    /** The configuration manager to use to retrieve settings. */
    private final ConfigManager manager;

    /**
     * Binds all annotated methods and fields of the given instance to this
     * binder's configuration manager.
     *
     * @param instance The instance to be bound
     * @param clazz The class to read bindings from
     */
    @SuppressWarnings("unchecked")
    public void bind(final Object instance, final Class<?> clazz) {
        final List<ConfigChangeListener> newListeners = new ArrayList<ConfigChangeListener>();
        final List<AccessibleObject> elements = new ArrayList<AccessibleObject>();

        elements.addAll(Arrays.asList(clazz.getDeclaredMethods()));
        elements.addAll(Arrays.asList(clazz.getDeclaredFields()));

        for (AccessibleObject element : elements) {
            final ConfigBinding binding = element.getAnnotation(ConfigBinding.class);
            if (binding != null) {
                final ConfigChangeListener listener = getListener(instance, element, binding);
                newListeners.add(listener);

                manager.addChangeListener(binding.domain(), binding.key(), listener);

                for (int i = 0; i < binding.fallbacks().length - 1; i += 2) {
                    manager.addChangeListener(binding.fallbacks()[i], binding.fallbacks()[i + 1], listener);
                }

                if (binding.applyInitially()) {
                    updateBoundMember(instance, element, binding);
                }
            }
        }

        addListeners(instance, newListeners);
    }

    /**
     * Creates a new listener which will call
     * {@link #updateBoundMember(Object, AccessibleObject, ConfigBinding)}
     * with the given arguments.
     *
     * @param instance The instance to create a listener for
     * @param element The element to create a listener for
     * @param binding The binding annotation on the above element
     * @return An appropriate config change listener
     */
    private ConfigChangeListener getListener(final Object instance,
            final AccessibleObject element, final ConfigBinding binding) {
        return new ConfigChangeListener() {
            /** {@inheritDoc} */
            @Override
            public void configChanged(final String domain, final String key) {
                updateBoundMember(instance, element, binding);
            }
        };
    }

    /**
     * Updates the specified element of the given instance with the current
     * value of the configuration key(s) specified by its binding.
     *
     * @param instance The instance to be updated
     * @param element The element to be updated
     * @param binding The binding which defines the configuration properties
     */
    private void updateBoundMember(final Object instance,
            final AccessibleObject element, final ConfigBinding binding) {
        if (!element.isAccessible()) {
            element.setAccessible(true);
        }

        final Object value = getValue(binding, getTargetClass(element));

        if (element instanceof Field) {
            try {
                ((Field) element).set(instance, value);
            } catch (IllegalAccessException ex) {
                // Ignore
            } catch (IllegalArgumentException ex) {
                // Ignore
            }
        }

        if (element instanceof Method) {
            try {
                ((Method) element).invoke(instance, value);
            } catch (IllegalAccessException ex) {
                // Ignore
            } catch (IllegalArgumentException ex) {
                // Ignore
            } catch (InvocationTargetException ex) {
               Logger.appError(ErrorLevel.HIGH,
                       "Exception when updating bound setting", ex);
            }
        }
    }

    /**
     * Gets a value from the configuration manager for use with the given
     * binding, and attempts to coerce it into the given class.
     *
     * @param binding The binding defining configuration parameters
     * @param targetClass The desired class
     * @return An object representing the current value of the configuration
     * key(s) associated with the binding, of the desired target class, or null
     * if the type conversion couldn't be performed.
     */
    private Object getValue(final ConfigBinding binding,
            final Class<?> targetClass) {
        if (targetClass.equals(String.class)) {
            return manager.getOptionString(binding.domain(), binding.key(), binding.fallbacks());
        }

        if (targetClass.equals(Colour.class)) {
            return manager.getOptionColour(binding.domain(), binding.key(), binding.fallbacks());
        }

        if (targetClass.equals(Boolean.class) || targetClass.equals(Boolean.TYPE)) {
            return manager.getOptionBool(binding.domain(), binding.key());
        }

        if (targetClass.equals(Character.class) || targetClass.equals(Character.TYPE)) {
            return manager.getOptionChar(binding.domain(), binding.key());
        }

        if (targetClass.equals(Integer.class) || targetClass.equals(Integer.TYPE)) {
            return manager.getOptionInt(binding.domain(), binding.key());
        }

        return null;
    }

    /**
     * Gets the type required for setting the given element.
     *
     * @param element The element to determine a type for
     * @return If the given element is a field, then the type of that field;
     * if the element is a method then the type of the first parameter;
     * otherwise, <code>String.class</code>.
     */
    private Class<?> getTargetClass(final AccessibleObject element) {
        if (element instanceof Field) {
            return ((Field) element).getType();
        }

        if (element instanceof Method) {
            return ((Method) element).getParameterTypes()[0];
        }

        return String.class;
    }

    /**
     * Adds the given listeners to the given instance's collection.
     *
     * @param instance The instance to add listeners for
     * @param newListeners The listeners to be added
     */
    @Synchronized
    private void addListeners(final Object instance,
            final Collection<ConfigChangeListener> newListeners) {
        listeners.add(instance, newListeners);
    }

    /**
     * Unbinds all elements of the given instance that have been bound using
     * this ConfigBinder.
     *
     * @param instance The instance to be unbound
     */
    @Synchronized
    public void unbind(final Object instance) {
        for (ConfigChangeListener listener : listeners.safeGet(instance)) {
            manager.removeListener(listener);
        }

        listeners.remove(instance);
    }
}
