/*
 * PropertyAwareBuilder.java
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
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * The base class for builders that create objects that can obfuscate based on property name.
 *
 * @author Rob Spoor
 * @param <B> The builder type. Sub classes should use themselves as builder type.
 * @param <T> The type of object to build.
 */
public abstract class PropertyAwareBuilder<B extends PropertyAwareBuilder<B, T>, T> {

    private final Map<String, Obfuscator> obfuscators;

    private boolean caseInsensitivePropertyNames;

    /**
     * Creates a new builder.
     */
    protected PropertyAwareBuilder() {
        obfuscators = new HashMap<>();
        caseInsensitivePropertyNames = false;
    }

    @SuppressWarnings("unchecked")
    private B thisObject() {
        return (B) this;
    }

    /**
     * Adds a property to obfuscate.
     *
     * @param property The name of the property.
     * @param obfuscator The obfuscator to use for obfuscating the property.
     * @return This object.
     */
    public B withProperty(String property, Obfuscator obfuscator) {
        Objects.requireNonNull(property);
        Objects.requireNonNull(obfuscator);
        obfuscators.put(property, obfuscator);
        return thisObject();
    }

    /**
     * Sets whether or not the case in property names should be ignored when looking up obfuscators. The default is {@code false}.
     * <p>
     * Note: it's undefined which obfuscator will be used if the case should be ignored and two obfuscators are added for two properties that are
     * equal ignoring the case.
     *
     * @param caseInsensitivePropertyNames {@code true} to ignore case, or {@code false} otherwise.
     * @return This object.
     */
    public B withCaseInsensitivePropertyNames(boolean caseInsensitivePropertyNames) {
        this.caseInsensitivePropertyNames = caseInsensitivePropertyNames;
        return thisObject();
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
    public <R> R transform(Function<? super B, ? extends R> f) {
        return f.apply(thisObject());
    }

    /**
     * Returns a new unmodifiable map with all added obfuscators. This map will use case insensitive lookups if
     * {@link #withCaseInsensitivePropertyNames(boolean) withCaseInsensitivePropertyNames(false)} was called.
     *
     * @return A new unmodifiable map with all added obfuscators.
     */
    public Map<String, Obfuscator> obfuscators() {
        Map<String, Obfuscator> obfuscatorMap = caseInsensitivePropertyNames
                ? new TreeMap<>(String.CASE_INSENSITIVE_ORDER)
                : new HashMap<>(obfuscators.size());
        obfuscatorMap.putAll(obfuscators);
        return Collections.unmodifiableMap(obfuscatorMap);
    }

    /**
     * Creates a new object with the properties and obfuscators added to this builder.
     *
     * @return The created object.
     */
    public abstract T build();
}
