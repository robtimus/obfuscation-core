/*
 * MapObfuscator.java
 * Copyright 2020 Rob Spoor
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
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * An immutable object that can obfuscate maps so that some or all of their values are obfuscated when their {@link Object#toString() toString()}
 * method is called.
 * <p>
 * The end result is similar to {@link Obfuscator#obfuscateMap(Map)}, except this class gives a more fine-grained control over which values to
 * obfuscate and with which obfuscator.
 *
 * @author Rob Spoor
 * @param <K> The map key type.
 * @param <V> The map value type.
 */
public final class MapObfuscator<K, V> {

    private final Map<K, Obfuscator> obfuscators;
    private final Obfuscator defaultObfuscator;

    private final BiFunction<K, String, CharSequence> valueObfuscator;

    private MapObfuscator(Builder<K, V> builder) {
        obfuscators = builder.obfuscators();
        defaultObfuscator = builder.defaultObfuscator;

        valueObfuscator = (k, v) -> {
            Obfuscator obfuscator = obfuscators.getOrDefault(k, defaultObfuscator);
            return obfuscator == null ? v : obfuscator.obfuscateText(v);
        };
    }

    /**
     * Obfuscates a map.
     * <p>
     * The result will be a map that will behave exactly the same as the given map, except it will obfuscate each value when its
     * {@link Object#toString() toString()} method is called. This is different from {@link Obfuscator#obfuscateObject(Object)} because it will not
     * obfuscate the map structure or the number of entries.
     *
     * @param map The map to obfuscate.
     * @return An obfuscating map wrapper around the given map.
     * @throws NullPointerException If the given map is {@code null}.
     */
    public Map<K, V> obfuscateMap(Map<K, V> map) {
        Objects.requireNonNull(map);
        return new ObfuscatingMap<>(map, valueObfuscator);
    }

    /**
     * Returns a builder that will create {@code MapObfuscators}.
     *
     * @param <K> The map key type.
     * @param <V> The map value type.
     * @return A builder that will create {@code MapObfuscators}.
     */
    public static <K, V> Builder<K, V> builder() {
        return new Builder<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || o.getClass() != getClass()) {
            return false;
        }
        MapObfuscator<?, ?> other = (MapObfuscator<?, ?>) o;
        return obfuscators.equals(other.obfuscators)
                && Objects.equals(defaultObfuscator, other.defaultObfuscator);
    }

    @Override
    public int hashCode() {
        return obfuscators.hashCode() ^ Objects.hashCode(defaultObfuscator);
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return getClass().getName()
                + "[obfuscators=" + obfuscators
                + ",defaultObfuscator=" + defaultObfuscator
                + "]";
    }

    /**
     * A builder for {@link MapObfuscator MapObfuscators}.
     *
     * @author Rob Spoor
     * @param <K> The map key type.
     * @param <V> The map value type.
     */
    public static final class Builder<K, V> {

        private final Map<K, Obfuscator> obfuscators;
        private Obfuscator defaultObfuscator;

        private Builder() {
            obfuscators = new HashMap<>();
            defaultObfuscator = null;
        }

        /**
         * Adds a key to obfuscate the value for. The value's {@link Object#toString() string representation} will be used to obfuscate the value.
         *
         * @param key The key to obfuscate the value for.
         * @param obfuscator The obfuscator to use for obfuscating the value.
         * @return This object.
         * @throws NullPointerException If the given obfuscator is {@code null}.
         */
        public Builder<K, V> withKey(K key, Obfuscator obfuscator) {
            Objects.requireNonNull(obfuscator);
            obfuscators.put(key, obfuscator);
            return this;
        }

        /**
         * Sets the default obfuscator to use, for entries that have no specific obfuscator defined. The default is {@code null}.
         *
         * @param obfuscator The default obfuscator to use. Use {@code null} to only obfuscate specific entries.
         * @return This object.
         */
        public Builder<K, V> withDefaultObfuscator(Obfuscator obfuscator) {
            this.defaultObfuscator = obfuscator;
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
        public <R> R transform(Function<? super Builder<K, V>, ? extends R> f) {
            return f.apply(this);
        }

        private Map<K, Obfuscator> obfuscators() {
            return Collections.unmodifiableMap(new HashMap<>(obfuscators));
        }

        /**
         * Creates a new map obfuscator with the current settings of this builder.
         *
         * @return A new map obfuscator with the current settings of this builder.
         */
        public MapObfuscator<K, V> build() {
            return new MapObfuscator<>(this);
        }
    }
}
