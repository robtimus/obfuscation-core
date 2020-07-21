/*
 * ExampleTest.java
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Verifies that the examples are correct.
 */
@SuppressWarnings("nls")
class ExampleTest {

    @Test
    @DisplayName("Obfuscating all characters")
    void testAll() {
        CharSequence obfuscated = Obfuscator.all().obfuscateText("Hello World");
        assertEquals("***********", obfuscated.toString());
    }

    @Test
    @DisplayName("Obfuscating with a fixed length")
    void testFixedLength() {
        CharSequence obfuscated = Obfuscator.fixedLength(5).obfuscateText("Hello World");
        assertEquals("*****", obfuscated.toString());
    }

    @Test
    @DisplayName("Obfuscating with a fixed value")
    void testFixedValue() {
        CharSequence obfuscated = Obfuscator.fixedValue("foo").obfuscateText("Hello World");
        assertEquals("foo", obfuscated.toString());
    }

    @Nested
    @DisplayName("Obfuscating portions of text")
    class Portions {

        @Nested
        @DisplayName("Obfuscating all but the last 4 characters")
        class AllButLast4 {

            @Test
            @DisplayName("without minimum obfuscation")
            void testAllButLast4() {
                CharSequence obfuscated = Obfuscator.portion()
                        .keepAtEnd(4)
                        .build()
                        .obfuscateText("1234567890123456");
                assertEquals("************3456", obfuscated.toString());
            }

            @Test
            @DisplayName("obfuscate minimum of 12")
            void testAllButLast4AtLeast12() {
                CharSequence obfuscated = Obfuscator.portion()
                        .keepAtEnd(4)
                        .atLeastFromStart(12)
                        .build()
                        .obfuscateText("1234567890");
                assertEquals("**********", obfuscated.toString());
            }
        }

        @Test
        @DisplayName("Obfuscating only the last 2 characters")
        void testLast2() {
            CharSequence obfuscated = Obfuscator.portion()
                    .keepAtStart(Integer.MAX_VALUE)
                    .atLeastFromEnd(2)
                    .build()
                    .obfuscateText("SW1A 2AA");
            assertEquals("SW1A 2**", obfuscated.toString());
        }

        @Test
        @DisplayName("Using a fixed length")
        void testFixedLength() {
            Obfuscator obfuscator = Obfuscator.portion()
                    .keepAtStart(2)
                    .keepAtEnd(2)
                    .withFixedTotalLength(6)
                    .build();
            CharSequence obfuscated = obfuscator.obfuscateText("Hello World");
            assertEquals("He**ld", obfuscated.toString());

            obfuscated = obfuscator.obfuscateText("foo");
            assertEquals("fo**oo", obfuscated.toString());
        }
    }

    @Nested
    @DisplayName("From function")
    class FromFunction {

        @Test
        @DisplayName("method takes CharSequence")
        void testCharSequenceArgument() {
            Obfuscator obfuscator = Obfuscator.fromFunction(this::obfuscate);
            CharSequence obfuscated = obfuscator.obfuscateText("Hello World");
            assertEquals("Hello World as CharSequence", obfuscated);
        }

        @Test
        @DisplayName("method takes String")
        void testCharStringArgument() {
            Obfuscator obfuscator = Obfuscator.fromFunction(s -> obfuscate(s.toString()));
            CharSequence obfuscated = obfuscator.obfuscateText("Hello World");
            assertEquals("Hello World as String", obfuscated);
        }

        private CharSequence obfuscate(CharSequence s) {
            return s + " as CharSequence";
        }

        private CharSequence obfuscate(String s) {
            return s + " as String";
        }
    }

    @Nested
    @DisplayName("Obfuscating objects")
    class Objects {

        @Test
        @DisplayName("obfuscated")
        void testObfuscated() {
            LocalDate date = LocalDate.of(2020, 1, 1);
            Obfuscated<LocalDate> obfuscated = Obfuscator.portion()
                    .keepAtStart(8)
                    .build()
                    .obfuscateObject(date);
            assertEquals("2020-01-**", obfuscated.toString());
            assertSame(date, obfuscated.value());
        }

        @Test
        @DisplayName("cached")
        void testCached() {
            LocalDate date = LocalDate.of(2020, 1, 1);
            Obfuscated<LocalDate> obfuscated = Obfuscator.portion()
                    .keepAtStart(8)
                    .build()
                    .obfuscateObject(date)
                    .cached();
            assertEquals("2020-01-**", obfuscated.toString());
            assertSame(obfuscated.toString(), obfuscated.toString());
        }
    }

    @Nested
    @DisplayName("Obfuscating collections")
    class Collections {

        @Test
        @DisplayName("list")
        void testList() {
            List<String> list = new ArrayList<>();
            list.add("hello");
            list.add("world");
            list = Obfuscator.fixedLength(3).obfuscateList(list);
            assertEquals("[***, ***]", list.toString());
        }

        @Test
        @DisplayName("set")
        void testSet() {
            Set<String> set = new HashSet<>();
            set.add("hello");
            set.add("world");
            set = Obfuscator.fixedLength(3).obfuscateSet(set);
            assertEquals("[***, ***]", set.toString());
        }

        @Test
        @DisplayName("collection")
        void testCollection() {
            Collection<String> collection = new ArrayList<>();
            collection.add("hello");
            collection.add("world");
            collection = Obfuscator.fixedLength(3).obfuscateCollection(collection);
            assertEquals("[***, ***]", collection.toString());
        }
    }

    @Nested
    @DisplayName("Obfuscating maps")
    class Maps {

        @Test
        @DisplayName("obfuscate each value the same way")
        void testObfuscateMap() {
            Map<String, String> map = new LinkedHashMap<>();
            map.put("username", "admin");
            map.put("password", "hello");
            map = Obfuscator.fixedLength(3).obfuscateMap(map);
            assertEquals("{username=***, password=***}", map.toString());
        }

        @Test
        @DisplayName("obfuscate each entry separately")
        void testMapObfuscator() {
            Map<String, String> map = new LinkedHashMap<>();
            map.put("username", "admin");
            map.put("password", "hello");
            map = MapObfuscator.<String, String>builder()
                    .withKey("password", Obfuscator.fixedLength(3))
                    .build()
                    .obfuscateMap(map);
            assertEquals("{username=admin, password=***}", map.toString());
        }
    }

    @Nested
    @DisplayName("Obfuscating Properties objects")
    class PropertiesObjects {

        @Test
        @DisplayName("toString")
        void testToString() {
            Properties properties = new Properties();
            properties.put("username", "admin");
            properties.put("password", "hello");
            properties = PropertiesObfuscator.builder()
                    .withProperty("password", Obfuscator.fixedLength(3))
                    .build()
                    .obfuscateProperties(properties);
            assertThat(properties.toString(), either(is("{username=admin, password=***}")).or(is("{password=***, username=admin}")));
        }

        @Test
        @DisplayName("list")
        void testList() {
            Properties properties = new Properties();
            properties.put("username", "admin");
            properties.put("password", "hello");
            properties = PropertiesObfuscator.builder()
                    .withProperty("password", Obfuscator.fixedLength(3))
                    .build()
                    .obfuscateProperties(properties);
            StringWriter writer = new StringWriter();
            try (PrintWriter printWriter = new PrintWriter(writer)) {
                properties.list(printWriter);
            }
            assertThat(writer.toString(), containsString("username=admin"));
            assertThat(writer.toString(), containsString("password=***"));
        }
    }

    @Test
    @DisplayName("Streaming obfuscation")
    void testStreaming() throws IOException {
        StringWriter writer = new StringWriter();

        Obfuscator obfuscator = Obfuscator.portion()
                .keepAtStart(24)
                .withFixedTotalLength(27)
                .build();
        try (Writer obfuscatingWriter = obfuscator.streamTo(writer)) {
            obfuscatingWriter.write("username=admin");
            obfuscatingWriter.write("&password=hello");
        }
        assertEquals("username=admin&password=***", writer.toString());
    }
}
