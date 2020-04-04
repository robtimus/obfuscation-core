/*
 * StreamTestBase.java
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import org.junit.jupiter.api.function.Executable;

@SuppressWarnings("nls")
abstract class StreamTestBase {

    static final String SOURCE = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa.";
    static final String LONG_SOURCE;

    static {
        StringBuilder sb = new StringBuilder(1000 * SOURCE.length());
        for (int i = 0; i < 1000; i++) {
            sb.append(SOURCE);
        }
        LONG_SOURCE = sb.toString();
    }

    void copy(InputStream input, OutputStream output) throws IOException {
        copy(input, output, 4096);
    }

    void copy(InputStream input, OutputStream output, int bufferSize) throws IOException {
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = input.read(buffer)) != -1) {
            output.write(buffer, 0, len);
        }
    }

    void copy(Reader reader, Writer writer) throws IOException {
        copy(reader, writer, 4096);
    }

    void copy(Reader reader, Writer writer, int bufferSize) throws IOException {
        char[] buffer = new char[bufferSize];
        int len;
        while ((len = reader.read(buffer)) != -1) {
            writer.write(buffer, 0, len);
        }
    }

    void assertClosed(Executable executable) {
        IOException thrown = assertThrows(IOException.class, executable);
        assertEquals(Messages.stream.closed.get(), thrown.getMessage());
    }

    // the following classes are not final so they can be spied / mocked

    static class FlushableAppendable implements Appendable, Flushable {

        @Override
        public Appendable append(CharSequence csq) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Appendable append(CharSequence csq, int start, int end) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Appendable append(char c) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void flush() throws IOException {
            // does nothing
        }
    }

    static class CloseableAppendable implements Appendable, Closeable {

        @Override
        public Appendable append(CharSequence csq) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Appendable append(CharSequence csq, int start, int end) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Appendable append(char c) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void close() throws IOException {
            // does nothing
        }
    }

    static class AutoCloseableAppendable implements Appendable, AutoCloseable {

        @Override
        public Appendable append(CharSequence csq) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Appendable append(CharSequence csq, int start, int end) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Appendable append(char c) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void close() throws Exception {
            // does nothing
        }
    }
}
