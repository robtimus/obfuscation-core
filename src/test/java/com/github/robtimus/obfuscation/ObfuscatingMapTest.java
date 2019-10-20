/*
 * ObfuscatingMapTest.java
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
import static com.github.robtimus.obfuscation.Obfuscator.portion;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.spy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@SuppressWarnings({ "javadoc", "nls" })
public class ObfuscatingMapTest {

    private static final Obfuscator OBFUSCATOR = portion()
            .keepAtStart(1)
            .keepAtEnd(1)
            .withFixedLength(3)
            .build();

    private Map<String, String> map;
    private Map<String, String> obfuscating;

    private static Map<String, String> newMap(String... keysAndValues) {
        Map<String, String> newMap = new LinkedHashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            newMap.put(keysAndValues[i], keysAndValues[i + 1]);
        }
        return newMap;
    }

    @BeforeEach
    public void init() {
        map = spy(newMap("foo", "FOO", "bar", "BAR", null, "<null>"));
        obfuscating = OBFUSCATOR.obfuscateMap(map);
    }

    @Test
    @DisplayName("size()")
    public void testSize() {
        assertEquals(3, obfuscating.size());

        map.clear();

        assertEquals(0, obfuscating.size());
    }

    @Test
    @DisplayName("isEmpty()")
    public void testisEmpty() {
        assertFalse(obfuscating.isEmpty());

        map.clear();

        assertTrue(obfuscating.isEmpty());
    }

    @Test
    @DisplayName("containsKey(Object)")
    public void testContainsKey() {
        assertTrue(obfuscating.containsKey("foo"));
        assertTrue(obfuscating.containsKey("bar"));
        assertTrue(obfuscating.containsKey(null));
        assertFalse(obfuscating.containsKey("FOO"));
        assertFalse(obfuscating.containsKey("BAR"));
        assertFalse(obfuscating.containsKey("<null>"));
    }

    @Test
    @DisplayName("containsValue(Object)")
    public void testContainsValue() {
        assertTrue(obfuscating.containsValue("FOO"));
        assertTrue(obfuscating.containsValue("BAR"));
        assertTrue(obfuscating.containsValue("<null>"));
        assertFalse(obfuscating.containsValue("foo"));
        assertFalse(obfuscating.containsValue("bar"));
        assertFalse(obfuscating.containsValue(null));
    }

    @Test
    @DisplayName("get(Object)")
    public void testGet() {
        assertEquals("FOO", obfuscating.get("foo"));
        assertEquals("BAR", obfuscating.get("bar"));
        assertEquals("<null>", obfuscating.get(null));
        assertNull(obfuscating.get("FOO"));
    }

    @Test
    @DisplayName("put(String, V)")
    public void testPut() {
        assertEquals("FOO", obfuscating.put("foo", "foo"));
        assertEquals("BAR", obfuscating.put("bar", "bar"));
        assertEquals("<null>", obfuscating.put(null, null));
        assertEquals(newMap("foo", "foo", "bar", "bar", null, null), map);
    }

    @Test
    @DisplayName("remove(Object)")
    public void testRemove() {
        assertEquals("FOO", obfuscating.remove("foo"));
        assertEquals("<null>", obfuscating.remove(null));
        assertNull(obfuscating.remove("baz"));
        assertEquals(newMap("bar", "BAR"), map);
    }

    @Test
    @DisplayName("putAll(Map<? extends String, ? extends V>)")
    public void testAddAll() {
        obfuscating.putAll(newMap("baz", "BAZ", "foo", "foo"));
        assertEquals(newMap("foo", "foo", "bar", "BAR", null, "<null>", "baz", "BAZ"), map);
    }

    @Test
    @DisplayName("clear()")
    public void testClear() {
        obfuscating.clear();
        assertTrue(map.isEmpty());
    }

    @Nested
    @DisplayName("keySet()")
    public class KeySet {

        @Test
        @DisplayName("caching")
        public void testCaching() {
            assertSame(obfuscating.entrySet(), obfuscating.entrySet());
        }

        @Test
        @DisplayName("toString()")
        public void testToString() {
            assertEquals("[foo, bar, null]", obfuscating.keySet().toString());

            map.remove("foo");
            assertEquals("[bar, null]", obfuscating.keySet().toString());

            map.remove("bar");
            assertEquals("[null]", obfuscating.keySet().toString());

            map.remove(null);
            assertEquals("[]", obfuscating.keySet().toString());

            Map<String, Object> objectMap = new LinkedHashMap<>();
            Map<String, Object> objectObfuscating = OBFUSCATOR.obfuscateMap(objectMap);
            objectMap.put("map", objectMap);
            objectMap.put("keySet", objectMap.keySet());
            objectMap.put("values", objectMap.values());
            objectMap.put("entrySet", objectMap.entrySet());
            objectMap.put("obfuscatingMap", objectObfuscating);
            objectMap.put("obfuscatingKeySet", objectObfuscating.keySet());
            objectMap.put("obfuscatingValues", objectObfuscating.values());
            objectMap.put("obfuscatingEntrySet", objectObfuscating.entrySet());

            assertEquals("[map, keySet, values, entrySet, obfuscatingMap, obfuscatingKeySet, obfuscatingValues, obfuscatingEntrySet]",
                    objectObfuscating.keySet().toString());
        }
    }

    @Nested
    @DisplayName("values()")
    public class Values {

        @Test
        @DisplayName("caching")
        public void testCaching() {
            assertSame(obfuscating.values(), obfuscating.values());
        }

        @Test
        @DisplayName("toString()")
        public void testToString() {
            assertEquals("[F***O, B***R, <***>]", obfuscating.values().toString());

            map.remove("foo");
            assertEquals("[B***R, <***>]", obfuscating.values().toString());

            map.remove("bar");
            assertEquals("[<***>]", obfuscating.values().toString());

            map.remove(null);
            assertEquals("[]", obfuscating.values().toString());

            Map<String, Object> objectMap = new LinkedHashMap<>();
            Map<String, Object> objectObfuscating = OBFUSCATOR.obfuscateMap(objectMap);
            objectMap.put("keySet", objectMap.keySet());
            objectMap.put("values", objectMap.values());
            objectMap.put("obfuscatingKeySet", objectObfuscating.keySet());
            objectMap.put("obfuscatingValues", objectObfuscating.values());
            // Adding map or entrySet causes a StackOverflowError because the map and values / entrySet keep ping-ponging.
            // That's fine though, as objectMap suffers the same issue.

            assertEquals("[[***], (***), [***], (***)]", objectObfuscating.values().toString());
        }
    }

    @Nested
    @DisplayName("entrySet()")
    public class EntrySet {

        @Test
        @DisplayName("caching")
        public void testCaching() {
            assertSame(obfuscating.entrySet(), obfuscating.entrySet());
        }

        @Test
        @DisplayName("toString()")
        public void testToString() {
            assertEquals("[foo=F***O, bar=B***R, null=<***>]", obfuscating.entrySet().toString());

            map.remove("foo");
            assertEquals("[bar=B***R, null=<***>]", obfuscating.entrySet().toString());

            map.remove("bar");
            assertEquals("[null=<***>]", obfuscating.entrySet().toString());

            map.remove(null);
            assertEquals("[]", obfuscating.entrySet().toString());

            Map<String, Object> objectMap = new LinkedHashMap<>();
            Map<String, Object> objectObfuscating = OBFUSCATOR.obfuscateMap(objectMap);
            objectMap.put("keySet", objectMap.keySet());
            objectMap.put("entrySet", objectMap.entrySet());
            objectMap.put("obfuscatingKeySet", objectObfuscating.keySet());
            objectMap.put("obfuscatingEntrySet", objectObfuscating.entrySet());
            // Adding map or values causes a StackOverflowError because the map and values / entrySet keep ping-ponging.
            // That's fine though, as objectMap suffers the same issue.

            assertEquals("[keySet=[***], entrySet=(***), obfuscatingKeySet=[***], obfuscatingEntrySet=(***)]",
                    objectObfuscating.entrySet().toString());
        }
    }

    @Test
    @DisplayName("getOrDefault(Object, V)")
    public void testGetOrDefault() {
        assertEquals("FOO", obfuscating.getOrDefault("foo", "bar"));
        assertEquals("BAR", obfuscating.getOrDefault("bar", "foo"));
        assertEquals("<null>", obfuscating.getOrDefault(null, "null"));
        assertEquals("foo", obfuscating.getOrDefault("FOO", "foo"));
    }

    @Test
    @DisplayName("forEach(BiConsumer<? super String, ? super V>)")
    public void testForEach() {
        List<String> result = new ArrayList<>();
        obfuscating.forEach((k, v) -> {
            result.add(k);
            result.add(v);
        });
        assertEquals(Arrays.asList("foo", "FOO", "bar", "BAR", null, "<null>"), result);
    }

    @Test
    @DisplayName("replaceAll(BiFunction<? super String, ? super V, ? extends V>)")
    public void testReplaceAll() {
        obfuscating.replaceAll((k, v) -> k + v);
        assertEquals(newMap("foo", "fooFOO", "bar", "barBAR", null, "null<null>"), map);
    }

    @Test
    @DisplayName("putIfAbsent(String, V)")
    public void testPutIfAbsent() {
        assertEquals("FOO", obfuscating.putIfAbsent("foo", "foo"));
        assertEquals("BAR", obfuscating.putIfAbsent("bar", "bar"));
        assertEquals("<null>", obfuscating.putIfAbsent(null, "null"));
        assertNull(obfuscating.putIfAbsent("baz", "BAZ"));
        assertEquals(newMap("foo", "FOO", "bar", "BAR", null, "<null>", "baz", "BAZ"), map);
    }

    @Test
    @DisplayName("remove(Object, Object)")
    public void testRemoveWithValue() {
        assertFalse(obfuscating.remove("foo", "foo"));
        assertTrue(obfuscating.remove("bar", "BAR"));
        assertFalse(obfuscating.remove(null, "null"));
        assertTrue(obfuscating.remove(null, "<null>"));
        assertFalse(obfuscating.remove("baz", "BAZ"));
        assertEquals(newMap("foo", "FOO"), map);
    }

    @Test
    @DisplayName("replace(String, V, V)")
    public void testReplaceWithValue() {
        assertFalse(obfuscating.replace("foo", "foo", "bar"));
        assertTrue(obfuscating.replace("bar", "BAR", "foo"));
        assertFalse(obfuscating.replace(null, "null", null));
        assertTrue(obfuscating.replace(null, "<null>", "null"));
        assertFalse(obfuscating.replace("baz", "BAZ", "baz"));
        assertEquals(newMap("foo", "FOO", "bar", "foo", null, "null"), map);
    }

    @Test
    @DisplayName("replace(String, V)")
    public void testReplace() {
        assertEquals("FOO", obfuscating.replace("foo", "foo"));
        assertEquals("BAR", obfuscating.replace("bar", "bar"));
        assertEquals("<null>", obfuscating.replace(null, "null"));
        assertNull(obfuscating.replace("baz", "BAZ"));
        assertEquals(newMap("foo", "foo", "bar", "bar", null, "null"), map);
    }

    @Test
    @DisplayName("computeIfAbsent(String, Function<? super String, ? extends V>)")
    public void testComputeIfAbsent() {
        assertEquals("FOO", obfuscating.computeIfAbsent("foo", Function.identity()));
        assertEquals("BAR", obfuscating.computeIfAbsent("bar", Function.identity()));
        assertEquals("<null>", obfuscating.computeIfAbsent(null, Function.identity()));
        assertEquals("BAZ", obfuscating.computeIfAbsent("baz", String::toUpperCase));
        assertNull(obfuscating.computeIfAbsent("new", k -> null));
        assertEquals(newMap("foo", "FOO", "bar", "BAR", null, "<null>", "baz", "BAZ"), map);
    }

    @Test
    @DisplayName("computeIfPresent(String, BiFunction<? super String, ? super V, ? extends V>)")
    public void testComputeIfPresent() {
        assertEquals("fooFOO", obfuscating.computeIfPresent("foo", (k, v) -> k + v));
        assertEquals(null, obfuscating.computeIfPresent("bar", (k, v) -> null));
        assertEquals("null<null>", obfuscating.computeIfPresent(null, (k, v) -> k + v));
        assertEquals(null, obfuscating.computeIfPresent("baz", (k, v) -> k + v));
        assertEquals(newMap("foo", "fooFOO", null, "null<null>"), map);
    }

    @Test
    @DisplayName("compute(String, BiFunction<? super String, ? super V, ? extends V>)")
    public void testCompute() {
        assertEquals("fooFOO", obfuscating.compute("foo", (k, v) -> k + v));
        assertEquals(null, obfuscating.compute("bar", (k, v) -> null));
        assertEquals("null<null>", obfuscating.compute(null, (k, v) -> k + v));
        assertEquals("baznull", obfuscating.compute("baz", (k, v) -> k + v));
        assertEquals(newMap("foo", "fooFOO", null, "null<null>", "baz", "baznull"), map);
    }

    @Test
    @DisplayName("merge(String, V, BiFunction<? super V, ? super V, ? extends V>)")
    public void testMerge() {
        assertEquals("FOObar", obfuscating.merge("foo", "bar", (v1, v2) -> v1 + v2));
        assertNull(obfuscating.merge("bar", "foo", (v1, v2) -> null));
        assertEquals("<null>null", obfuscating.merge(null, "null", (v1, v2) -> v1 + v2));
        assertEquals("BAZ", obfuscating.merge("baz", "BAZ", (v1, v2) -> v1 + v2));
        assertEquals(newMap("foo", "FOObar", null, "<null>null", "baz", "BAZ"), map);
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource
    @DisplayName("equals(Object)")
    public void testEquals(Map<String, String> m, Object object, boolean expected) {
        assertEquals(expected, m.equals(object));
    }

    static Arguments[] testEquals() {
        Map<String, String> map = newMap("foo", "FOO", "bar", "BAR", null, "<null>");
        Map<String, String> obfuscating = OBFUSCATOR.obfuscateMap(map);
        return new Arguments[] {
                arguments(obfuscating, obfuscating, true),
                arguments(obfuscating, map, true),
                arguments(obfuscating, null, false),
                arguments(obfuscating, all().obfuscateMap(map), true),
                arguments(obfuscating, all().obfuscateMap(all().obfuscateMap(map)), true),
                arguments(obfuscating, "foo", false),
        };
    }

    @Test
    @DisplayName("hashCode()")
    public void testHashCode() {
        assertEquals(obfuscating.hashCode(), obfuscating.hashCode());
        assertEquals(obfuscating.hashCode(), map.hashCode());
    }

    @Test
    @DisplayName("toString()")
    public void testToString() {
        assertEquals("{foo=F***O, bar=B***R, null=<***>}", obfuscating.toString());

        map.remove("foo");
        assertEquals("{bar=B***R, null=<***>}", obfuscating.toString());

        map.remove("bar");
        assertEquals("{null=<***>}", obfuscating.toString());

        map.remove(null);
        assertEquals("{}", obfuscating.toString());

        Map<String, Object> objectMap = new LinkedHashMap<>();
        Map<String, Object> objectObfuscating = OBFUSCATOR.obfuscateMap(objectMap);
        objectMap.put("map", objectMap);
        objectMap.put("keySet", objectMap.keySet());
        objectMap.put("obfuscatingMap", objectObfuscating);
        objectMap.put("obfuscatingKeySet", objectObfuscating.keySet());
        // Adding values or entrySet causes a StackOverflowError because the map and values / entrySet keep ping-ponging.
        // That's fine though, as objectMap suffers the same issue.

        assertEquals("{map=(***), keySet=[***], obfuscatingMap=(***), obfuscatingKeySet=[***]}", objectObfuscating.toString());
    }
}
