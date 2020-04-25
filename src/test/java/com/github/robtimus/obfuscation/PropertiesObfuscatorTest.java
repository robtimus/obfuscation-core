/*
 * PropertiesObfuscatorTest.java
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

import static com.github.robtimus.obfuscation.ObfuscatingPropertiesTest.assertHasToString;
import static com.github.robtimus.obfuscation.ObfuscatingPropertiesTest.newProperties;
import static com.github.robtimus.obfuscation.Obfuscator.portion;
import static com.github.robtimus.obfuscation.PropertiesObfuscator.builder;
import static com.github.robtimus.obfuscation.support.CaseSensitivity.CASE_INSENSITIVE;
import static com.github.robtimus.obfuscation.support.CaseSensitivity.CASE_SENSITIVE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import com.github.robtimus.obfuscation.PropertiesObfuscator.Builder;

@SuppressWarnings({ "javadoc", "nls" })
@TestInstance(Lifecycle.PER_CLASS)
public class PropertiesObfuscatorTest {

    @Nested
    @DisplayName("obfuscateProperties(Properties)")
    public class ObfuscateProperties {

        @Nested
        @DisplayName("caseSensitiveByDefault()")
        public class CaseSensitiveByDefault {

            @Test
            @DisplayName("toString()")
            public void testToString() {
                Properties properties = newProperties("foo", "foo", "FOO", "FOO", "bar", "bar", "BAR", "BAR", "baz", "baz", "BAZ", "BAZ");
                Properties obfuscating = createObfuscator(builder().caseSensitiveByDefault())
                        .obfuscateProperties(properties);
                // foo and bar are case sensitive, baz is case insensitive

                assertHasToString(obfuscating, "{", "}", "foo=f***o", "FOO=FOO", "bar=b***r", "BAR=BAR", "baz=b***z", "BAZ=B***Z");
            }

            @Test
            @DisplayName("list(PrintStream)")
            public void testListToPrintStream() throws IOException {
                Properties properties = newProperties("foo", "foo", "FOO", "FOO", "bar", "bar", "BAR", "BAR", "baz", "baz", "BAZ", "BAZ");
                Properties obfuscating = createObfuscator(builder().caseSensitiveByDefault())
                        .obfuscateProperties(properties);
                // foo and bar are case sensitive, baz is case insensitive

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                try (PrintStream printStream = new PrintStream(outputStream)) {
                    obfuscating.list(printStream);
                }

                String contents = outputStream.toString("UTF-8");
                assertThat(contents, containsString("foo=f***o" + System.lineSeparator()));
                assertThat(contents, containsString("FOO=FOO" + System.lineSeparator()));
                assertThat(contents, containsString("bar=b***r" + System.lineSeparator()));
                assertThat(contents, containsString("BAR=BAR" + System.lineSeparator()));
                assertThat(contents, containsString("baz=b***z" + System.lineSeparator()));
                assertThat(contents, containsString("BAZ=B***Z" + System.lineSeparator()));
            }

            @Test
            @DisplayName("list(PrintWriter)")
            public void testListToPrintWriter() {
                Properties properties = newProperties("foo", "foo", "FOO", "FOO", "bar", "bar", "BAR", "BAR", "baz", "baz", "BAZ", "BAZ");
                Properties obfuscating = createObfuscator(builder().caseSensitiveByDefault())
                        .obfuscateProperties(properties);
                // foo and bar are case sensitive, baz is case insensitive

                StringWriter writer = new StringWriter();
                try (PrintWriter printWriter = new PrintWriter(writer)) {
                    obfuscating.list(printWriter);
                }

                String contents = writer.toString();
                assertThat(contents, containsString("foo=f***o" + System.lineSeparator()));
                assertThat(contents, containsString("FOO=FOO" + System.lineSeparator()));
                assertThat(contents, containsString("bar=b***r" + System.lineSeparator()));
                assertThat(contents, containsString("BAR=BAR" + System.lineSeparator()));
                assertThat(contents, containsString("baz=b***z" + System.lineSeparator()));
                assertThat(contents, containsString("BAZ=B***Z" + System.lineSeparator()));
            }
        }

        @Nested
        @DisplayName("caseInsensitiveByDefault()")
        public class CaseInsensitiveByDefault {

            @Test
            @DisplayName("toString()")
            public void testToString() {
                Properties properties = newProperties("foo", "foo", "FOO", "FOO", "bar", "bar", "BAR", "BAR", "baz", "baz", "BAZ", "BAZ");
                Properties obfuscating = createObfuscator(builder().caseInsensitiveByDefault())
                        .obfuscateProperties(properties);
                // foo and baz are case insensitive, bar is case sensitive

                assertHasToString(obfuscating, "{", "}", "foo=f***o", "FOO=F***O", "bar=b***r", "BAR=BAR", "baz=b***z", "BAZ=B***Z");
            }

            @Test
            @DisplayName("list(PrintStream)")
            public void testListToPrintStream() throws IOException {
                Properties properties = newProperties("foo", "foo", "FOO", "FOO", "bar", "bar", "BAR", "BAR", "baz", "baz", "BAZ", "BAZ");
                Properties obfuscating = createObfuscator(builder().caseInsensitiveByDefault())
                        .obfuscateProperties(properties);
                // foo and baz are case insensitive, bar is case sensitive

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                try (PrintStream printStream = new PrintStream(outputStream)) {
                    obfuscating.list(printStream);
                }

                String contents = outputStream.toString("UTF-8");
                assertThat(contents, containsString("foo=f***o" + System.lineSeparator()));
                assertThat(contents, containsString("FOO=F***O" + System.lineSeparator()));
                assertThat(contents, containsString("bar=b***r" + System.lineSeparator()));
                assertThat(contents, containsString("BAR=BAR" + System.lineSeparator()));
                assertThat(contents, containsString("baz=b***z" + System.lineSeparator()));
                assertThat(contents, containsString("BAZ=B***Z" + System.lineSeparator()));
            }

            @Test
            @DisplayName("list(PrintWriter)")
            public void testListToPrintWriter() {
                Properties properties = newProperties("foo", "foo", "FOO", "FOO", "bar", "bar", "BAR", "BAR", "baz", "baz", "BAZ", "BAZ");
                Properties obfuscating = createObfuscator(builder().caseInsensitiveByDefault())
                        .obfuscateProperties(properties);
                // foo and baz are case insensitive, bar is case sensitive

                StringWriter writer = new StringWriter();
                try (PrintWriter printWriter = new PrintWriter(writer)) {
                    obfuscating.list(printWriter);
                }

                String contents = writer.toString();
                assertThat(contents, containsString("foo=f***o" + System.lineSeparator()));
                assertThat(contents, containsString("FOO=F***O" + System.lineSeparator()));
                assertThat(contents, containsString("bar=b***r" + System.lineSeparator()));
                assertThat(contents, containsString("BAR=BAR" + System.lineSeparator()));
                assertThat(contents, containsString("baz=b***z" + System.lineSeparator()));
                assertThat(contents, containsString("BAZ=B***Z" + System.lineSeparator()));
            }
        }
    }

    @Nested
    @DisplayName("Builder")
    public class BuilderTest {

        @Test
        @DisplayName("transform")
        public void testTransform() {
            Builder builder = builder();
            @SuppressWarnings("unchecked")
            Function<Builder, String> f = mock(Function.class);
            when(f.apply(builder)).thenReturn("result");

            assertEquals("result", builder.transform(f));
            verify(f).apply(builder);
            verifyNoMoreInteractions(f);
        }
    }

    private PropertiesObfuscator createObfuscator(Builder builder) {
        Obfuscator obfuscator = portion()
                .keepAtStart(1)
                .keepAtEnd(1)
                .withFixedLength(3)
                .build();
        return builder
                .withProperty("foo", obfuscator)
                .withProperty("bar", obfuscator, CASE_SENSITIVE)
                .withProperty("baz", obfuscator, CASE_INSENSITIVE)
                .build();
    }
}
