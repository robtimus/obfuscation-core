/*
 * ObfuscatedTest.java
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

import static com.github.robtimus.obfuscation.Obfuscator.all;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@SuppressWarnings({ "javadoc", "nls" })
public class ObfuscatedTest {

    @ParameterizedTest(name = "{1}")
    @MethodSource
    @DisplayName("equals(Object)")
    public void testEquals(Obfuscated<?> obfuscated, Object object, boolean expected) {
        assertEquals(expected, obfuscated.equals(object));
    }

    static Arguments[] testEquals() {
        Obfuscated<?> obfuscated = all('x').obfuscateObject("foo");
        return new Arguments[] {
                arguments(obfuscated, obfuscated, true),
                arguments(obfuscated, null, false),
                arguments(obfuscated, all('x').obfuscateObject("foo"), true),
                arguments(obfuscated, all().obfuscateObject("foo"), true),
                arguments(obfuscated, all('x').obfuscateObject("bar"), false),
                arguments(obfuscated, all().obfuscateObject("bar"), false),
                arguments(obfuscated, "foo", false),
        };
    }

    @Test
    @DisplayName("hashCode()")
    public void testHashCode() {
        Obfuscated<?> obfuscated = all('x').obfuscateObject("foo");
        assertEquals(obfuscated.hashCode(), obfuscated.hashCode());
        assertEquals(obfuscated.hashCode(), all().obfuscateObject("foo").hashCode());
    }
}
