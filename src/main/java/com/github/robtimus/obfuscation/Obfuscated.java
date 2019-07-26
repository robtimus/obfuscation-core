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
 * A class that provides obfuscation for another value. This obfuscation is performed when {@link #toString()} is called, and can be used to prevent
 * leaking the obfuscated value's string presentation by accident, e.g. by logging it.
 *
 * @author Rob Spoor
 * @param <T> The obfuscated value type.
 */
public final class Obfuscated<T> {

    private final T obfuscated;
    private final Obfuscator obfuscator;
    private final Supplier<? extends CharSequence> representation;

    Obfuscated(T obfuscated, Obfuscator obfuscator, Supplier<? extends CharSequence> representation) {
        this.obfuscated = Objects.requireNonNull(obfuscated);
        this.obfuscator = Objects.requireNonNull(obfuscator);
        this.representation = Objects.requireNonNull(representation);
    }

    /**
     * Returns the obfuscated object.
     *
     * @return The obfuscated object.
     */
    public T obfuscated() {
        return obfuscated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || o.getClass() != getClass()) {
            return false;
        }
        Obfuscated<?> other = (Obfuscated<?>) o;
        return obfuscated.equals(other.obfuscated);
    }

    @Override
    public int hashCode() {
        return obfuscated.hashCode();
    }

    @Override
    public String toString() {
        return obfuscator.obfuscateText(representation.get()).toString();
    }
}
