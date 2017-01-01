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

package com.dmdirc.util;


import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provides utility methods for classes that read from YAML files.
 */
public final class YamlReaderUtils {

    private YamlReaderUtils() {
        // Shouldn't be instantiated
    }

    /**
     * Performs an unchecked cast from a wildcard map to an object map. This will always work, but
     * generates an unchecked warning anyway. This method allows the scope of the warning
     * suppression to be reduced just to the single line.
     *
     * @param map The wildcard map to be cast.
     *
     * @return A usable object-to-object map.
     */
    @SuppressWarnings("unchecked")
    private static Map<Object, Object> uncheckedCast(final Map<?, ?> map) {
        return (Map<Object, Object>) map;
    }

    /**
     * Performs an unchecked cast from a wildcard list to an object list. This will always work, but
     * generates an unchecked warning anyway. This method allows the scope of the warning
     * suppression to be reduced just to the single line.
     *
     * @param list The wildcard list to be cast.
     *
     * @return A usable object list.
     */
    @SuppressWarnings("unchecked")
    private static <T> List<T> uncheckedCast(final List<?> list) {
        return (List<T>) list;
    }

    /**
     * Checks that the specified object is a map, and casts it.
     *
     * @param object The object to be cast.
     *
     * @return The given object cast as a map.
     *
     * @throws IllegalArgumentException If the specified object is not a map.
     */
    public static Map<Object, Object> asMap(@Nullable final Object object)
            throws IllegalArgumentException {
        if (object instanceof Map) {
            return uncheckedCast((Map<?, ?>) object);
        }

        throw new IllegalArgumentException("Unexpected element. Found "
                + simpleName(object) + ", expected Map");
    }

    /**
     * Checks that the specified object is a list, and casts it.
     *
     * @param object The object to be cast.
     *
     * @return The given object cast as a list.
     *
     * @throws IllegalArgumentException If the specified object is not a list.
     */
    public static List<Object> asList(@Nullable final Object object)
            throws IllegalArgumentException {
        if (object instanceof List) {
            return uncheckedCast((List<?>) object);
        }

        throw new IllegalArgumentException("Unexpected element. Found "
                + simpleName(object) + ", expected List");
    }

    /**
     * Checks that the specified object is a list, and casts it.
     *
     * @param object The object to be cast.
     * @param convertor The function to use to convert each list member to the desired type.
     *
     * @return The given object cast as a list.
     *
     * @throws IllegalArgumentException If the specified object is not a list.
     */
    public static <T> List<T> asList(@Nullable final Object object,
            final Function<Object, Optional<T>> convertor) throws IllegalArgumentException {
        return asList(object).parallelStream()
                .map(convertor).filter(Optional::isPresent)
                .map(Optional::get).collect(Collectors.toList());
    }

    /**
     * Validates and returns a required string value from the given map.
     *
     * @param map The map to retrieve the entry from.
     * @param key The key of the entry to retrieve.
     *
     * @return The string representation of the value of the key.
     *
     * @throws IllegalArgumentException If the specified key is not present, or is empty.
     */
    public static String requiredString(final Map<Object, Object> map, final String key)
            throws IllegalArgumentException {
        checkNotNull(map);
        checkNotNull(key);

        if (!map.containsKey(key)) {
            throw new IllegalArgumentException("Required key not present: " + key);
        }

        final String value = map.get(key).toString();
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException("Required key is empty: " + key);
        }

        return value;
    }

    /**
     * Returns an optional string value from the given map.
     *
     * @param map The map to retrieve the entry from.
     * @param key The key of the entry to retrieve.
     *
     * @return The string value of the entry with the given key, or {@code null} if not present.
     */
    @Nullable
    public static String optionalString(final Map<Object, Object> map, final String key) {
        checkNotNull(map);
        checkNotNull(key);

        final Object value = map.get(key);
        return value == null ? null : value.toString();
    }

    /**
     * Gets the simple name of the given object's class.
     *
     * @param object The object to get the class name of
     *
     * @return The simple class name of the object, or the string {@code "null"}.
     */
    private static String simpleName(@Nullable final Object object) {
        return object == null ? "null" : object.getClass().getSimpleName();
    }

}
