/*
 * ObfuscatingWriter.java
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

/**
 * Abstract class for writing obfuscated text to character streams.
 *
 * @author Rob Spoor
 */
public abstract class ObfuscatingWriter extends Writer {

    private boolean closed = false;

    /**
     * Checks whether or not this stream is already closed. This method should be called from all methods that write or append text.
     *
     * @throws IOException If this stream is already closed.
     */
    protected final void checkClosed() throws IOException {
        if (closed) {
            throw new IOException(Messages.ObfuscatingWriter.alreadyClosed.get());
        }
    }

    /**
     * Flushes the stream. This implementation only {@link #checkClosed() checks whether or not this stream is already closed}. Sub classes can add
     * additional behaviour if needed.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void flush() throws IOException {
        checkClosed();
    }

    /**
     * Closes the stream. This method will make sure that {@link #onClose() actual closing} will only be performed once.
     * After calling this method, {@link #checkClosed()} will throw an {@link IOException}.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public final void close() throws IOException {
        if (!closed) {
            onClose();
            closed = true;
        }
    }

    /**
     * Returns whether or not this stream is already closed.
     *
     * @return {@code true} if this stream is already closed, or {@code false} otherwise.
     */
    protected final boolean closed() {
        return closed;
    }

    /**
     * Closes the stream, cleaning up any resources. This default implementation does nothing.
     *
     * @throws IOException If an I/O error occurs.
     */
    protected void onClose() throws IOException {
        // place holder method
    }
}
