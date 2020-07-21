/*
 * ObfuscatingPropertiesTest.java
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

import static com.github.robtimus.obfuscation.Obfuscator.none;
import static com.github.robtimus.obfuscation.Obfuscator.portion;
import static com.github.robtimus.obfuscation.PropertiesObfuscator.builder;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.spy;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@SuppressWarnings("nls")
class ObfuscatingPropertiesTest {

    private static final PropertiesObfuscator OBFUSCATOR = builder()
            .withDefaultObfuscator(portion()
                    .keepAtStart(1)
                    .keepAtEnd(1)
                    .withFixedTotalLength(5)
                    .build())
            .withProperty("not-obfuscated", none())
            .build();

    private Properties properties;
    private Properties obfuscating;

    static Properties newProperties(Properties defaults, String... keysAndValues) {
        Properties newProperties = new Properties(defaults);
        for (int i = 0; i < keysAndValues.length; i += 2) {
            newProperties.put(keysAndValues[i], keysAndValues[i + 1]);
        }
        return newProperties;
    }

    static Properties newProperties(String... keysAndValues) {
        return newProperties(null, keysAndValues);
    }

    @BeforeEach
    void init() {
        Properties defaults = newProperties("default", "DEFAULT");
        properties = spy(newProperties(defaults, "foo", "FOO", "bar", "BAR"));
        obfuscating = OBFUSCATOR.obfuscateProperties(properties);
    }

    @Test
    @DisplayName("size()")
    void testSize() {
        assertEquals(2, obfuscating.size());

        properties.clear();

        assertEquals(0, obfuscating.size());
    }

    @Test
    @DisplayName("isEmpty()")
    void testisEmpty() {
        assertFalse(obfuscating.isEmpty());

        properties.clear();

        assertTrue(obfuscating.isEmpty());
    }

    @Test
    @DisplayName("containsKey(Object)")
    void testContainsKey() {
        assertTrue(obfuscating.containsKey("foo"));
        assertTrue(obfuscating.containsKey("bar"));
        assertFalse(obfuscating.containsKey("FOO"));
        assertFalse(obfuscating.containsKey("BAR"));
    }

    @Test
    @DisplayName("containsValue(Object)")
    void testContainsValue() {
        assertTrue(obfuscating.containsValue("FOO"));
        assertTrue(obfuscating.containsValue("BAR"));
        assertFalse(obfuscating.containsValue("foo"));
        assertFalse(obfuscating.containsValue("bar"));
    }

    @Test
    @DisplayName("get(Object)")
    void testGet() {
        assertEquals("FOO", obfuscating.get("foo"));
        assertEquals("BAR", obfuscating.get("bar"));
        assertNull(obfuscating.get("FOO"));
    }

    @Test
    @DisplayName("put(Object, Object)")
    void testPut() {
        assertEquals("FOO", obfuscating.put("foo", "foo"));
        assertEquals("BAR", obfuscating.put("bar", "bar"));
        assertEquals(newProperties("foo", "foo", "bar", "bar"), properties);
    }

    @Test
    @DisplayName("remove(Object)")
    void testRemove() {
        assertEquals("FOO", obfuscating.remove("foo"));
        assertNull(obfuscating.remove("baz"));
        assertEquals(newProperties("bar", "BAR"), properties);
    }

    @Test
    @DisplayName("putAll(Map<? extends Object, ? extends Object>)")
    void testAddAll() {
        obfuscating.putAll(newProperties("baz", "BAZ", "foo", "foo"));
        assertEquals(newProperties("foo", "foo", "bar", "BAR", "baz", "BAZ"), properties);
    }

    @Test
    @DisplayName("clear()")
    void testClear() {
        obfuscating.clear();
        assertTrue(properties.isEmpty());
    }

    @Nested
    @DisplayName("keySet()")
    class KeySet {

        @Test
        @DisplayName("caching")
        void testCaching() {
            assertSame(obfuscating.entrySet(), obfuscating.entrySet());
        }

        @Test
        @DisplayName("toString()")
        void testToString() {
            assertHasToString(obfuscating.keySet(), "[", "]", "foo", "bar");

            properties.remove("foo");
            assertHasToString(obfuscating.keySet(), "[", "]", "bar");

            properties.remove("bar");
            assertHasToString(obfuscating.keySet(), "[", "]");

            Properties objectProperties = new Properties();
            Properties objectObfuscating = OBFUSCATOR.obfuscateProperties(objectProperties);
            objectProperties.put("properties", objectProperties);
            objectProperties.put("keySet", objectProperties.keySet());
            objectProperties.put("values", objectProperties.values());
            objectProperties.put("entrySet", objectProperties.entrySet());
            objectProperties.put("obfuscatingProperties", objectObfuscating);
            objectProperties.put("obfuscatingKeySet", objectObfuscating.keySet());
            objectProperties.put("obfuscatingValues", objectObfuscating.values());
            objectProperties.put("obfuscatingEntrySet", objectObfuscating.entrySet());

            assertHasToString(objectObfuscating.keySet(), "[", "]", "properties", "keySet", "values", "entrySet", "obfuscatingProperties",
                    "obfuscatingKeySet", "obfuscatingValues", "obfuscatingEntrySet");
        }
    }

    @Nested
    @DisplayName("values()")
    class Values {

        @Test
        @DisplayName("caching")
        void testCaching() {
            assertSame(obfuscating.values(), obfuscating.values());
        }

        @Test
        @DisplayName("toString()")
        void testToString() {
            assertHasToString(obfuscating.values(), "[", "]", "F***O", "B***R");

            properties.remove("foo");
            assertHasToString(obfuscating.values(), "[", "]", "B***R");

            properties.remove("bar");
            assertHasToString(obfuscating.values(), "[", "]");

            Properties objectProperties = new Properties();
            Properties objectObfuscating = OBFUSCATOR.obfuscateProperties(objectProperties);
            objectProperties.put("keySet", objectProperties.keySet());
            objectProperties.put("values", objectProperties.values());
            objectProperties.put("obfuscatingKeySet", objectObfuscating.keySet());
            objectProperties.put("obfuscatingValues", objectObfuscating.values());
            // Adding properties or entrySet causes a StackOverflowError because the map and values / entrySet keep ping-ponging.
            // That's fine though, as objectProperties suffers the same issue.

            assertHasToString(objectObfuscating.values(), "[", "]", "[***]", "(***)", "[***]", "(***)");
        }
    }

    @Nested
    @DisplayName("entrySet()")
    class EntrySet {

        @Test
        @DisplayName("caching")
        void testCaching() {
            assertSame(obfuscating.entrySet(), obfuscating.entrySet());
        }

        @Test
        @DisplayName("toString()")
        void testToString() {
            assertHasToString(obfuscating.entrySet(), "[", "]", "foo=F***O", "bar=B***R");

            properties.remove("foo");
            assertHasToString(obfuscating.entrySet(), "[", "]", "bar=B***R");

            properties.remove("bar");
            assertHasToString(obfuscating.entrySet(), "[", "]");

            Properties objectProperties = new Properties();
            Properties objectObfuscating = OBFUSCATOR.obfuscateProperties(objectProperties);
            objectProperties.put("keySet", objectProperties.keySet());
            objectProperties.put("entrySet", objectProperties.entrySet());
            objectProperties.put("obfuscatingKeySet", objectObfuscating.keySet());
            objectProperties.put("obfuscatingEntrySet", objectObfuscating.entrySet());
            // Adding properties or values causes a StackOverflowError because the map and values / entrySet keep ping-ponging.
            // That's fine though, as objectProperties suffers the same issue.

            assertHasToString(objectObfuscating.entrySet(), "[", "]", "keySet=[***]", "entrySet=(***)", "obfuscatingKeySet=[***]",
                    "obfuscatingEntrySet=(***)");
        }
    }

    @Test
    @DisplayName("getOrDefault(Object, Object)")
    void testGetOrDefault() {
        assertEquals("FOO", obfuscating.getOrDefault("foo", "bar"));
        assertEquals("BAR", obfuscating.getOrDefault("bar", "foo"));
        assertEquals("foo", obfuscating.getOrDefault("FOO", "foo"));
    }

    @Test
    @DisplayName("forEach(BiConsumer<? super Object, ? super Object>)")
    void testForEach() {
        List<Object> result = new ArrayList<>();
        obfuscating.forEach((k, v) -> {
            result.add(k);
            result.add(v);
        });
        assertEquals(Arrays.asList("foo", "FOO", "bar", "BAR"), result);
    }

    @Test
    @DisplayName("replaceAll(BiFunction<? super Object, ? super Object, ? extends Object>)")
    void testReplaceAll() {
        obfuscating.replaceAll((k, v) -> k.toString() + v);
        assertEquals(newProperties("foo", "fooFOO", "bar", "barBAR"), properties);
    }

    @Test
    @DisplayName("putIfAbsent(Object, Object)")
    void testPutIfAbsent() {
        assertEquals("FOO", obfuscating.putIfAbsent("foo", "foo"));
        assertEquals("BAR", obfuscating.putIfAbsent("bar", "bar"));
        assertNull(obfuscating.putIfAbsent("baz", "BAZ"));
        assertEquals(newProperties("foo", "FOO", "bar", "BAR", "baz", "BAZ"), properties);
    }

    @Test
    @DisplayName("remove(Object, Object)")
    void testRemoveWithValue() {
        assertFalse(obfuscating.remove("foo", "foo"));
        assertTrue(obfuscating.remove("bar", "BAR"));
        assertFalse(obfuscating.remove("baz", "BAZ"));
        assertEquals(newProperties("foo", "FOO"), properties);
    }

    @Test
    @DisplayName("replace(Object, Object, Object)")
    void testReplaceWithValue() {
        assertFalse(obfuscating.replace("foo", "foo", "bar"));
        assertTrue(obfuscating.replace("bar", "BAR", "foo"));
        assertFalse(obfuscating.replace("baz", "BAZ", "baz"));
        assertEquals(newProperties("foo", "FOO", "bar", "foo"), properties);
    }

    @Test
    @DisplayName("replace(Object, Object)")
    void testReplace() {
        assertEquals("FOO", obfuscating.replace("foo", "foo"));
        assertEquals("BAR", obfuscating.replace("bar", "bar"));
        assertNull(obfuscating.replace("baz", "BAZ"));
        assertEquals(newProperties("foo", "foo", "bar", "bar"), properties);
    }

    @Test
    @DisplayName("computeIfAbsent(Object, Function<? super Object, ? extends Object>)")
    void testComputeIfAbsent() {
        assertEquals("FOO", obfuscating.computeIfAbsent("foo", Function.identity()));
        assertEquals("BAR", obfuscating.computeIfAbsent("bar", Function.identity()));
        assertEquals("BAZ", obfuscating.computeIfAbsent("baz", k -> k.toString().toUpperCase()));
        assertNull(obfuscating.computeIfAbsent("new", k -> null));
        assertEquals(newProperties("foo", "FOO", "bar", "BAR", "baz", "BAZ"), properties);
    }

    @Test
    @DisplayName("computeIfPresent(Object, BiFunction<? super Object, ? super Object, ? extends Object>)")
    void testComputeIfPresent() {
        assertEquals("fooFOO", obfuscating.computeIfPresent("foo", (k, v) -> k.toString() + v));
        assertEquals(null, obfuscating.computeIfPresent("bar", (k, v) -> null));
        assertEquals(null, obfuscating.computeIfPresent("baz", (k, v) -> k.toString() + v));
        assertEquals(newProperties("foo", "fooFOO"), properties);
    }

    @Test
    @DisplayName("compute(Object, BiFunction<? super Object, ? super Object, ? extends Object>)")
    void testCompute() {
        assertEquals("fooFOO", obfuscating.compute("foo", (k, v) -> k.toString() + v));
        assertEquals(null, obfuscating.compute("bar", (k, v) -> null));
        assertEquals("baznull", obfuscating.compute("baz", (k, v) -> k.toString() + v));
        assertEquals(newProperties("foo", "fooFOO", "baz", "baznull"), properties);
    }

    @Test
    @DisplayName("merge(Object, Object, BiFunction<? super Object, ? super Object, ? extends Object>)")
    void testMerge() {
        assertEquals("FOObar", obfuscating.merge("foo", "bar", (v1, v2) -> v1.toString() + v2));
        assertNull(obfuscating.merge("bar", "foo", (v1, v2) -> null));
        assertEquals("BAZ", obfuscating.merge("baz", "BAZ", (v1, v2) -> v1.toString() + v2));
        assertEquals(newProperties("foo", "FOObar", "baz", "BAZ"), properties);
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource
    @DisplayName("equals(Object)")
    void testEquals(Properties p, Object object, boolean expected) {
        assertEquals(expected, p.equals(object));
    }

    static Arguments[] testEquals() {
        Properties properties = newProperties("foo", "FOO", "bar", "BAR");
        Properties obfuscating = OBFUSCATOR.obfuscateProperties(properties);
        return new Arguments[] {
                arguments(obfuscating, obfuscating, true),
                arguments(obfuscating, properties, true),
                arguments(obfuscating, null, false),
                arguments(obfuscating, OBFUSCATOR.obfuscateProperties(properties), true),
                arguments(obfuscating, OBFUSCATOR.obfuscateProperties(OBFUSCATOR.obfuscateProperties(properties)), true),
                arguments(obfuscating, "foo", false),
        };
    }

    @Test
    @DisplayName("hashCode()")
    void testHashCode() {
        assertEquals(obfuscating.hashCode(), obfuscating.hashCode());
        assertEquals(obfuscating.hashCode(), properties.hashCode());
    }

    @Test
    @DisplayName("toString()")
    void testToString() {
        assertHasToString(obfuscating, "{", "}", "foo=F***O", "bar=B***R");

        properties.remove("foo");
        assertHasToString(obfuscating, "{", "}", "bar=B***R");

        properties.remove("bar");
        assertHasToString(obfuscating, "{", "}");

        Properties objectProperties = new Properties();
        Properties objectObfuscating = OBFUSCATOR.obfuscateProperties(objectProperties);
        objectProperties.put("properties", objectProperties);
        objectProperties.put("keySet", objectProperties.keySet());
        objectProperties.put("obfuscatingProperties", objectObfuscating);
        objectProperties.put("obfuscatingKeySet", objectObfuscating.keySet());
        // Adding values or entrySet causes a StackOverflowError because the map and values / entrySet keep ping-ponging.
        // That's fine though, as objectProperties suffers the same issue.

        assertHasToString(objectObfuscating, "{", "}", "properties=(***)", "keySet=[***]", "obfuscatingProperties=(***)", "obfuscatingKeySet=[***]");
    }

    @Test
    @DisplayName("clone()")
    void testClone() {
        Properties obfuscatingClone = (Properties) obfuscating.clone();
        assertNotSame(obfuscating, obfuscatingClone);
        assertNotSame(obfuscating.keySet(), obfuscatingClone.keySet());
        assertNotSame(obfuscating.values(), obfuscatingClone.values());
        assertNotSame(obfuscating.entrySet(), obfuscatingClone.entrySet());

        assertEquals("BAR", obfuscatingClone.put("bar", "bar"));
        assertEquals(newProperties("foo", "FOO", "bar", "bar"), obfuscatingClone);
        assertEquals(newProperties("foo", "FOO", "bar", "BAR"), obfuscating);
    }

    @Test
    @DisplayName("keys()")
    void testKeys() {
        Enumeration<?> enumeration = obfuscating.keys();
        assertThat(enumeration.toString(), allOf(not(containsString("foo")), not(containsString("bar"))));

        List<Object> keys = new ArrayList<>();
        while (enumeration.hasMoreElements()) {
            keys.add(enumeration.nextElement());
        }
        assertThat(keys, containsInAnyOrder("foo", "bar"));
    }

    @Test
    @DisplayName("elements()")
    void testElements() {
        Enumeration<?> enumeration = obfuscating.elements();
        assertThat(enumeration.toString(), allOf(not(containsString("FOO")), not(containsString("BAR"))));

        List<Object> elements = new ArrayList<>();
        while (enumeration.hasMoreElements()) {
            elements.add(enumeration.nextElement());
        }
        assertThat(elements, containsInAnyOrder("FOO", "BAR"));
    }

    @Test
    @DisplayName("contains(Object)")
    void testContains() {
        assertTrue(obfuscating.contains("FOO"));
        assertTrue(obfuscating.contains("BAR"));
        assertFalse(obfuscating.contains("foo"));
        assertFalse(obfuscating.contains("bar"));
    }

    @Test
    @DisplayName("getProperty(String)")
    void testGetProperty() {
        assertEquals("FOO", obfuscating.getProperty("foo"));
        assertEquals("BAR", obfuscating.getProperty("bar"));
        assertNull(obfuscating.getProperty("FOO"));
    }

    @Test
    @DisplayName("getProperty(String, String)")
    void testGetPropertyWithDefault() {
        assertEquals("FOO", obfuscating.getProperty("foo", "baz"));
        assertEquals("BAR", obfuscating.getProperty("bar", "baz"));
        assertEquals("baz", obfuscating.getProperty("FOO", "baz"));
    }

    @Test
    @DisplayName("setProperty(String, String)")
    void testSetProperty() {
        assertEquals("FOO", obfuscating.setProperty("foo", "foo"));
        assertEquals("BAR", obfuscating.setProperty("bar", "bar"));
        assertEquals(newProperties("foo", "foo", "bar", "bar"), properties);
    }

    @Test
    @DisplayName("propertyNames()")
    void testPropertyNames() {
        Enumeration<?> enumeration = obfuscating.propertyNames();
        assertThat(enumeration.toString(), allOf(not(containsString("foo")), not(containsString("bar"))));

        List<Object> propertyNames = new ArrayList<>();
        while (enumeration.hasMoreElements()) {
            propertyNames.add(enumeration.nextElement());
        }
        assertThat(propertyNames, containsInAnyOrder("default", "foo", "bar"));
    }

    @Test
    @DisplayName("stringPropertyNames()")
    void testStringPropertyNames() {
        Set<String> propertyNames = obfuscating.stringPropertyNames();
        assertThat(propertyNames, containsInAnyOrder("default", "foo", "bar"));
    }

    @Test
    @DisplayName("load(Reader)")
    void testLoadFromReader() throws IOException {
        String contents = "foo=foo\nbaz=BAZ";
        obfuscating.load(new StringReader(contents));
        assertEquals(newProperties("foo", "foo", "bar", "BAR", "baz", "BAZ"), obfuscating);
    }

    @Test
    @DisplayName("load(InputStream)")
    void testLoadFromInputStream() throws IOException {
        String contents = "foo=foo\nbaz=BAZ";
        obfuscating.load(new ByteArrayInputStream(contents.getBytes(StandardCharsets.UTF_8)));
        assertEquals(newProperties("foo", "foo", "bar", "BAR", "baz", "BAZ"), obfuscating);
    }

    @Test
    @DisplayName("save(OutputStream, String)")
    @SuppressWarnings("deprecation")
    void testSave() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        obfuscating.save(outputStream, "comments");

        String contents = outputStream.toString("UTF-8");
        assertThat(contents, startsWith("#comments" + System.lineSeparator()));
        assertThat(contents, containsString("#" + DateTimeFormatter.ofPattern("EEE MMM dd").format(LocalDate.now())));
        assertThat(contents, containsString("foo=FOO" + System.lineSeparator()));
        assertThat(contents, containsString("bar=BAR" + System.lineSeparator()));
    }

    @Test
    @DisplayName("store(Writer, String)")
    void testStoreToWriter() throws IOException {
        StringWriter writer = new StringWriter();
        obfuscating.store(writer, "comments");

        String contents = writer.toString();
        assertThat(contents, startsWith("#comments" + System.lineSeparator()));
        assertThat(contents, containsString("#" + DateTimeFormatter.ofPattern("EEE MMM dd").format(LocalDate.now())));
        assertThat(contents, containsString("foo=FOO" + System.lineSeparator()));
        assertThat(contents, containsString("bar=BAR" + System.lineSeparator()));
    }

    @Test
    @DisplayName("store(OutputStream, String)")
    void testStoreToOutputStream() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        obfuscating.store(outputStream, "comments");

        String contents = outputStream.toString("UTF-8");
        assertThat(contents, startsWith("#comments" + System.lineSeparator()));
        assertThat(contents, containsString("#" + DateTimeFormatter.ofPattern("EEE MMM dd").format(LocalDate.now())));
        assertThat(contents, containsString("foo=FOO" + System.lineSeparator()));
        assertThat(contents, containsString("bar=BAR" + System.lineSeparator()));
    }

    @Test
    @DisplayName("loadFromXML(InputStream)")
    void testLoadFromFromXML() throws IOException {
        String contents = "<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">"
                + "<properties><entry key=\"foo\">foo</entry><entry key=\"baz\">BAZ</entry></properties>";
        obfuscating.loadFromXML(new ByteArrayInputStream(contents.getBytes(StandardCharsets.UTF_8)));
        assertEquals(newProperties("foo", "foo", "bar", "BAR", "baz", "BAZ"), obfuscating);
    }

    @Test
    @DisplayName("storeToXML(OutputStream, String)")
    void testStoreToXML() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        obfuscating.storeToXML(outputStream, "comments");

        String contents = outputStream.toString("UTF-8");
        assertThat(contents, containsString("<comment>comments</comment>"));
        assertThat(contents, containsString("<entry key=\"foo\">FOO</entry>"));
        assertThat(contents, containsString("<entry key=\"bar\">BAR</entry>"));
    }

    @Test
    @DisplayName("storeToXML(OutputStream, String, String)")
    void testStoreToXMLWithEncoding() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        obfuscating.storeToXML(outputStream, "comments", "UTF-8");

        String contents = outputStream.toString("UTF-8");
        assertThat(contents, containsString("<comment>comments</comment>"));
        assertThat(contents, containsString("<entry key=\"foo\">FOO</entry>"));
        assertThat(contents, containsString("<entry key=\"bar\">BAR</entry>"));
    }

    @Test
    @DisplayName("list(PrintStream)")
    void testListToPrintStream() throws IOException {
        properties.setProperty("foo", "123456789A123456789B123456789C123456789D123456789E");
        properties.setProperty("not-obfuscated", "123456789A123456789B123456789C123456789D123456789E");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (PrintStream printStream = new PrintStream(outputStream)) {
            obfuscating.list(printStream);
        }

        String contents = outputStream.toString("UTF-8");
        assertThat(contents, containsString("default=D***T" + System.lineSeparator()));
        assertThat(contents, containsString("foo=1***E" + System.lineSeparator()));
        assertThat(contents, containsString("bar=B***R" + System.lineSeparator()));
        assertThat(contents, containsString("not-obfuscated=123456789A123456789B123456789C1234567..." + System.lineSeparator()));
    }

    @Test
    @DisplayName("list(PrintWriter)")
    void testListToPrintWriter() {
        properties.setProperty("foo", "123456789A123456789B123456789C123456789D123456789E");
        properties.setProperty("not-obfuscated", "123456789A123456789B123456789C123456789D123456789E");

        StringWriter writer = new StringWriter();
        try (PrintWriter printWriter = new PrintWriter(writer)) {
            obfuscating.list(printWriter);
        }

        String contents = writer.toString();
        assertThat(contents, containsString("default=D***T" + System.lineSeparator()));
        assertThat(contents, containsString("foo=1***E" + System.lineSeparator()));
        assertThat(contents, containsString("bar=B***R" + System.lineSeparator()));
        assertThat(contents, containsString("not-obfuscated=123456789A123456789B123456789C1234567..." + System.lineSeparator()));
    }

    static void assertHasToString(Object object, String expectedPrefix, String expectedPostfix, String... expectedParts) {
        assertThat(object.toString(), toStringMatcher(expectedPrefix, expectedPostfix, expectedParts));
    }

    private static Matcher<String> toStringMatcher(String expectedPrefix, String expectedPostfix, String... expectedParts) {
        List<String> currentParts = new ArrayList<>();
        List<String> remainingParts = new ArrayList<>(Arrays.asList(expectedParts));
        List<Matcher<? super String>> matchers = toStringMatchers(expectedPrefix, expectedPostfix, currentParts, remainingParts)
                .collect(toList());

        return anyOf(matchers);
    }

    private static Stream<Matcher<String>> toStringMatchers(String expectedPrefix, String expectedPostfix,
            List<String> currentParts, List<String> remainingParts) {

        if (remainingParts.isEmpty()) {
            String toStringValue = currentParts.stream()
                    .collect(joining(", ", expectedPrefix, expectedPostfix));
            return Stream.of(is(toStringValue));
        }

        return IntStream.range(0, remainingParts.size())
                .mapToObj(i -> i)
                .flatMap(i -> {
                    List<String> newParts = new ArrayList<>(currentParts);
                    newParts.add(remainingParts.get(i));
                    List<String> newRemainingParts = new ArrayList<>(remainingParts);
                    newRemainingParts.remove(i.intValue());
                    return toStringMatchers(expectedPrefix, expectedPostfix, newParts, newRemainingParts);
                });
    }
}
