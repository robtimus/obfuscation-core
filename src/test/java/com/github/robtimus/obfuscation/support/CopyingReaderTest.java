/*
 * CopyingReaderTest.java
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
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

@SuppressWarnings("nls")
class CopyingReaderTest extends StreamTestBase {

    @Test
    @DisplayName("read()")
    void testReadChar() throws IOException {
        Writer writer = new StringWriter();
        Appendable appendable = new StringBuilder();
        try (Reader reader = new CopyingReader(new CharSequenceReader(SOURCE), appendable)) {
            int c;
            while ((c = reader.read()) != -1) {
                writer.write(c);
            }
        }
        assertEquals(SOURCE, writer.toString());
        assertEquals(SOURCE, appendable.toString());
    }

    @Test
    @DisplayName("read(char[], int, int)")
    void testReadCharArrayRange() throws IOException {
        Writer writer = new StringWriter();
        Appendable appendable = new StringBuilder();
        try (Reader reader = new CopyingReader(new CharSequenceReader(SOURCE), appendable)) {
            assertEquals(0, reader.read(new char[5], 0, 0));
            assertEquals("", writer.toString());
            copy(reader, writer, 5);
        }
        assertEquals(SOURCE, writer.toString());
        assertEquals(SOURCE, appendable.toString());
    }

    @Test
    @DisplayName("skip(long)")
    void testSkip() throws IOException {
        Writer writer = new StringWriter();
        Appendable appendable = new StringBuilder();
        try (Reader reader = new CopyingReader(new CharSequenceReader(SOURCE), appendable)) {
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
        assertEquals(SOURCE.substring(0, 10) + SOURCE.substring(20, SOURCE.length()), writer.toString());
        assertEquals(SOURCE, appendable.toString());
    }

    @Test
    @DisplayName("ready()")
    void testReady() throws IOException {
        try (Reader reader = new CopyingReader(new CharSequenceReader(SOURCE), new StringBuilder())) {
            for (int i = 0; i < SOURCE.length(); i++) {
                assertTrue(reader.ready());
                assertNotEquals(-1, reader.read());
            }
            assertFalse(reader.ready());
            assertEquals(-1, reader.read());
        }
    }

    @TestFactory
    @DisplayName("mark(int) and reset()")
    DynamicTest[] testMarkReset() {
        return new DynamicTest[] {
                dynamicTest("Writer", () -> {
                    Writer writer = new StringWriter();
                    Appendable appendable = new StringWriter();
                    try (Reader reader = new CopyingReader(new CharSequenceReader(SOURCE), appendable)) {
                        assertFalse(reader.markSupported());
                        assertThrows(IOException.class, () -> reader.mark(5));
                        copy(reader, writer);
                        assertThrows(IOException.class, reader::reset);
                    }
                    assertEquals(SOURCE, writer.toString());
                    assertEquals(SOURCE, appendable.toString());
                }),
                dynamicTest("StringBuilder with CharSequenceReader", () -> {
                    Writer writer = new StringWriter();
                    Appendable appendable = new StringBuilder();
                    try (Reader reader = new CopyingReader(new CharSequenceReader(SOURCE), appendable)) {
                        assertTrue(reader.markSupported());
                        reader.mark(5);
                        copy(reader, writer);
                        reader.reset();
                        copy(reader, writer);
                    }
                    assertEquals(SOURCE + SOURCE, writer.toString());
                    assertEquals(SOURCE, appendable.toString());
                }),
                dynamicTest("StringBuilder with InputStreamReader", () -> {
                    Writer writer = new StringWriter();
                    Appendable appendable = new StringBuilder();
                    try (Reader reader = new CopyingReader(new InputStreamReader(new ByteArrayInputStream(SOURCE.getBytes())), appendable)) {
                        // InputStreamReader does not support mark or reset
                        assertFalse(reader.markSupported());
                        assertThrows(IOException.class, () -> reader.mark(5));
                        copy(reader, writer);
                        assertThrows(IOException.class, reader::reset);
                    }
                    assertEquals(SOURCE, writer.toString());
                    assertEquals(SOURCE, appendable.toString());
                }),
                dynamicTest("StringBuffer with CharSequenceReader", () -> {
                    Writer writer = new StringWriter();
                    Appendable appendable = new StringBuffer();
                    try (Reader reader = new CopyingReader(new CharSequenceReader(SOURCE), appendable)) {
                        assertTrue(reader.markSupported());
                        reader.mark(5);
                        copy(reader, writer);
                        reader.reset();
                        copy(reader, writer);
                    }
                    assertEquals(SOURCE + SOURCE, writer.toString());
                    assertEquals(SOURCE, appendable.toString());
                }),
                dynamicTest("StringBuffer with InputStreamReader", () -> {
                    Writer writer = new StringWriter();
                    Appendable appendable = new StringBuffer();
                    try (Reader reader = new CopyingReader(new InputStreamReader(new ByteArrayInputStream(SOURCE.getBytes())), appendable)) {
                        // InputStreamReader does not support mark or reset
                        assertFalse(reader.markSupported());
                        assertThrows(IOException.class, () -> reader.mark(5));
                        copy(reader, writer);
                        assertThrows(IOException.class, reader::reset);
                    }
                    assertEquals(SOURCE, writer.toString());
                    assertEquals(SOURCE, appendable.toString());
                }),
        };
    }

    @Test
    @DisplayName("close()")
    void testClose() throws IOException {
        Reader input = spy(new StringReader(SOURCE));
        @SuppressWarnings("resource")
        Appendable appendable = spy(new CloseableAppendable());
        try (Reader reader = new CopyingReader(input, appendable)) {
            // does nothing
        }
        verify(input).close();
        verifyNoMoreInteractions(input, appendable);
    }
}
