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
import static com.github.robtimus.obfuscation.ObfuscatorUtils.checkOffsetAndLength;
import static com.github.robtimus.obfuscation.ObfuscatorUtils.checkStartAndEnd;
import static com.github.robtimus.obfuscation.ObfuscatorUtils.copyTo;
import static com.github.robtimus.obfuscation.ObfuscatorUtils.indexOf;
import static com.github.robtimus.obfuscation.ObfuscatorUtils.reader;
import static com.github.robtimus.obfuscation.ObfuscatorUtils.repeatChar;
import static com.github.robtimus.obfuscation.ObfuscatorUtils.skipLeadingWhitespace;
import static com.github.robtimus.obfuscation.ObfuscatorUtils.skipTrailingWhitespace;
import static com.github.robtimus.obfuscation.ObfuscatorUtils.wrapArray;
import static com.github.robtimus.obfuscation.ObfuscatorUtils.writer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import java.io.IOException;
import java.io.StringReader;
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
import org.mockito.ArgumentCaptor;

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
}
