/*
 * ObfuscatingMap.java
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

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

class ObfuscatingMap<K, V> implements Map<K, V> {

    private final Map<K, V> map;

    private final BiFunction<K, ? super V, CharSequence> valueRepresentation;
    private final BiFunction<K, CharSequence, CharSequence> valueObfuscator;

    private Collection<V> values;
    private Set<Map.Entry<K, V>> entrySet;

    ObfuscatingMap(Map<K, V> map, BiFunction<K, ? super V, CharSequence> valueRepresentation,
            BiFunction<K, CharSequence, CharSequence> valueObfuscator) {

        this.map = Objects.requireNonNull(map);
        this.valueRepresentation = Objects.requireNonNull(valueRepresentation);
        this.valueObfuscator = Objects.requireNonNull(valueObfuscator);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return map.get(key);
    }

    @Override
    public V put(K key, V value) {
        return map.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return map.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        map.putAll(m);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<V> values() {
        if (values == null) {
            values = new Values<>(this);
        }
        return values;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        if (entrySet == null) {
            entrySet = new EntrySet<>(this);
        }
        return entrySet;
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return map.getOrDefault(key, defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        map.forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        map.replaceAll(function);
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return map.putIfAbsent(key, value);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return map.remove(key, value);
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return map.replace(key, oldValue, newValue);
    }

    @Override
    public V replace(K key, V value) {
        return map.replace(key, value);
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return map.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return map.computeIfPresent(key, remappingFunction);
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return map.compute(key, remappingFunction);
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return map.merge(key, value, remappingFunction);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || map.equals(unwrap(obj));
    }

    private Object unwrap(Object obj) {
        Object result = obj;
        while (result instanceof ObfuscatingMap<?, ?>) {
            result = ((ObfuscatingMap<?, ?>) result).map;
        }
        return result;
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    @Override
    public String toString() {
        Iterator<Entry<K, V>> iterator = map.entrySet().iterator();
        if (!iterator.hasNext()) {
            return "{}"; //$NON-NLS-1$
        }

        StringBuilder sb = new StringBuilder();
        sb.append('{');
        while (iterator.hasNext()) {
            Entry<K, V> entry = iterator.next();
            K key = entry.getKey();
            V value = entry.getValue();
            sb.append(key);
            sb.append('=');
            appendValue(key, value, sb, map, "(this Map)", this::unwrap); //$NON-NLS-1$
            if (iterator.hasNext()) {
                sb.append(", "); //$NON-NLS-1$
            }
        }
        return sb.append('}').toString();
    }

    void appendValue(K key, V value, StringBuilder sb, Object unlessSame, String ifSame, Function<Object, Object> unwrapper) {
        CharSequence s;
        if (value == null) {
            s = null;
        } else if (unwrapper.apply(value) == unwrapper.apply(unlessSame)) {
            s = ifSame;
        } else {
            s = valueRepresentation.apply(key, value);
        }
        CharSequence obfuscated = valueObfuscator.apply(key, s == null ? "null" : s); //$NON-NLS-1$
        sb.append(obfuscated);
    }

    private static final class Values<K, V> extends ObfuscatingCollection<V> {

        private final ObfuscatingMap<K, V> map;
        private final Set<Entry<K, V>> entrySet;

        private Values(ObfuscatingMap<K, V> map) {
            super(map.map.values(), unsupportedOperation(), unsupportedOperation());
            this.map = map;
            entrySet = map.map.entrySet();
        }

        @Override
        public String toString() {
            Iterator<Entry<K, V>> iterator = entrySet.iterator();
            if (!iterator.hasNext()) {
                return "[]"; //$NON-NLS-1$
            }

            StringBuilder sb = new StringBuilder();
            sb.append('[');
            while (iterator.hasNext()) {
                Entry<K, V> entry = iterator.next();
                K key = entry.getKey();
                V value = entry.getValue();
                map.appendValue(key, value, sb, map.values, "(this Collection)", this::unwrap); //$NON-NLS-1$
                if (iterator.hasNext()) {
                    sb.append(", "); //$NON-NLS-1$
                }
            }
            return sb.append(']').toString();
        }
    }

    private static final class EntrySet<K, V> extends ObfuscatingSet<Map.Entry<K, V>> {

        private final ObfuscatingMap<K, V> map;
        private final Set<Entry<K, V>> entrySet;

        private EntrySet(ObfuscatingMap<K, V> map) {
            super(map.map.entrySet(), unsupportedOperation(), unsupportedOperation());
            this.map = map;
            entrySet = map.map.entrySet();
        }

        @Override
        public String toString() {
            Iterator<Entry<K, V>> iterator = entrySet.iterator();
            if (!iterator.hasNext()) {
                return "[]"; //$NON-NLS-1$
            }

            StringBuilder sb = new StringBuilder();
            sb.append('[');
            while (iterator.hasNext()) {
                Entry<K, V> entry = iterator.next();
                K key = entry.getKey();
                V value = entry.getValue();
                sb.append(key);
                sb.append('=');
                map.appendValue(key, value, sb, map.entrySet, "(this Collection)", this::unwrap); //$NON-NLS-1$
                if (iterator.hasNext()) {
                    sb.append(", "); //$NON-NLS-1$
                }
            }
            return sb.append(']').toString();
        }
    }

    private static <T, R> Function<T, R> unsupportedOperation() {
        return t -> {
            throw new UnsupportedOperationException();
        };
    }
}
