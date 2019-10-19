/*
 * RequestParameterObfuscatorTest.java
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
import static com.github.robtimus.obfuscation.Obfuscator.requestParameters;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@SuppressWarnings({ "javadoc", "nls" })
@TestInstance(Lifecycle.PER_CLASS)
public class RequestParameterObfuscatorTest {

    @Test
    @DisplayName("obfuscateText(CharSequence, int, int)")
    public void testObfuscateTextCharSequence() {
        String input = "xfoo=bar&hello=world&no-valuey";
        String expected = "foo=***&hello=world&no-value";

        Obfuscator obfuscator = createObfuscator();

        assertEquals(expected, obfuscator.obfuscateText(input + "&x=y", 1, input.length() - 1).toString());
        assertEquals("foo=**", obfuscator.obfuscateText(input, 1, 7).toString());
        assertEquals("foo", obfuscator.obfuscateText(input, 1, 4).toString());
    }

    @Test
    @DisplayName("obfuscateText(CharSequence, int, int, Appendable)")
    public void testObfuscateTextCharSequenceToAppendable() throws IOException {
        String input = "xfoo=bar&hello=world&no-valuey";
        String expected = "foo=***&hello=world&no-value";

        Obfuscator obfuscator = createObfuscator();

        StringBuilder destination = new StringBuilder();
        obfuscator.obfuscateText(input + "&x=y", 1, input.length() - 1, destination);
        assertEquals(expected, destination.toString());
    }

    @Test
    @DisplayName("obfuscateText(Reader, Appendable)")
    public void testObfuscateTextReaderToAppendable() throws IOException {
        String input = "foo=bar&hello=world&no-value";
        String expected = "foo=***&hello=world&no-value";

        Obfuscator obfuscator = createObfuscator();

        StringBuilder destination = new StringBuilder();
        obfuscator.obfuscateText(new StringReader(input), destination);
        assertEquals(expected, destination.toString());

        destination.delete(0, destination.length());
        obfuscator.obfuscateText(new BufferedReader(new StringReader(input)), destination);
        assertEquals(expected, destination.toString());
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    @DisplayName("streamTo(Appendable)")
    public class StreamTo {

        @Test
        @DisplayName("write(int)")
        public void testWriteInt() throws IOException {
            Obfuscator obfuscator = createObfuscator();

            String input = "foo=bar&hello=world&no-value";
            String expected = "foo=***&hello=world&no-value";

            Writer writer = new StringWriter();
            try (Writer w = obfuscator.streamTo(writer)) {
                for (int i = 0; i < input.length(); i++) {
                    w.write(input.charAt(i));
                }
            }
            assertEquals(expected, writer.toString());

            IOException exception = assertThrows(IOException.class, () -> {
                @SuppressWarnings("resource")
                Writer w = obfuscator.streamTo(writer);
                w.close();
                w.write('x');
            });
            assertEquals(Messages.stream.closed.get(), exception.getMessage());

            StringBuilder sb = new StringBuilder();
            try (Writer w = obfuscator.streamTo(sb)) {
                for (int i = 0; i < input.length(); i++) {
                    w.write(input.charAt(i));
                }
            }
            assertEquals(expected, sb.toString());

            exception = assertThrows(IOException.class, () -> {
                @SuppressWarnings("resource")
                Writer w = obfuscator.streamTo(sb);
                w.close();
                w.write('x');
            });
            assertEquals(Messages.stream.closed.get(), exception.getMessage());
        }

        @Test
        @DisplayName("write(char[])")
        public void testWriteCharArray() throws IOException {
            Obfuscator obfuscator = createObfuscator();

            String input = "foo=bar&hello=world&no-value";
            String expected = "foo=***&hello=world&no-value";

            Writer writer = new StringWriter();
            try (Writer w = obfuscator.streamTo(writer)) {
                w.write(input.toCharArray());
            }
            assertEquals(expected, writer.toString());

            IOException exception = assertThrows(IOException.class, () -> {
                @SuppressWarnings("resource")
                Writer w = obfuscator.streamTo(writer);
                w.close();
                w.write(input.toCharArray());
            });
            assertEquals(Messages.stream.closed.get(), exception.getMessage());

            StringBuilder sb = new StringBuilder();
            try (Writer w = obfuscator.streamTo(sb)) {
                w.write(input.toCharArray());
            }
            assertEquals(expected, sb.toString());

            exception = assertThrows(IOException.class, () -> {
                @SuppressWarnings("resource")
                Writer w = obfuscator.streamTo(sb);
                w.close();
                w.write(input.toCharArray());
            });
            assertEquals(Messages.stream.closed.get(), exception.getMessage());
        }

        @Test
        @DisplayName("write(char[], int, int)")
        public void testWriteCharArrayRange() throws IOException {
            Obfuscator obfuscator = createObfuscator();

            String input = "foo=bar&hello=world&no-value";
            String expected = "foo=***&hello=world&no-value";

            Writer writer = new StringWriter();
            try (Writer w = obfuscator.streamTo(writer)) {
                char[] content = input.toCharArray();
                int index = 0;
                while (index < input.length()) {
                    int to = Math.min(index + 5, input.length());
                    w.write(content, index, to - index);
                    index = to;
                }

                assertThrows(IndexOutOfBoundsException.class, () -> w.write(content, 0, content.length + 1));
                assertThrows(IndexOutOfBoundsException.class, () -> w.write(content, -1, content.length));
                assertThrows(IndexOutOfBoundsException.class, () -> w.write(content, 1, content.length));
                assertThrows(IndexOutOfBoundsException.class, () -> w.write(content, 0, -1));
            }
            assertEquals(expected, writer.toString());

            IOException exception = assertThrows(IOException.class, () -> {
                @SuppressWarnings("resource")
                Writer w = obfuscator.streamTo(writer);
                w.close();
                w.write(input.toCharArray(), 0, 1);
            });
            assertEquals(Messages.stream.closed.get(), exception.getMessage());

            StringBuilder sb = new StringBuilder();
            try (Writer w = obfuscator.streamTo(sb)) {
                char[] content = input.toCharArray();
                int index = 0;
                while (index < input.length()) {
                    int to = Math.min(index + 5, input.length());
                    w.write(content, index, to - index);
                    index = to;
                }

                assertThrows(IndexOutOfBoundsException.class, () -> w.write(content, 0, content.length + 1));
                assertThrows(IndexOutOfBoundsException.class, () -> w.write(content, -1, content.length));
                assertThrows(IndexOutOfBoundsException.class, () -> w.write(content, 1, content.length));
                assertThrows(IndexOutOfBoundsException.class, () -> w.write(content, 0, -1));
            }
            assertEquals(expected, sb.toString());

            exception = assertThrows(IOException.class, () -> {
                @SuppressWarnings("resource")
                Writer w = obfuscator.streamTo(sb);
                w.close();
                w.write(input.toCharArray(), 0, 1);
            });
            assertEquals(Messages.stream.closed.get(), exception.getMessage());
        }

        @Test
        @DisplayName("write(String)")
        public void testWriteString() throws IOException {
            Obfuscator obfuscator = createObfuscator();

            String input = "foo=bar&hello=world&no-value";
            String expected = "foo=***&hello=world&no-value";

            Writer writer = new StringWriter();
            try (Writer w = obfuscator.streamTo(writer)) {
                w.write(input);
            }
            assertEquals(expected, writer.toString());

            IOException exception = assertThrows(IOException.class, () -> {
                @SuppressWarnings("resource")
                Writer w = obfuscator.streamTo(writer);
                w.close();
                w.write(input);
            });
            assertEquals(Messages.stream.closed.get(), exception.getMessage());

            StringBuilder sb = new StringBuilder();
            try (Writer w = obfuscator.streamTo(sb)) {
                w.write(input);
            }
            assertEquals(expected, sb.toString());

            exception = assertThrows(IOException.class, () -> {
                @SuppressWarnings("resource")
                Writer w = obfuscator.streamTo(sb);
                w.close();
                w.write(input);
            });
            assertEquals(Messages.stream.closed.get(), exception.getMessage());
        }

        @Test
        @DisplayName("write(String, int, int)")
        public void testWriteStringRange() throws IOException {
            Obfuscator obfuscator = createObfuscator();

            String input = "foo=bar&hello=world&no-value";
            String expected = "foo=***&hello=world&no-value";

            Writer writer = new StringWriter();
            try (Writer w = obfuscator.streamTo(writer)) {
                int index = 0;
                while (index < input.length()) {
                    int to = Math.min(index + 5, input.length());
                    w.write(input, index, to - index);
                    index = to;
                }
            }
            assertEquals(expected, writer.toString());

            IOException exception = assertThrows(IOException.class, () -> {
                @SuppressWarnings("resource")
                Writer w = obfuscator.streamTo(writer);
                w.close();
                w.write(input, 0, 1);
            });
            assertEquals(Messages.stream.closed.get(), exception.getMessage());

            StringBuilder sb = new StringBuilder();
            try (Writer w = obfuscator.streamTo(sb)) {
                int index = 0;
                while (index < input.length()) {
                    int to = Math.min(index + 5, input.length());
                    w.write(input, index, to - index);
                    index = to;
                }
            }
            assertEquals(expected, sb.toString());

            exception = assertThrows(IOException.class, () -> {
                @SuppressWarnings("resource")
                Writer w = obfuscator.streamTo(sb);
                w.close();
                w.write(input, 0, 1);
            });
            assertEquals(Messages.stream.closed.get(), exception.getMessage());
        }

        @Test
        @DisplayName("append(CharSequence)")
        public void testAppendCharSequence() throws IOException {
            Obfuscator obfuscator = createObfuscator();

            String input = "foo=bar&hello=world&no-value";
            String expected = "foo=***&hello=world&no-value";

            Writer writer = new StringWriter();
            try (Writer w = obfuscator.streamTo(writer)) {
                w.append(input);
            }
            assertEquals(expected, writer.toString());

            IOException exception = assertThrows(IOException.class, () -> {
                @SuppressWarnings("resource")
                Writer w = obfuscator.streamTo(writer);
                w.close();
                w.append(input);
            });
            assertEquals(Messages.stream.closed.get(), exception.getMessage());

            StringBuilder sb = new StringBuilder();
            try (Writer w = obfuscator.streamTo(sb)) {
                w.append(input);
            }
            assertEquals(expected, sb.toString());

            exception = assertThrows(IOException.class, () -> {
                @SuppressWarnings("resource")
                Writer w = obfuscator.streamTo(writer);
                w.close();
                w.append(input);
            });
            assertEquals(Messages.stream.closed.get(), exception.getMessage());
        }

        @Test
        @DisplayName("append(CharSequence, int, int)")
        public void testAppendCharSequenceRange() throws IOException {
            Obfuscator obfuscator = createObfuscator();

            String input = "foo=bar&hello=world&no-value";
            String expected = "foo=***&hello=world&no-value";

            Writer writer = new StringWriter();
            try (Writer w = obfuscator.streamTo(writer)) {
                int index = 0;
                while (index < input.length()) {
                    int to = Math.min(index + 5, input.length());
                    w.append(input, index, to);
                    index = to;
                }

                assertThrows(IndexOutOfBoundsException.class, () -> w.write(input, 0, input.length() + 1));
                assertThrows(IndexOutOfBoundsException.class, () -> w.write(input, -1, input.length()));
                assertThrows(IndexOutOfBoundsException.class, () -> w.write(input, 1, input.length()));
                assertThrows(IndexOutOfBoundsException.class, () -> w.write(input, 0, -1));
            }
            assertEquals(expected, writer.toString());

            IOException exception = assertThrows(IOException.class, () -> {
                @SuppressWarnings("resource")
                Writer w = obfuscator.streamTo(writer);
                w.close();
                w.append(input, 0, 1);
            });
            assertEquals(Messages.stream.closed.get(), exception.getMessage());

            StringBuilder sb = new StringBuilder();
            try (Writer w = obfuscator.streamTo(sb)) {
                int index = 0;
                while (index < input.length()) {
                    int to = Math.min(index + 5, input.length());
                    w.append(input, index, to);
                    index = to;
                }

                assertThrows(IndexOutOfBoundsException.class, () -> w.write(input, 0, input.length() + 1));
                assertThrows(IndexOutOfBoundsException.class, () -> w.write(input, -1, input.length()));
                assertThrows(IndexOutOfBoundsException.class, () -> w.write(input, 1, input.length()));
                assertThrows(IndexOutOfBoundsException.class, () -> w.write(input, 0, -1));
            }
            assertEquals(expected, sb.toString());

            exception = assertThrows(IOException.class, () -> {
                @SuppressWarnings("resource")
                Writer w = obfuscator.streamTo(writer);
                w.close();
                w.append(input, 0, 1);
            });
            assertEquals(Messages.stream.closed.get(), exception.getMessage());
        }

        @Test
        @DisplayName("append(char)")
        public void testAppendChar() throws IOException {
            Obfuscator obfuscator = createObfuscator();

            String input = "foo=bar&hello=world&no-value";
            String expected = "foo=***&hello=world&no-value";

            Writer writer = new StringWriter();
            try (Writer w = obfuscator.streamTo(writer)) {
                for (int i = 0; i < input.length(); i++) {
                    w.append(input.charAt(i));
                }
            }
            assertEquals(expected, writer.toString());

            IOException exception = assertThrows(IOException.class, () -> {
                @SuppressWarnings("resource")
                Writer w = obfuscator.streamTo(writer);
                w.close();
                w.append('x');
            });
            assertEquals(Messages.stream.closed.get(), exception.getMessage());

            StringBuilder sb = new StringBuilder();
            try (Writer w = obfuscator.streamTo(sb)) {
                for (int i = 0; i < input.length(); i++) {
                    w.append(input.charAt(i));
                }
            }
            assertEquals(expected, sb.toString());

            exception = assertThrows(IOException.class, () -> {
                @SuppressWarnings("resource")
                Writer w = obfuscator.streamTo(sb);
                w.close();
                w.append('x');
            });
            assertEquals(Messages.stream.closed.get(), exception.getMessage());
        }

        @Test
        public void testFlush() throws IOException {
            Obfuscator obfuscator = createObfuscator();

            Writer writer = new StringWriter();
            try (Writer w = obfuscator.streamTo(writer)) {
                w.flush();
            }

            IOException exception = assertThrows(IOException.class, () -> {
                @SuppressWarnings("resource")
                Writer w = obfuscator.streamTo(writer);
                w.close();
                w.flush();
            });
            assertEquals(Messages.stream.closed.get(), exception.getMessage());

            StringBuilder sb = new StringBuilder();
            try (Writer w = obfuscator.streamTo(sb)) {
                w.flush();
            }

            exception = assertThrows(IOException.class, () -> {
                @SuppressWarnings("resource")
                Writer w = obfuscator.streamTo(sb);
                w.close();
                w.flush();
            });
            assertEquals(Messages.stream.closed.get(), exception.getMessage());
        }
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource
    @DisplayName("equals(Object)")
    public void testEquals(Obfuscator obfuscator, Object object, boolean expected) {
        assertEquals(expected, obfuscator.equals(object));
    }

    Arguments[] testEquals() {
        Obfuscator obfuscator = createObfuscator();
        return new Arguments[] {
                arguments(obfuscator, obfuscator, true),
                arguments(obfuscator, null, false),
                arguments(obfuscator, createObfuscator(), true),
                arguments(obfuscator, requestParameters().build(), false),
                arguments(obfuscator, createObfuscator(StandardCharsets.US_ASCII), false),
                arguments(obfuscator, "foo", false),
        };
    }

    @Test
    @DisplayName("hashCode()")
    public void testHashCode() {
        Obfuscator obfuscator = createObfuscator();
        assertEquals(obfuscator.hashCode(), obfuscator.hashCode());
        assertEquals(obfuscator.hashCode(), createObfuscator().hashCode());
    }

    private Obfuscator createObfuscator() {
        return createObfuscator(requestParameters());
    }

    private Obfuscator createObfuscator(Charset encoding) {
        return createObfuscator(requestParameters(encoding));
    }

    private Obfuscator createObfuscator(PropertyObfuscatorBuilder builder) {
        Obfuscator obfuscator = all();
        return builder
                .withProperty("foo", obfuscator)
                .withProperty("no-value", obfuscator)
                .build();
    }
}
