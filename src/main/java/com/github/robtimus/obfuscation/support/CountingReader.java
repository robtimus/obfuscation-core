/*
 * CountingReader.java
 * Copyright 2022 Rob Spoor
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

import java.io.IOException;
import java.io.Reader;
import java.util.Objects;

/**
 * A {@link Reader} that counts the number of characters that are read.
 *
 * @author Rob Spoor
 * @since 1.4
 */
public class CountingReader extends Reader {

    private final Reader reader;

    private long count;
    private long mark;

    CountingReader(Reader reader) {
        this.reader = Objects.requireNonNull(reader);
        this.count = 0;
        this.mark = 0;
    }

    /**
     * Returns the number of characters read so far. It also includes the number of skipped characters.
     *
     * @return The number of characters read or skipped so far.
     */
    public long count() {
        return count;
    }

    @Override
    public int read() throws IOException {
        int result = reader.read();
        if (result != -1) {
            count++;
        }
        return result;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int result = reader.read(cbuf, off, len);
        if (result != -1) {
            count += result;
        }
        return result;
    }

    @Override
    public long skip(long n) throws IOException {
        long result = reader.skip(n);
        count += result;
        return result;
    }

    @Override
    public boolean ready() throws IOException {
        return reader.ready();
    }

    @Override
    public boolean markSupported() {
        return reader.markSupported();
    }

    @Override
    public void mark(int readAheadLimit) throws IOException {
        reader.mark(readAheadLimit);
        mark = count;
    }

    @Override
    public void reset() throws IOException {
        reader.reset();
        count = mark;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
