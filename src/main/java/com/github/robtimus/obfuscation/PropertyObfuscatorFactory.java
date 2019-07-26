/*
 * PropertyObfuscatorFactory.java
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

import java.util.ServiceLoader;
import com.github.robtimus.obfuscation.PropertyObfuscator.Builder;

/**
 * A factory for {@link PropertyObfuscator PropertyObfuscators}.
 * <p>
 * {@code PropertyObfuscatorFactory} instances can be configured using the {@link ServiceLoader} mechanism. These can then be used in
 * {@link PropertyObfuscator#ofType(String)} to return a builder for a {@code PropertyObfuscator}.
 *
 * @author Rob Spoor
 */
public interface PropertyObfuscatorFactory {

    /**
     * Returns the type of content that can be obfuscated with created {@code PropertyObfuscators}.
     * This value is used in {@link PropertyObfuscator#ofType(String)} to find a {@code PropertyObfuscator},
     * and is returned by {@link PropertyObfuscator#availableTypes()}.
     *
     * @return The type of content that can be obfuscated with created {@code PropertyObfuscators}.
     */
    String type();

    /**
     * Creates a {@code PropertyObfuscator}.
     *
     * @param builder The builder with the properties that the created {@code PropertyObfuscator} can obfuscate.
     * @return The created {@code PropertyObfuscator}.
     */
    PropertyObfuscator createPropertyObfuscator(Builder builder);
}
