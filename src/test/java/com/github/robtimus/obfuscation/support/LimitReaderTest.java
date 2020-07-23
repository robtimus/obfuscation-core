/*
 * LimitReaderTest.java
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

import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.readAtMost;
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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings("nls")
class LimitReaderTest extends StreamTestBase {

    @Nested
    @DisplayName("zero limit")
    class ZeroLimit {

        @Test
        @DisplayName("read()")
        void testRead() throws IOException {
            Writer writer = new StringWriter();
            try (CharSequenceReader input = new CharSequenceReader(SOURCE);
                    Reader reader = readAtMost(input, 0)) {

                int c;
                while ((c = reader.read()) != -1) {
                    writer.write(c);
                }
                assertEquals(SOURCE.charAt(0), (char) input.read());
            }
            assertEquals("", writer.toString());
        }

        @Test
        @DisplayName("read(char[], int, int)")
        void testReadBulk() throws IOException {
            Writer writer = new StringWriter();
            try (CharSequenceReader input = new CharSequenceReader(SOURCE);
                    Reader reader = readAtMost(input, 0)) {

                char[] buffer = new char[4];
                int len;
                while ((len = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, len);
                }
                assertEquals(SOURCE.charAt(0), (char) input.read());
            }
            assertEquals("", writer.toString());
        }

        @Test
        @DisplayName("skip(long)")
        void testSkip() throws IOException {
            try (CharSequenceReader input = new CharSequenceReader(SOURCE);
                    Reader reader = readAtMost(input, 0)) {

                assertEquals(0, reader.skip(1));
                assertEquals(-1, reader.read());
                assertEquals(SOURCE.charAt(0), (char) input.read());
            }
        }

        @Test
        @DisplayName("ready()")
        void testReady() throws IOException {
            try (CharSequenceReader input = new CharSequenceReader(SOURCE);
                    Reader reader = readAtMost(input, 0)) {

                assertFalse(reader.ready());
                assertEquals(-1, reader.read());
                assertTrue(input.ready());
                assertEquals(SOURCE.charAt(0), (char) input.read());
            }
        }

        @Test
        @DisplayName("mark(int) and reset()")
        void testMarkReset() throws IOException {
            Writer writer = new StringWriter();
            try (CharSequenceReader input = new CharSequenceReader(SOURCE);
                    Reader reader = readAtMost(input, 0)) {

                assertTrue(reader.markSupported());
                assertEquals(-1, reader.read());
                reader.mark(10);
                copy(reader, writer);
                reader.reset();
                copy(reader, writer);
                assertEquals(SOURCE.charAt(0), (char) input.read());
            }
            assertEquals("", writer.toString());
        }
    }

    @Nested
    @DisplayName("limit smaller than input size")
    class LimitSmallerThanInputSize {

        @Test
        @DisplayName("read()")
        void testRead() throws IOException {
            Writer writer = new StringWriter();
            try (CharSequenceReader input = new CharSequenceReader(SOURCE);
                    Reader reader = readAtMost(input, 5)) {

                int c;
                while ((c = reader.read()) != -1) {
                    writer.write(c);
                }
                assertEquals(SOURCE.charAt(5), (char) input.read());
            }
            assertEquals(SOURCE.substring(0, 5), writer.toString());
        }

        @Test
        @DisplayName("read(char[], int, int)")
        void testReadBulk() throws IOException {
            Writer writer = new StringWriter();
            try (CharSequenceReader input = new CharSequenceReader(SOURCE);
                    Reader reader = readAtMost(input, 5)) {

                char[] buffer = new char[4];
                int len;
                while ((len = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, len);
                }
                assertEquals(SOURCE.charAt(5), (char) input.read());
            }
            assertEquals(SOURCE.substring(0, 5), writer.toString());
        }

        @Test
        @DisplayName("skip(long)")
        void testSkip() throws IOException {
            try (CharSequenceReader input = new CharSequenceReader(SOURCE);
                    Reader reader = readAtMost(input, 5)) {

                assertEquals(1, reader.skip(1));
                assertEquals(SOURCE.charAt(1), (char) reader.read());
                assertEquals(3, reader.skip(5));
                assertEquals(0, reader.skip(1));
                assertEquals(SOURCE.charAt(5), (char) input.read());
            }
        }

        @Test
        @DisplayName("ready()")
        void testReady() throws IOException {
            try (CharSequenceReader input = new CharSequenceReader(SOURCE);
                    Reader reader = readAtMost(input, 5)) {

                for (int i = 0; i < 5; i++) {
                    assertTrue(reader.ready());
                    assertNotEquals(-1, reader.read());
                }
                assertFalse(reader.ready());
                assertEquals(-1, reader.read());
                assertTrue(input.ready());
                assertEquals(SOURCE.charAt(5), (char) input.read());
            }
        }

        @Test
        @DisplayName("mark(int) and reset()")
        void testMarkReset() throws IOException {
            Writer writer = new StringWriter();
            try (CharSequenceReader input = new CharSequenceReader(SOURCE);
                    Reader reader = readAtMost(input, 5)) {

                assertTrue(reader.markSupported());
                assertEquals(SOURCE.charAt(0), (char) reader.read());
                reader.mark(10);
                copy(reader, writer);
                reader.reset();
                copy(reader, writer);
                assertEquals(SOURCE.charAt(5), (char) input.read());
            }
            String singleExpected = SOURCE.substring(1, 5);
            assertEquals(singleExpected + singleExpected, writer.toString());
        }
    }

    @Nested
    @DisplayName("limit equal to input size")
    class LimitEqualToInputSize {

        @Test
        @DisplayName("read()")
        void testRead() throws IOException {
            Writer writer = new StringWriter();
            try (CharSequenceReader input = new CharSequenceReader(SOURCE);
                    Reader reader = readAtMost(input, SOURCE.length())) {

                int c;
                while ((c = reader.read()) != -1) {
                    writer.write(c);
                }
                assertEquals(-1, input.read());
            }
            assertEquals(SOURCE, writer.toString());
        }

        @Test
        @DisplayName("read(char[], int, int)")
        void testReadBulk() throws IOException {
            Writer writer = new StringWriter();
            try (CharSequenceReader input = new CharSequenceReader(SOURCE);
                    Reader reader = readAtMost(input, SOURCE.length())) {

                char[] buffer = new char[4];
                int len;
                while ((len = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, len);
                }
                assertEquals(-1, input.read());
            }
            assertEquals(SOURCE, writer.toString());
        }

        @Test
        @DisplayName("skip(long)")
        void testSkip() throws IOException {
            Writer writer = new StringWriter();
            try (CharSequenceReader input = new CharSequenceReader(SOURCE);
                    Reader reader = readAtMost(input, SOURCE.length())) {

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
        }

        @Test
        @DisplayName("ready()")
        void testReady() throws IOException {
            try (CharSequenceReader input = new CharSequenceReader(SOURCE);
                    Reader reader = readAtMost(input, SOURCE.length())) {

                for (int i = 0; i < SOURCE.length(); i++) {
                    assertTrue(reader.ready());
                    assertNotEquals(-1, reader.read());
                }
                assertFalse(reader.ready());
                assertEquals(-1, reader.read());
                assertFalse(input.ready());
                assertEquals(-1, input.read());
            }
        }

        @Test
        @DisplayName("mark(int) and reset()")
        void testMarkReset() throws IOException {
            Writer writer = new StringWriter();
            try (CharSequenceReader input = new CharSequenceReader(SOURCE);
                    Reader reader = readAtMost(input, SOURCE.length())) {

                assertTrue(reader.markSupported());
                reader.mark(5);
                copy(reader, writer);
                reader.reset();
                copy(reader, writer);
            }
            String singleExpected = SOURCE;
            assertEquals(singleExpected + singleExpected, writer.toString());
        }
    }

    @Nested
    @DisplayName("limit larger than input size")
    class LimitLargerThanInputSize {

        @Test
        @DisplayName("read()")
        void testRead() throws IOException {
            Writer writer = new StringWriter();
            try (CharSequenceReader input = new CharSequenceReader(SOURCE);
                    Reader reader = readAtMost(input, SOURCE.length() * 2)) {

                int c;
                while ((c = reader.read()) != -1) {
                    writer.write(c);
                }
                assertEquals(-1, input.read());
            }
            assertEquals(SOURCE, writer.toString());
        }

        @Test
        @DisplayName("read(char[], int, int)")
        void testReadBulk() throws IOException {
            Writer writer = new StringWriter();
            try (CharSequenceReader input = new CharSequenceReader(SOURCE);
                    Reader reader = readAtMost(input, SOURCE.length() * 2)) {

                char[] buffer = new char[4];
                int len;
                while ((len = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, len);
                }
                assertEquals(-1, input.read());
            }
            assertEquals(SOURCE, writer.toString());
        }

        @Test
        @DisplayName("skip(long)")
        void testSkip() throws IOException {
            Writer writer = new StringWriter();
            try (CharSequenceReader input = new CharSequenceReader(SOURCE);
                    Reader reader = readAtMost(input, SOURCE.length() * 2)) {

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
        }

        @Test
        @DisplayName("ready()")
        void testReady() throws IOException {
            try (CharSequenceReader input = new CharSequenceReader(SOURCE);
                    Reader reader = readAtMost(input, SOURCE.length() * 2)) {

                for (int i = 0; i < SOURCE.length(); i++) {
                    assertTrue(reader.ready());
                    assertNotEquals(-1, reader.read());
                }
                assertFalse(reader.ready());
                assertEquals(-1, reader.read());
                assertFalse(input.ready());
                assertEquals(-1, input.read());
            }
        }

        @Test
        @DisplayName("mark(int) and reset()")
        void testMarkReset() throws IOException {
            Writer writer = new StringWriter();
            try (CharSequenceReader input = new CharSequenceReader(SOURCE);
                    Reader reader = readAtMost(input, SOURCE.length() * 2)) {

                assertTrue(reader.markSupported());
                reader.mark(5);
                copy(reader, writer);
                reader.reset();
                copy(reader, writer);
            }
            String singleExpected = SOURCE;
            assertEquals(singleExpected + singleExpected, writer.toString());
        }
    }

    @Test
    @DisplayName("close()")
    void testOperationsOnClosedStream() throws IOException {
        StringReader input = spy(new StringReader(SOURCE));
        try (Reader reader = readAtMost(input, 5)) {
            // do nothing
        }
        verify(input).close();
    }
}
