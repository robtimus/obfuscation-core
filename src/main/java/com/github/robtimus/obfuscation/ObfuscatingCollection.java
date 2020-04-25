/*
 * ObfuscatingCollection.java
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

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

class ObfuscatingCollection<E> implements Collection<E> {

    private final Collection<E> collection;

    final Function<String, CharSequence> elementObfuscator;

    ObfuscatingCollection(Collection<E> collection, Function<String, CharSequence> elementObfuscator) {
        this.collection = Objects.requireNonNull(collection);
        this.elementObfuscator = Objects.requireNonNull(elementObfuscator);
    }

    @Override
    public int size() {
        return collection.size();
    }

    @Override
    public boolean isEmpty() {
        return collection.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return collection.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return collection.iterator();
    }

    @Override
    public Object[] toArray() {
        return collection.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return collection.toArray(a);
    }

    @Override
    public boolean add(E e) {
        return collection.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return collection.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return collection.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return collection.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return collection.removeAll(c);
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        return collection.removeIf(filter);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return collection.retainAll(c);
    }

    @Override
    public void clear() {
        collection.clear();
    }

    @Override
    public Spliterator<E> spliterator() {
        return collection.spliterator();
    }

    @Override
    public Stream<E> stream() {
        return collection.stream();
    }

    @Override
    public Stream<E> parallelStream() {
        return collection.parallelStream();
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        collection.forEach(action);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || collection.equals(unwrap(obj));
    }

    Object unwrap(Object obj) {
        Object result = obj;
        while (result instanceof ObfuscatingCollection<?>) {
            result = ((ObfuscatingCollection<?>) result).collection;
        }
        return result;
    }

    @Override
    public int hashCode() {
        return collection.hashCode();
    }

    @Override
    public String toString() {
        Iterator<E> iterator = collection.iterator();
        if (!iterator.hasNext()) {
            return "[]"; //$NON-NLS-1$
        }

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        while (iterator.hasNext()) {
            E element = iterator.next();
            appendElement(element, sb);
            if (iterator.hasNext()) {
                sb.append(", "); //$NON-NLS-1$
            }
        }
        return sb.append(']').toString();
    }

    private void appendElement(E element, StringBuilder sb) {
        String s;
        if (element == null) {
            s = null;
        } else if (unwrap(element) == unwrap(collection)) {
            s = "(this Collection)"; //$NON-NLS-1$
        } else {
            s = element.toString();
        }
        CharSequence obfuscated = elementObfuscator.apply(s == null ? "null" : s); //$NON-NLS-1$
        sb.append(obfuscated);
    }
}
