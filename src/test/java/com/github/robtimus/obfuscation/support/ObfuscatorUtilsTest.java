/*
 * ObfuscatorUtilsTest.java
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

package com.github.robtimus.obfuscation.support;

import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.append;
import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.appendAtMost;
import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.checkIndex;
import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.checkOffsetAndLength;
import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.checkStartAndEnd;
import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.concat;
import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.copyAll;
import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.copyTo;
import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.counting;
import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.discardAll;
import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.getChars;
import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.indexOf;
import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.maskAll;
import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.readAll;
import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.readAtMost;
import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.reader;
import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.repeatChar;
import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.skipLeadingWhitespace;
import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.skipTrailingWhitespace;
import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.wrapArray;
import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.writer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@SuppressWarnings("nls")
@TestInstance(Lifecycle.PER_CLASS)
class ObfuscatorUtilsTest {

    @ParameterizedTest(name = "{1} in {0}[{2}, {3})")
    @MethodSource
    @DisplayName("indexOf(CharSequence, int, int, int)")
    void testIndexOf(String s, int ch, int fromIndex, int toIndex, int expected) {
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
    void testSkipLeadingWhitespace(CharSequence s, int fromIndex, int toIndex, int expected) {
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
    void testSkipTrailingWhitespace(CharSequence s, int fromIndex, int toIndex, int expected) {
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
    DynamicNode[] testGetChars() {
        return new DynamicNode[] {
                testGetChars("String", s -> s),
                testGetChars("StringBuilder", StringBuilder::new),
                testGetChars("StringBuffer", StringBuffer::new),
                testGetChars("CharSequence", CharBuffer::wrap),
        };
    }

    private DynamicNode testGetChars(String type, Function<String, CharSequence> constructor) {
        String input = "hello world";
        int length = input.length();
        CharSequence sequence = constructor.apply(input);

        DynamicTest[] tests = {
                dynamicTest("negative srcBegin", () -> assertThrows(IndexOutOfBoundsException.class,
                        () -> getChars(sequence, -1, length, new char[length], 0))),
                dynamicTest("too large srcEnd", () -> assertThrows(IndexOutOfBoundsException.class,
                        () -> getChars(sequence, 0, length + 1, new char[length], 0))),
                dynamicTest("srcBegin > srcEnd", () -> assertThrows(IndexOutOfBoundsException.class,
                        () -> getChars(sequence, 1, 0, new char[length], 0))),
                dynamicTest("negative dstBegin", () -> assertThrows(IndexOutOfBoundsException.class,
                        () -> getChars(sequence, 0, length, new char[length], -1))),
                dynamicTest("too large portion", () -> assertThrows(IndexOutOfBoundsException.class,
                        () -> getChars(sequence, 0, length, new char[length], 1))),
                dynamicTest("get all", () -> {
                    char[] dst = new char[length + 2];
                    getChars(sequence, 0, length, dst, 1);
                    char[] expected = ('\0' + input + '\0').toCharArray();
                    assertArrayEquals(expected, dst);
                }),
                dynamicTest("get some", () -> {
                    char[] dst = new char[length];
                    getChars(sequence, 1, length - 1, dst, 1);
                    char[] expected = ('\0' + input.substring(1, length - 1) + '\0').toCharArray();
                    assertArrayEquals(expected, dst);
                }),
        };
        return dynamicContainer(type, Arrays.asList(tests));
    }

    @Test
    @DisplayName("checkIndex(char[], int)")
    void testCheckIndexForCharArray() {
        char[] array = "hello world".toCharArray();
        checkIndex(array, 0);
        assertThrows(IndexOutOfBoundsException.class, () -> checkIndex(array, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> checkIndex(array, array.length));
        checkIndex(array, array.length - 1);
    }

    @Test
    @DisplayName("checkOffsetAndLength(char[], int, int)")
    void testCheckOffsetAndLengthForCharArray() {
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
    void testCheckStartAndEndForCharArray() {
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
    void testCheckIndexForCharSequence() {
        CharSequence sequence = "hello world";
        int length = sequence.length();

        checkIndex(sequence, 0);
        assertThrows(IndexOutOfBoundsException.class, () -> checkIndex(sequence, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> checkIndex(sequence, length));
        checkIndex(sequence, length - 1);
    }

    @Test
    @DisplayName("checkOffsetAndLength(CharSequence, int, int)")
    void testCheckOffsetAndLengthForCharSequence() {
        CharSequence sequence = "hello world";
        int length = sequence.length();

        checkOffsetAndLength(sequence, 0, length);
        assertThrows(IndexOutOfBoundsException.class, () -> checkOffsetAndLength(sequence, -1, length));
        assertThrows(IndexOutOfBoundsException.class, () -> checkOffsetAndLength(sequence, 1, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> checkOffsetAndLength(sequence, 0, length + 1));
        assertThrows(IndexOutOfBoundsException.class, () -> checkOffsetAndLength(sequence, 1, length));
        checkOffsetAndLength(sequence, 1, 0);
    }

    @Test
    @DisplayName("checkStartAndEnd(CharSequence, int, int)")
    void testCheckStartAndEndForCharSequence() {
        CharSequence sequence = "hello world";
        int length = sequence.length();

        checkStartAndEnd(sequence, 0, length);
        assertThrows(IndexOutOfBoundsException.class, () -> checkStartAndEnd(sequence, -1, length));
        assertThrows(IndexOutOfBoundsException.class, () -> checkStartAndEnd(sequence, 1, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> checkStartAndEnd(sequence, 0, length + 1));
        checkStartAndEnd(sequence, 1, length);
        assertThrows(IndexOutOfBoundsException.class, () -> checkStartAndEnd(sequence, 1, 0));
    }

    @Test
    @DisplayName("wrapArray(char[])")
    void testWrapArray() {
        assertThat(wrapArray(new char[0]), instanceOf(CharArraySequence.class));
        assertThrows(NullPointerException.class, () -> wrapArray(null));
    }

    @Test
    @DisplayName("repeatChar(char, int)")
    void testRepeatChar() {
        assertThat(repeatChar('*', 1), instanceOf(RepeatingCharSequence.class));
        assertThrows(IllegalArgumentException.class, () -> repeatChar('*', -1));
    }

    @Test
    @DisplayName("concat(CharSequence, CharSequence)")
    void testConcat() {
        assertThat(concat("", ""), instanceOf(ConcatCharSequence.class));
        assertThrows(NullPointerException.class, () -> concat(null, ""));
        assertThrows(NullPointerException.class, () -> concat("", null));
    }

    @Test
    @DisplayName("reader(CharSequence)")
    @SuppressWarnings("resource")
    void testReader() {
        assertThat(reader(""), instanceOf(CharSequenceReader.class));
        assertThrows(NullPointerException.class, () -> reader(null));
    }

    @Test
    @DisplayName("reader(CharSequence, int, int)")
    @SuppressWarnings("resource")
    void testReaderWithRange() {
        CharSequence sequence = "hello world";
        int length = sequence.length();

        assertThat(reader(sequence, 0, length), instanceOf(CharSequenceReader.class));
        assertThrows(NullPointerException.class, () -> reader(null, 0, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> reader(sequence, -1, length));
        assertThrows(IndexOutOfBoundsException.class, () -> reader(sequence, 1, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> reader(sequence, 0, length + 1));
        assertThrows(IndexOutOfBoundsException.class, () -> reader(sequence, 1, 0));
    }

    @Test
    @DisplayName("writer(Appendable)")
    @SuppressWarnings("resource")
    void testWriter() {
        Writer writer = new StringWriter();
        assertSame(writer, writer(writer));

        StringBuilder sb = new StringBuilder();
        writer = writer(sb);
        assertThat(writer, instanceOf(AppendableWriter.class));

        assertThrows(NullPointerException.class, () -> writer(null));
    }

    @Test
    @DisplayName("count(Reader)")
    @SuppressWarnings("resource")
    void testCount() {
        assertThat(counting(new StringReader("")), instanceOf(CountingReader.class));
        assertThrows(NullPointerException.class, () -> counting(null));
    }

    @Test
    @DisplayName("copyTo(Reader, Appendable)")
    @SuppressWarnings("resource")
    void testCopyTo() {
        StringReader reader = new StringReader("");
        StringBuilder sb = new StringBuilder();

        assertThat(copyTo(reader, sb), instanceOf(CopyingReader.class));
        assertThrows(NullPointerException.class, () -> copyTo(null, sb));
        assertThrows(NullPointerException.class, () -> copyTo(reader, null));
    }

    @Test
    @DisplayName("readAtMost(Reader, int)")
    @SuppressWarnings("resource")
    void testReadAtMost() {
        StringReader stringReader = new StringReader("");

        assertThat(readAtMost(stringReader, 5), instanceOf(LimitReader.class));
        assertThrows(NullPointerException.class, () -> readAtMost(null, 5));
        assertThrows(IllegalArgumentException.class, () -> readAtMost(stringReader, -1));
    }

    @Test
    @DisplayName("appendAtMost(Appendable, int)")
    void testAppendAtMost() {
        StringWriter writer = new StringWriter();

        assertThat(appendAtMost(writer, 5), instanceOf(LimitAppendable.class));
        assertThrows(NullPointerException.class, () -> appendAtMost(null, 5));
        assertThrows(IllegalArgumentException.class, () -> appendAtMost(writer, -1));
    }

    @Test
    @DisplayName("readAll(Reader)")
    void testReadAll() throws IOException {
        String string = "hello world";
        StringReader input = new StringReader(string);
        assertEquals(string, readAll(input).toString());
        assertEquals(-1, input.read());
    }

    @Test
    @DisplayName("discardAll(Reader)")
    void testDiscardAll() throws IOException {
        String string = "hello world";
        StringReader input = new StringReader(string);
        discardAll(input);
        assertEquals(-1, input.read());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("appendableArguments")
    @DisplayName("copyAll(Reader, Appendable)")
    void testCopyAll(@SuppressWarnings("unused") String appendableType, Supplier<Appendable> destinationSupplier) throws IOException {
        String string = "hello world";
        StringReader input = new StringReader(string);
        Appendable destination = destinationSupplier.get();
        copyAll(input, destination);
        assertEquals(string, destination.toString());
        assertEquals(-1, input.read());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("appendableArguments")
    @DisplayName("maskAll(Reader, char, Appendable)")
    void testMaskAll(@SuppressWarnings("unused") String appendableType, Supplier<Appendable> destinationSupplier) throws IOException {
        String string = "hello world";
        String expected = string.replaceAll(".", "*");
        StringReader input = new StringReader(string);
        Appendable destination = destinationSupplier.get();
        maskAll(input, '*', destination);
        assertEquals(expected, destination.toString());
        assertEquals(-1, input.read());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("appendableArguments")
    @DisplayName("append(int)")
    void testAppendInt(@SuppressWarnings("unused") String appendableType, Supplier<Appendable> destinationSupplier) throws IOException {
        int c = 1 << 16 | 'a';

        Appendable destination = destinationSupplier.get();
        append(c, destination);
        assertEquals("a", destination.toString());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("appendableArguments")
    @DisplayName("append(char, int)")
    void testAppendRepeated(@SuppressWarnings("unused") String appendableType, Supplier<Appendable> destinationSupplier) throws IOException {
        char c = '*';
        int count = 2048 + 32;

        char[] array = new char[count];
        Arrays.fill(array, c);
        String expected = new String(array);

        Appendable destination = destinationSupplier.get();
        append(c, 0, destination);
        assertEquals("", destination.toString());
        append(c, count, destination);
        assertEquals(expected, destination.toString());

        assertThrows(IllegalArgumentException.class, () -> append(c, -1, destination));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("appendableArguments")
    @DisplayName("append(char[])")
    void testAppendCharArray(@SuppressWarnings("unused") String appendableType, Supplier<Appendable> destinationSupplier) throws IOException {
        String input = "hello world";
        char[] array = input.toCharArray();

        Appendable destination = destinationSupplier.get();
        append(array, destination);
        assertEquals(input, destination.toString());

        assertThrows(NullPointerException.class, () -> append((char[]) null, destination));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("appendableArguments")
    @DisplayName("append(char[], int, int)")
    void testAppendCharArrayRange(@SuppressWarnings("unused") String appendableType, Supplier<Appendable> destinationSupplier)
            throws IOException {

        String input = "hello world";
        char[] array = input.toCharArray();

        Appendable destination = destinationSupplier.get();
        append(array, 5, 5, destination);
        assertEquals("", destination.toString());
        append(array, 3, 8, destination);
        assertEquals(input.substring(3, 8), destination.toString());

        assertThrows(NullPointerException.class, () -> append((char[]) null, 0, 0, destination));

        assertThrows(IndexOutOfBoundsException.class, () -> append(array, -1, array.length, destination));
        assertThrows(IndexOutOfBoundsException.class, () -> append(array, 0, array.length + 1, destination));
        assertThrows(IndexOutOfBoundsException.class, () -> append(array, 0, -1, destination));
        assertThrows(IndexOutOfBoundsException.class, () -> append(array, array.length + 1, array.length, destination));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("appendableArguments")
    @DisplayName("append(String)")
    void testAppendString(@SuppressWarnings("unused") String appendableType, Supplier<Appendable> destinationSupplier) throws IOException {
        String input = "hello world";

        Appendable destination = destinationSupplier.get();
        append(input, destination);
        assertEquals(input, destination.toString());

        assertThrows(NullPointerException.class, () -> append((String) null, destination));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("appendableArguments")
    @DisplayName("append(String, int, int)")
    void testAppendStringRange(@SuppressWarnings("unused") String appendableType, Supplier<Appendable> destinationSupplier)
            throws IOException {

        String input = "hello world";
        int length = input.length();

        Appendable destination = destinationSupplier.get();
        append(input, 5, 5, destination);
        assertEquals("", destination.toString());
        append(input, 3, 8, destination);
        assertEquals(input.substring(3, 8), destination.toString());

        assertThrows(NullPointerException.class, () -> append((String) null, 0, 0, destination));

        assertThrows(IndexOutOfBoundsException.class, () -> append(input, -1, length, destination));
        assertThrows(IndexOutOfBoundsException.class, () -> append(input, 0, length + 1, destination));
        assertThrows(IndexOutOfBoundsException.class, () -> append(input, 0, -1, destination));
        assertThrows(IndexOutOfBoundsException.class, () -> append(input, length + 1, length, destination));
    }

    Arguments[] appendableArguments() {
        return new Arguments[] {
                arguments(StringBuilder.class.getSimpleName(), (Supplier<Appendable>) StringBuilder::new),
                arguments(StringBuffer.class.getSimpleName(), (Supplier<Appendable>) StringBuffer::new),
                arguments(StringWriter.class.getSimpleName(), (Supplier<Appendable>) StringWriter::new),
                arguments(Appendable.class.getSimpleName(), (Supplier<Appendable>) TestAppendable::new),
        };
    }
}
