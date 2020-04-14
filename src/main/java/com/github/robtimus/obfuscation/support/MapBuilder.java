/*
 * MapBuilder.java
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

package com.github.robtimus.obfuscation.support;

import static com.github.robtimus.obfuscation.support.CaseSensitivity.CASE_INSENSITIVE;
import static com.github.robtimus.obfuscation.support.CaseSensitivity.CASE_SENSITIVE;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A builder for maps that support both case sensitive and case insensitive mappings.
 * <p>
 * This class can be used to create builders for objects that use values (like obfuscators) per keys, where each key can be treated individually
 * as case sensitive or case insensitive.
 * <p>
 * Note that like a {@link TreeMap} created with {@link String#CASE_INSENSITIVE_ORDER}, maps built with this builder fail to obey the general
 * contract of {@link Map#equals(Object)} if they contain any case insensitive mappings. Two maps built with the same settings will be equal to
 * each other though.
 *
 * @author Rob Spoor
 * @param <V> The value type for built maps.
 */
public final class MapBuilder<V> {

    private final Map<String, V> caseSensitiveMap;
    private final Map<String, V> caseInsensitiveMap;

    private CaseSensitivity defaultCaseSensitivity = CASE_SENSITIVE;

    /**
     * Creates a new map builder.
     */
    public MapBuilder() {
        caseSensitiveMap = new HashMap<>();
        caseInsensitiveMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    }

    /**
     * Adds an entry.
     * This method is an alias for {@link #withEntry(String, Object, CaseSensitivity)} with the last specified default case sensitivity using
     * {@link #caseSensitiveByDefault()} or {@link #caseInsensitiveByDefault()}. The default is {@link CaseSensitivity#CASE_SENSITIVE}.
     *
     * @param key The key for the entry.
     * @param value The value for the entry.
     * @return This object.
     * @throws NullPointerException If the key or value is {@code null}.
     * @throws IllegalArgumentException If an entry with the same key and the same case sensitivity was already added.
     */
    public MapBuilder<V> withEntry(String key, V value) {
        return withEntry(key, value, defaultCaseSensitivity);
    }

    /**
     * Adds an entry.
     *
     * @param key The key for the entry.
     * @param value The value for the entry.
     * @param caseSensitivity The case sensitivity for the key.
     * @return This object.
     * @throws NullPointerException If the key, value or case sensitivity is {@code null}.
     * @throws IllegalArgumentException If an entry with the same key and the same case sensitivity was already added.
     */
    public MapBuilder<V> withEntry(String key, V value, CaseSensitivity caseSensitivity) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        Objects.requireNonNull(caseSensitivity);

        Map<String, V> map = caseSensitivity.isCaseSensitive() ? caseSensitiveMap : caseInsensitiveMap;
        if (map.containsKey(key)) {
            throw new IllegalArgumentException(Messages.stringMap.duplicateKey.get(key, caseSensitivity));
        }
        map.put(key, value);
        return this;
    }

    /**
     * Sets the default case sensitivity for new entries to {@link CaseSensitivity#CASE_SENSITIVE}. This is the default setting.
     * <p>
     * Note that this will not change the case sensitivity of any entry that was already added.
     *
     * @return This object.
     */
    public MapBuilder<V> caseSensitiveByDefault() {
        defaultCaseSensitivity = CASE_SENSITIVE;
        return this;
    }

    /**
     * Sets the default case sensitivity for new entries to {@link CaseSensitivity#CASE_INSENSITIVE}.
     * <p>
     * Note that this will not change the case sensitivity of any entry that was already added.
     *
     * @return This object.
     */
    public MapBuilder<V> caseInsensitiveByDefault() {
        defaultCaseSensitivity = CASE_INSENSITIVE;
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
    public <R> R transform(Function<? super MapBuilder<?>, ? extends R> f) {
        return f.apply(this);
    }

    /**
     * Returns an immutable map with the entries added to this builder. This map is serializable.
     *
     * @return An immutable map with the entries added to this builder.
     */
    public Map<String, V> build() {
        if (caseSensitiveMap.isEmpty() && caseInsensitiveMap.isEmpty()) {
            return Collections.emptyMap();
        }
        if (caseSensitiveMap.isEmpty()) {
            return caseInsensitiveMap();
        }
        if (caseInsensitiveMap.isEmpty()) {
            return caseSensitiveMap();
        }
        return new StringMap<>(this);
    }

    Map<String, V> caseSensitiveMap() {
        return Collections.unmodifiableMap(new HashMap<>(caseSensitiveMap));
    }

    Map<String, V> caseInsensitiveMap() {
        Map<String, V> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        result.putAll(caseInsensitiveMap);
        return Collections.unmodifiableMap(result);
    }

    private static final class StringMap<V> extends AbstractMap<String, V> implements Serializable {

        private static final long serialVersionUID = -5691881236129965377L;

        private final Map<String, V> caseSensitiveMap;
        private final Map<String, V> caseInsensitiveMap;

        private transient Set<Entry<String, V>> entrySet;

        private StringMap(MapBuilder<V> builder) {
            caseSensitiveMap = builder.caseSensitiveMap();
            caseInsensitiveMap = builder.caseInsensitiveMap();
        }

        @Override
        public int size() {
            long size = (long) caseSensitiveMap.size() + caseInsensitiveMap.size();
            return (int) Math.min(size, Integer.MAX_VALUE);
        }

        @Override
        public boolean isEmpty() {
            // this should always return false since the StringMap instance is only created if neither map is empty
            return caseSensitiveMap.isEmpty() && caseInsensitiveMap.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return key instanceof String && (caseSensitiveMap.containsKey(key) || caseInsensitiveMap.containsKey(key));
        }

        @Override
        public boolean containsValue(Object value) {
            return value != null && (caseSensitiveMap.containsValue(value) || caseInsensitiveMap.containsValue(value));
        }

        @Override
        public V get(Object key) {
            if (key instanceof String) {
                V value = caseSensitiveMap.get(key);
                return value != null ? value : caseInsensitiveMap.get(key);
            }
            return null;
        }

        @Override
        public V put(String key, V value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V remove(Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putAll(Map<? extends String, ? extends V> m) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<Entry<String, V>> entrySet() {
            if (entrySet == null) {
                entrySet = new EntrySet();
            }
            return entrySet;
        }

        private class EntrySet extends AbstractSet<Entry<String, V>> {

            @Override
            public int size() {
                return StringMap.this.size();
            }

            @Override
            public boolean isEmpty() {
                return StringMap.this.isEmpty();
            }

            @Override
            public boolean contains(Object o) {
                return o instanceof Entry<?, ?> && contains((Entry<?, ?>) o);
            }

            private boolean contains(Entry<?, ?> entry) {
                Object key = entry.getKey();
                if (key instanceof String) {
                    V value = get(entry.getKey());
                    return value != null && value.equals(entry.getValue());
                }
                return false;
            }

            @Override
            public Iterator<Entry<String, V>> iterator() {
                return new Iterator<Entry<String, V>>() {
                    private final Iterator<Entry<String, V>> caseSensitiveIterator = caseSensitiveMap.entrySet().iterator();
                    private final Iterator<Entry<String, V>> caseInsensitiveIterator = caseInsensitiveMap.entrySet().iterator();

                    @Override
                    public boolean hasNext() {
                        return caseSensitiveIterator.hasNext() || caseInsensitiveIterator.hasNext();
                    }

                    @Override
                    public Entry<String, V> next() {
                        // Let caseInsensitiveIterator throw an exception if both iterators have been exhausted
                        return caseSensitiveIterator.hasNext() ? caseSensitiveIterator.next() : caseInsensitiveIterator.next();
                    }
                };
            }

            @Override
            public boolean add(Entry<String, V> e) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean remove(Object o) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean addAll(Collection<? extends Entry<String, V>> c) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean retainAll(Collection<?> c) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean removeAll(Collection<?> c) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void clear() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean removeIf(Predicate<? super Entry<String, V>> filter) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Stream<Entry<String, V>> stream() {
                return Stream.concat(caseSensitiveMap.entrySet().stream(), caseInsensitiveMap.entrySet().stream());
            }

            @Override
            public Stream<Entry<String, V>> parallelStream() {
                return Stream.concat(caseSensitiveMap.entrySet().parallelStream(), caseInsensitiveMap.entrySet().parallelStream());
            }

            @Override
            public void forEach(Consumer<? super Entry<String, V>> action) {
                Objects.requireNonNull(action);
                caseSensitiveMap.entrySet().forEach(action);
                caseInsensitiveMap.entrySet().forEach(action);
            }
        }

        @Override
        public V getOrDefault(Object key, V defaultValue) {
            V value = get(key);
            return value != null ? value : defaultValue;
        }

        @Override
        public void forEach(BiConsumer<? super String, ? super V> action) {
            Objects.requireNonNull(action);
            caseSensitiveMap.forEach(action);
            caseInsensitiveMap.forEach(action);
        }

        @Override
        public void replaceAll(BiFunction<? super String, ? super V, ? extends V> function) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V putIfAbsent(String key, V value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object key, Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean replace(String key, V oldValue, V newValue) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V replace(String key, V value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int hashCode() {
            // The hash code is defined to be the sum of all entries.
            // That means that the result is the sum of the hash codes of both individual maps.
            return caseSensitiveMap.hashCode() + caseInsensitiveMap.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof StringMap<?>) {
                StringMap<?> other = (StringMap<?>) obj;
                // If both maps are equal then the objects are equal.
                // This matches mostly what Map.equals defines, apart from the case insensitive checks.
                // However, that is the same issue that TreeMap has when using a Comparator that is not consistent with equals.
                return caseSensitiveMap.equals(other.caseSensitiveMap) && caseInsensitiveMap.equals(other.caseInsensitiveMap);
            }
            // delegate to super.equals which performs entry-by-entry comparison
            return super.equals(obj);
        }
    }
}
