/*
 * PropertyObfuscatorBuilder.java
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
import java.util.Objects;
import java.util.function.Function;

/**
 * A builder for {@link Obfuscator Obfuscators} that can obfuscate certain properties in {@link CharSequence CharSequences} or the contents of
 * {@link Reader Readers}.
 * <p>
 * This builder needs a factory to build {@link Obfuscator Obfuscators} based on this builder. This allows sub classes of {@link Obfuscator} that are
 * property aware to be built easily by just providing a constructor or factory method to that takes a {@code PropertyObfuscatorBuilder}.
 * For instance:
 * <pre><code>
 * public class MyObfuscator extends Obfuscator {
 *
 *     private MyObfuscator(PropertyObfuscatorBuilder builder) {
 *         ...
 *     }
 *
 *     public static PropertyObfuscatorBuilder builder() {
 *         return new PropertyObfuscatorBuilder(MyObfuscator::new);
 *     }
 * }
 * </code></pre>
 *
 * @author Rob Spoor
 */
public class PropertyObfuscatorBuilder extends PropertyAwareBuilder<PropertyObfuscatorBuilder, Obfuscator> {

    private final Function<? super PropertyObfuscatorBuilder, ? extends Obfuscator> factory;

    /**
     * Creates a new builder.
     *
     * @param factory The factory build {@link Obfuscator Obfuscators} based on this builder.
     * @throws NullPointerException If the given factory is {@code null}.
     */
    public PropertyObfuscatorBuilder(Function<? super PropertyObfuscatorBuilder, ? extends Obfuscator> factory) {
        this.factory = Objects.requireNonNull(factory);
    }

    @Override
    public Obfuscator build() {
        return Objects.requireNonNull(factory.apply(this));
    }
}
