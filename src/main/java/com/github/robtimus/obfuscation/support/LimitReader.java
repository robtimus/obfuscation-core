/*
 * LimitReader.java
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

import java.io.IOException;
import java.io.Reader;

final class LimitReader extends Reader {

    private final Reader reader;

    private int remaining;
    private int mark = 0;

    LimitReader(Reader reader, int limit) {
        this.reader = reader;
        remaining = limit;
    }

    @Override
    public int read() throws IOException {
        if (remaining == 0) {
            return -1;
        }
        int read = reader.read();
        if (read == -1) {
            remaining = 0;
            return -1;
        }
        remaining--;
        return read;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        if (remaining == 0) {
            return -1;
        }
        int toRead = Math.min(len, remaining);
        int read = reader.read(cbuf, off, toRead);
        if (read == -1) {
            remaining = 0;
            return -1;
        }
        remaining -= read;
        return read;
    }

    @Override
    public long skip(long n) throws IOException {
        long toSkip = Math.min(remaining, n);
        long skipped = reader.skip(toSkip);
        // note: skipped should never be larger than remaining so the subtraction is considered safe
        remaining -= skipped;
        return skipped;
    }

    @Override
    public boolean ready() throws IOException {
        return remaining > 0 && reader.ready();
    }

    @Override
    public boolean markSupported() {
        return reader.markSupported();
    }

    @Override
    public void mark(int readAheadLimit) throws IOException {
        reader.mark(Math.min(remaining, readAheadLimit));
        mark = remaining;
    }

    @Override
    public void reset() throws IOException {
        reader.reset();
        remaining = mark;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
