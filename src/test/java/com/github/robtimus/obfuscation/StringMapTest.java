/*
 * StringMapTest.java
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

import static com.github.robtimus.obfuscation.StringMap.builder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import com.github.robtimus.obfuscation.StringMap.Builder;

@SuppressWarnings({ "javadoc", "nls" })
public class StringMapTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource
    @DisplayName("size()")
    public void testSize(StringMap<Integer> map, int expected) {
        assertEquals(expected, map.size());
    }

    static Arguments[] testSize() {
        return new Arguments[] {
                arguments(StringMap.<Integer>builder().build(), 0),
                arguments(StringMap.<Integer>builder().withEntry("caseSensitive", 1, true).build(), 1),
                arguments(StringMap.<Integer>builder().withEntry("caseInsensitive", 2, false).build(), 1),
                arguments(StringMap.<Integer>builder().withEntry("caseSensitive", 1, true).withEntry("caseInsensitive", 2, false).build(), 2),
        };
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    @DisplayName("isEmpty()")
    public void testIsEmpty(StringMap<Integer> map, boolean expected) {
        assertEquals(expected, map.isEmpty());
    }

    static Arguments[] testIsEmpty() {
        return new Arguments[] {
                arguments(StringMap.<Integer>builder().build(), true),
                arguments(StringMap.<Integer>builder().withEntry("caseSensitive", 1, true).build(), false),
                arguments(StringMap.<Integer>builder().withEntry("caseInsensitive", 2, false).build(), false),
                arguments(StringMap.<Integer>builder().withEntry("caseSensitive", 1, true).withEntry("caseInsensitive", 2, false).build(), false),
        };
    }

    @ParameterizedTest(name = "{0}.containsKey({1})")
    @MethodSource
    @DisplayName("containsKey(String)")
    public void testContainsKey(StringMap<Integer> map, String key, boolean expected) {
        assertEquals(expected, map.containsKey(key));
    }

    static Arguments[] testContainsKey() {
        StringMap<Integer> map = StringMap.<Integer>builder()
                .withEntry("caseSensitive", 1, true)
                .withEntry("caseInsensitive", 2, false)
                .build();
        return new Arguments[] {
                arguments(map, null, false),
                arguments(map, "", false),
                arguments(map, "caseSensitive", true),
                arguments(map, "caseInsensitive", true),
                arguments(map, "casesensitive", false),
                arguments(map, "caseinsensitive", true),
        };
    }

    @ParameterizedTest(name = "{0}.get({1})")
    @MethodSource
    @DisplayName("get(String)")
    public void testGet(StringMap<Integer> map, String key, Integer expected) {
        assertEquals(expected, map.get(key));
    }

    static Arguments[] testGet() {
        StringMap<Integer> map = StringMap.<Integer>builder()
                .withEntry("caseSensitive", 1, true)
                .withEntry("caseInsensitive", 2, false)
                .build();
        return new Arguments[] {
                arguments(map, null, null),
                arguments(map, "", null),
                arguments(map, "caseSensitive", 1),
                arguments(map, "caseInsensitive", 2),
                arguments(map, "casesensitive", null),
                arguments(map, "caseinsensitive", 2),
        };
    }

    @ParameterizedTest(name = "{0}.get({1}, {2})")
    @MethodSource
    @DisplayName("get(String, V)")
    public void testGetWithDefault(StringMap<Integer> map, String key, Integer defaultValue, Integer expected) {
        assertEquals(expected, map.get(key, defaultValue));
    }

    static Arguments[] testGetWithDefault() {
        StringMap<Integer> map = StringMap.<Integer>builder()
                .withEntry("caseSensitive", 1, true)
                .withEntry("caseInsensitive", 2, false)
                .build();
        Integer defaultValue = 0;
        return new Arguments[] {
                arguments(map, null, defaultValue, defaultValue),
                arguments(map, "", defaultValue, defaultValue),
                arguments(map, "caseSensitive", defaultValue, 1),
                arguments(map, "caseInsensitive", defaultValue, 2),
                arguments(map, "casesensitive", defaultValue, defaultValue),
                arguments(map, "caseinsensitive", defaultValue, 2),
        };
    }

    @ParameterizedTest(name = "{0}.get({1}, Supplier<V>)")
    @MethodSource
    @DisplayName("get(String, Supplier<? extends V>)")
    public void testGetWithSupplier(StringMap<Integer> map, String key, Supplier<Integer> valueSupplier, Integer expected) {
        assertEquals(expected, map.get(key, valueSupplier));
    }

    static Arguments[] testGetWithSupplier() {
        StringMap<Integer> map = StringMap.<Integer>builder()
                .withEntry("caseSensitive", 1, true)
                .withEntry("caseInsensitive", 2, false)
                .build();
        Integer defaultValue = 0;
        Supplier<Integer> valueSupplier = () -> defaultValue;
        return new Arguments[] {
                arguments(map, null, valueSupplier, defaultValue),
                arguments(map, "", valueSupplier, defaultValue),
                arguments(map, "caseSensitive", valueSupplier, 1),
                arguments(map, "caseInsensitive", valueSupplier, 2),
                arguments(map, "casesensitive", valueSupplier, defaultValue),
                arguments(map, "caseinsensitive", valueSupplier, 2),
        };
    }

    @Test
    @DisplayName("forEach(Consumer<? super String, ? super V>)")
    public void testForEach() {
        StringMap<Integer> map = StringMap.<Integer>builder()
                .withEntry("caseSensitive", 1, true)
                .withEntry("caseInsensitive", 2, false)
                .build();

        Map<String, Integer> expected = new HashMap<>();
        expected.put("caseSensitive", 1);
        expected.put("caseInsensitive", 2);

        Map<String, Integer> collected = new HashMap<>();
        map.forEach(collected::put);

        assertEquals(expected, collected);
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource
    @DisplayName("equals(Object)")
    public void testEquals(StringMap<Integer> map, Object object, boolean expected) {
        assertEquals(expected, map.equals(object));
    }

    static Arguments[] testEquals() {
        StringMap<Integer> map = StringMap.<Integer>builder()
                .withEntry("caseSensitive", 1, true)
                .withEntry("caseInsensitive", 2, false)
                .build();
        return new Arguments[] {
                arguments(map, map, true),
                arguments(map, null, false),
                arguments(map, StringMap.<Integer>builder().withEntry("caseSensitive", 1, true).withEntry("caseInsensitive", 2, true).build(), false),
                arguments(map, StringMap.<Integer>builder().withEntry("caseSensitive", 1, true).withEntry("caseInsensitive", 2, false).build(), true),
                arguments(map, StringMap.<Integer>builder().withEntry("caseSensitive", 1, false).withEntry("caseInsensitive", 2, true).build(),
                        false),
                arguments(map, StringMap.<Integer>builder().withEntry("caseSensitive", 1, false).withEntry("caseInsensitive", 2, false).build(),
                        false),
                arguments(map, StringMap.<Integer>builder().withEntry("caseSensitive", 1, true).withEntry("caseInsensitive", 3, false).build(),
                        false),
                arguments(map, "foo", false),
        };
    }

    @Test
    @DisplayName("hashCode()")
    public void testHashCode() {
        StringMap<Integer> map = StringMap.<Integer>builder()
                .withEntry("caseSensitive", 1, true)
                .withEntry("caseInsensitive", 2, false)
                .build();
        assertEquals(map.hashCode(), map.hashCode());
        assertEquals(map.hashCode(),
                StringMap.<Integer>builder().withEntry("caseSensitive", 1, true).withEntry("caseInsensitive", 2, false).build().hashCode());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    @DisplayName("toString()")
    public void testToString(StringMap<Integer> map, String expected) {
        assertEquals(expected, map.toString());
    }

    static Arguments[] testToString() {
        return new Arguments[] {
                arguments(StringMap.<Integer>builder().build(), "{}"),
                arguments(StringMap.<Integer>builder().withEntry("caseSensitive", 1, true).build(), "{caseSensitive=1}"),
                arguments(StringMap.<Integer>builder().withEntry("caseInsensitive", 2, false).build(), "{caseInsensitive=2}"),
                arguments(StringMap.<Integer>builder().withEntry("caseSensitive", 1, true).withEntry("caseInsensitive1", 2, false)
                        .withEntry("caseInsensitive2", 3, false).build(), "{caseSensitive=1, caseInsensitive1=2, caseInsensitive2=3}"),
        };
    }

    @Nested
    @DisplayName("Builder")
    public class BuilderTest {

        @Test
        @DisplayName("withEntry(String, V)")
        public void testWithEntry() {
            Builder<Integer> builder = StringMap.<Integer>builder()
                    .withEntry("caseSensitive", 1);

            assertEquals(Collections.singletonMap("caseSensitive", 1), builder.caseSensitiveMap());
            assertEquals(Collections.emptyMap(), builder.caseInsensitiveMap());
        }

        @Test
        @DisplayName("withEntry(String, V, boolean)")
        public void testWithEntryWithCaseSensitivity() {
            Builder<Integer> builder = StringMap.<Integer>builder()
                    .withEntry("caseSensitive", 1, false)
                    .withEntry("caseSensitive", 2, true);

            assertEquals(Collections.singletonMap("caseSensitive", 2), builder.caseSensitiveMap());
            assertEquals(Collections.emptyMap(), builder.caseInsensitiveMap());

            builder = StringMap.<Integer>builder()
                    .withEntry("caseInsensitive", 1, true)
                    .withEntry("caseInsensitive", 2, false);

            assertEquals(Collections.emptyMap(), builder.caseSensitiveMap());
            assertEquals(Collections.singletonMap("caseInsensitive", 2), builder.caseInsensitiveMap());
        }

        @Test
        @DisplayName("transform")
        public void testTransform() {
            Builder<Integer> builder = builder();
            @SuppressWarnings("unchecked")
            Function<Builder<?>, String> f = mock(Function.class);
            when(f.apply(builder)).thenReturn("result");

            assertEquals("result", builder.transform(f));
            verify(f).apply(builder);
            verifyNoMoreInteractions(f);
        }
    }
}
