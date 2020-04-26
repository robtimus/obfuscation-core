/*
 * ObfuscatingProperties.java
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

@SuppressWarnings("serial")
final class ObfuscatingProperties extends Properties {

    private Properties properties;

    private final Map<String, Obfuscator> obfuscators;
    private final Obfuscator defaultObfuscator;

    private transient Collection<Object> values;
    private transient Set<Map.Entry<Object, Object>> entrySet;

    ObfuscatingProperties(Properties properties, Map<String, Obfuscator> obfuscators, Obfuscator defaultObfuscator) {
        this.properties = properties;
        this.obfuscators = obfuscators;
        this.defaultObfuscator = defaultObfuscator;
    }

    @Override
    public synchronized int size() {
        return properties.size();
    }

    @Override
    public synchronized boolean isEmpty() {
        return properties.isEmpty();
    }

    @Override
    public synchronized boolean containsKey(Object key) {
        return properties.containsKey(key);
    }

    @Override
    public synchronized boolean containsValue(Object value) {
        return properties.containsValue(value);
    }

    @Override
    public synchronized Object get(Object key) {
        return properties.get(key);
    }

    @Override
    public synchronized Object put(Object key, Object value) {
        return properties.put(key, value);
    }

    @Override
    public synchronized Object remove(Object key) {
        return properties.remove(key);
    }

    @Override
    public synchronized void putAll(Map<? extends Object, ? extends Object> t) {
        properties.putAll(t);
    }

    @Override
    public synchronized void clear() {
        properties.clear();
    }

    @Override
    public Set<Object> keySet() {
        return properties.keySet();
    }

    @Override
    public Collection<Object> values() {
        if (values == null) {
            values = new Values(this);
        }
        return values;
    }

    @Override
    public Set<Map.Entry<Object, Object>> entrySet() {
        if (entrySet == null) {
            entrySet = new EntrySet(this);
        }
        return entrySet;
    }

    @Override
    public synchronized Object getOrDefault(Object key, Object defaultValue) {
        return properties.getOrDefault(key, defaultValue);
    }

    @Override
    public synchronized void forEach(BiConsumer<? super Object, ? super Object> action) {
        properties.forEach(action);
    }

    @Override
    public synchronized void replaceAll(BiFunction<? super Object, ? super Object, ? extends Object> function) {
        properties.replaceAll(function);
    }

    @Override
    public synchronized Object putIfAbsent(Object key, Object value) {
        return properties.putIfAbsent(key, value);
    }

    @Override
    public synchronized boolean remove(Object key, Object value) {
        return properties.remove(key, value);
    }

    @Override
    public synchronized boolean replace(Object key, Object oldValue, Object newValue) {
        return properties.replace(key, oldValue, newValue);
    }

    @Override
    public synchronized Object replace(Object key, Object value) {
        return properties.replace(key, value);
    }

    @Override
    public synchronized Object computeIfAbsent(Object key, Function<? super Object, ? extends Object> mappingFunction) {
        return properties.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public synchronized Object computeIfPresent(Object key, BiFunction<? super Object, ? super Object, ? extends Object> remappingFunction) {
        return properties.computeIfPresent(key, remappingFunction);
    }

    @Override
    public synchronized Object compute(Object key, BiFunction<? super Object, ? super Object, ? extends Object> remappingFunction) {
        return properties.compute(key, remappingFunction);
    }

    @Override
    public synchronized Object merge(Object key, Object value, BiFunction<? super Object, ? super Object, ? extends Object> remappingFunction) {
        return properties.merge(key, value, remappingFunction);
    }

    @Override
    public synchronized boolean equals(Object o) {
        return this == o || properties.equals(unwrap(o));
    }

    private Object unwrap(Object obj) {
        Object result = obj;
        while (result instanceof ObfuscatingProperties) {
            result = ((ObfuscatingProperties) result).properties;
        }
        return result;
    }

    @Override
    public synchronized int hashCode() {
        return properties.hashCode();
    }

    @Override
    public synchronized String toString() {
        Iterator<Map.Entry<Object, Object>> iterator = properties.entrySet().iterator();
        if (!iterator.hasNext()) {
            return "{}"; //$NON-NLS-1$
        }

        StringBuilder sb = new StringBuilder();
        sb.append('{');
        while (iterator.hasNext()) {
            Map.Entry<?, ?> entry = iterator.next();
            Object key = entry.getKey();
            Object value = entry.getValue();
            sb.append(key);
            sb.append('=');
            appendValue(key, value, sb, properties, "(this Map)", this::unwrap); //$NON-NLS-1$
            if (iterator.hasNext()) {
                sb.append(", "); //$NON-NLS-1$
            }
        }
        return sb.append('}').toString();
    }

    void appendValue(Object key, Object value, StringBuilder sb, Object unlessSame, String ifSame, UnaryOperator<Object> unwrapper) {
        CharSequence s;
        Obfuscator obfuscator = defaultObfuscator;
        if (unwrapper.apply(value) == unwrapper.apply(unlessSame)) {
            s = ifSame;
        } else {
            s = value.toString();
            obfuscator = obfuscators.getOrDefault(key, defaultObfuscator);
        }
        CharSequence obfuscated = obfuscator != null ? obfuscator.obfuscateText(s) : s;
        sb.append(obfuscated);
    }

    @Override
    public synchronized Object clone() {
        ObfuscatingProperties clone = (ObfuscatingProperties) super.clone();
        clone.properties = (Properties) properties.clone();
        clone.values = null;
        clone.entrySet = null;
        return clone;
    }

    @Override
    public synchronized Enumeration<Object> keys() {
        return properties.keys();
    }

    @Override
    public synchronized Enumeration<Object> elements() {
        return properties.elements();
    }

    @Override
    public synchronized boolean contains(Object value) {
        return properties.contains(value);
    }

    @Override
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    @Override
    public synchronized Object setProperty(String key, String value) {
        return properties.setProperty(key, value);
    }

    @Override
    public Enumeration<?> propertyNames() {
        return properties.propertyNames();
    }

    @Override
    public Set<String> stringPropertyNames() {
        return properties.stringPropertyNames();
    }

    @Override
    public synchronized void load(Reader reader) throws IOException {
        properties.load(reader);
    }

    @Override
    public synchronized void load(InputStream inStream) throws IOException {
        properties.load(inStream);
    }

    @Override
    @Deprecated
    public synchronized void save(OutputStream out, String comments) {
        properties.save(out, comments);
    }

    @Override
    public void store(Writer writer, String comments) throws IOException {
        properties.store(writer, comments);
    }

    @Override
    public void store(OutputStream out, String comments) throws IOException {
        properties.store(out, comments);
    }

    @Override
    public synchronized void loadFromXML(InputStream in) throws IOException {
        properties.loadFromXML(in);
    }

    @Override
    public void storeToXML(OutputStream os, String comment) throws IOException {
        properties.storeToXML(os, comment);
    }

    @Override
    public void storeToXML(OutputStream os, String comment, String encoding) throws IOException {
        properties.storeToXML(os, comment, encoding);
    }

    @Override
    public void list(PrintStream out) {
        out.println("-- listing properties --"); //$NON-NLS-1$
        for (Enumeration<?> e = properties.propertyNames(); e.hasMoreElements(); ) {
            String key = (String) e.nextElement();
            String value = properties.getProperty(key);

            Obfuscator obfuscator = obfuscators.getOrDefault(key, defaultObfuscator);
            CharSequence s = obfuscator != null ? obfuscator.obfuscateText(value) : value;
            if (s.length() > 40) {
                s = s.subSequence(0, 37) + "..."; //$NON-NLS-1$
            }
            out.println(key + "=" + s); //$NON-NLS-1$
        }
    }

    @Override
    public void list(PrintWriter out) {
        out.println("-- listing properties --"); //$NON-NLS-1$
        for (Enumeration<?> e = properties.propertyNames(); e.hasMoreElements(); ) {
            String key = (String) e.nextElement();
            String value = properties.getProperty(key);

            Obfuscator obfuscator = obfuscators.getOrDefault(key, defaultObfuscator);
            CharSequence s = obfuscator != null ? obfuscator.obfuscateText(value) : value;
            if (s.length() > 40) {
                s = s.subSequence(0, 37) + "..."; //$NON-NLS-1$
            }
            out.println(key + "=" + s); //$NON-NLS-1$
        }
    }

    private static final class Values extends ObfuscatingCollection<Object> {

        private final ObfuscatingProperties properties;
        private final Set<Map.Entry<Object, Object>> entrySet;

        private Values(ObfuscatingProperties properties) {
            super(properties.properties.values(), unsupportedOperation());
            this.properties = properties;
            entrySet = properties.properties.entrySet();
        }

        @Override
        public String toString() {
            Iterator<Map.Entry<Object, Object>> iterator = entrySet.iterator();
            if (!iterator.hasNext()) {
                return "[]"; //$NON-NLS-1$
            }

            StringBuilder sb = new StringBuilder();
            sb.append('[');
            while (iterator.hasNext()) {
                Map.Entry<?, ?> entry = iterator.next();
                Object key = entry.getKey();
                Object value = entry.getValue();
                properties.appendValue(key, value, sb, properties.values, "(this Collection)", this::unwrap); //$NON-NLS-1$
                if (iterator.hasNext()) {
                    sb.append(", "); //$NON-NLS-1$
                }
            }
            return sb.append(']').toString();
        }
    }

    private static final class EntrySet extends ObfuscatingSet<Map.Entry<Object, Object>> {

        private final ObfuscatingProperties properties;
        private final Set<Entry<Object, Object>> entrySet;

        private EntrySet(ObfuscatingProperties properties) {
            super(properties.properties.entrySet(), unsupportedOperation());
            this.properties = properties;
            entrySet = properties.properties.entrySet();
        }

        @Override
        public String toString() {
            Iterator<Entry<Object, Object>> iterator = entrySet.iterator();
            if (!iterator.hasNext()) {
                return "[]"; //$NON-NLS-1$
            }

            StringBuilder sb = new StringBuilder();
            sb.append('[');
            while (iterator.hasNext()) {
                Entry<?, ?> entry = iterator.next();
                Object key = entry.getKey();
                Object value = entry.getValue();
                sb.append(key);
                sb.append('=');
                properties.appendValue(key, value, sb, properties.entrySet, "(this Collection)", this::unwrap); //$NON-NLS-1$
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
