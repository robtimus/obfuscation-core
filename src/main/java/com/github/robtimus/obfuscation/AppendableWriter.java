/*
 * AppendableWriter.java
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
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;
import java.util.Objects;

final class AppendableWriter extends Writer {

    private final Appendable appendable;
    private CharArraySequence array;

    AppendableWriter(Appendable appendable) {
        this.appendable = appendable;
    }

    @Override
    public void write(int c) throws IOException {
        appendable.append((char) c);
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        checkOffsetAndLength(cbuf, off, len);
        if (len > 0) {
            if (appendable instanceof StringBuilder) {
                ((StringBuilder) appendable).append(cbuf, off, len);
            } else if (appendable instanceof StringBuffer) {
                ((StringBuffer) appendable).append(cbuf, off, len);
            } else {
                if (array == null) {
                    array = new CharArraySequence();
                }
                array.resetWithOffsetAndLength(cbuf, off, len);
                appendable.append(array);
            }
        }
    }

    @Override
    public void write(String str) throws IOException {
        Objects.requireNonNull(str);
        if (!str.isEmpty()) {
            appendable.append(str);
        }
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        checkOffsetAndLength(str, off, len);
        if (len > 0) {
            appendable.append(str, off, off + len);
        }
    }

    @Override
    public Writer append(CharSequence csq) throws IOException {
        appendable.append(csq);
        return this;
    }

    @Override
    public Writer append(CharSequence csq, int start, int end) throws IOException {
        appendable.append(csq, start, end);
        return this;
    }

    @Override
    public Writer append(char c) throws IOException {
        appendable.append(c);
        return this;
    }

    @Override
    public void flush() throws IOException {
        if (appendable instanceof Flushable) {
            ((Flushable) appendable).flush();
        }
    }

    @Override
    public void close() throws IOException {
        if (appendable instanceof Closeable) {
            ((Closeable) appendable).close();
        } else if (appendable instanceof AutoCloseable) {
            try {
                ((AutoCloseable) appendable).close();
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
    }
}
