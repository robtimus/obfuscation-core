/*
 * PropertiesObfuscator.java
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

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Function;
import com.github.robtimus.obfuscation.support.CaseSensitivity;
import com.github.robtimus.obfuscation.support.MapBuilder;

/**
 * An immutable object that can obfuscate {@link Properties} objects so that some or all of their values are obfuscated when their
 * {@link Object#toString() toString()}, {@link Properties#list(PrintStream) list(PrintStream)} or
 * {@link Properties#list(PrintWriter) list(PrintWriter)}  method is called.
 *
 * @author Rob Spoor
 */
public final class PropertiesObfuscator {

    private final Map<String, Obfuscator> obfuscators;
    private final Obfuscator defaultObfuscator;

    private PropertiesObfuscator(Builder builder) {
        obfuscators = builder.obfuscators();
        defaultObfuscator = builder.defaultObfuscator;
    }

    /**
     * Obfuscates a {@link Properties} object.
     * <p>
     * The result will be a {@link Properties} object that will behave exactly the same as the given {@link Properties} object, except it will
     * obfuscate each value when its {@link Object#toString() toString()}, {@link Properties#list(PrintStream) list(PrintStream)} or
     * {@link Properties#list(PrintWriter) list(PrintWriter)} method is called.
     * <p>
     * Note that the result is <b>not</b> serializable.
     *
     * @param properties The {@link Properties} object to obfuscate.
     * @return An obfuscating {@link Properties} wrapper around the given {@link Properties} object.
     * @throws NullPointerException If the given {@link Properties} object is {@code null}.
     */
    public Properties obfuscateProperties(Properties properties) {
        Objects.requireNonNull(properties);
        return new ObfuscatingProperties(properties, obfuscators, defaultObfuscator);
    }

    /**
     * Returns a builder that will create {@code PropertiesObfuscators}.
     *
     * @return A builder that will create {@code PropertiesObfuscators}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * A builder for {@link PropertiesObfuscator PropertiesObfuscators}.
     *
     * @author Rob Spoor
     */
    public static final class Builder {

        private final MapBuilder<Obfuscator> obfuscators;
        private Obfuscator defaultObfuscator;

        private Builder() {
            obfuscators = new MapBuilder<>();
            defaultObfuscator = null;
        }

        /**
         * Adds a property to obfuscate.
         * This method is an alias for {@link #withProperty(String, Obfuscator, CaseSensitivity)} with the last specified default case sensitivity
         * using {@link #caseSensitiveByDefault()} or {@link #caseInsensitiveByDefault()}. The default is {@link CaseSensitivity#CASE_SENSITIVE}.
         *
         * @param property The name of the property.
         * @param obfuscator The obfuscator to use for obfuscating the property.
         * @return This object.
         * @throws NullPointerException If the given property name or obfuscator is {@code null}.
         * @throws IllegalArgumentException If a property with the same name and the same case sensitivity was already added.
         */
        public Builder withProperty(String property, Obfuscator obfuscator) {
            obfuscators.withEntry(property, obfuscator);
            return this;
        }

        /**
         * Adds a property to obfuscate.
         *
         * @param property The name of the property.
         * @param obfuscator The obfuscator to use for obfuscating the property.
         * @param caseSensitivity The case sensitivity for the property.
         * @return This object.
         * @throws NullPointerException If the given property name, obfuscator or case sensitivity is {@code null}.
         * @throws IllegalArgumentException If a property with the same name and the same case sensitivity was already added.
         */
        public Builder withProperty(String property, Obfuscator obfuscator, CaseSensitivity caseSensitivity) {
            obfuscators.withEntry(property, obfuscator, caseSensitivity);
            return this;
        }

        /**
         * Sets the default case sensitivity for new properties to {@link CaseSensitivity#CASE_SENSITIVE}. This is the default setting.
         * <p>
         * Note that this will not change the case sensitivity of any property that was already added.
         *
         * @return This object.
         */
        public Builder caseSensitiveByDefault() {
            obfuscators.caseSensitiveByDefault();
            return this;
        }

        /**
         * Sets the default case sensitivity for new properties to {@link CaseSensitivity#CASE_INSENSITIVE}.
         * <p>
         * Note that this will not change the case sensitivity of any property that was already added.
         *
         * @return This object.
         */
        public Builder caseInsensitiveByDefault() {
            obfuscators.caseInsensitiveByDefault();
            return this;
        }

        /**
         * Sets the default obfuscator to use, for properties that have no specific obfuscator defined. The default is {@code null}.
         *
         * @param obfuscator The default obfuscator to use. Use {@code null} to only obfuscate specific properties.
         * @return This object.
         */
        public Builder withDefaultObfuscator(Obfuscator obfuscator) {
            this.defaultObfuscator = obfuscator;
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
        public <R> R transform(Function<? super Builder, ? extends R> f) {
            return f.apply(this);
        }

        private Map<String, Obfuscator> obfuscators() {
            return obfuscators.build();
        }

        /**
         * Creates a new {@link Properties} obfuscator with the current settings of this builder.
         *
         * @return A new {@link Properties} obfuscator with the current settings of this builder.
         */
        public PropertiesObfuscator build() {
            return new PropertiesObfuscator(this);
        }
    }
}
