/*
 * Obfuscated.java
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

import java.util.Objects;
import java.util.function.Supplier;

/**
 * An object that provides obfuscation for another value. This obfuscation is performed when {@link #toString()} is called, and can be used to prevent
 * leaking the obfuscated value's string presentation by accident, e.g. by logging it.
 *
 * @author Rob Spoor
 * @param <T> The obfuscated value type.
 */
public abstract class Obfuscated<T> {

    private final T value;

    private Obfuscated(T value) {
        this.value = Objects.requireNonNull(value);
    }

    private Obfuscated(Obfuscated<T> other) {
        this.value = other.value;
    }

    static <T> Obfuscated<T> of(T value, Obfuscator obfuscator, Supplier<? extends CharSequence> representation) {
        return new Obfuscating<>(value, obfuscator, representation);
    }

    /**
     * Returns the obfuscated value.
     *
     * @return The obfuscated value.
     */
    public final T value() {
        return value;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof Obfuscated) {
            Obfuscated<?> other = (Obfuscated<?>) o;
            return value.equals(other.value);
        }
        return false;
    }

    @Override
    public final int hashCode() {
        return value.hashCode();
    }

    @Override
    public abstract String toString();

    /**
     * Returns an obfuscated object that caches the results of obfuscating.
     * This can be used when the result of obfuscation never changes, for example when obfuscating immutable objects.
     *
     * @return An obfuscated object that caches the results of obfuscating.
     */
    public abstract Obfuscated<T> cached();

    private static final class Obfuscating<T> extends Obfuscated<T> {

        private final Obfuscator obfuscator;
        private final Supplier<? extends CharSequence> representation;

        Obfuscating(T obfuscated, Obfuscator obfuscator, Supplier<? extends CharSequence> representation) {
            super(obfuscated);
            this.obfuscator = Objects.requireNonNull(obfuscator);
            this.representation = Objects.requireNonNull(representation);
        }

        @Override
        public String toString() {
            return obfuscator.obfuscateText(representation.get()).toString();
        }

        @Override
        public Obfuscated<T> cached() {
            return new Cached<>(this, toString());
        }
    }

    private static final class Cached<T> extends Obfuscated<T> {

        private final String stringValue;

        private Cached(Obfuscated<T> obfuscated, String stringValue) {
            super(obfuscated);
            this.stringValue = Objects.requireNonNull(stringValue);
        }

        @Override
        public String toString() {
            return stringValue;
        }

        @Override
        public Obfuscated<T> cached() {
            return this;
        }
    }
}
