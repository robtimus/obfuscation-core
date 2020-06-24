/*
 * MapObfuscatorTest.java
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

import static com.github.robtimus.obfuscation.MapObfuscator.builder;
import static com.github.robtimus.obfuscation.MapObfuscator.stringKeyedBuilder;
import static com.github.robtimus.obfuscation.Obfuscator.all;
import static com.github.robtimus.obfuscation.Obfuscator.fixedLength;
import static com.github.robtimus.obfuscation.Obfuscator.none;
import static com.github.robtimus.obfuscation.Obfuscator.portion;
import static com.github.robtimus.obfuscation.support.CaseSensitivity.CASE_INSENSITIVE;
import static com.github.robtimus.obfuscation.support.CaseSensitivity.CASE_SENSITIVE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import com.github.robtimus.obfuscation.MapObfuscator.Builder;
import com.github.robtimus.obfuscation.MapObfuscator.StringKeyedBuilder;

@SuppressWarnings("nls")
@TestInstance(Lifecycle.PER_CLASS)
class MapObfuscatorTest {
    @Nested
    @DisplayName("obfuscateMap(Map<K, V>)")
    class ObfuscateMap {

        @Test
        @DisplayName("without default obfuscator")
        void testObfuscateWithoutDefaultObfuscator() {
            MapObfuscator<Integer, String> obfuscator = createBuilder()
                    .build();
            Map<Integer, String> map = createMap();
            Map<Integer, String> obfuscated = obfuscator.obfuscateMap(map);
            assertEquals("{0=***, 1=value1, 2=******, 3=val***, 4=value4, -1=null, null=<***>}", obfuscated.toString());
            assertEquals("[0, 1, 2, 3, 4, -1, null]", obfuscated.keySet().toString());
            assertEquals("[***, value1, ******, val***, value4, null, <***>]", obfuscated.values().toString());
            assertEquals("[0=***, 1=value1, 2=******, 3=val***, 4=value4, -1=null, null=<***>]", obfuscated.entrySet().toString());
        }

        @Test
        @DisplayName("with default obfuscator")
        void testObfuscateWithDefaultObfuscator() {
            Obfuscator defaultObfuscator = portion()
                    .keepAtStart(1)
                    .keepAtEnd(1)
                    .withFixedLength(3)
                    .withMaskChar('x')
                    .build();
            MapObfuscator<Integer, String> obfuscator = createBuilder()
                    .withDefaultObfuscator(defaultObfuscator)
                    .build();
            Map<Integer, String> map = createMap();
            Map<Integer, String> obfuscated = obfuscator.obfuscateMap(map);
            assertEquals("{0=***, 1=value1, 2=******, 3=val***, 4=vxxx4, -1=nxxxl, null=<***>}", obfuscated.toString());
            assertEquals("[0, 1, 2, 3, 4, -1, null]", obfuscated.keySet().toString());
            assertEquals("[***, value1, ******, val***, vxxx4, nxxxl, <***>]", obfuscated.values().toString());
            assertEquals("[0=***, 1=value1, 2=******, 3=val***, 4=vxxx4, -1=nxxxl, null=<***>]", obfuscated.entrySet().toString());
        }

        @Nested
        @DisplayName("caseSensitiveByDefault()")
        class CaseSensitiveByDefault {

            @Test
            @DisplayName("without default obfuscator")
            void testObfuscateWithoutDefaultObfuscator() {
                MapObfuscator<String, String> obfuscator = createStringKeyedBuilder(StringKeyedBuilder::caseSensitiveByDefault)
                        .build();

                Map<String, String> map = createStringKeyedMap();
                Map<String, String> obfuscated = obfuscator.obfuscateMap(map);
                // all but d are case sensitive
                assertEquals("{a=***, A=value1, b=value2, B=value2, c=******, C=value3, d=val***, D=val***, null=<null>}", obfuscated.toString());
            }

            @Test
            @DisplayName("with default obfuscator")
            void testObfuscateWithDefaultObfuscator() {
                Obfuscator defaultObfuscator = portion()
                        .keepAtStart(1)
                        .keepAtEnd(1)
                        .withFixedLength(3)
                        .withMaskChar('x')
                        .build();
                MapObfuscator<String, String> obfuscator = createStringKeyedBuilder(StringKeyedBuilder::caseSensitiveByDefault)
                        .withDefaultObfuscator(defaultObfuscator)
                        .build();

                Map<String, String> map = createStringKeyedMap();
                Map<String, String> obfuscated = obfuscator.obfuscateMap(map);
                // all but d are case sensitive
                assertEquals("{a=***, A=vxxx1, b=value2, B=vxxx2, c=******, C=vxxx3, d=val***, D=val***, null=<xxx>}", obfuscated.toString());
            }
        }

        @Nested
        @DisplayName("caseInsensitiveByDefault()")
        class CaseInsensitiveByDefault {

            @Test
            @DisplayName("without default obfuscator")
            void testObfuscateWithoutDefaultObfuscator() {
                MapObfuscator<String, String> obfuscator = createStringKeyedBuilder(StringKeyedBuilder::caseInsensitiveByDefault)
                        .build();

                Map<String, String> map = createStringKeyedMap();
                Map<String, String> obfuscated = obfuscator.obfuscateMap(map);
                // all but c are case insensitive
                assertEquals("{a=***, A=***, b=value2, B=value2, c=******, C=value3, d=val***, D=val***, null=<null>}", obfuscated.toString());
            }

            @Test
            @DisplayName("with default obfuscator")
            void testObfuscateWithDefaultObfuscator() {
                Obfuscator defaultObfuscator = portion()
                        .keepAtStart(1)
                        .keepAtEnd(1)
                        .withFixedLength(3)
                        .withMaskChar('x')
                        .build();
                MapObfuscator<String, String> obfuscator = createStringKeyedBuilder(StringKeyedBuilder::caseInsensitiveByDefault)
                        .withDefaultObfuscator(defaultObfuscator)
                        .build();

                Map<String, String> map = createStringKeyedMap();
                Map<String, String> obfuscated = obfuscator.obfuscateMap(map);
                // all but c are case insensitive
                assertEquals("{a=***, A=***, b=value2, B=value2, c=******, C=vxxx3, d=val***, D=val***, null=<xxx>}", obfuscated.toString());
            }
        }

        private Map<Integer, String> createMap() {
            Map<Integer, String> map = new LinkedHashMap<>();
            for (int i = 0; i < 5; i++) {
                map.put(i, "value" + i);
            }
            map.put(-1, null);
            map.put(null, "<null>");
            return map;
        }

        private Map<String, String> createStringKeyedMap() {
            Map<String, String> map = new LinkedHashMap<>();
            map.put("a", "value1");
            map.put("A", "value1");
            map.put("b", "value2");
            map.put("B", "value2");
            map.put("c", "value3");
            map.put("C", "value3");
            map.put("d", "value4");
            map.put("D", "value4");
            map.put(null, "<null>");
            return map;
        }
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource
    @DisplayName("equals(Object)")
    void testEquals(MapObfuscator<?, ?> obfuscator, Object object, boolean expected) {
        assertEquals(expected, obfuscator.equals(object));
    }

    Arguments[] testEquals() {
        MapObfuscator<?, ?> obfuscator = createBuilder().build();
        return new Arguments[] {
                arguments(obfuscator, obfuscator, true),
                arguments(obfuscator, null, false),
                arguments(obfuscator, createBuilder().build(), true),
                arguments(obfuscator, builder().build(), false),
                arguments(obfuscator, createBuilder().withDefaultObfuscator(all()).build(), false),
                arguments(obfuscator, "foo", false),
        };
    }

    @Test
    @DisplayName("hashCode()")
    void testHashCode() {
        MapObfuscator<?, ?> obfuscator = createBuilder().build();
        assertEquals(obfuscator.hashCode(), obfuscator.hashCode());
        assertEquals(obfuscator.hashCode(), createBuilder().build().hashCode());
    }

    @Nested
    @DisplayName("Builder")
    class BuilderTest {
        @Test
        @DisplayName("transform")
        void testTransform() {
            Builder<Integer, String> builder = builder();
            @SuppressWarnings("unchecked")
            Function<Builder<Integer, String>, String> f = mock(Function.class);
            when(f.apply(builder)).thenReturn("result");

            assertEquals("result", builder.transform(f));
            verify(f).apply(builder);
            verifyNoMoreInteractions(f);
        }
    }

    @Nested
    @DisplayName("StringKeyedBuilder")
    class StringKeyeedBuilderTest {
        @Test
        @DisplayName("transform")
        void testTransform() {
            StringKeyedBuilder<String> builder = stringKeyedBuilder();
            @SuppressWarnings("unchecked")
            Function<StringKeyedBuilder<String>, String> f = mock(Function.class);
            when(f.apply(builder)).thenReturn("result");

            assertEquals("result", builder.transform(f));
            verify(f).apply(builder);
            verifyNoMoreInteractions(f);
        }
    }

    private Builder<Integer, String> createBuilder() {
        return MapObfuscator.<Integer, String>builder()
                .withKey(0, fixedLength(3))
                .withKey(1, none())
                .withKey(2, all())
                .withKey(3, portion().keepAtStart(3).withFixedLength(3).build())
                .withKey(null, portion().keepAtStart(1).keepAtEnd(1).withFixedLength(3).build())
                ;
    }

    private StringKeyedBuilder<String> createStringKeyedBuilder(UnaryOperator<StringKeyedBuilder<String>> transformation) {
        return MapObfuscator.<String>stringKeyedBuilder()
                .transform(transformation)
                .withKey("a", fixedLength(3))
                .withKey("b", none())
                .withKey("c", all(), CASE_SENSITIVE)
                .withKey("d", portion().keepAtStart(3).withFixedLength(3).build(), CASE_INSENSITIVE)
                ;
    }
}
