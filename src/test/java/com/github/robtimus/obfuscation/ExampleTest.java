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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Verifies that the examples are correct.
 */
@SuppressWarnings({ "javadoc", "nls" })
public class ExampleTest {

    @Test
    @DisplayName("Obfuscating all characters")
    public void testAll() {
        CharSequence obfuscated = Obfuscator.all().obfuscateText("Hello World");
        assertEquals("***********", obfuscated.toString());
    }

    @Test
    @DisplayName("Obfuscating with a fixed length")
    public void testFixedLength() {
        CharSequence obfuscated = Obfuscator.fixedLength(5).obfuscateText("Hello World");
        assertEquals("*****", obfuscated.toString());
    }

    @Test
    @DisplayName("Obfuscating with a fixed value")
    public void testFixedValue() {
        CharSequence obfuscated = Obfuscator.fixedValue("foo").obfuscateText("Hello World");
        assertEquals("foo", obfuscated.toString());
    }

    @Nested
    @DisplayName("Obfuscating portions of text")
    public class Portions {

        @Nested
        @DisplayName("Obfuscating all but the last 4 characters")
        public class AllButLast4 {

            @Test
            @DisplayName("without minimum obfuscation")
            public void testAllButLast4() {
                CharSequence obfuscated = Obfuscator.portion()
                        .keepAtEnd(4)
                        .build()
                        .obfuscateText("1234567890123456");
                assertEquals("************3456", obfuscated.toString());
            }

            @Test
            @DisplayName("obfuscate minimum of 12")
            public void testAllButLast4AtLeast12() {
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
        public void testLast2() {
            CharSequence obfuscated = Obfuscator.portion()
                    .keepAtStart(Integer.MAX_VALUE)
                    .atLeastFromEnd(2)
                    .build()
                    .obfuscateText("SW1A 2AA");
            assertEquals("SW1A 2**", obfuscated.toString());
        }

        @Test
        @DisplayName("Using a fixed length")
        public void testFixedLength() {
            Obfuscator obfuscator = Obfuscator.portion()
                    .keepAtStart(2)
                    .keepAtEnd(2)
                    .withFixedLength(3)
                    .build();
            CharSequence obfuscated = obfuscator.obfuscateText("Hello World");
            assertEquals("He***ld", obfuscated.toString());

            obfuscated = obfuscator.obfuscateText("foo");
            assertEquals("fo***o", obfuscated.toString());
        }
    }

    @Nested
    @DisplayName("From function")
    public class FromFunction {

        @Test
        @DisplayName("method takes CharSequence")
        public void testCharSequenceArgument() {
            Obfuscator obfuscator = Obfuscator.fromFunction(this::obfuscate);
            CharSequence obfuscated = obfuscator.obfuscateText("Hello World");
            assertEquals("Hello World as CharSequence", obfuscated);
        }

        @Test
        @DisplayName("method takes String")
        public void testCharStringArgument() {
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
    public class Objects {

        @Test
        @DisplayName("obfuscated")
        public void testObfuscated() {
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
        public void testCached() {
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
    public class Collections {

        @Test
        @DisplayName("list")
        public void testList() {
            List<String> list = new ArrayList<>();
            list.add("hello");
            list.add("world");
            list = Obfuscator.fixedLength(3).obfuscateList(list);
            assertEquals("[***, ***]", list.toString());
        }

        @Test
        @DisplayName("set")
        public void testSet() {
            Set<String> set = new HashSet<>();
            set.add("hello");
            set.add("world");
            set = Obfuscator.fixedLength(3).obfuscateSet(set);
            assertEquals("[***, ***]", set.toString());
        }

        @Test
        @DisplayName("collection")
        public void testCollection() {
            Collection<String> collection = new ArrayList<>();
            collection.add("hello");
            collection.add("world");
            collection = Obfuscator.fixedLength(3).obfuscateCollection(collection);
            assertEquals("[***, ***]", collection.toString());
        }
    }

    @Nested
    @DisplayName("Obfuscating maps")
    public class Maps {

        @Test
        @DisplayName("obfuscate each value the same way")
        public void testObfuscateMap() {
            Map<String, String> map = new LinkedHashMap<>();
            map.put("username", "admin");
            map.put("password", "hello");
            map = Obfuscator.fixedLength(3).obfuscateMap(map);
            assertEquals("{username=***, password=***}", map.toString());
        }

        @Test
        @DisplayName("obfuscate each entry separately")
        public void testMapObfuscator() {
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

    @Test
    @DisplayName("Obfuscating request parameters")
    public void testRequestParameters() {
        CharSequence obfuscated = RequestParameterObfuscator.builder()
                .withParameter("password", Obfuscator.fixedLength(3))
                .build()
                .obfuscateText("username=admin&password=hello");
        assertEquals("username=admin&password=***", obfuscated.toString());
    }

    @Test
    @DisplayName("Streaming obfuscation")
    public void testStreaming() throws IOException {
        StringWriter writer = new StringWriter();

        Obfuscator obfuscator = RequestParameterObfuscator.builder()
                .withParameter("password", Obfuscator.fixedLength(3))
                .build();
        try (Writer obfuscatingWriter = obfuscator.streamTo(writer)) {
            obfuscatingWriter.write("username=admin");
            obfuscatingWriter.write("&password=hello");
        }
        assertEquals("username=admin&password=***", writer.toString());
    }
}
