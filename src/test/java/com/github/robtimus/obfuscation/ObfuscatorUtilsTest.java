/*
 * ObfuscatorUtilsTest.java
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

import static com.github.robtimus.obfuscation.ObfuscatorUtils.append;
import static com.github.robtimus.obfuscation.ObfuscatorUtils.indexOf;
import static com.github.robtimus.obfuscation.ObfuscatorUtils.repeatChar;
import static com.github.robtimus.obfuscation.ObfuscatorUtils.skipLeadingWhitespace;
import static com.github.robtimus.obfuscation.ObfuscatorUtils.skipTrailingWhitespace;
import static com.github.robtimus.obfuscation.ObfuscatorUtils.wrapArray;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@SuppressWarnings({ "javadoc", "nls" })
@TestInstance(Lifecycle.PER_CLASS)
public class ObfuscatorUtilsTest {

    @ParameterizedTest(name = "{1} in {0}[{2}, {3})")
    @MethodSource
    @DisplayName("indexOf(CharSequence, int, int, int)")
    public void testIndexOf(String s, int ch, int fromIndex, int toIndex, int expected) {
        assertEquals(expected, indexOf(s, ch, fromIndex, toIndex));
        assertEquals(expected, indexOf(new StringBuilder(s), ch, fromIndex, toIndex));
    }

    Arguments[] testIndexOf() {
        return new Arguments[] {
                arguments("hello", 'l', -1, 10, 2),
                arguments("hello", 'l', 0, 5, 2),
                arguments("hello", 'l', 3, 5, 3),
                arguments("hello", 'l', 0, 2, -1),
                arguments("hello", 'x', -1, 10, -1),
                arguments("hello", 'x', 0, 5, -1),
                arguments("hello", 'x', 3, 5, -1),
                arguments("hello", 'x', 0, 2, -1),
        };
    }

    @ParameterizedTest(name = "{0}[{1}, {2})")
    @MethodSource
    @DisplayName("skipLeadingWhitespace(CharSequence, int, int)")
    public void testSkipLeadingWhitespace(CharSequence s, int fromIndex, int toIndex, int expected) {
        assertEquals(expected, skipLeadingWhitespace(s, fromIndex, toIndex));
    }

    Arguments[] testSkipLeadingWhitespace() {
        return new Arguments[] {
                arguments("hello world", 0, 11, 0),
                arguments("hello world", 4, 11, 4),
                arguments("hello world", 5, 11, 6),
                arguments("hello world", 5, 6, 6),
        };
    }

    @ParameterizedTest(name = "{0}[{1}, {2})")
    @MethodSource
    @DisplayName("skipTrailingWhitespace(CharSequence, int, int)")
    public void testSkipTrailingWhitespace(CharSequence s, int fromIndex, int toIndex, int expected) {
        assertEquals(expected, skipTrailingWhitespace(s, fromIndex, toIndex));
    }

    Arguments[] testSkipTrailingWhitespace() {
        return new Arguments[] {
                arguments("hello world", 0, 11, 11),
                arguments("hello world", 0, 7, 7),
                arguments("hello world", 0, 6, 5),
                arguments("hello world", 5, 6, 5),
        };
    }

    @Test
    @DisplayName("wrapArray(null)")
    public void testWrapArrayNull() {
        assertThrows(NullPointerException.class, () -> wrapArray(null));
    }

    @Test
    @DisplayName("repeatChar(-1)")
    public void testRepeatCharNegativeCount() {
        assertThrows(IllegalArgumentException.class, () -> repeatChar('*', -1));
    }

    @Test
    @DisplayName("append(int)")
    public void testAppendInt() throws IOException {
        int c = 1 << 16 | 'a';

        Writer writer = new StringWriter();
        append(c, writer);
        assertEquals("a", writer.toString());

        StringBuilder sb = new StringBuilder();
        append(c, sb);
        assertEquals("a", sb.toString());
    }

    @Test
    @DisplayName("append(char, int)")
    public void testAppendRepeated() throws IOException {
        char c = '*';
        int count = 2048 + 32;

        char[] array = new char[count];
        Arrays.fill(array, c);
        String expected = new String(array);

        Writer writer = new StringWriter();
        append(c, 0, writer);
        assertEquals("", writer.toString());
        append(c, count, writer);
        assertEquals(expected, writer.toString());

        StringBuilder sb = new StringBuilder();
        append(c, 0, sb);
        assertEquals("", sb.toString());
        append(c, count, sb);
        assertEquals(expected, sb.toString());

        assertThrows(IllegalArgumentException.class, () -> append(c, -1, writer));
        assertThrows(IllegalArgumentException.class, () -> append(c, -1, sb));
    }

    @Test
    @DisplayName("append(char[])")
    public void testAppendCharArray() throws IOException {
        String input = "hello world";
        char[] array = input.toCharArray();

        Writer writer = new StringWriter();
        append(array, writer);
        assertEquals(input, writer.toString());

        StringBuilder sb = new StringBuilder();
        append(array, sb);
        assertEquals(input, sb.toString());

        assertThrows(NullPointerException.class, () -> append((char[]) null, writer));
        assertThrows(NullPointerException.class, () -> append((char[]) null, sb));
    }

    @Test
    @DisplayName("append(char[], int, int)")
    public void testAppendCharArrayRange() throws IOException {
        String input = "hello world";
        char[] array = input.toCharArray();

        Writer writer = new StringWriter();
        append(array, 5, 5, writer);
        assertEquals("", writer.toString());
        append(array, 3, 8, writer);
        assertEquals(input.substring(3, 8), writer.toString());

        StringBuilder sb = new StringBuilder();
        append(array, 5, 5, sb);
        assertEquals("", sb.toString());
        append(array, 3, 8, sb);
        assertEquals(input.substring(3, 8), sb.toString());

        assertThrows(NullPointerException.class, () -> append((char[]) null, 0, 0, writer));
        assertThrows(NullPointerException.class, () -> append((char[]) null, 0, 0, sb));

        assertThrows(IndexOutOfBoundsException.class, () -> append(array, -1, array.length, writer));
        assertThrows(IndexOutOfBoundsException.class, () -> append(array, 0, array.length + 1, writer));
        assertThrows(IndexOutOfBoundsException.class, () -> append(array, 0, -1, writer));
        assertThrows(IndexOutOfBoundsException.class, () -> append(array, array.length + 1, array.length, writer));
        assertThrows(IndexOutOfBoundsException.class, () -> append(array, -1, array.length, sb));
        assertThrows(IndexOutOfBoundsException.class, () -> append(array, 0, array.length + 1, sb));
        assertThrows(IndexOutOfBoundsException.class, () -> append(array, 0, -1, sb));
        assertThrows(IndexOutOfBoundsException.class, () -> append(array, array.length + 1, array.length, sb));
    }

    @Test
    @DisplayName("append(String)")
    public void testAppendString() throws IOException {
        String input = "hello world";

        Writer writer = new StringWriter();
        append(input, writer);
        assertEquals(input, writer.toString());

        StringBuilder sb = new StringBuilder();
        append(input, sb);
        assertEquals(input, sb.toString());

        assertThrows(NullPointerException.class, () -> append((String) null, writer));
        assertThrows(NullPointerException.class, () -> append((String) null, sb));
    }

    @Test
    @DisplayName("append(String, int, int)")
    public void testAppendStringRange() throws IOException {
        String input = "hello world";

        Writer writer = new StringWriter();
        append(input, 5, 5, writer);
        assertEquals("", writer.toString());
        append(input, 3, 8, writer);
        assertEquals(input.substring(3, 8), writer.toString());

        StringBuilder sb = new StringBuilder();
        append(input, 5, 5, sb);
        assertEquals("", sb.toString());
        append(input, 3, 8, sb);
        assertEquals(input.substring(3, 8), sb.toString());

        assertThrows(NullPointerException.class, () -> append((String) null, 0, 0, writer));
        assertThrows(NullPointerException.class, () -> append((String) null, 0, 0, sb));

        assertThrows(IndexOutOfBoundsException.class, () -> append(input, -1, input.length(), writer));
        assertThrows(IndexOutOfBoundsException.class, () -> append(input, 0, input.length() + 1, writer));
        assertThrows(IndexOutOfBoundsException.class, () -> append(input, 0, -1, writer));
        assertThrows(IndexOutOfBoundsException.class, () -> append(input, input.length() + 1, input.length(), writer));
        assertThrows(IndexOutOfBoundsException.class, () -> append(input, -1, input.length(), sb));
        assertThrows(IndexOutOfBoundsException.class, () -> append(input, 0, input.length() + 1, sb));
        assertThrows(IndexOutOfBoundsException.class, () -> append(input, 0, -1, sb));
        assertThrows(IndexOutOfBoundsException.class, () -> append(input, input.length() + 1, input.length(), sb));
    }
}
