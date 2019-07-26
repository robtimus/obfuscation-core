/*
 * CachingObfuscatingWriter.java
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

import java.io.IOException;
import java.io.Writer;
import java.util.Objects;

/**
 * A writer that caches all contents until it is closed. The cached contents will then be obfuscated and appended to the writer's destination.
 * This class can be useful for obfuscators that need to examine all contents before being able to perform obfuscation.
 *
 * @author Rob Spoor
 */
public final class CachingObfuscatingWriter extends ObfuscatingWriter {

    private final Obfuscator obfuscator;
    private final Appendable destination;
    private final StringBuilder content;

    /**
     * Creates a new writer.
     *
     * @param obfuscator The obfuscator to use for obfuscating text.
     * @param destination The destination to write to.
     * @throws NullPointerException If the obfuscator or destination is {@code null}.
     */
    public CachingObfuscatingWriter(Obfuscator obfuscator, Appendable destination) {
        this.obfuscator = Objects.requireNonNull(obfuscator);
        this.destination = Objects.requireNonNull(destination);
        content = new StringBuilder();
    }

    /**
     * Creates a new writer.
     *
     * @param obfuscator The obfuscator to use for obfuscating text.
     * @param destination The destination to write to.
     * @param capacity The initial capacity.
     * @throws NullPointerException If the obfuscator or destination is {@code null}.
     * @throws NegativeArraySizeException If the capacity is negative.
     */
    public CachingObfuscatingWriter(Obfuscator obfuscator, Appendable destination, int capacity) {
        this.obfuscator = Objects.requireNonNull(obfuscator);
        this.destination = Objects.requireNonNull(destination);
        content = new StringBuilder(capacity);
    }

    @Override
    public void write(int c) throws IOException {
        checkClosed();
        content.append((char) c);
    }

    @Override
    public void write(char[] cbuf) throws IOException {
        checkClosed();
        content.append(cbuf);
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        checkClosed();
        content.append(cbuf, off, len);
    }

    @Override
    public void write(String str) throws IOException {
        checkClosed();
        Objects.requireNonNull(str);
        content.append(str);
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        checkClosed();
        Objects.requireNonNull(str);
        content.append(str, off, off + len);
    }

    @Override
    public Writer append(CharSequence csq) throws IOException {
        checkClosed();
        content.append(csq);
        return this;
    }

    @Override
    public Writer append(CharSequence csq, int start, int end) throws IOException {
        checkClosed();
        content.append(csq, start, end);
        return this;
    }

    @Override
    public Writer append(char c) throws IOException {
        checkClosed();
        content.append(c);
        return this;
    }

    /**
     * Obfuscates the cached contents.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Override
    protected void onClose() throws IOException {
        obfuscator.obfuscateText(content, destination);
    }
}
