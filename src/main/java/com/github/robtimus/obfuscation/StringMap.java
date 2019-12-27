/*
 * StringMap.java
 * Copyright 2019 Rob Spoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.robtimus.obfuscation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * An immutable mapping from keys to values. This class is a simplified, specialized version of {@link Map} that can contain both case sensitive and
 * case insensitive mappings.
 *
 * @author Rob Spoor
 * @param <V> The value type.
 */
public final class StringMap<V> {

    private final Map<String, V> caseSensitiveMap;
    private final Map<String, V> caseInsensitiveMap;

    private StringMap(Builder<V> builder) {
        caseSensitiveMap = builder.caseSensitiveMap();
        caseInsensitiveMap = builder.caseInsensitiveMap();
    }

    /**
     * Returns the number of mappings.
     *
     * @return The number of mappings, with a maximum of {@link Integer#MAX_VALUE}.
     * @see Map#size()
     */
    public long size() {
        long size = (long) caseSensitiveMap.size() + caseInsensitiveMap.size();
        return (int) Math.min(size, Integer.MAX_VALUE);
    }

    /**
     * Returns whether or not there are any mappings.
     *
     * @return {@code true} if there are any mappings, or {@code false} otherwise.
     * @see Map#isEmpty()
     */
    public boolean isEmpty() {
        return caseSensitiveMap.isEmpty() && caseInsensitiveMap.isEmpty();
    }

    /**
     * Returns whether or not there is a mapping for a specific key.
     *
     * @param key The key to check.
     * @return {@code true} if there is a mapping for the given key, or {@code false} otherwise.
     * @see Map#containsKey(Object)
     */
    public boolean containsKey(String key) {
        return key != null && (caseSensitiveMap.containsKey(key) || caseInsensitiveMap.containsKey(key));
    }

    /**
     * Returns the mapped value for a specific key.
     *
     * @param key The key to return the mapped value for.
     * @return The value mapped to the given key, or {@code null} if there is no such value.
     * @see Map#get(Object)
     */
    public V get(String key) {
        if (key == null) {
            return null;
        }
        V value = caseSensitiveMap.get(key);
        return value != null ? value : caseInsensitiveMap.get(key);
    }

    /**
     * Returns the mapped value for a specific key.
     *
     * @param key The key to return the mapped value for.
     * @param defaultValue The value to return if there is no value mapped to the given key.
     * @return The value mapped to the given key, or the given default value if there is no such value.
     * @see Map#getOrDefault(Object, Object)
     */
    public V get(String key, V defaultValue) {
        if (key == null) {
            return defaultValue;
        }
        V value = caseSensitiveMap.get(key);
        return value != null ? value : caseInsensitiveMap.getOrDefault(key, defaultValue);
    }

    /**
     * Returns the mapped value for a specific key.
     *
     * @param key The key to return the mapped value for.
     * @param valueSupplier A supplier that will provide a value if there is no value mapped to the given key.
     * @return The value mapped to the given key, or a value provided by the given supplier if there is no such value.
     * @throws NullPointerException If the given supplier is {@code null}.
     */
    public V get(String key, Supplier<? extends V> valueSupplier) {
        Objects.requireNonNull(valueSupplier);
        if (key == null) {
            return valueSupplier.get();
        }
        V value = caseSensitiveMap.get(key);
        if (value != null) {
            return value;
        }
        value = caseInsensitiveMap.get(key);
        if (value != null) {
            return value;
        }
        return valueSupplier.get();
    }

    /**
     * Performs an action for each mapping. If the action throwns an exception this is propagated to the caller.
     *
     * @param action The action to perform.
     * @throws NullPointerException If the given action is {@code null}.
     */
    public void forEach(BiConsumer<? super String, ? super V> action) {
        Objects.requireNonNull(action);
        caseSensitiveMap.forEach(action);
        caseInsensitiveMap.forEach(action);
    }

    /**
     * Compares the specified object with this map for equality. An object is equal if it is a {@code StringMap} with the same case sensitive and
     * case insensitive mappings.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || o.getClass() != getClass()) {
            return false;
        }
        StringMap<?> other = (StringMap<?>) o;
        return caseSensitiveMap.equals(other.caseSensitiveMap) && caseInsensitiveMap.equals(other.caseInsensitiveMap);
    }

    @Override
    public int hashCode() {
        return caseSensitiveMap.hashCode() ^ caseInsensitiveMap.hashCode();
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        // if both are empty, caseInsensitive will be printed which is OK if that's empty too
        if (caseSensitiveMap.isEmpty()) {
            return caseInsensitiveMap.toString();
        }
        if (caseInsensitiveMap.isEmpty()) {
            return caseSensitiveMap.toString();
        }

        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (Iterator<Map.Entry<String, V>> i = caseSensitiveMap.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry<String, V> entry = i.next();
            // no need to check for key == this or value == this because this map cannot have itself added
            sb.append(entry.getKey());
            sb.append('=');
            sb.append(entry.getValue());
            sb.append(", ");
        }
        for (Iterator<Map.Entry<String, V>> i = caseInsensitiveMap.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry<String, V> entry = i.next();
            // no need to check for key == this or value == this because this map cannot have itself added
            sb.append(entry.getKey());
            sb.append('=');
            sb.append(entry.getValue());
            if (i.hasNext()) {
                sb.append(", ");
            }
        }
        sb.append('}');
        return sb.toString();
    }

    /**
     * Returns a builder that will create {@code StringMaps}.
     *
     * @param <V> The value type.
     * @return A builder that will create {@code StringMaps}.
     */
    public static <V> Builder<V> builder() {
        return new Builder<>();
    }

    /**
     * A builder for {@link StringMap} objects.
     *
     * @author Rob Spoor
     * @param <V> The value type for created {@link StringMap} objects.
     */
    public static final class Builder<V> {

        private final Map<String, V> caseSensitiveMap;
        private final Map<String, V> caseInsensitiveMap;

        private Builder() {
            caseSensitiveMap = new HashMap<>();
            caseInsensitiveMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        }

        /**
         * Adds or overwrites an entry. This method is an alias for {@link #withEntry(String, Object, boolean) withEntry(key, value, true)}.
         *
         * @param key The key for the entry.
         * @param value The value for the entry.
         * @return This object.
         * @throws NullPointerException If the key or value is {@code null}.
         */
        public Builder<V> withEntry(String key, V value) {
            return withEntry(key, value, true);
        }

        /**
         * Adds or overwrites an entry.
         *
         * @param key           The key for the entry.
         * @param value         The value for the entry.
         * @param caseSensitive {@code true} if the key should be treated case sensitively,
         *                          or {@code false} if it should be treated case insensitively.
         * @return This object.
         * @throws NullPointerException If the key or value is {@code null}.
         * @see Map#put(Object, Object)
         */
        public Builder<V> withEntry(String key, V value, boolean caseSensitive) {
            Objects.requireNonNull(key);
            Objects.requireNonNull(value);
            if (caseSensitive) {
                caseInsensitiveMap.remove(key);
                caseSensitiveMap.put(key, value);
            } else {
                caseSensitiveMap.remove(key);
                caseInsensitiveMap.put(key, value);
            }
            return this;
        }

        /**
         * This method allows the application of a function to this builder.
         * <p>
         * Any exception thrown by the function will be propagated to the caller.
         *
         * @param <R> The type of the result of the function.
         * @param f The function to apply.
         * @return The result of applying the function to this builder.
         */
        public <R> R transform(Function<? super Builder<?>, ? extends R> f) {
            return f.apply(this);
        }

        /**
         * Creates a {@link StringMap} object with the entries currently added to this builder.
         *
         * @return The created {@link StringMap} object.
         */
        public StringMap<V> build() {
            return new StringMap<>(this);
        }

        Map<String, V> caseSensitiveMap() {
            return caseSensitiveMap.isEmpty()
                    ? Collections.emptyMap()
                    : Collections.unmodifiableMap(new HashMap<>(caseSensitiveMap));
        }

        Map<String, V> caseInsensitiveMap() {
            if (caseInsensitiveMap.isEmpty()) {
                return Collections.emptyMap();
            }
            Map<String, V> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            result.putAll(caseInsensitiveMap);
            return Collections.unmodifiableMap(result);
        }
    }
}
