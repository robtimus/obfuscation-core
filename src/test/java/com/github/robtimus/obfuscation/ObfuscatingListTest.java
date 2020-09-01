/*
 * ObfuscatingListTest.java
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

import static com.github.robtimus.obfuscation.Obfuscator.portion;
import static java.util.Comparator.reverseOrder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings("nls")
class ObfuscatingListTest {

    private static final Obfuscator OBFUSCATOR = portion()
            .keepAtStart(1)
            .keepAtEnd(1)
            .withFixedTotalLength(5)
            .build();

    private List<String> list;
    private List<String> obfuscating;

    @BeforeEach
    void init() {
        list = spy(new ArrayList<>(Arrays.asList("foo", null, "bar")));
        obfuscating = OBFUSCATOR.obfuscateList(list);
    }

    @Test
    @DisplayName("RandomAccess")
    void testRandomAccess() {
        assertThat(obfuscating, instanceOf(RandomAccess.class));
        assertThat(obfuscating.subList(0, 1), instanceOf(RandomAccess.class));

        List<String> obfuscatingLinkedList = OBFUSCATOR.obfuscateList(new LinkedList<>(list));
        assertThat(obfuscatingLinkedList, not(instanceOf(RandomAccess.class)));
        assertThat(obfuscatingLinkedList.subList(0, 1), not(instanceOf(RandomAccess.class)));
    }

    @Test
    @DisplayName("add(E)")
    void testAddAll() {
        assertTrue(obfuscating.addAll(1, Arrays.asList("baz")));
        assertEquals(Arrays.asList("foo", "baz", null, "bar"), list);
    }

    @Test
    @DisplayName("replaceAll(UnaryOperator<E>)")
    void testReplaceAll() {
        obfuscating.replaceAll(s -> String.valueOf(s).substring(1));
        assertEquals(Arrays.asList("oo", "ull", "ar"), list);
    }

    @Test
    @DisplayName("sort(Comparator<? super E>)")
    void testSort() {
        obfuscating.remove(null);

        obfuscating.sort(null);
        assertEquals(Arrays.asList("bar", "foo"), list);

        obfuscating.sort(reverseOrder());
        assertEquals(Arrays.asList("foo", "bar"), list);
    }

    @Test
    @DisplayName("get(int)")
    void testGet() {
        assertEquals("foo", obfuscating.get(0));
        assertNull(obfuscating.get(1));
        assertEquals("bar", obfuscating.get(2));
    }

    @Test
    @DisplayName("set(int, E)")
    void testSet() {
        assertEquals("foo", obfuscating.set(0, "FOO"));
        assertNull(obfuscating.set(1, "NULL"));
        assertEquals("bar", obfuscating.set(2, "BAR"));
        assertEquals(Arrays.asList("FOO", "NULL", "BAR"), list);
    }

    @Test
    @DisplayName("add(int, E)")
    void testAdd() {
        obfuscating.add(1, "baz");
        assertEquals(Arrays.asList("foo", "baz", null, "bar"), list);
    }

    @Test
    @DisplayName("remove(int)")
    void testRemove() {
        assertEquals("foo", obfuscating.remove(0));
        assertEquals("bar", obfuscating.remove(1));
        assertEquals(Arrays.asList((Object) null), list);
    }

    @Test
    @DisplayName("indexOf(Object)")
    void testIndexOf() {
        list.add("foo");
        assertEquals(0, obfuscating.indexOf("foo"));
        assertEquals(1, obfuscating.indexOf(null));
        assertEquals(2, obfuscating.indexOf("bar"));
        assertEquals(-1, obfuscating.indexOf("baz"));
    }

    @Test
    @DisplayName("lastIndexOf(Object)")
    void testLastIndexOf() {
        list.add("foo");
        assertEquals(3, obfuscating.lastIndexOf("foo"));
        assertEquals(2, obfuscating.lastIndexOf("bar"));
        assertEquals(1, obfuscating.indexOf(null));
        assertEquals(-1, obfuscating.lastIndexOf("baz"));
    }

    @Test
    @DisplayName("listIterator()")
    void testIterator() {
        ListIterator<String> iterator = obfuscating.listIterator();

        assertTrue(iterator.hasNext());
        assertEquals(0, iterator.nextIndex());
        assertFalse(iterator.hasPrevious());
        assertEquals(-1, iterator.previousIndex());
        assertEquals("foo", iterator.next());
        iterator.remove();

        assertTrue(iterator.hasNext());
        assertEquals(0, iterator.nextIndex());
        assertFalse(iterator.hasPrevious());
        assertEquals(-1, iterator.previousIndex());
        assertNull(iterator.next());

        assertTrue(iterator.hasNext());
        assertEquals(1, iterator.nextIndex());
        assertTrue(iterator.hasPrevious());
        assertEquals(0, iterator.previousIndex());
        assertEquals("bar", iterator.next());

        assertFalse(iterator.hasNext());
        assertEquals(2, iterator.nextIndex());
        assertTrue(iterator.hasPrevious());
        assertEquals(1, iterator.previousIndex());
        assertEquals("bar", iterator.previous());

        assertTrue(iterator.hasNext());
        assertEquals(1, iterator.nextIndex());
        assertTrue(iterator.hasPrevious());
        assertEquals(0, iterator.previousIndex());
        assertNull(iterator.previous());

        assertTrue(iterator.hasNext());
        assertEquals(0, iterator.nextIndex());
        assertFalse(iterator.hasPrevious());
        assertEquals(-1, iterator.previousIndex());
        assertNull(iterator.next());

        assertEquals(Arrays.asList(null, "bar"), list);
    }

    @Test
    @DisplayName("listIterator(int)")
    void testIteratorWithIndex() {
        ListIterator<String> iterator = obfuscating.listIterator(2);

        assertTrue(iterator.hasNext());
        assertEquals(2, iterator.nextIndex());
        assertTrue(iterator.hasPrevious());
        assertEquals(1, iterator.previousIndex());
        assertEquals("bar", iterator.next());
        iterator.remove();

        assertFalse(iterator.hasNext());
        assertEquals(2, iterator.nextIndex());
        assertTrue(iterator.hasPrevious());
        assertEquals(1, iterator.previousIndex());
        assertNull(iterator.previous());

        assertTrue(iterator.hasNext());
        assertEquals(1, iterator.nextIndex());
        assertTrue(iterator.hasPrevious());
        assertEquals(0, iterator.previousIndex());
        assertEquals("foo", iterator.previous());

        assertTrue(iterator.hasNext());
        assertEquals(0, iterator.nextIndex());
        assertFalse(iterator.hasPrevious());
        assertEquals(-1, iterator.previousIndex());
        assertEquals("foo", iterator.next());

        assertEquals(Arrays.asList("foo", null), list);
    }

    @Nested
    @DisplayName("subList(int, int)")
    class SubList {

        @Test
        @DisplayName("with default element representation")
        void testWithDefaultElementRepresentation() {
            assertEquals("[f***o, n***l, b***r]", obfuscating.toString());

            assertEquals("[f***o]", obfuscating.subList(0, 1).toString());

            assertEquals("[]", obfuscating.subList(0, 0).toString());
        }

        @Test
        @DisplayName("with custom element representation")
        void testWithCustomElementRepresentation() {
            obfuscating = OBFUSCATOR.obfuscateList(list, String::toUpperCase);

            assertEquals("[F***O, n***l, B***R]", obfuscating.toString());

            assertEquals("[F***O]", obfuscating.subList(0, 1).toString());

            assertEquals("[]", obfuscating.subList(0, 0).toString());
        }
    }
}
