/*
 * MapBuilderTest.java
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

package com.github.robtimus.obfuscation.support;

import static com.github.robtimus.obfuscation.support.CaseSensitivity.CASE_INSENSITIVE;
import static com.github.robtimus.obfuscation.support.CaseSensitivity.CASE_SENSITIVE;
import static java.util.stream.Collectors.toConcurrentMap;
import static java.util.stream.Collectors.toMap;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@SuppressWarnings("nls")
@TestInstance(Lifecycle.PER_CLASS)
class MapBuilderTest {

    @Test
    @DisplayName("withEntry(String, K)")
    void testWithEntryCaseSensitive() {
        MapBuilder<Integer> builder = new MapBuilder<Integer>()
                .withEntry("a", 1)
                .withEntry("b", 2);

        Map<String, Integer> expectedCaseSensitiveMap = new HashMap<>();
        expectedCaseSensitiveMap.put("a", 1);
        expectedCaseSensitiveMap.put("b", 2);
        Map<String, Integer> expectedCaseInsensitiveMap = new HashMap<>();

        assertEquals(expectedCaseSensitiveMap, builder.caseSensitiveMap());
        assertEquals(expectedCaseInsensitiveMap, builder.caseInsensitiveMap());
    }

    @Test
    @DisplayName("withEntry(String, K, CaseSensitivity)")
    void testWithEntry() {
        MapBuilder<Integer> builder = new MapBuilder<Integer>()
                .withEntry("a", 1, CASE_SENSITIVE)
                .withEntry("b", 2, CASE_INSENSITIVE);

        Map<String, Integer> expectedCaseSensitiveMap = new HashMap<>();
        expectedCaseSensitiveMap.put("a", 1);
        Map<String, Integer> expectedCaseInsensitiveMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        expectedCaseInsensitiveMap.put("b", 2);

        assertEquals(expectedCaseSensitiveMap, builder.caseSensitiveMap());
        assertEquals(expectedCaseInsensitiveMap, builder.caseInsensitiveMap());

        builder.withEntry("a", 3, CASE_INSENSITIVE);
        builder.withEntry("b", 4, CASE_SENSITIVE);

        expectedCaseSensitiveMap.put("b", 4);
        expectedCaseInsensitiveMap.put("a", 3);

        assertEquals(expectedCaseSensitiveMap, builder.caseSensitiveMap());
        assertEquals(expectedCaseInsensitiveMap, builder.caseInsensitiveMap());

        assertDuplicateKey(builder, "a", 5, CaseSensitivity.CASE_SENSITIVE);
        assertDuplicateKey(builder, "b", 7, CaseSensitivity.CASE_SENSITIVE);
        assertDuplicateKey(builder, "a", 9, CaseSensitivity.CASE_INSENSITIVE);
        assertDuplicateKey(builder, "b", 11, CaseSensitivity.CASE_INSENSITIVE);

        assertEquals(expectedCaseSensitiveMap, builder.caseSensitiveMap());
        assertEquals(expectedCaseInsensitiveMap, builder.caseInsensitiveMap());
    }

    private void assertDuplicateKey(MapBuilder<Integer> builder, String key, Integer value, CaseSensitivity caseSensitivity) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> builder.withEntry(key, value, caseSensitivity));
        assertEquals(Messages.stringMap.duplicateKey(key, caseSensitivity), exception.getMessage());
    }

    @Test
    @DisplayName("testEntry(String, K)")
    void testTestEntryCaseSensitive() {
        MapBuilder<Integer> builder = new MapBuilder<Integer>()
                .testEntry("a")
                .testEntry("b");

        Map<String, Integer> expectedCaseSensitiveMap = new HashMap<>();
        Map<String, Integer> expectedCaseInsensitiveMap = new HashMap<>();

        assertEquals(expectedCaseSensitiveMap, builder.caseSensitiveMap());
        assertEquals(expectedCaseInsensitiveMap, builder.caseInsensitiveMap());
    }

    @Test
    @DisplayName("testEntry(String, K, CaseSensitivity)")
    void testTestEntry() {
        MapBuilder<Integer> builder = new MapBuilder<Integer>()
                .withEntry("a", 1, CASE_SENSITIVE)
                .withEntry("b", 2, CASE_INSENSITIVE);

        Map<String, Integer> expectedCaseSensitiveMap = new HashMap<>();
        expectedCaseSensitiveMap.put("a", 1);
        Map<String, Integer> expectedCaseInsensitiveMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        expectedCaseInsensitiveMap.put("b", 2);

        assertEquals(expectedCaseSensitiveMap, builder.caseSensitiveMap());
        assertEquals(expectedCaseInsensitiveMap, builder.caseInsensitiveMap());

        builder.testEntry("a", CASE_INSENSITIVE);
        builder.testEntry("b", CASE_SENSITIVE);

        assertEquals(expectedCaseSensitiveMap, builder.caseSensitiveMap());
        assertEquals(expectedCaseInsensitiveMap, builder.caseInsensitiveMap());

        assertDuplicateKey(builder, "a", CaseSensitivity.CASE_SENSITIVE);
        builder.testEntry("b", CaseSensitivity.CASE_SENSITIVE);
        builder.testEntry("a", CaseSensitivity.CASE_INSENSITIVE);
        assertDuplicateKey(builder, "b", CaseSensitivity.CASE_INSENSITIVE);

        assertEquals(expectedCaseSensitiveMap, builder.caseSensitiveMap());
        assertEquals(expectedCaseInsensitiveMap, builder.caseInsensitiveMap());
    }

    private void assertDuplicateKey(MapBuilder<Integer> builder, String key, CaseSensitivity caseSensitivity) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> builder.testEntry(key, caseSensitivity));
        assertEquals(Messages.stringMap.duplicateKey(key, caseSensitivity), exception.getMessage());
    }

    @Test
    @DisplayName("default case sensitivity")
    void testDefaultCaseSensitivity() {
        MapBuilder<Integer> builder = new MapBuilder<Integer>()
                .caseInsensitiveByDefault()
                .withEntry("a", 1)
                .caseSensitiveByDefault()
                .withEntry("b", 2);

        Map<String, Integer> expectedCaseSensitiveMap = new HashMap<>();
        expectedCaseSensitiveMap.put("b", 2);
        Map<String, Integer> expectedCaseInsensitiveMap = new HashMap<>();
        expectedCaseInsensitiveMap.put("a", 1);

        assertEquals(expectedCaseSensitiveMap, builder.caseSensitiveMap());
        assertEquals(expectedCaseInsensitiveMap, builder.caseInsensitiveMap());
    }

    @Test
    @DisplayName("transform")
    void testTransform() {
        MapBuilder<Integer> builder = new MapBuilder<>();
        @SuppressWarnings("unchecked")
        Function<MapBuilder<?>, String> f = mock(Function.class);
        when(f.apply(builder)).thenReturn("result");

        assertEquals("result", builder.transform(f));
        verify(f).apply(builder);
        verifyNoMoreInteractions(f);
    }

    @Nested
    @DisplayName("build()")
    class BuildTest {

        @Test
        @DisplayName("empty")
        void testEmpty() {
            Map<String, Integer> map = new MapBuilder<Integer>().build();
            assertSame(Collections.emptyMap(), map);
        }

        @Test
        @DisplayName("only case sensitive entries")
        void testOnlyCaseSensitiveEntries() {
            Map<String, Integer> map = new MapBuilder<Integer>()
                    .withEntry("a", 1, CASE_SENSITIVE)
                    .withEntry("b", 2, CASE_SENSITIVE)
                    .withEntry("c", 3, CASE_SENSITIVE)
                    .build();

            Map<String, Integer> expectedMap = new HashMap<>();
            expectedMap.put("a", 1);
            expectedMap.put("b", 2);
            expectedMap.put("c", 3);
            expectedMap = Collections.unmodifiableMap(expectedMap);

            assertEquals(expectedMap, map);
            assertEquals(expectedMap.getClass(), map.getClass());

            assertEquals(1, map.get("a"));
            assertEquals(2, map.get("b"));
            assertEquals(3, map.get("c"));

            assertEquals(null, map.get("A"));
            assertEquals(null, map.get("B"));
            assertEquals(null, map.get("C"));
        }

        @Test
        @DisplayName("only case insensitive entries")
        void testOnlyCaseInsensitiveEntries() {
            Map<String, Integer> map = new MapBuilder<Integer>()
                    .withEntry("a", 1, CASE_INSENSITIVE)
                    .withEntry("b", 2, CASE_INSENSITIVE)
                    .withEntry("c", 3, CASE_INSENSITIVE)
                    .build();

            Map<String, Integer> expectedMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            expectedMap.put("A", 1);
            expectedMap.put("B", 2);
            expectedMap.put("C", 3);
            expectedMap = Collections.unmodifiableMap(expectedMap);

            assertEquals(expectedMap, map);
            assertEquals(expectedMap.getClass(), map.getClass());

            assertEquals(1, map.get("a"));
            assertEquals(2, map.get("b"));
            assertEquals(3, map.get("c"));

            assertEquals(1, map.get("A"));
            assertEquals(2, map.get("B"));
            assertEquals(3, map.get("C"));
        }

        @Nested
        @DisplayName("both case sensitive and case insensitive entries")
        @TestInstance(Lifecycle.PER_CLASS)
        class MixedCaseEntriesTest {

            private final Map<String, Integer> map = new MapBuilder<Integer>()
                    .withEntry("a", 1, CASE_SENSITIVE)
                    .withEntry("b", 2, CASE_INSENSITIVE)
                    .withEntry("c", 3, CASE_SENSITIVE)
                    .withEntry("d", 4, CASE_INSENSITIVE)
                    .build();

            @Test
            @DisplayName("size()")
            void testSize() {
                assertEquals(4, map.size());
            }

            @Test
            @DisplayName("isEmpty()")
            void testIsEmpty() {
                assertFalse(map.isEmpty());
            }

            @ParameterizedTest(name = "{0}: {1}")
            @MethodSource
            @DisplayName("containsKey(Object)")
            void testContainsKey(Object key, boolean expected) {
                assertEquals(expected, map.containsKey(key));
            }

            Arguments[] testContainsKey() {
                return new Arguments[] {
                        arguments(null, false),
                        arguments("", false),
                        arguments("a", true),
                        arguments("A", false),
                        arguments("b", true),
                        arguments("B", true),
                        arguments("c", true),
                        arguments("C", false),
                        arguments("d", true),
                        arguments("D", true),
                        arguments(1, false),
                };
            }

            @ParameterizedTest(name = "{0}: {1}")
            @MethodSource
            @DisplayName("containsValue(Object)")
            void testContainsValue(Object key, boolean expected) {
                assertEquals(expected, map.containsValue(key));
            }

            Arguments[] testContainsValue() {
                return new Arguments[] {
                        arguments(null, false),
                        arguments(0, false),
                        arguments(1, true),
                        arguments(2, true),
                        arguments(3, true),
                        arguments(4, true),
                        arguments(5, false),
                        arguments("a", false),
                };
            }

            @ParameterizedTest(name = "{0}: {1}")
            @MethodSource
            @DisplayName("get(Object)")
            void testGet(Object key, Object expected) {
                assertEquals(expected, map.get(key));
            }

            Arguments[] testGet() {
                return new Arguments[] {
                        arguments(null, null),
                        arguments("", null),
                        arguments("a", 1),
                        arguments("A", null),
                        arguments("b", 2),
                        arguments("B", 2),
                        arguments("c", 3),
                        arguments("C", null),
                        arguments("d", 4),
                        arguments("D", 4),
                        arguments(1, null),
                };
            }

            @Test
            @DisplayName("put(String, V)")
            void testPut() {
                assertThrows(UnsupportedOperationException.class, () -> map.put("a", 0));
                assertThrows(UnsupportedOperationException.class, () -> map.put("x", 0));
                assertEquals(4, map.size());
                assertEquals(1, map.get("a"));
            }

            @Test
            @DisplayName("remove(Object)")
            void testRemove() {
                assertThrows(UnsupportedOperationException.class, () -> map.remove("a"));
                assertThrows(UnsupportedOperationException.class, () -> map.remove("x"));
                assertEquals(4, map.size());
            }

            @Test
            @DisplayName("putAll(Map<? extends String, ? extends V>)")
            void testPutAll() {
                assertThrows(UnsupportedOperationException.class, () -> map.putAll(Collections.emptyMap()));
                assertThrows(UnsupportedOperationException.class, () -> map.putAll(Collections.singletonMap("a", 0)));
                assertEquals(4, map.size());
                assertEquals(1, map.get("a"));
            }

            @Test
            @DisplayName("clear()")
            void testClear() {
                assertThrows(UnsupportedOperationException.class, () -> map.clear());
                assertEquals(4, map.size());
            }

            @Nested
            @DisplayName("entrySet()")
            @TestInstance(Lifecycle.PER_CLASS)
            class EntrySetTest {

                private final Set<Map.Entry<String, Integer>> entrySet = map.entrySet();

                @Test
                @DisplayName("size()")
                void testSize() {
                    assertEquals(4, entrySet.size());
                }

                @Test
                @DisplayName("isEmpty()")
                void testIsEmpty() {
                    assertFalse(entrySet.isEmpty());
                }

                @ParameterizedTest(name = "{0}: {1}")
                @MethodSource
                @DisplayName("contains(Object)")
                void testContains(Object o, boolean expected) {
                    assertEquals(expected, entrySet.contains(o));
                }

                Arguments[] testContains() {
                    return new Arguments[] {
                            arguments(null, false),
                            arguments("", false),
                            arguments(new SimpleEntry<>(null, 1), false),
                            arguments(new SimpleEntry<>(1, 1), false),
                            arguments(new SimpleEntry<>("a", 1), true),
                            arguments(new SimpleEntry<>("a", 2), false),
                            arguments(new SimpleEntry<>("A", 1), false),
                            arguments(new SimpleEntry<>("b", 2), true),
                            arguments(new SimpleEntry<>("b", 3), false),
                            arguments(new SimpleEntry<>("B", 2), true),
                            arguments(new SimpleEntry<>("c", 3), true),
                            arguments(new SimpleEntry<>("c", 4), false),
                            arguments(new SimpleEntry<>("C", 3), false),
                            arguments(new SimpleEntry<>("d", 4), true),
                            arguments(new SimpleEntry<>("d", 5), false),
                            arguments(new SimpleEntry<>("D", 4), true),
                            arguments(new SimpleEntry<>("x", 1), false),
                    };
                }

                @Test
                @DisplayName("iterator()")
                void testIterator() {
                    Iterator<Map.Entry<String, Integer>> iterator = entrySet.iterator();

                    // iteration order of HashMap is not defined but is consistent within a JVM
                    Map<String, Integer> hashMap = new HashMap<>();
                    hashMap.put("a", 1);
                    hashMap.put("c", 3);
                    Iterator<Map.Entry<String, Integer>> hashMapIterator = hashMap.entrySet().iterator();

                    assertTrue(iterator.hasNext());
                    assertEquals(hashMapIterator.next(), iterator.next());

                    assertTrue(iterator.hasNext());
                    assertEquals(hashMapIterator.next(), iterator.next());

                    // iteration order of TreeMap is defined
                    assertTrue(iterator.hasNext());
                    assertEquals(new SimpleEntry<>("b", 2), iterator.next());

                    assertTrue(iterator.hasNext());
                    assertEquals(new SimpleEntry<>("d", 4), iterator.next());

                    assertFalse(iterator.hasNext());
                    assertThrows(NoSuchElementException.class, iterator::next);
                }

                @Test
                @DisplayName("add(Entry<String, V>)")
                void testAdd() {
                    assertThrows(UnsupportedOperationException.class, () -> entrySet.add(new SimpleEntry<>("a", 0)));
                    assertThrows(UnsupportedOperationException.class, () -> entrySet.add(new SimpleEntry<>("x", 0)));
                    assertEquals(4, entrySet.size());
                    assertEquals(1, map.get("a"));
                }

                @Test
                @DisplayName("remove(Object)")
                void testRemove() {
                    assertThrows(UnsupportedOperationException.class, () -> entrySet.remove(new SimpleEntry<>("a", 0)));
                    assertThrows(UnsupportedOperationException.class, () -> entrySet.remove(new SimpleEntry<>("x", 0)));
                    assertEquals(4, entrySet.size());
                    assertEquals(1, map.get("a"));
                }

                @Test
                @DisplayName("addAll(Collection<? extends Entry<String, V>)")
                void testAddAll() {
                    assertThrows(UnsupportedOperationException.class, () -> entrySet.addAll(Collections.emptyList()));
                    assertThrows(UnsupportedOperationException.class, () -> entrySet.addAll(Collections.singleton(new SimpleEntry<>("a", 0))));
                    assertEquals(4, entrySet.size());
                    assertEquals(1, map.get("a"));
                }

                @Test
                @DisplayName("retainAll(Collection<?>)")
                void testRetainAll() {
                    assertThrows(UnsupportedOperationException.class, () -> entrySet.retainAll(Collections.emptyList()));
                    assertThrows(UnsupportedOperationException.class, () -> entrySet.retainAll(Collections.singleton(new SimpleEntry<>("a", 0))));
                    assertEquals(4, entrySet.size());
                    assertEquals(1, map.get("a"));
                }

                @Test
                @DisplayName("removeAll(Collection<?>)")
                void testRemoveAll() {
                    assertThrows(UnsupportedOperationException.class, () -> entrySet.removeAll(Collections.emptyList()));
                    assertThrows(UnsupportedOperationException.class, () -> entrySet.removeAll(Collections.singleton(new SimpleEntry<>("a", 0))));
                    assertEquals(4, entrySet.size());
                    assertEquals(1, map.get("a"));
                }

                @Test
                @DisplayName("clear()")
                void testClear() {
                    assertThrows(UnsupportedOperationException.class, () -> entrySet.clear());
                    assertEquals(4, entrySet.size());
                }

                @Test
                @DisplayName("removeIf(Predicate<? super Entry<String, V>)")
                void testRemoveIf() {
                    assertThrows(UnsupportedOperationException.class, () -> entrySet.removeIf(e -> false));
                    assertThrows(UnsupportedOperationException.class, () -> entrySet.removeIf(e -> true));
                    assertEquals(4, entrySet.size());
                    assertEquals(1, map.get("a"));
                }

                @Test
                @DisplayName("stream()")
                void testStream() {
                    Map<String, Integer> result = entrySet.stream()
                            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

                    Map<String, Integer> expected = new HashMap<>();
                    expected.put("a", 1);
                    expected.put("b", 2);
                    expected.put("c", 3);
                    expected.put("d", 4);

                    assertEquals(expected, result);
                }

                @Test
                @DisplayName("parallelStream()")
                void testParallelStream() {
                    Map<String, Integer> result = entrySet.parallelStream()
                            .collect(toConcurrentMap(Map.Entry::getKey, Map.Entry::getValue));

                    Map<String, Integer> expected = new HashMap<>();
                    expected.put("a", 1);
                    expected.put("b", 2);
                    expected.put("c", 3);
                    expected.put("d", 4);

                    assertEquals(expected, result);
                }

                @Test
                @DisplayName("forEach(Consumer<? super Entry<String, V>>)")
                void testForEach() {
                    Map<String, Integer> expected = new HashMap<>();
                    expected.put("a", 1);
                    expected.put("b", 2);
                    expected.put("c", 3);
                    expected.put("d", 4);

                    Map<String, Integer> collected = new HashMap<>();
                    entrySet.forEach(e -> collected.put(e.getKey(), e.getValue()));

                    assertEquals(expected, collected);
                }
            }

            @ParameterizedTest(name = "{0}, {1}: {2}")
            @MethodSource
            @DisplayName("getOrDefault(Object, V)")
            void testGetOrDefault(Object key, Integer defaultValue, Object expected) {
                assertEquals(expected, map.getOrDefault(key, defaultValue));
            }

            Arguments[] testGetOrDefault() {
                final Integer defaultValue = 0;
                return new Arguments[] {
                        arguments(null, defaultValue, defaultValue),
                        arguments("", defaultValue, defaultValue),
                        arguments("a", defaultValue, 1),
                        arguments("A", defaultValue, defaultValue),
                        arguments("b", defaultValue, 2),
                        arguments("B", defaultValue, 2),
                        arguments("c", defaultValue, 3),
                        arguments("C", defaultValue, defaultValue),
                        arguments("d", defaultValue, 4),
                        arguments("D", defaultValue, 4),
                        arguments(1, defaultValue, defaultValue),
                };
            }

            @Test
            @DisplayName("forEach(BiConsumer<? super String, ? super V>)")
            void testForEach() {
                Map<String, Integer> expected = new HashMap<>();
                expected.put("a", 1);
                expected.put("b", 2);
                expected.put("c", 3);
                expected.put("d", 4);

                Map<String, Integer> collected = new HashMap<>();
                map.forEach(collected::put);

                assertEquals(expected, collected);
            }

            @Test
            @DisplayName("replaceAll(BiFunction<? super String, ? super V, ? extends V>)")
            void testReplaceAll() {
                assertThrows(UnsupportedOperationException.class, () -> map.replaceAll((s, v) -> v));
                assertEquals(4, map.size());
            }

            @Test
            @DisplayName("putIfAbsent(String, V)")
            void testPutIfAbsent() {
                assertThrows(UnsupportedOperationException.class, () -> map.putIfAbsent("a", 0));
                assertThrows(UnsupportedOperationException.class, () -> map.putIfAbsent("x", 0));
                assertEquals(4, map.size());
                assertEquals(1, map.get("a"));
            }

            @Test
            @DisplayName("remove(Object, Object)")
            void testRemoveWithValue() {
                assertThrows(UnsupportedOperationException.class, () -> map.remove("a", 1));
                assertThrows(UnsupportedOperationException.class, () -> map.remove("a", 2));
                assertThrows(UnsupportedOperationException.class, () -> map.remove("x", 1));
                assertEquals(4, map.size());
                assertEquals(1, map.get("a"));
            }

            @Test
            @DisplayName("replace(String, V, V)")
            void testReplaceWithValue() {
                assertThrows(UnsupportedOperationException.class, () -> map.replace("a", 1, 0));
                assertThrows(UnsupportedOperationException.class, () -> map.replace("a", 0, -1));
                assertThrows(UnsupportedOperationException.class, () -> map.replace("x", 0, 1));
                assertEquals(4, map.size());
                assertEquals(1, map.get("a"));
            }

            @Test
            @DisplayName("replace(String, V)")
            void testReplace() {
                assertThrows(UnsupportedOperationException.class, () -> map.replace("a", 0));
                assertThrows(UnsupportedOperationException.class, () -> map.replace("x", 0));
                assertEquals(4, map.size());
                assertEquals(1, map.get("a"));
            }

            @Test
            @DisplayName("hashCode()")
            void testHashCode() {
                Map<String, Integer> hashMap = new HashMap<>();
                hashMap.put("a", 1);
                hashMap.put("b", 2);
                hashMap.put("c", 3);
                hashMap.put("d", 4);
                assertEquals(hashMap.hashCode(), map.hashCode());

                Map<String, Integer> treeMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
                treeMap.put("a", 1);
                treeMap.put("b", 2);
                treeMap.put("c", 3);
                treeMap.put("d", 4);
                assertEquals(treeMap.hashCode(), map.hashCode());
            }

            @ParameterizedTest(name = "{0}: {1}")
            @MethodSource
            @DisplayName("equals(Object)")
            void testEquals(Object object, boolean expected) {
                assertEquals(expected, map.equals(object));
            }

            Arguments[] testEquals() {
                Map<String, Integer> hashMap = new HashMap<>();
                hashMap.put("a", 1);
                hashMap.put("b", 2);
                hashMap.put("c", 3);
                hashMap.put("d", 4);

                Map<String, Integer> treeMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
                treeMap.put("a", 1);
                treeMap.put("B", 2);
                treeMap.put("c", 3);
                treeMap.put("D", 4);

                Map<String, Integer> differentMap = new HashMap<>();
                differentMap.put("a", 0);
                differentMap.put("b", 1);
                differentMap.put("c", 2);
                differentMap.put("d", 3);

                return new Arguments[] {
                        // reflexivity
                        arguments(map, true),
                        // null
                        arguments(null, false),
                        // same mappings
                        arguments(new MapBuilder<Integer>()
                                .withEntry("a", 1, CASE_SENSITIVE)
                                .withEntry("b", 2, CASE_INSENSITIVE)
                                .withEntry("c", 3, CASE_SENSITIVE)
                                .withEntry("d", 4, CASE_INSENSITIVE)
                                .build(), true),
                        // same mappings, since case insensitive keys match case insensitively
                        arguments(new MapBuilder<Integer>()
                                .withEntry("a", 1, CASE_SENSITIVE)
                                .withEntry("B", 2, CASE_INSENSITIVE)
                                .withEntry("c", 3, CASE_SENSITIVE)
                                .withEntry("D", 4, CASE_INSENSITIVE)
                                .build(), true),
                        // different mappings, since the cases have been swapped
                        arguments(new MapBuilder<Integer>()
                                .withEntry("a", 1, CASE_INSENSITIVE)
                                .withEntry("b", 2, CASE_SENSITIVE)
                                .withEntry("c", 3, CASE_INSENSITIVE)
                                .withEntry("d", 4, CASE_SENSITIVE)
                                .build(), false),
                        // different mappings, since case sensitive keys match case sensitively;
                        arguments(new MapBuilder<Integer>()
                                .withEntry("A", 1, CASE_SENSITIVE)
                                .withEntry("b", 2, CASE_INSENSITIVE)
                                .withEntry("c", 3, CASE_SENSITIVE)
                                .withEntry("d", 4, CASE_INSENSITIVE)
                                .build(), false),
                        // case sensitive is different, case insensitive is the same
                        arguments(new MapBuilder<Integer>()
                                .withEntry("a", 1, CASE_INSENSITIVE)
                                .withEntry("b", 2, CASE_SENSITIVE)
                                .withEntry("d", 4, CASE_SENSITIVE)
                                .build(), false),
                        // case sensitive is the same, case insensitive is different
                        arguments(new MapBuilder<Integer>()
                                .withEntry("a", 1, CASE_SENSITIVE)
                                .withEntry("b", 2, CASE_INSENSITIVE)
                                .withEntry("c", 3, CASE_SENSITIVE)
                                .build(), false),
                        // case sensitive and case insensitive are both different
                        arguments(new MapBuilder<Integer>()
                                .withEntry("a", 1, CASE_SENSITIVE)
                                .withEntry("b", 2, CASE_INSENSITIVE)
                                .build(), false),
                        // a different map type with the same mappings; equals checks that the given map contains entries of map
                        arguments(hashMap, true),
                        // a different map type with the same mappings; equals checks that the given map contains entries of map
                        arguments(treeMap, true),
                        // a different map type with different mappings
                        arguments(differentMap, false),
                        // a different type
                        arguments("foo", false),
                };
            }

            @Test
            @DisplayName("serializability")
            void testSerializability() throws IOException, ClassNotFoundException {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                try (ObjectOutputStream objectOutput = new ObjectOutputStream(output)) {
                    objectOutput.writeObject(map);
                }
                byte[] serialized = output.toByteArray();

                try (ObjectInputStream objectInput = new ObjectInputStream(new ByteArrayInputStream(serialized))) {
                    @SuppressWarnings("unchecked")
                    Map<String, Integer> deserialized = (Map<String, Integer>) objectInput.readObject();
                    assertEquals(map, deserialized);
                    assertNotSame(map, deserialized);
                }
            }
        }

        @Nested
        @DisplayName("serialization")
        class SerializationTest {

            @Test
            @DisplayName("empty")
            void testEmpty() {
                Map<String, Integer> map = new MapBuilder<Integer>().build();
                map = serializeAndDeserialize(map);
                assertSame(Collections.emptyMap(), map);
            }

            @Test
            @DisplayName("only case sensitive entries")
            void testOnlyCaseSensitiveEntries() {
                Map<String, Integer> map = new MapBuilder<Integer>()
                        .withEntry("a", 1, CASE_SENSITIVE)
                        .withEntry("b", 2, CASE_SENSITIVE)
                        .withEntry("c", 3, CASE_SENSITIVE)
                        .build();
                map = serializeAndDeserialize(map);

                Map<String, Integer> expectedMap = new HashMap<>();
                expectedMap.put("a", 1);
                expectedMap.put("b", 2);
                expectedMap.put("c", 3);
                expectedMap = Collections.unmodifiableMap(expectedMap);

                assertNotSame(expectedMap, map);
                assertEquals(expectedMap, map);
                assertEquals(expectedMap.getClass(), map.getClass());

                assertEquals(1, map.get("a"));
                assertEquals(2, map.get("b"));
                assertEquals(3, map.get("c"));

                assertEquals(null, map.get("A"));
                assertEquals(null, map.get("B"));
                assertEquals(null, map.get("C"));
            }

            @Test
            @DisplayName("only case insensitive entries")
            void testOnlyCaseInsensitiveEntries() {
                Map<String, Integer> map = new MapBuilder<Integer>()
                        .withEntry("a", 1, CASE_INSENSITIVE)
                        .withEntry("b", 2, CASE_INSENSITIVE)
                        .withEntry("c", 3, CASE_INSENSITIVE)
                        .build();
                map = serializeAndDeserialize(map);

                Map<String, Integer> expectedMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
                expectedMap.put("A", 1);
                expectedMap.put("B", 2);
                expectedMap.put("C", 3);
                expectedMap = Collections.unmodifiableMap(expectedMap);

                assertNotSame(expectedMap, map);
                assertEquals(expectedMap, map);
                assertEquals(expectedMap.getClass(), map.getClass());

                assertEquals(1, map.get("a"));
                assertEquals(2, map.get("b"));
                assertEquals(3, map.get("c"));

                assertEquals(1, map.get("A"));
                assertEquals(2, map.get("B"));
                assertEquals(3, map.get("C"));
            }

            @Test
            @DisplayName("both case sensitive and case insensitive entries")
            void testMixedCaseEntries() {
                Map<String, Integer> map = new MapBuilder<Integer>()
                        .withEntry("a", 1, CASE_SENSITIVE)
                        .withEntry("b", 2, CASE_INSENSITIVE)
                        .withEntry("c", 3, CASE_SENSITIVE)
                        .withEntry("d", 4, CASE_INSENSITIVE)
                        .build();
                map = serializeAndDeserialize(map);

                Map<String, Integer> expectedMap = new MapBuilder<Integer>()
                        .withEntry("a", 1, CASE_SENSITIVE)
                        .withEntry("b", 2, CASE_INSENSITIVE)
                        .withEntry("c", 3, CASE_SENSITIVE)
                        .withEntry("d", 4, CASE_INSENSITIVE)
                        .build();

                assertNotSame(expectedMap, map);
                assertEquals(expectedMap, map);
                assertEquals(expectedMap.getClass(), map.getClass());

                assertEquals(1, map.get("a"));
                assertEquals(2, map.get("b"));
                assertEquals(3, map.get("c"));
                assertEquals(4, map.get("d"));

                assertEquals(null, map.get("A"));
                assertEquals(2, map.get("B"));
                assertEquals(null, map.get("C"));
                assertEquals(4, map.get("D"));
            }

            @SuppressWarnings("unchecked")
            private <T> T serializeAndDeserialize(T object) {
                return assertDoesNotThrow(() -> {
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    try (ObjectOutputStream objectOutput = new ObjectOutputStream(output)) {
                        objectOutput.writeObject(object);
                    }
                    try (ObjectInputStream objectInput = new ObjectInputStream(new ByteArrayInputStream(output.toByteArray()))) {
                        return (T) objectInput.readObject();
                    }
                });
            }
        }
    }
}
