/*
 * ObfuscatingSetTest.java
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

import static com.github.robtimus.obfuscation.Obfuscator.all;
import static com.github.robtimus.obfuscation.Obfuscator.portion;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@SuppressWarnings({ "javadoc", "nls" })
public class ObfuscatingSetTest {

    private static final Obfuscator OBFUSCATOR = portion()
            .keepAtStart(1)
            .keepAtEnd(1)
            .withFixedLength(3)
            .build();

    private Set<String> set;
    private Set<String> obfuscating;

    @BeforeEach
    public void init() {
        set = spy(new LinkedHashSet<>(Arrays.asList("foo", null, "bar")));
        obfuscating = OBFUSCATOR.obfuscateSet(set);
    }

    @Test
    @DisplayName("size()")
    public void testSize() {
        assertEquals(3, obfuscating.size());

        set.clear();

        assertEquals(0, obfuscating.size());
    }

    @Test
    @DisplayName("isEmpty()")
    public void testisEmpty() {
        assertFalse(obfuscating.isEmpty());

        set.clear();

        assertTrue(obfuscating.isEmpty());
    }

    @Test
    @DisplayName("contains(Object)")
    public void testContains() {
        assertTrue(obfuscating.contains("foo"));
        assertTrue(obfuscating.contains(null));
        assertTrue(obfuscating.contains("bar"));
        assertFalse(obfuscating.contains("baz"));
    }

    @Test
    @DisplayName("iterator()")
    public void testIterator() {
        Iterator<String> iterator = obfuscating.iterator();

        assertTrue(iterator.hasNext());
        assertEquals("foo", iterator.next());
        iterator.remove();

        assertTrue(iterator.hasNext());
        assertNull(iterator.next());

        assertTrue(iterator.hasNext());
        assertEquals("bar", iterator.next());

        assertFalse(iterator.hasNext());

        assertEquals(new HashSet<>(Arrays.asList(null, "bar")), set);
    }

    @Test
    @DisplayName("toArray()")
    public void testToArray() {
        Object[] array = obfuscating.toArray();
        Object[] expected = { "foo", null, "bar" };
        assertArrayEquals(expected, array);
    }

    @Nested
    @DisplayName("toArray(T[]")
    public class ToArrayTest {

        @Test
        @DisplayName("equal size array")
        public void testEqualSizeArray() {
            Object[] expected = { "foo", null, "bar" };
            String[] input = new String[3];
            String[] array = obfuscating.toArray(input);
            assertSame(input, array);
            assertArrayEquals(expected, array);
        }

        @Test
        @DisplayName("smaller array")
        public void testSmallerArray() {
            Object[] expected = { "foo", null, "bar" };
            String[] input = new String[1];
            String[] array = obfuscating.toArray(input);
            assertNotSame(input, array);
            assertArrayEquals(expected, array);
        }

        @Test
        @DisplayName("larger array")
        public void testLargerArray() {
            Object[] expected = { "foo", null, "bar", null, null };
            String[] input = new String[5];
            String[] array = obfuscating.toArray(input);
            assertSame(input, array);
            assertArrayEquals(expected, array);
        }
    }

    @Test
    @DisplayName("add(E)")
    public void testAdd() {
        assertTrue(obfuscating.add("baz"));
        assertFalse(obfuscating.add("bar"));
        assertFalse(obfuscating.add(null));
        assertEquals(new HashSet<>(Arrays.asList("foo", null, "bar", "baz")), set);
    }

    @Test
    @DisplayName("remove(Object)")
    public void testRemove() {
        assertTrue(obfuscating.remove("foo"));
        assertTrue(obfuscating.remove(null));
        assertFalse(obfuscating.remove("baz"));
        assertEquals(new HashSet<>(Arrays.asList("bar")), set);
    }

    @Test
    @DisplayName("containsAll(Collection<?>)")
    public void testContainsAll() {
        assertTrue(obfuscating.containsAll(Arrays.asList("foo")));
        assertTrue(obfuscating.containsAll(Arrays.asList("foo", null)));
        assertTrue(obfuscating.containsAll(Arrays.asList((Object) null)));
        assertTrue(obfuscating.containsAll(Arrays.asList("bar")));
        assertTrue(obfuscating.containsAll(Arrays.asList("foo", "bar")));
        assertTrue(obfuscating.containsAll(Arrays.asList("foo", null, "bar")));
        assertFalse(obfuscating.containsAll(Arrays.asList("baz")));
        assertFalse(obfuscating.containsAll(Arrays.asList("foo", "baz")));
    }

    @Test
    @DisplayName("add(E)")
    public void testAddAll() {
        assertTrue(obfuscating.addAll(Arrays.asList("foo", "baz", null)));
        assertEquals(new HashSet<>(Arrays.asList("foo", null, "bar", "baz")), set);
    }

    @Test
    @DisplayName("removeAll(Collection<?>)")
    public void testRemoveAll() {
        assertTrue(obfuscating.removeAll(Arrays.asList("foo")));
        assertTrue(obfuscating.removeAll(Arrays.asList((Object) null)));
        assertFalse(obfuscating.removeAll(Arrays.asList("baz")));
        assertEquals(new HashSet<>(Arrays.asList("bar")), set);
    }

    @Test
    @DisplayName("removeIf(Predicate<? super E>)")
    public void testRemoveIf() {
        assertTrue(obfuscating.removeIf(Objects::isNull));
        assertTrue(obfuscating.removeIf(e -> e.startsWith("f")));
        assertFalse(obfuscating.removeIf(e -> e.startsWith("f")));
        assertEquals(new HashSet<>(Arrays.asList("bar")), set);
    }

    @Test
    @DisplayName("retainAll(Collection<?>)")
    public void testRetainAll() {
        assertTrue(obfuscating.retainAll(Arrays.asList("foo")));
        assertFalse(obfuscating.retainAll(Arrays.asList("foo", "baz")));
        assertEquals(new HashSet<>(Arrays.asList("foo")), set);
    }

    @Test
    @DisplayName("clear()")
    public void testClear() {
        obfuscating.clear();
        assertTrue(set.isEmpty());
    }

    @Test
    @DisplayName("spliterator()")
    public void testSpliterator() {
        Spliterator<String> spliterator = obfuscating.spliterator();
        assertTrue(spliterator.tryAdvance(e -> assertEquals("foo", e)));
        assertTrue(spliterator.tryAdvance(e -> assertNull(e)));
        assertTrue(spliterator.tryAdvance(e -> assertEquals("bar", e)));
        assertFalse(spliterator.tryAdvance(e -> fail()));
    }

    @Test
    @DisplayName("stream()")
    public void testStream() {
        List<String> list = obfuscating.stream()
                .filter(Objects::nonNull)
                .filter(e -> e.startsWith("f"))
                .collect(toList());
        assertEquals(Arrays.asList("foo"), list);

        verify(set).stream();
        verify(set, never()).parallelStream();
    }

    @Test
    @DisplayName("parallelStream()")
    public void testParallelStream() {
        List<String> list = obfuscating.parallelStream()
                .filter(Objects::nonNull)
                .filter(e -> e.startsWith("f"))
                .collect(toList());
        assertEquals(Arrays.asList("foo"), list);

        verify(set).parallelStream();
        verify(set, never()).stream();
    }

    @Test
    @DisplayName("forEach(Consumer<? super E>)")
    public void testForEach() {
        List<String> result = new ArrayList<>();
        obfuscating.forEach(result::add);
        assertEquals(Arrays.asList("foo", null, "bar"), result);
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource
    @DisplayName("equals(Object)")
    public void testEquals(Set<String> c, Object object, boolean expected) {
        assertEquals(expected, c.equals(object));
    }

    static Arguments[] testEquals() {
        Set<String> set = new HashSet<>(Arrays.asList("foo", null, "bar"));
        Set<String> obfuscating = OBFUSCATOR.obfuscateSet(set);
        return new Arguments[] {
                arguments(obfuscating, obfuscating, true),
                arguments(obfuscating, set, true),
                arguments(obfuscating, null, false),
                arguments(obfuscating, all().obfuscateSet(set), true),
                arguments(obfuscating, all().obfuscateSet(all().obfuscateSet(set)), true),
                arguments(obfuscating, "foo", false),
        };
    }

    @Test
    @DisplayName("hashCode()")
    public void testHashCode() {
        assertEquals(obfuscating.hashCode(), obfuscating.hashCode());
        assertEquals(obfuscating.hashCode(), set.hashCode());
    }

    @Test
    @DisplayName("toString()")
    public void testToString() {
        assertEquals("[f***o, n***l, b***r]", obfuscating.toString());

        set.remove("foo");
        assertEquals("[n***l, b***r]", obfuscating.toString());

        set.remove(null);
        assertEquals("[b***r]", obfuscating.toString());

        set.remove("bar");
        assertEquals("[]", obfuscating.toString());
    }
}
