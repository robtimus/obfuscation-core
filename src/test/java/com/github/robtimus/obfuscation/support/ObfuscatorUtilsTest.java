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
import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.checkIndex;
import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.checkOffsetAndLength;
import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.checkStartAndEnd;
import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.copyAll;
import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.copyTo;
import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.discardAll;
import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.getChars;
import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.indexOf;
import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.maskAll;
import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.readAll;
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

@SuppressWarnings({ "javadoc", "nls" })
@TestInstance(Lifecycle.PER_CLASS)
public class ObfuscatorUtilsTest {

    public static void assertClosedException(IOException exception) {
        assertEquals(Messages.stream.closed.get(), exception.getMessage());
    }

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
    @SuppressWarnings("resource")
    public void testReader() {
        assertThat(reader(""), instanceOf(CharSequenceReader.class));
        assertThrows(NullPointerException.class, () -> reader(null));
    }

    @Test
    @DisplayName("reader(CharSequence, int, int)")
    @SuppressWarnings("resource")
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
    @SuppressWarnings("resource")
    public void testCopyTo() {
        assertThat(copyTo(new StringReader(""), new StringBuilder()), instanceOf(CopyingReader.class));
        assertThrows(NullPointerException.class, () -> copyTo(null, new StringBuilder()));
        assertThrows(NullPointerException.class, () -> copyTo(new StringReader(""), null));
    }

    @Test
    @DisplayName("readAll(Reader)")
    public void testReadAll() throws IOException {
        String string = "hello world";
        StringReader input = new StringReader(string);
        assertEquals(string, readAll(input).toString());
        assertEquals(-1, input.read());
    }

    @Test
    @DisplayName("discardAll(Reader)")
    public void testDiscardAll() throws IOException {
        String string = "hello world";
        StringReader input = new StringReader(string);
        discardAll(input);
        assertEquals(-1, input.read());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("appendableArguments")
    @DisplayName("copyAll(Reader, Appendable)")
    public void testCopyAll(@SuppressWarnings("unused") String appendableType, Supplier<Appendable> destinationSupplier) throws IOException {
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
    public void testMaskAll(@SuppressWarnings("unused") String appendableType, Supplier<Appendable> destinationSupplier) throws IOException {
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
    public void testAppendInt(@SuppressWarnings("unused") String appendableType, Supplier<Appendable> destinationSupplier) throws IOException {
        int c = 1 << 16 | 'a';

        Appendable destination = destinationSupplier.get();
        append(c, destination);
        assertEquals("a", destination.toString());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("appendableArguments")
    @DisplayName("append(char, int)")
    public void testAppendRepeated(@SuppressWarnings("unused") String appendableType, Supplier<Appendable> destinationSupplier) throws IOException {
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
    public void testAppendCharArray(@SuppressWarnings("unused") String appendableType, Supplier<Appendable> destinationSupplier) throws IOException {
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
    public void testAppendCharArrayRange(@SuppressWarnings("unused") String appendableType, Supplier<Appendable> destinationSupplier)
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
    public void testAppendString(@SuppressWarnings("unused") String appendableType, Supplier<Appendable> destinationSupplier) throws IOException {
        String input = "hello world";

        Appendable destination = destinationSupplier.get();
        append(input, destination);
        assertEquals(input, destination.toString());

        assertThrows(NullPointerException.class, () -> append((String) null, destination));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("appendableArguments")
    @DisplayName("append(String, int, int)")
    public void testAppendStringRange(@SuppressWarnings("unused") String appendableType, Supplier<Appendable> destinationSupplier)
            throws IOException {

        String input = "hello world";

        Appendable destination = destinationSupplier.get();
        append(input, 5, 5, destination);
        assertEquals("", destination.toString());
        append(input, 3, 8, destination);
        assertEquals(input.substring(3, 8), destination.toString());

        assertThrows(NullPointerException.class, () -> append((String) null, 0, 0, destination));

        assertThrows(IndexOutOfBoundsException.class, () -> append(input, -1, input.length(), destination));
        assertThrows(IndexOutOfBoundsException.class, () -> append(input, 0, input.length() + 1, destination));
        assertThrows(IndexOutOfBoundsException.class, () -> append(input, 0, -1, destination));
        assertThrows(IndexOutOfBoundsException.class, () -> append(input, input.length() + 1, input.length(), destination));
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
