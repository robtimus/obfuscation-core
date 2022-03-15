/*
 * CountingReaderTest.java
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
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SuppressWarnings("nls")
class CountingReaderTest extends StreamTestBase {

    @Test
    @DisplayName("read()")
    void testReadChar() throws IOException {
        Writer writer = new StringWriter();
        long expected = 0;
        try (CountingReader reader = new CountingReader(new CharSequenceReader(SOURCE))) {
            int c;
            while ((c = reader.read()) != -1) {
                writer.write(c);
                expected++;
                assertEquals(expected, reader.count());
            }
            assertEquals(SOURCE.length(), reader.count());
        }
        assertEquals(SOURCE, writer.toString());
    }

    @Test
    @DisplayName("read(char[], int, int)")
    void testReadCharArrayRange() throws IOException {
        Writer writer = new StringWriter();
        try (CountingReader reader = new CountingReader(new CharSequenceReader(SOURCE))) {
            assertEquals(0, reader.read(new char[5], 0, 0));
            assertEquals(0, reader.count());
            assertEquals("", writer.toString());
            copy(reader, writer, 5);
            assertEquals(SOURCE.length(), reader.count());
        }
        assertEquals(SOURCE, writer.toString());
    }

    @Test
    @DisplayName("skip(long)")
    void testSkip() throws IOException {
        Writer writer = new StringWriter();
        try (CountingReader reader = new CountingReader(new CharSequenceReader(SOURCE))) {
            char[] data = new char[10];
            int len = reader.read(data);
            assertEquals(data.length, len);
            assertEquals(len, reader.count());
            writer.write(data);
            assertEquals(0, reader.skip(0));
            assertEquals(len, reader.count());
            assertThrows(IllegalArgumentException.class, () -> reader.skip(-1));
            assertEquals(len, reader.count());
            assertEquals(10, reader.skip(10));
            assertEquals(len + 10, reader.count());
            copy(reader, writer);
            assertEquals(0, reader.skip(1));
            assertEquals(SOURCE.length(), reader.count());
        }
        assertEquals(SOURCE.substring(0, 10) + SOURCE.substring(20, SOURCE.length()), writer.toString());
    }

    @Test
    @DisplayName("ready()")
    void testReady() throws IOException {
        try (CountingReader reader = new CountingReader(new CharSequenceReader(SOURCE))) {
            for (int i = 0; i < SOURCE.length(); i++) {
                assertTrue(reader.ready());
                assertNotEquals(-1, reader.read());
            }
            assertFalse(reader.ready());
            assertEquals(-1, reader.read());
        }
    }

    @Test
    @DisplayName("mark(int) and reset()")
    void testMarkReset() throws IOException {
        Writer writer = new StringWriter();
        try (CountingReader reader = new CountingReader(new CharSequenceReader(SOURCE))) {
            assertTrue(reader.markSupported());
            reader.mark(5);
            copy(reader, writer);
            assertEquals(SOURCE.length(), reader.count());
            reader.reset();
            assertEquals(0, reader.count());
            copy(reader, writer);
            assertEquals(SOURCE.length(), reader.count());
        }
        assertEquals(SOURCE + SOURCE, writer.toString());
    }

    @Test
    @DisplayName("close()")
    void testClose() throws IOException {
        Reader input = spy(new StringReader(SOURCE));
        try (CountingReader reader = new CountingReader(input)) {
            // does nothing
        }
        verify(input).close();
    }
}
