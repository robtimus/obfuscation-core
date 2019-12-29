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
import static com.github.robtimus.obfuscation.ObfuscatorUtils.checkIndex;
import static com.github.robtimus.obfuscation.ObfuscatorUtils.checkOffsetAndLength;
import static com.github.robtimus.obfuscation.ObfuscatorUtils.checkStartAndEnd;
import static com.github.robtimus.obfuscation.ObfuscatorUtils.copyAll;
import static com.github.robtimus.obfuscation.ObfuscatorUtils.copyTo;
import static com.github.robtimus.obfuscation.ObfuscatorUtils.discardAll;
import static com.github.robtimus.obfuscation.ObfuscatorUtils.getChars;
import static com.github.robtimus.obfuscation.ObfuscatorUtils.indexOf;
import static com.github.robtimus.obfuscation.ObfuscatorUtils.map;
import static com.github.robtimus.obfuscation.ObfuscatorUtils.readAll;
import static com.github.robtimus.obfuscation.ObfuscatorUtils.reader;
import static com.github.robtimus.obfuscation.ObfuscatorUtils.repeatChar;
import static com.github.robtimus.obfuscation.ObfuscatorUtils.skipLeadingWhitespace;
import static com.github.robtimus.obfuscation.ObfuscatorUtils.skipTrailingWhitespace;
import static com.github.robtimus.obfuscation.ObfuscatorUtils.wrapArray;
import static com.github.robtimus.obfuscation.ObfuscatorUtils.writer;
import static java.util.stream.Collectors.toConcurrentMap;
import static java.util.stream.Collectors.toMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.CharBuffer;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import com.github.robtimus.obfuscation.ObfuscatorUtils.MapBuilder;

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
                arguments("hello", 'l', 10, -1, -1),
                arguments("hello", 'l', 0, 5, 2),
                arguments("hello", 'l', 5, 0, -1),
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

    @TestFactory
    @DisplayName("getChars(CharSequence, int, int, char, int)")
    public DynamicNode[] testGetChars() {
        return new DynamicNode[] {
                testGetChars("String", s -> s),
                testGetChars("StringBuilder", StringBuilder::new),
                testGetChars("StringBuffer", StringBuffer::new),
                testGetChars("CharSequence", CharBuffer::wrap),
        };
    }

    private DynamicNode testGetChars(String type, Function<String, CharSequence> constructor) {
        String input = "hello world";
        DynamicTest[] tests = {
                dynamicTest("negative srcBegin", () -> assertThrows(IndexOutOfBoundsException.class,
                        () -> getChars(constructor.apply(input), -1, input.length(), new char[input.length()], 0))),
                dynamicTest("too large srcEnd", () -> assertThrows(IndexOutOfBoundsException.class,
                        () -> getChars(constructor.apply(input), 0, input.length() + 1, new char[input.length()], 0))),
                dynamicTest("srcBegin > srcEnd", () -> assertThrows(IndexOutOfBoundsException.class,
                        () -> getChars(constructor.apply(input), 1, 0, new char[input.length()], 0))),
                dynamicTest("negative dstBegin", () -> assertThrows(IndexOutOfBoundsException.class,
                        () -> getChars(constructor.apply(input), 0, input.length(), new char[input.length()], -1))),
                dynamicTest("too large portion", () -> assertThrows(IndexOutOfBoundsException.class,
                        () -> getChars(constructor.apply(input), 0, input.length(), new char[input.length()], 1))),
                dynamicTest("get all", () -> {
                    char[] dst = new char[input.length() + 2];
                    getChars(constructor.apply(input), 0, input.length(), dst, 1);
                    char[] expected = ('\0' + input + '\0').toCharArray();
                    assertArrayEquals(expected, dst);
                }),
                dynamicTest("get some", () -> {
                    char[] dst = new char[input.length()];
                    getChars(constructor.apply(input), 1, input.length() - 1, dst, 1);
                    char[] expected = ('\0' + input.substring(1, input.length() - 1) + '\0').toCharArray();
                    assertArrayEquals(expected, dst);
                }),
        };
        return dynamicContainer(type, Arrays.asList(tests));
    }

    @Test
    @DisplayName("checkIndex(char[], int)")
    public void testCheckIndexForCharArray() {
        char[] array = "hello world".toCharArray();
        checkIndex(array, 0);
        assertThrows(IndexOutOfBoundsException.class, () -> checkIndex(array, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> checkIndex(array, array.length));
        checkIndex(array, array.length - 1);
    }

    @Test
    @DisplayName("checkOffsetAndLength(char[], int, int)")
    public void testCheckOffsetAndLengthForCharArray() {
        char[] array = "hello world".toCharArray();
        checkOffsetAndLength(array, 0, array.length);
        assertThrows(IndexOutOfBoundsException.class, () -> checkOffsetAndLength(array, -1, array.length));
        assertThrows(IndexOutOfBoundsException.class, () -> checkOffsetAndLength(array, 1, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> checkOffsetAndLength(array, 0, array.length + 1));
        assertThrows(IndexOutOfBoundsException.class, () -> checkOffsetAndLength(array, 1, array.length));
        checkOffsetAndLength(array, 1, 0);
    }

    @Test
    @DisplayName("checkStartAndEnd(char[], int, int)")
    public void testCheckStartAndEndForCharArray() {
        char[] array = "hello world".toCharArray();
        checkStartAndEnd(array, 0, array.length);
        assertThrows(IndexOutOfBoundsException.class, () -> checkStartAndEnd(array, -1, array.length));
        assertThrows(IndexOutOfBoundsException.class, () -> checkStartAndEnd(array, 1, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> checkStartAndEnd(array, 0, array.length + 1));
        checkStartAndEnd(array, 1, array.length);
        assertThrows(IndexOutOfBoundsException.class, () -> checkStartAndEnd(array, 1, 0));
    }

    @Test
    @DisplayName("checkIndex(CharSequence, int)")
    public void testCheckIndexForCharSequence() {
        CharSequence sequence = "hello world";
        checkIndex(sequence, 0);
        assertThrows(IndexOutOfBoundsException.class, () -> checkIndex(sequence, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> checkIndex(sequence, sequence.length()));
        checkIndex(sequence, sequence.length() - 1);
    }

    @Test
    @DisplayName("checkOffsetAndLength(CharSequence, int, int)")
    public void testCheckOffsetAndLengthForCharSequence() {
        CharSequence sequence = "hello world";
        checkOffsetAndLength(sequence, 0, sequence.length());
        assertThrows(IndexOutOfBoundsException.class, () -> checkOffsetAndLength(sequence, -1, sequence.length()));
        assertThrows(IndexOutOfBoundsException.class, () -> checkOffsetAndLength(sequence, 1, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> checkOffsetAndLength(sequence, 0, sequence.length() + 1));
        assertThrows(IndexOutOfBoundsException.class, () -> checkOffsetAndLength(sequence, 1, sequence.length()));
        checkOffsetAndLength(sequence, 1, 0);
    }

    @Test
    @DisplayName("checkStartAndEnd(CharSequence, int, int)")
    public void testCheckStartAndEndForCharSequence() {
        CharSequence sequence = "hello world";
        checkStartAndEnd(sequence, 0, sequence.length());
        assertThrows(IndexOutOfBoundsException.class, () -> checkStartAndEnd(sequence, -1, sequence.length()));
        assertThrows(IndexOutOfBoundsException.class, () -> checkStartAndEnd(sequence, 1, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> checkStartAndEnd(sequence, 0, sequence.length() + 1));
        checkStartAndEnd(sequence, 1, sequence.length());
        assertThrows(IndexOutOfBoundsException.class, () -> checkStartAndEnd(sequence, 1, 0));
    }

    @Test
    @DisplayName("wrapArray(char[])")
    public void testWrapArray() {
        assertThat(wrapArray(new char[0]), instanceOf(CharArraySequence.class));
        assertThrows(NullPointerException.class, () -> wrapArray(null));
    }

    @Test
    @DisplayName("repeatChar(char, int)")
    public void testRepeatChar() {
        assertThat(repeatChar('*', 1), instanceOf(RepeatingCharSequence.class));
        assertThrows(IllegalArgumentException.class, () -> repeatChar('*', -1));
    }

    @Test
    @DisplayName("reader(CharSequence)")
    public void testReader() {
        assertThat(reader(""), instanceOf(CharSequenceReader.class));
        assertThrows(NullPointerException.class, () -> reader(null));
    }

    @Test
    @DisplayName("reader(CharSequence, int, int)")
    public void testReaderWithRange() {
        CharSequence sequence = "hello world";
        assertThat(reader(sequence, 0, sequence.length()), instanceOf(CharSequenceReader.class));
        assertThrows(NullPointerException.class, () -> reader(null, 0, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> reader(sequence, -1, sequence.length()));
        assertThrows(IndexOutOfBoundsException.class, () -> reader(sequence, 1, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> reader(sequence, 0, sequence.length() + 1));
        assertThrows(IndexOutOfBoundsException.class, () -> reader(sequence, 1, 0));
    }

    @Test
    @DisplayName("writer(Appendable)")
    @SuppressWarnings("resource")
    public void testWriter() {
        Writer writer = new StringWriter();
        assertSame(writer, writer(writer));

        StringBuilder sb = new StringBuilder();
        writer = writer(sb);
        assertThat(writer, instanceOf(AppendableWriter.class));

        assertThrows(NullPointerException.class, () -> writer(null));
    }

    @Test
    @DisplayName("copyTo(Reader, Appendable) with nulls")
    public void testCopyTo() {
        assertThat(copyTo(new StringReader(""), new StringBuilder()), instanceOf(CopyingReader.class));
        assertThrows(NullPointerException.class, () -> copyTo(null, new StringBuilder()));
        assertThrows(NullPointerException.class, () -> copyTo(new StringReader(""), null));
    }

    @Test
    @DisplayName("readAll()")
    public void testReadAll() throws IOException {
        String string = "hello world";
        StringReader input = new StringReader(string);
        assertEquals(string, readAll(input).toString());
        assertEquals(-1, input.read());
    }

    @Test
    @DisplayName("discardAll()")
    public void testDiscardAll() throws IOException {
        String string = "hello world";
        StringReader input = new StringReader(string);
        discardAll(input);
        assertEquals(-1, input.read());
    }

    @Test
    @DisplayName("copyAll()")
    public void testCopyAll() throws IOException {
        String string = "hello world";
        StringReader input = new StringReader(string);
        StringBuilder destination = new StringBuilder();
        copyAll(input, destination);
        assertEquals(string, destination.toString());
        assertEquals(-1, input.read());
    }

    @Test
    @DisplayName("append(int)")
    public void testAppendInt() throws IOException {
        int c = 1 << 16 | 'a';

        Writer writer = new StringWriter();
        append(c, writer);
        assertEquals("a", writer.toString());

        StringBuilder builder = new StringBuilder();
        append(c, builder);
        assertEquals("a", builder.toString());

        StringBuffer buffer = new StringBuffer();
        append(c, buffer);
        assertEquals("a", buffer.toString());

        Appendable appendable = mock(Appendable.class);
        append(c, appendable);
        verify(appendable).append('a');
        verifyNoMoreInteractions(appendable);
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

        StringBuilder builder = new StringBuilder();
        append(array, builder);
        assertEquals(input, builder.toString());

        StringBuffer buffer = new StringBuffer();
        append(array, buffer);
        assertEquals(input, buffer.toString());

        Appendable appendable = mock(Appendable.class);
        ArgumentCaptor<CharSequence> captor = ArgumentCaptor.forClass(CharSequence.class);
        append(array, appendable);
        verify(appendable).append(captor.capture());
        verifyNoMoreInteractions(appendable);
        assertEquals(input, captor.getValue().toString());

        assertThrows(NullPointerException.class, () -> append((char[]) null, writer));
        assertThrows(NullPointerException.class, () -> append((char[]) null, builder));
        assertThrows(NullPointerException.class, () -> append((char[]) null, buffer));
        assertThrows(NullPointerException.class, () -> append((char[]) null, appendable));
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

        StringBuilder builder = new StringBuilder();
        append(array, 5, 5, builder);
        assertEquals("", builder.toString());
        append(array, 3, 8, builder);
        assertEquals(input.substring(3, 8), builder.toString());

        StringBuffer buffer = new StringBuffer();
        append(array, 5, 5, buffer);
        assertEquals("", buffer.toString());
        append(array, 3, 8, buffer);
        assertEquals(input.substring(3, 8), buffer.toString());

        Appendable appendable = mock(Appendable.class);
        ArgumentCaptor<CharSequence> captor = ArgumentCaptor.forClass(CharSequence.class);
        append(array, 5, 5, appendable);
        verify(appendable, never()).append(any());
        verify(appendable, never()).append(any(), anyInt(), anyInt());
        append(array, 3, 8, appendable);
        verify(appendable).append(captor.capture(), eq(3), eq(8));
        verifyNoMoreInteractions(appendable);
        assertEquals(input, captor.getValue().toString());

        assertThrows(NullPointerException.class, () -> append((char[]) null, 0, 0, writer));
        assertThrows(NullPointerException.class, () -> append((char[]) null, 0, 0, builder));
        assertThrows(NullPointerException.class, () -> append((char[]) null, 0, 0, buffer));
        assertThrows(NullPointerException.class, () -> append((char[]) null, 0, 0, appendable));

        assertThrows(IndexOutOfBoundsException.class, () -> append(array, -1, array.length, writer));
        assertThrows(IndexOutOfBoundsException.class, () -> append(array, 0, array.length + 1, writer));
        assertThrows(IndexOutOfBoundsException.class, () -> append(array, 0, -1, writer));
        assertThrows(IndexOutOfBoundsException.class, () -> append(array, array.length + 1, array.length, writer));
        assertThrows(IndexOutOfBoundsException.class, () -> append(array, -1, array.length, builder));
        assertThrows(IndexOutOfBoundsException.class, () -> append(array, 0, array.length + 1, builder));
        assertThrows(IndexOutOfBoundsException.class, () -> append(array, 0, -1, builder));
        assertThrows(IndexOutOfBoundsException.class, () -> append(array, array.length + 1, array.length, builder));
        assertThrows(IndexOutOfBoundsException.class, () -> append(array, -1, array.length, buffer));
        assertThrows(IndexOutOfBoundsException.class, () -> append(array, 0, array.length + 1, buffer));
        assertThrows(IndexOutOfBoundsException.class, () -> append(array, 0, -1, buffer));
        assertThrows(IndexOutOfBoundsException.class, () -> append(array, array.length + 1, array.length, buffer));
        assertThrows(IndexOutOfBoundsException.class, () -> append(array, -1, array.length, appendable));
        assertThrows(IndexOutOfBoundsException.class, () -> append(array, 0, array.length + 1, appendable));
        assertThrows(IndexOutOfBoundsException.class, () -> append(array, 0, -1, appendable));
        assertThrows(IndexOutOfBoundsException.class, () -> append(array, array.length + 1, array.length, appendable));
    }

    @Test
    @DisplayName("append(String)")
    public void testAppendString() throws IOException {
        String input = "hello world";

        Writer writer = new StringWriter();
        append(input, writer);
        assertEquals(input, writer.toString());

        StringBuilder builder = new StringBuilder();
        append(input, builder);
        assertEquals(input, builder.toString());

        StringBuffer buffer = new StringBuffer();
        append(input, buffer);
        assertEquals(input, buffer.toString());

        Appendable appendable = mock(Appendable.class);
        ArgumentCaptor<CharSequence> captor = ArgumentCaptor.forClass(CharSequence.class);
        append(input, appendable);
        verify(appendable).append(captor.capture());
        verifyNoMoreInteractions(appendable);
        assertEquals(input, captor.getValue().toString());

        assertThrows(NullPointerException.class, () -> append((String) null, writer));
        assertThrows(NullPointerException.class, () -> append((String) null, builder));
        assertThrows(NullPointerException.class, () -> append((String) null, buffer));
        assertThrows(NullPointerException.class, () -> append((String) null, appendable));
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

        StringBuilder builder = new StringBuilder();
        append(input, 5, 5, builder);
        assertEquals("", builder.toString());
        append(input, 3, 8, builder);
        assertEquals(input.substring(3, 8), builder.toString());

        StringBuffer buffer = new StringBuffer();
        append(input, 5, 5, buffer);
        assertEquals("", buffer.toString());
        append(input, 3, 8, buffer);
        assertEquals(input.substring(3, 8), buffer.toString());

        Appendable appendable = mock(Appendable.class);
        ArgumentCaptor<CharSequence> captor = ArgumentCaptor.forClass(CharSequence.class);
        append(input, 5, 5, appendable);
        verify(appendable, never()).append(any());
        verify(appendable, never()).append(any(), anyInt(), anyInt());
        append(input, 3, 8, appendable);
        verify(appendable).append(captor.capture(), eq(3), eq(8));
        verifyNoMoreInteractions(appendable);
        assertEquals(input, captor.getValue().toString());

        assertThrows(NullPointerException.class, () -> append((String) null, 0, 0, writer));
        assertThrows(NullPointerException.class, () -> append((String) null, 0, 0, builder));
        assertThrows(NullPointerException.class, () -> append((String) null, 0, 0, buffer));
        assertThrows(NullPointerException.class, () -> append((String) null, 0, 0, appendable));

        assertThrows(IndexOutOfBoundsException.class, () -> append(input, -1, input.length(), writer));
        assertThrows(IndexOutOfBoundsException.class, () -> append(input, 0, input.length() + 1, writer));
        assertThrows(IndexOutOfBoundsException.class, () -> append(input, 0, -1, writer));
        assertThrows(IndexOutOfBoundsException.class, () -> append(input, input.length() + 1, input.length(), writer));
        assertThrows(IndexOutOfBoundsException.class, () -> append(input, -1, input.length(), builder));
        assertThrows(IndexOutOfBoundsException.class, () -> append(input, 0, input.length() + 1, builder));
        assertThrows(IndexOutOfBoundsException.class, () -> append(input, 0, -1, builder));
        assertThrows(IndexOutOfBoundsException.class, () -> append(input, input.length() + 1, input.length(), builder));
        assertThrows(IndexOutOfBoundsException.class, () -> append(input, -1, input.length(), buffer));
        assertThrows(IndexOutOfBoundsException.class, () -> append(input, 0, input.length() + 1, buffer));
        assertThrows(IndexOutOfBoundsException.class, () -> append(input, 0, -1, buffer));
        assertThrows(IndexOutOfBoundsException.class, () -> append(input, input.length() + 1, input.length(), buffer));
        assertThrows(IndexOutOfBoundsException.class, () -> append(input, -1, input.length(), appendable));
        assertThrows(IndexOutOfBoundsException.class, () -> append(input, 0, input.length() + 1, appendable));
        assertThrows(IndexOutOfBoundsException.class, () -> append(input, 0, -1, appendable));
        assertThrows(IndexOutOfBoundsException.class, () -> append(input, input.length() + 1, input.length(), appendable));
    }

    @Nested
    @DisplayName("map()")
    public class MapTest {

        @Test
        @DisplayName("withEntry(String, K)")
        public void testWithEntryCaseSensitive() {
            MapBuilder<Integer> builder = ObfuscatorUtils.<Integer>map()
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
        @DisplayName("withEntry(String, K, boolean)")
        public void testWithEntry() {
            MapBuilder<Integer> builder = ObfuscatorUtils.<Integer>map()
                    .withEntry("a", 1, true)
                    .withEntry("b", 2, false);

            Map<String, Integer> expectedCaseSensitiveMap = new HashMap<>();
            expectedCaseSensitiveMap.put("a", 1);
            Map<String, Integer> expectedCaseInsensitiveMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            expectedCaseInsensitiveMap.put("b", 2);

            assertEquals(expectedCaseSensitiveMap, builder.caseSensitiveMap());
            assertEquals(expectedCaseInsensitiveMap, builder.caseInsensitiveMap());

            builder.withEntry("a", 3, false);
            builder.withEntry("b", 4, true);

            expectedCaseSensitiveMap.put("b", 4);
            expectedCaseInsensitiveMap.put("a", 3);

            assertEquals(expectedCaseSensitiveMap, builder.caseSensitiveMap());
            assertEquals(expectedCaseInsensitiveMap, builder.caseInsensitiveMap());
        }

        @Test
        @DisplayName("transform")
        public void testTransform() {
            MapBuilder<Integer> builder = map();
            @SuppressWarnings("unchecked")
            Function<MapBuilder<?>, String> f = mock(Function.class);
            when(f.apply(builder)).thenReturn("result");

            assertEquals("result", builder.transform(f));
            verify(f).apply(builder);
            verifyNoMoreInteractions(f);
        }

        @Nested
        @DisplayName("build()")
        public class BuildTest {

            @Test
            @DisplayName("empty")
            public void testEmpty() {
                Map<String, Integer> map = ObfuscatorUtils.<Integer>map().build();
                assertSame(Collections.emptyMap(), map);
            }

            @Test
            @DisplayName("only case sensitive entries")
            public void testOnlyCaseSensitiveEntries() {
                Map<String, Integer> map = ObfuscatorUtils.<Integer>map()
                        .withEntry("a", 1, true)
                        .withEntry("b", 2, true)
                        .withEntry("c", 3, true)
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
            public void testOnlyCaseInsensitiveEntries() {
                Map<String, Integer> map = ObfuscatorUtils.<Integer>map()
                        .withEntry("a", 1, false)
                        .withEntry("b", 2, false)
                        .withEntry("c", 3, false)
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
            public class MixedCaseEntriesTest {

                private final Map<String, Integer> map = ObfuscatorUtils.<Integer>map()
                        .withEntry("a", 1, true)
                        .withEntry("b", 2, false)
                        .withEntry("c", 3, true)
                        .withEntry("d", 4, false)
                        .build();

                @Test
                @DisplayName("size()")
                public void testSize() {
                    assertEquals(4, map.size());
                }

                @Test
                @DisplayName("isEmpty()")
                public void testIsEmpty() {
                    assertFalse(map.isEmpty());
                }

                @ParameterizedTest(name = "{0}: {1}")
                @MethodSource
                @DisplayName("containsKey(Object)")
                public void testContainsKey(Object key, boolean expected) {
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
                public void testContainsValue(Object key, boolean expected) {
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
                public void testGet(Object key, Object expected) {
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
                public void testPut() {
                    assertThrows(UnsupportedOperationException.class, () -> map.put("a", 0));
                    assertThrows(UnsupportedOperationException.class, () -> map.put("x", 0));
                    assertEquals(4, map.size());
                    assertEquals(1, map.get("a"));
                }

                @Test
                @DisplayName("remove(Object)")
                public void testRemove() {
                    assertThrows(UnsupportedOperationException.class, () -> map.remove("a"));
                    assertThrows(UnsupportedOperationException.class, () -> map.remove("x"));
                    assertEquals(4, map.size());
                }

                @Test
                @DisplayName("putAll(Map<? extends String, ? extends V>)")
                public void testPutAll() {
                    assertThrows(UnsupportedOperationException.class, () -> map.putAll(Collections.emptyMap()));
                    assertThrows(UnsupportedOperationException.class, () -> map.putAll(Collections.singletonMap("a", 0)));
                    assertEquals(4, map.size());
                    assertEquals(1, map.get("a"));
                }

                @Test
                @DisplayName("clear()")
                public void testClear() {
                    assertThrows(UnsupportedOperationException.class, () -> map.clear());
                    assertEquals(4, map.size());
                }

                @Nested
                @DisplayName("entrySet()")
                @TestInstance(Lifecycle.PER_CLASS)
                public class EntrySetTest {

                    private final Set<Map.Entry<String, Integer>> entrySet = map.entrySet();

                    @Test
                    @DisplayName("size()")
                    public void testSize() {
                        assertEquals(4, entrySet.size());
                    }

                    @Test
                    @DisplayName("isEmpty()")
                    public void testIsEmpty() {
                        assertFalse(entrySet.isEmpty());
                    }

                    @ParameterizedTest(name = "{0}: {1}")
                    @MethodSource
                    @DisplayName("contains(Object)")
                    public void testContains(Object o, boolean expected) {
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
                    public void testIterator() {
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
                    public void testAdd() {
                        assertThrows(UnsupportedOperationException.class, () -> entrySet.add(new SimpleEntry<>("a", 0)));
                        assertThrows(UnsupportedOperationException.class, () -> entrySet.add(new SimpleEntry<>("x", 0)));
                        assertEquals(4, entrySet.size());
                        assertEquals(1, map.get("a"));
                    }

                    @Test
                    @DisplayName("remove(Object)")
                    public void testRemove() {
                        assertThrows(UnsupportedOperationException.class, () -> entrySet.remove(new SimpleEntry<>("a", 0)));
                        assertThrows(UnsupportedOperationException.class, () -> entrySet.remove(new SimpleEntry<>("x", 0)));
                        assertEquals(4, entrySet.size());
                        assertEquals(1, map.get("a"));
                    }

                    @Test
                    @DisplayName("addAll(Collection<? extends Entry<String, V>)")
                    public void testAddAll() {
                        assertThrows(UnsupportedOperationException.class, () -> entrySet.addAll(Collections.emptyList()));
                        assertThrows(UnsupportedOperationException.class, () -> entrySet.addAll(Collections.singleton(new SimpleEntry<>("a", 0))));
                        assertEquals(4, entrySet.size());
                        assertEquals(1, map.get("a"));
                    }

                    @Test
                    @DisplayName("retainAll(Collection<?>)")
                    public void testRetainAll() {
                        assertThrows(UnsupportedOperationException.class, () -> entrySet.retainAll(Collections.emptyList()));
                        assertThrows(UnsupportedOperationException.class, () -> entrySet.retainAll(Collections.singleton(new SimpleEntry<>("a", 0))));
                        assertEquals(4, entrySet.size());
                        assertEquals(1, map.get("a"));
                    }

                    @Test
                    @DisplayName("removeAll(Collection<?>)")
                    public void testRemoveAll() {
                        assertThrows(UnsupportedOperationException.class, () -> entrySet.removeAll(Collections.emptyList()));
                        assertThrows(UnsupportedOperationException.class, () -> entrySet.removeAll(Collections.singleton(new SimpleEntry<>("a", 0))));
                        assertEquals(4, entrySet.size());
                        assertEquals(1, map.get("a"));
                    }

                    @Test
                    @DisplayName("clear()")
                    public void testClear() {
                        assertThrows(UnsupportedOperationException.class, () -> entrySet.clear());
                        assertEquals(4, entrySet.size());
                    }

                    @Test
                    @DisplayName("removeIf(Predicate<? super Entry<String, V>)")
                    public void testRemoveIf() {
                        assertThrows(UnsupportedOperationException.class, () -> entrySet.removeIf(e -> false));
                        assertThrows(UnsupportedOperationException.class, () -> entrySet.removeIf(e -> true));
                        assertEquals(4, entrySet.size());
                        assertEquals(1, map.get("a"));
                    }

                    @Test
                    @DisplayName("stream()")
                    public void testStream() {
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
                    public void testParallelStream() {
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
                    public void testForEach() {
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
                public void testGetOrDefault(Object key, Integer defaultValue, Object expected) {
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
                public void testForEach() {
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
                public void testReplaceAll() {
                    assertThrows(UnsupportedOperationException.class, () -> map.replaceAll((s, v) -> v));
                    assertEquals(4, map.size());
                }

                @Test
                @DisplayName("putIfAbsent(String, V)")
                public void testPutIfAbsent() {
                    assertThrows(UnsupportedOperationException.class, () -> map.putIfAbsent("a", 0));
                    assertThrows(UnsupportedOperationException.class, () -> map.putIfAbsent("x", 0));
                    assertEquals(4, map.size());
                    assertEquals(1, map.get("a"));
                }

                @Test
                @DisplayName("remove(Object, Object)")
                public void testRemoveWithValue() {
                    assertThrows(UnsupportedOperationException.class, () -> map.remove("a", 1));
                    assertThrows(UnsupportedOperationException.class, () -> map.remove("a", 2));
                    assertThrows(UnsupportedOperationException.class, () -> map.remove("x", 1));
                    assertEquals(4, map.size());
                    assertEquals(1, map.get("a"));
                }

                @Test
                @DisplayName("replace(String, V, V)")
                public void testReplaceWithValue() {
                    assertThrows(UnsupportedOperationException.class, () -> map.replace("a", 1, 0));
                    assertThrows(UnsupportedOperationException.class, () -> map.replace("a", 0, -1));
                    assertThrows(UnsupportedOperationException.class, () -> map.replace("x", 0, 1));
                    assertEquals(4, map.size());
                    assertEquals(1, map.get("a"));
                }

                @Test
                @DisplayName("replace(String, V)")
                public void testReplace() {
                    assertThrows(UnsupportedOperationException.class, () -> map.replace("a", 0));
                    assertThrows(UnsupportedOperationException.class, () -> map.replace("x", 0));
                    assertEquals(4, map.size());
                    assertEquals(1, map.get("a"));
                }

                @Test
                @DisplayName("hashCode()")
                public void testHashCode() {
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
                public void testEquals(Object object, boolean expected) {
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
                            arguments(ObfuscatorUtils.<Integer>map()
                                    .withEntry("a", 1, true)
                                    .withEntry("b", 2, false)
                                    .withEntry("c", 3, true)
                                    .withEntry("d", 4, false)
                                    .build(), true),
                            // same mappings, since case insensitive keys match case insensitively
                            arguments(ObfuscatorUtils.<Integer>map()
                                    .withEntry("a", 1, true)
                                    .withEntry("B", 2, false)
                                    .withEntry("c", 3, true)
                                    .withEntry("D", 4, false)
                                    .build(), true),
                            // different mappings, since the cases have been swapped
                            arguments(ObfuscatorUtils.<Integer>map()
                                    .withEntry("a", 1, false)
                                    .withEntry("b", 2, true)
                                    .withEntry("c", 3, false)
                                    .withEntry("d", 4, true)
                                    .build(), false),
                            // different mappings, since case sensitive keys match case sensitively;
                            arguments(ObfuscatorUtils.<Integer>map()
                                    .withEntry("A", 1, true)
                                    .withEntry("b", 2, false)
                                    .withEntry("c", 3, true)
                                    .withEntry("d", 4, false)
                                    .build(), false),
                            // case sensitive is different, case insensitive is the same
                            arguments(ObfuscatorUtils.<Integer>map()
                                    .withEntry("a", 1, false)
                                    .withEntry("b", 2, true)
                                    .withEntry("d", 4, true)
                                    .build(), false),
                            // case sensitive is the same, case insensitive is different
                            arguments(ObfuscatorUtils.<Integer>map()
                                    .withEntry("a", 1, true)
                                    .withEntry("b", 2, false)
                                    .withEntry("c", 3, true)
                                    .build(), false),
                            // case sensitive and case insensitive are both different
                            arguments(ObfuscatorUtils.<Integer>map()
                                    .withEntry("a", 1, true)
                                    .withEntry("b", 2, false)
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
                public void testSerializability() throws IOException, ClassNotFoundException {
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
        }
    }
}
