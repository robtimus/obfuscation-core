/*
 * CharSequenceReader.java
 * Copyright 2019 Rob Spoor
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

package com.github.robtimus.obfuscation;

import static com.github.robtimus.obfuscation.ObfuscatorUtils.checkOffsetAndLength;
import java.io.IOException;
import java.io.Reader;

final class CharSequenceReader extends Reader {

    private CharSequence s;
    private final int end;

    private int index;
    private int mark;

    CharSequenceReader(CharSequence s) {
        this(s, 0, s.length());
    }

    CharSequenceReader(CharSequence s, int start, int end) {
        this.s = s;
        this.end = end;

        index = start;
        mark = index;
    }

    private void checkClosed() throws IOException {
        if (s == null) {
            throw new IOException(Messages.stream.closed.get());
        }
    }

    @Override
    public int read() throws IOException {
        checkClosed();
        return index < end ? s.charAt(index++) : -1;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        checkClosed();
        checkOffsetAndLength(cbuf, off, len);
        if (len == 0) {
            return 0;
        }
        if (index >= end) {
            return -1;
        }
        int read = Math.min(len, end - index);
        if (s instanceof String) {
            ((String) s).getChars(index, index + read, cbuf, off);
            index += read;
        } else if (s instanceof StringBuilder) {
            ((StringBuilder) s).getChars(index, index + read, cbuf, off);
            index += read;
        } else if (s instanceof StringBuffer) {
            ((StringBuffer) s).getChars(index, index + read, cbuf, off);
            index += read;
        } else {
            for (int i = 0, j = off; i < len && index < end; i++, j++, index++) {
                cbuf[j] = s.charAt(index);
            }
        }
        return read;
    }

    @Override
    public long skip(long n) throws IOException {
        checkClosed();
        if (n < 0) {
            throw new IllegalArgumentException(n + " < 0"); //$NON-NLS-1$
        }
        if (n == 0 || index >= end) {
            return 0;
        }
        int newIndex = (int) Math.min(end, index + Math.min(n, Integer.MAX_VALUE));
        long skipped = newIndex - index;
        index = newIndex;
        return skipped;
    }

    @Override
    public boolean ready() throws IOException {
        checkClosed();
        return index < end;
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public void mark(int readAheadLimit) throws IOException {
        checkClosed();
        mark = index;
    }

    @Override
    public void reset() throws IOException {
        checkClosed();
        index = mark;
    }

    @Override
    public void close() throws IOException {
        if (s != null) {
            s = null;
        }
    }
}
