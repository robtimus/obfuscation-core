/*
 * PropertyObfuscator.java
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

import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * An obfuscator that obfuscates certain properties in {@link CharSequence CharSequences} or the contents of {@link Reader Readers}.
 *
 * @author Rob Spoor
 */
public abstract class PropertyObfuscator extends Obfuscator {

    private static final ServiceLoader<PropertyObfuscatorFactory> FACTORY_LOADER = ServiceLoader.load(PropertyObfuscatorFactory.class);

    private final Map<String, Obfuscator> obfuscators;

    /**
     * Creates a new obfuscator.
     *
     * @param builder The builder that is used to create this obfuscator.
     */
    protected PropertyObfuscator(Builder builder) {
        this.obfuscators = Collections.unmodifiableMap(new HashMap<>(builder.obfuscators));
    }

    /**
     * Returns a mapping from properties to obfuscators. This mapping is unmodifiable.
     *
     * @return A mapping from properties to obfuscators
     */
    protected final Map<String, Obfuscator> obfuscators() {
        return obfuscators;
    }

    /**
     * Returns the obfuscator for a specific property.
     *
     * @param property The name of the property to get the obfuscator for.
     * @return The obfuscator for the given property, or {@code null} if no obfuscator is configured for the given property.
     */
    protected final Obfuscator getObfuscator(String property) {
        return obfuscators.get(property);
    }

    /**
     * Returns the obfuscator for a specific property.
     *
     * @param property The name of the property to get the obfuscator for.
     * @return The obfuscator for the given property, or {@link #none()} if no obfuscator is configured for the given property.
     */
    protected final Obfuscator getNonNullObfuscator(String property) {
        return obfuscators.getOrDefault(property, none());
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * This base implementation returns {@code true} if the given object is a {@code PropertyObfuscator} of the same type with the same property
     * obfuscators. When overriding this method, sub classes should always first call {@code super.equals(o)}; if this returns {@code false} the
     * method should false, otherwise the given object can safely be cast to the sub class' type.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || o.getClass() != getClass()) {
            return false;
        }
        PropertyObfuscator other = (PropertyObfuscator) o;
        return obfuscators.equals(other.obfuscators);
    }

    @Override
    public int hashCode() {
        return obfuscators.hashCode();
    }

    /**
     * Returns a builder that will create obfuscators that can handle UTF-8 encoded request parameter strings.
     * These can be used for both query strings and form data strings.
     *
     * @return A builder that will create obfuscators that can handle request parameter strings.
     */
    public static Builder requestParameters() {
        return new Builder(b -> new RequestParameterObfuscator(b, StandardCharsets.UTF_8));
    }

    /**
     * Returns a builder that will create obfuscators that can handle request parameter strings.
     * These can be used for both query strings and form data strings.
     *
     * @param encoding The encoding to use.
     * @return A builder that will create obfuscators that can handle request parameter strings.
     * @throws NullPointerException If the encoding is {@code null}.
     */
    public static Builder requestParameters(Charset encoding) {
        Objects.requireNonNull(encoding);
        return new Builder(b -> new RequestParameterObfuscator(b, encoding));
    }

    /**
     * Returns a builder that will create obfuscators that can handle strings that contain comma separated key-value pairs in the form
     * {@code key=value}. Whitespace before and after the {@code =} sign and after commas will be ignored; whitespace before commas will be considered
     * a part of the value.
     *
     * @return A builder that will create obfuscators that can handle strings that contain comma separated key-value pairs in the form
     *         {@code key=value}.
     */
    public static Builder commaSeparated() {
        return new Builder(b -> new CommaSeparatedObfuscator(b));
    }

    /**
     * Returns a builder that will create obfuscators using a specific factory.
     *
     * @param factory The factory to use for creating obfuscators.
     * @return A builder that will use the given factory to create obfuscators.
     * @throws NullPointerException If the given factory is {@code null}.
     */
    public static Builder withFactory(PropertyObfuscatorFactory factory) {
        Objects.requireNonNull(factory);
        return new Builder(factory::createPropertyObfuscator);
    }

    /**
     * Returns a builder that will create obfuscators of a specific type. The available types are returned by {@link #availableTypes()}, and are
     * determined by the available {@link PropertyObfuscatorFactory} instances that are configured using the {@link ServiceLoader} mechanism.
     *
     * @param type The type of obfuscator to create.
     * @return A builder that will create obfuscators of the given type.
     * @throws IllegalArgumentException If no obfuscator can be found for the given type.
     * @see PropertyObfuscatorFactory#type()
     */
    public static Builder ofType(String type) {
        PropertyObfuscatorFactory factory = findFactory(f -> f.type().equals(type));
        if (factory == null) {
            throw new IllegalArgumentException(Messages.PropertyObfuscator.invalidType.get(type));
        }
        return new Builder(factory::createPropertyObfuscator);
    }

    private static PropertyObfuscatorFactory findFactory(Predicate<PropertyObfuscatorFactory> predicate) {
        synchronized (FACTORY_LOADER) {
            for (PropertyObfuscatorFactory factory : FACTORY_LOADER) {
                if (predicate.test(factory)) {
                    return factory;
                }
            }
            return null;
        }
    }

    /**
     * Returns the available types that can be used with {@link #ofType(String)}.
     *
     * @return The available types; never {@code null} but possibly empty.
     */
    public static Set<String> availableTypes() {
        Set<String> types = new HashSet<>();
        synchronized (FACTORY_LOADER) {
            for (PropertyObfuscatorFactory factory : FACTORY_LOADER) {
                types.add(factory.type());
            }
        }
        return types;
    }

    /**
     * Reloads the available types that can be used with {@link #ofType(String)}.
     * <p>
     * This method is intended for use in situations in which new providers can be installed into a running Java virtual machine.
     */
    public static void reloadTypes() {
        synchronized (FACTORY_LOADER) {
            FACTORY_LOADER.reload();
        }
    }

    /**
     * A builder for {@link PropertyObfuscator PropertyObfuscators}.
     *
     * @author Rob Spoor
     */
    public static class Builder {

        private final Function<Builder, PropertyObfuscator> constructor;
        private final Map<String, Obfuscator> obfuscators;

        private Builder(Function<Builder, PropertyObfuscator> constructor) {
            this.constructor = Objects.requireNonNull(constructor);
            obfuscators = new HashMap<>();
        }

        /**
         * Creates a new builder that will use a specific {@code PropertyObfuscatorFactory} to create {@link PropertyObfuscator PropertyObfuscators}.
         * This constructor allows sub classes to be created that can add additional properties.
         *
         * @param factory The factory to use.
         */
        protected Builder(PropertyObfuscatorFactory factory) {
            this.constructor = factory::createPropertyObfuscator;
            obfuscators = new HashMap<>();
        }

        /**
         * Adds a property to obfuscate.
         *
         * @param property The name of the property.
         * @param obfuscator The obfuscator to use for obfuscating the property.
         * @return This object.
         */
        public Builder withProperty(String property, Obfuscator obfuscator) {
            Objects.requireNonNull(property);
            Objects.requireNonNull(obfuscator);
            obfuscators.put(property, obfuscator);
            return this;
        }

        /**
         * Creates a new {@code PropertyObfuscator} with the properties and obfuscators added to this builder.
         *
         * @return The created {@code PropertyObfuscator}.
         */
        public PropertyObfuscator build() {
            PropertyObfuscator obfuscator = constructor.apply(this);
            return Objects.requireNonNull(obfuscator);
        }
    }
}
