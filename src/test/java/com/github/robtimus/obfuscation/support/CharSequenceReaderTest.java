/*
 * CharSequenceReaderTest.java
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@SuppressWarnings({ "javadoc", "nls" })
public class CharSequenceReaderTest extends StreamTestBase {

    @Test
    @DisplayName("read()")
    public void testRead() throws IOException {
        Writer writer = new StringWriter();
        try (Reader reader = new CharSequenceReader(SOURCE, 1, SOURCE.length() - 1)) {
            int c;
            while ((c = reader.read()) != -1) {
                writer.write(c);
            }
        }
        assertEquals(SOURCE.substring(1, SOURCE.length() - 1), writer.toString());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    @DisplayName("read(char[], int, int)")
    public void testReadBulk(@SuppressWarnings("unused") String displayName, CharSequence source, String expected) throws IOException {
        Writer writer = new StringWriter();
        try (Reader reader = new CharSequenceReader(source, 1, source.length() - 1)) {
            assertEquals(0, reader.read(new char[5], 0, 0));
            assertEquals("", writer.toString());
            copy(reader, writer, 5);
        }
        assertEquals(expected, writer.toString());
    }

    static Arguments[] testReadBulk() {
        final String expected = SOURCE.substring(1, SOURCE.length() - 1);
        return new Arguments[] {
                arguments(String.class.getSimpleName(), SOURCE, expected),
                arguments(StringBuilder.class.getSimpleName(), new StringBuilder(SOURCE), expected),
                arguments(StringBuffer.class.getSimpleName(), new StringBuffer(SOURCE), expected),
                arguments(CharSequence.class.getSimpleName(), new CharArraySequence(SOURCE.toCharArray()), expected),
        };
    }

    @Test
    @DisplayName("skip(long)")
    public void testSkip() throws IOException {
        Writer writer = new StringWriter();
        try (Reader reader = new CharSequenceReader(SOURCE, 1, SOURCE.length() - 1)) {
            char[] data = new char[10];
            int len = reader.read(data);
            assertEquals(data.length, len);
            writer.write(data);
            assertEquals(0, reader.skip(0));
            assertThrows(IllegalArgumentException.class, () -> reader.skip(-1));
            assertEquals(10, reader.skip(10));
            copy(reader, writer);
            assertEquals(0, reader.skip(1));
        }
        assertEquals(SOURCE.substring(1, 11) + SOURCE.substring(21, SOURCE.length() - 1), writer.toString());
    }

    @Test
    @DisplayName("ready()")
    public void testReady() throws IOException {
        try (Reader reader = new CharSequenceReader(SOURCE, 1, SOURCE.length() - 1)) {
            for (int i = 1; i < SOURCE.length() - 1; i++) {
                assertTrue(reader.ready());
                assertNotEquals(-1, reader.read());
            }
            assertFalse(reader.ready());
            assertEquals(-1, reader.read());
        }
    }

    @Test
    @DisplayName("mark(int) and reset()")
    public void testMarkReset() throws IOException {
        Writer writer = new StringWriter();
        try (Reader reader = new CharSequenceReader(SOURCE, 1, SOURCE.length() - 1)) {
            assertTrue(reader.markSupported());
            reader.mark(5);
            copy(reader, writer);
            reader.reset();
            copy(reader, writer);
        }
        String singleExpected = SOURCE.substring(1, SOURCE.length() - 1);
        assertEquals(singleExpected + singleExpected, writer.toString());
    }

    @Test
    @DisplayName("operations on closed stream")
    public void testOperationsOnClosedStream() throws IOException {
        @SuppressWarnings("resource")
        Reader reader = new CharSequenceReader(SOURCE);
        reader.close();
        assertClosed(() -> reader.read());
        assertClosed(() -> reader.read(new char[0], 0, 0));
        assertClosed(() -> reader.skip(0));
        assertClosed(() -> reader.ready());
        assertClosed(() -> reader.mark(1));
        assertClosed(() -> reader.reset());
        reader.close();
    }
}
