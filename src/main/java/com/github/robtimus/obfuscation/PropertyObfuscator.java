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
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * An obfuscator that obfuscates certain properties in {@link CharSequence CharSequences} or the contents of {@link Reader Readers}.
 *
 * @author Rob Spoor
 */
public abstract class PropertyObfuscator extends Obfuscator {

    private final Map<String, Obfuscator> obfuscators;

    /**
     * Creates a new obfuscator.
     *
     * @param builder The builder that is used to create this obfuscator.
     */
    protected PropertyObfuscator(PropertyAwareBuilder<?, ? extends PropertyObfuscator> builder) {
        obfuscators = builder.obfuscators();
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
    public final Obfuscator getObfuscator(String property) {
        return obfuscators.get(property);
    }

    /**
     * Returns the obfuscator for a specific property.
     *
     * @param property The name of the property to get the obfuscator for.
     * @return The obfuscator for the given property, or {@link #none()} if no obfuscator is configured for the given property.
     */
    public final Obfuscator getNonNullObfuscator(String property) {
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
     * Returns a builder that will create obfuscators using a specific factory function.
     * This method can be used to create obfuscators where the default builder is sufficient.
     *
     * @param factory The factory function to use for converting the returned builder into an obfuscator.
     * @return A builder that will use the given factory function to create obfuscators.
     * @throws NullPointerException If the given factory function is {@code null}.
     */
    public static Builder withFactory(Function<? super Builder, ? extends PropertyObfuscator> factory) {
        Objects.requireNonNull(factory);
        return new Builder(factory);
    }

    /**
     * A builder for {@link PropertyObfuscator PropertyObfuscators}.
     *
     * @author Rob Spoor
     */
    public static final class Builder extends PropertyAwareBuilder<Builder, PropertyObfuscator> {

        private final Function<? super Builder, ? extends PropertyObfuscator> factory;

        private Builder(Function<? super Builder, ? extends PropertyObfuscator> factory) {
            this.factory = Objects.requireNonNull(factory);
        }

        @Override
        public PropertyObfuscator build() {
            return Objects.requireNonNull(factory.apply(this));
        }
    }
}
