/*
 * ObfuscatingList.java
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
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;
import java.util.function.Function;
import java.util.function.UnaryOperator;

class ObfuscatingList<E> extends ObfuscatingCollection<E> implements List<E> {

    private final List<E> list;

    private ObfuscatingList(List<E> list, Function<String, CharSequence> elementObfuscator) {
        super(list, elementObfuscator);
        this.list = list;
    }

    static <E> List<E> of(List<E> list, Function<String, CharSequence> elementObfuscator) {

        return list instanceof RandomAccess
                ? new RandomAccessList<>(list, elementObfuscator)
                : new ObfuscatingList<>(list, elementObfuscator);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        return list.addAll(index, c);
    }

    @Override
    public void replaceAll(UnaryOperator<E> operator) {
        list.replaceAll(operator);
    }

    @Override
    public void sort(Comparator<? super E> c) {
        list.sort(c);
    }

    @Override
    public E get(int index) {
        return list.get(index);
    }

    @Override
    public E set(int index, E element) {
        return list.set(index, element);
    }

    @Override
    public void add(int index, E element) {
        list.add(index, element);
    }

    @Override
    public E remove(int index) {
        return list.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    @Override
    public ListIterator<E> listIterator() {
        return list.listIterator();
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return list.listIterator(index);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return of(list.subList(fromIndex, toIndex), elementObfuscator);
    }

    private static final class RandomAccessList<E> extends ObfuscatingList<E> implements RandomAccess {

        RandomAccessList(List<E> list, Function<String, CharSequence> elementObfuscator) {
            super(list, elementObfuscator);
        }
    }
}
