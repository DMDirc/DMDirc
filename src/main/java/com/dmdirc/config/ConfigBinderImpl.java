package com.dmdirc.config;

import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigChangeListener;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.dmdirc.util.LogUtils.APP_ERROR;

/**
 * Facilitates automatically binding fields or methods annotated with a {@link ConfigBinding}
 * element to a configuration value.
 */
class ConfigBinderImpl implements ConfigBinder {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigBinder.class);

    /** A map of instances to created listeners. */
    private final Multimap<Object, ConfigChangeListener> listeners = ArrayListMultimap.create();
    /** The default domain to use. */
    private final Optional<String> defaultDomain;
    /** The configuration manager to use to retrieve settings. */
    private final AggregateConfigProvider manager;
    /** Retriever to use to get typed config values. */
    private final ConfigValueRetriever valueRetriever;

    ConfigBinderImpl(final AggregateConfigProvider manager) {
        this.manager = manager;
        this.valueRetriever = new ConfigValueRetriever(manager);
        this.defaultDomain = Optional.empty();
    }

    ConfigBinderImpl(final AggregateConfigProvider manager, @Nonnull final String domain) {
        this.manager = manager;
        this.valueRetriever = new ConfigValueRetriever(manager);
        this.defaultDomain = Optional.of(domain);
    }

    @Override
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

        final Object value = valueRetriever.getValue(
                getTargetClass(element),
                getDomain(binding.domain()),
                binding.key(),
                binding.required(),
                binding.fallbacks());

        try {
            binding.invocation().newInstance().invoke(element, instance, value);
        } catch (ReflectiveOperationException ex) {
            LOG.error(APP_ERROR, "Exception when updating bound setting", ex);
        }
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
            final Iterable<ConfigChangeListener> newListeners) {
        synchronized (listeners) {
            listeners.putAll(instance, newListeners);
        }
    }

    @Override
    public void unbind(final Object instance) {
        synchronized (listeners) {
            listeners.get(instance).forEach(manager::removeListener);
            listeners.removeAll(instance);
        }
    }

    @Override
    public ConfigBinder withDefaultDomain(@Nonnull final String domain) {
        return new ConfigBinderImpl(manager, domain);
    }

}
