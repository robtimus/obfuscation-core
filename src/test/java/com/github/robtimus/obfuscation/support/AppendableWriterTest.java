/*
 * AppendableWriterTest.java
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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.function.Supplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

@SuppressWarnings("nls")
class AppendableWriterTest extends StreamTestBase {

    @Nested
    @DisplayName("StringBuilder")
    class StringBuilderTest extends WriterTest {

        StringBuilderTest() {
            super(StringBuilder::new);
        }
    }

    @Nested
    @DisplayName("StringBuffer")
    class StringBufferTest extends WriterTest {

        StringBufferTest() {
            super(StringBuffer::new);
        }
    }

    @Nested
    @DisplayName("Appendable")
    class AppendableTest extends WriterTest {

        AppendableTest() {
            super(StringWriter::new);
        }
    }

    abstract static class WriterTest {

        private final Supplier<Appendable> appendableSupplier;

        WriterTest(Supplier<Appendable> appendableSupplier) {
            this.appendableSupplier = appendableSupplier;
        }

        @Test
        @DisplayName("write(int)")
        void testWriteInt() throws IOException {
            Appendable appendable = appendableSupplier.get();
            try (Writer writer = new AppendableWriter(appendable)) {
                for (int i = 0; i < SOURCE.length(); i++) {
                    writer.write(SOURCE.charAt(i));
                }
            }
            assertEquals(SOURCE, appendable.toString());
        }

        @Test
        @DisplayName("write(char[])")
        void testWriteCharArray() throws IOException {
            Appendable appendable = appendableSupplier.get();
            try (Writer writer = new AppendableWriter(appendable)) {
                writer.write(SOURCE.toCharArray());
                writer.write(SOURCE.toCharArray());
            }
            assertEquals(SOURCE + SOURCE, appendable.toString());
        }

        @Test
        @DisplayName("write(char[], int, int)")
        void testWriteCharArrayRange() throws IOException {
            Appendable appendable = appendableSupplier.get();
            try (Writer writer = new AppendableWriter(appendable)) {
                char[] content = SOURCE.toCharArray();
                int index = 0;
                writer.write(content, index, 0);
                while (index < SOURCE.length()) {
                    int to = Math.min(index + 5, SOURCE.length());
                    writer.write(content, index, to - index);
                    index = to;
                }

                assertThrows(IndexOutOfBoundsException.class, () -> writer.write(content, 0, content.length + 1));
                assertThrows(IndexOutOfBoundsException.class, () -> writer.write(content, -1, content.length));
                assertThrows(IndexOutOfBoundsException.class, () -> writer.write(content, 1, content.length));
                assertThrows(IndexOutOfBoundsException.class, () -> writer.write(content, 0, -1));
            }
            assertEquals(SOURCE, appendable.toString());
        }

        @Test
        @DisplayName("write(String)")
        void testWriteString() throws IOException {
            Appendable appendable = appendableSupplier.get();
            try (Writer writer = new AppendableWriter(appendable)) {
                writer.write("");
                writer.write(SOURCE);
            }
            assertEquals(SOURCE, appendable.toString());
        }

        @Test
        @DisplayName("write(String, int, int)")
        void testWriteStringRange() throws IOException {
            Appendable appendable = appendableSupplier.get();
            try (Writer writer = new AppendableWriter(appendable)) {
                int length = SOURCE.length();

                int index = 0;
                writer.write(SOURCE, index, 0);
                while (index < length) {
                    int to = Math.min(index + 5, length);
                    writer.write(SOURCE, index, to - index);
                    index = to;
                }

                assertThrows(IndexOutOfBoundsException.class, () -> writer.write(SOURCE, 0, length + 1));
                assertThrows(IndexOutOfBoundsException.class, () -> writer.write(SOURCE, -1, length));
                assertThrows(IndexOutOfBoundsException.class, () -> writer.write(SOURCE, 1, length));
                assertThrows(IndexOutOfBoundsException.class, () -> writer.write(SOURCE, 0, -1));
            }
            assertEquals(SOURCE, appendable.toString());
        }

        @Test
        @DisplayName("append(CharSequence)")
        void testAppendCharSequence() throws IOException {
            Appendable appendable = appendableSupplier.get();
            try (Writer writer = new AppendableWriter(appendable)) {
                writer.append(SOURCE);
                writer.append(null);
            }
            assertEquals(SOURCE + "null", appendable.toString());
        }

        @Test
        @DisplayName("append(CharSequence, int, int)")
        void testAppendCharSequenceRange() throws IOException {
            Appendable appendable = appendableSupplier.get();
            try (Writer writer = new AppendableWriter(appendable)) {
                int length = SOURCE.length();

                int index = 0;
                while (index < length) {
                    int to = Math.min(index + 5, length);
                    writer.append(SOURCE, index, to);
                    index = to;
                }
                writer.append(null, 0, 2);
                writer.append(null, 2, 4);

                assertThrows(IndexOutOfBoundsException.class, () -> writer.append(SOURCE, 0, length + 1));
                assertThrows(IndexOutOfBoundsException.class, () -> writer.append(SOURCE, -1, length));
                assertThrows(IndexOutOfBoundsException.class, () -> writer.append(SOURCE, 1, 0));

                assertThrows(IndexOutOfBoundsException.class, () -> writer.append(null, 0, 5));
                assertThrows(IndexOutOfBoundsException.class, () -> writer.append(null, -1, 4));
                assertThrows(IndexOutOfBoundsException.class, () -> writer.append(null, 1, 0));
            }
            assertEquals(SOURCE + "null", appendable.toString());
        }

        @Test
        @DisplayName("append(char)")
        void testAppendChar() throws IOException {
            Appendable appendable = appendableSupplier.get();
            try (Writer writer = new AppendableWriter(appendable)) {
                for (int i = 0; i < SOURCE.length(); i++) {
                    writer.append(SOURCE.charAt(i));
                }
            }
            assertEquals(SOURCE, appendable.toString());
        }
    }

    @TestFactory
    @DisplayName("flush()")
    DynamicTest[] testFlush() {
        return new DynamicTest[] {
                dynamicTest("not Flushable", () -> {
                    Appendable appendable = mock(Appendable.class);
                    try (Writer writer = new AppendableWriter(appendable)) {
                        writer.flush();
                    }
                    verifyNoMoreInteractions(appendable);
                }),
                dynamicTest("Flushable", () -> {
                    FlushableAppendable flushable = spy(new FlushableAppendable());
                    try (Writer writer = new AppendableWriter(flushable)) {
                        writer.flush();
                    }

                    verify(flushable).flush();
                    verifyNoMoreInteractions(flushable);
                }),
        };
    }

    @TestFactory
    @DisplayName("close()")
    DynamicTest[] testClose() throws Exception {
        return new DynamicTest[] {
                dynamicTest("not Closeable", () -> {
                    Appendable appendable = mock(Appendable.class);
                    try (Writer writer = new AppendableWriter(appendable)) {
                        // does nothing
                    }
                    verifyNoMoreInteractions(appendable);
                }),
                dynamicTest("Closeable, not throwing", () -> {
                    @SuppressWarnings("resource")
                    CloseableAppendable closeable = spy(new CloseableAppendable());
                    try (Writer writer = new AppendableWriter(closeable)) {
                        // does nothing
                    }
                    verify(closeable).close();
                    verifyNoMoreInteractions(closeable);
                }),
                dynamicTest("Closeable, throwing", () -> {
                    @SuppressWarnings("resource")
                    CloseableAppendable closeable = spy(new CloseableAppendable());
                    IOException exception = new IOException();
                    doThrow(exception).when(closeable).close();
                    IOException thrown = assertThrows(IOException.class, () -> {
                        try (Writer writer = new AppendableWriter(closeable)) {
                            // does nothing
                        }
                    });
                    assertSame(exception, thrown);

                    verify(closeable).close();
                    verifyNoMoreInteractions(closeable);
                }),
                dynamicTest("AutoCloseable, not throwing", () -> {
                    @SuppressWarnings("resource")
                    AutoCloseableAppendable autoCloseable = spy(new AutoCloseableAppendable());
                    try (Writer writer = new AppendableWriter(autoCloseable)) {
                        // does nothing
                    }
                    verify(autoCloseable).close();
                    verifyNoMoreInteractions(autoCloseable);
                }),
                dynamicTest("AutoCloseable, throwing IOException", () -> {
                    @SuppressWarnings("resource")
                    AutoCloseableAppendable autoCloseable = spy(new AutoCloseableAppendable());
                    IOException exception = new IOException();
                    doThrow(exception).when(autoCloseable).close();
                    IOException thrown = assertThrows(IOException.class, () -> {
                        try (Writer writer = new AppendableWriter(autoCloseable)) {
                            // does nothing
                        }
                    });
                    assertSame(exception, thrown);

                    verify(autoCloseable).close();
                    verifyNoMoreInteractions(autoCloseable);
                }),
                dynamicTest("AutoCloseable, throwing non-IOException", () -> {
                    @SuppressWarnings("resource")
                    AutoCloseableAppendable autoCloseable = spy(new AutoCloseableAppendable());
                    IllegalStateException exception = new IllegalStateException();
                    doThrow(exception).when(autoCloseable).close();
                    IOException thrown = assertThrows(IOException.class, () -> {
                        try (Writer writer = new AppendableWriter(autoCloseable)) {
                            // does nothing
                        }
                    });
                    assertSame(exception, thrown.getCause());

                    verify(autoCloseable).close();
                    verifyNoMoreInteractions(autoCloseable);
                }),
        };
    }
}
