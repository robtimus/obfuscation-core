/*
 * CopyingReader.java
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
import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.checkOffsetAndLength;
import java.io.IOException;
import java.io.Reader;

final class CopyingReader extends Reader {

    private final Reader input;
    private final Appendable appendable;

    private int mark;

    CopyingReader(Reader input, Appendable appendable) {
        this.input = input;
        this.appendable = appendable;

        mark = 0;
    }

    @Override
    public int read() throws IOException {
        int read = input.read();
        if (read != -1) {
            append(read, appendable);
        }
        return read;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        checkOffsetAndLength(cbuf, off, len);
        int read = input.read(cbuf, off, len);
        if (read != -1) {
            append(cbuf, off, off + read, appendable);
        }
        return read;
    }

    @Override
    public boolean ready() throws IOException {
        return input.ready();
    }

    // support mark and reset only if the tee is a StringBuilder or StringBuffer, as these support deleting

    @Override
    public boolean markSupported() {
        return input.markSupported() && (appendable instanceof StringBuilder || appendable instanceof StringBuffer);
    }

    @Override
    public void mark(int readAheadLimit) throws IOException {
        if (appendable instanceof StringBuilder) {
            input.mark(readAheadLimit);
            mark = ((StringBuilder) appendable).length();
        } else if (appendable instanceof StringBuffer) {
            input.mark(readAheadLimit);
            mark = ((StringBuffer) appendable).length();
        } else {
            super.mark(readAheadLimit);
        }
    }

    @Override
    public void reset() throws IOException {
        if (appendable instanceof StringBuilder) {
            input.reset();
            StringBuilder sb = (StringBuilder) appendable;
            sb.delete(mark, sb.length());
        } else if (appendable instanceof StringBuffer) {
            input.reset();
            StringBuffer sb = (StringBuffer) appendable;
            sb.delete(mark, sb.length());
        } else {
            super.reset();
        }
    }

    @Override
    public void close() throws IOException {
        input.close();
    }
}
