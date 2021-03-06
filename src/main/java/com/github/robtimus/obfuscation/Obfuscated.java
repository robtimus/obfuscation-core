/*
 * Obfuscated.java
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

import java.util.Objects;
import java.util.function.Function;
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
    private final Obfuscator obfuscator;

    private Obfuscated(T value, Obfuscator obfuscator) {
        this.value = Objects.requireNonNull(value);
        this.obfuscator = Objects.requireNonNull(obfuscator);
    }

    private Obfuscated(Obfuscated<T> other) {
        this.value = other.value;
        this.obfuscator = other.obfuscator;
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

    final Obfuscator obfuscator() {
        return obfuscator;
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
     * Applies a mapping function to the obfuscated value.
     * The result is similar to calling {@link Obfuscator#obfuscateObject(Object)} on the obfuscator that created this object, passing the result of
     * applying the given mapping function to the obfuscated value.
     *
     * @param <U> The result type of the mapping function.
     * @param mapper The mapping function to apply.
     * @return An {@code Obfuscated} object wrapping the result of applying the given mapping function to this object's value.
     * @throws NullPointerException If the mapping function is {@code null},
     *                                  or if the mapping function returns a {@code null} value when applied to this object's value.
     * @since 1.1
     */
    public final <U> Obfuscated<U> map(Function<? super T, ? extends U> mapper) {
        U mapped = mapper.apply(value);
        return obfuscator().obfuscateObject(mapped);
    }

    /**
     * Applies a mapping function to the obfuscated value.
     * The result is similar to calling {@link Obfuscator#obfuscateObject(Object, Supplier)} on the obfuscator that created this object, passing the
     * result of applying the given mapping function to the obfuscated value.
     *
     * @param <U> The result type of the mapping function.
     * @param mapper The mapping function to apply.
     * @param representation A supplier for the character representation that will be used to obfuscate the value.
     *                           This can be used for values that don't have a sensible {@link Object#toString() string representation} of their own.
     * @return An {@code Obfuscated} object wrapping the result of applying the given mapping function to this object's value.
     * @throws NullPointerException If the mapping function or supplier is {@code null},
     *                                  or if the mapping function returns a {@code null} value when applied to this object's value.
     * @since 1.1
     */
    public final <U> Obfuscated<U> map(Function<? super T, ? extends U> mapper, Supplier<? extends CharSequence> representation) {
        U mapped = mapper.apply(value);
        return obfuscator().obfuscateObject(mapped, representation);
    }

    /**
     * Applies a mapping function to the obfuscating value.
     * Unlike {@link #map(Function)} and {@link #map(Function, Supplier)}, the result will use the same representation to obfuscate as this object.
     * If this object {@link #cached() caches} the results of obfuscating, so will the result.
     *
     * @param <U> The result type of the mapping function.
     * @param mapper The mapping function to apply.
     * @return An {@code Obfuscated} object wrapping the result of applying the given mapping function to this object's value.
     * @throws NullPointerException If the mapping function is {@code null},
     *                                  or if the mapping function returns a {@code null} value when applied to this object's value.
     * @since 1.1
     */
    public abstract <U> Obfuscated<U> mapWithSameRepresentation(Function<? super T, ? extends U> mapper);

    /**
     * Returns an obfuscated object that caches the results of obfuscating.
     * This can be used when the result of obfuscation never changes, for example when obfuscating immutable objects.
     *
     * @return An obfuscated object that caches the results of obfuscating.
     */
    public abstract Obfuscated<T> cached();

    private static final class Obfuscating<T> extends Obfuscated<T> {

        private final Supplier<? extends CharSequence> representation;

        private Obfuscating(T obfuscated, Obfuscator obfuscator, Supplier<? extends CharSequence> representation) {
            super(obfuscated, obfuscator);
            this.representation = Objects.requireNonNull(representation);
        }

        @Override
        public String toString() {
            return obfuscator().obfuscateText(representation.get()).toString();
        }

        @Override
        public <U> Obfuscated<U> mapWithSameRepresentation(Function<? super T, ? extends U> mapper) {
            U mapped = mapper.apply(value());
            return new Obfuscating<>(mapped, obfuscator(), representation);
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

        private Cached(T obfuscated, Obfuscator obfuscator, String stringValue) {
            super(obfuscated, obfuscator);
            this.stringValue = Objects.requireNonNull(stringValue);
        }

        @Override
        public String toString() {
            return stringValue;
        }

        @Override
        public <U> Obfuscated<U> mapWithSameRepresentation(Function<? super T, ? extends U> mapper) {
            U mapped = mapper.apply(value());
            return new Cached<>(mapped, obfuscator(), stringValue);
        }

        @Override
        public Obfuscated<T> cached() {
            return this;
        }
    }
}
