/*
 * LimitAppendable.java
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

/**
 * An {@link Appendable} that limits the amount of text that can be appended.
 *
 * @author Rob Spoor
 * @since 1.4
 */
public class LimitAppendable implements Appendable {

    private final Appendable appendable;

    private long remaining;

    LimitAppendable(Appendable appendable, long limit) {
        this.appendable = appendable;
        this.remaining = limit;
    }

    /**
     * Returns whether or not the limit has been reached.
     *
     * @return {@code true} if the limit has been reached, or {@code false} otherwise.
     */
    public boolean limitReached() {
        return remaining <= 0;
    }

    /**
     * Returns whether or not the limit has been exceeded. The difference between this method and {@link #limitReached()} is that this method will
     * return {@code false} if the total length that was appended exactly matches the limit, and {@link #limitReached()} will return {@code true}.
     *
     * @return {@code true} if the limit has been exceeded, or {@code false} otherwise.
     */
    public boolean limitExceeded() {
        return remaining == -1;
    }

    @Override
    public Appendable append(CharSequence csq) throws IOException {
        if (remaining > 0) {
            CharSequence cs = csq != null ? csq : "null"; //$NON-NLS-1$
            int length = cs.length();
            if (remaining >= length) {
                appendable.append(csq);
                remaining -= length;
            } else {
                appendable.append(csq, 0, (int) remaining);
                remaining = -1;
            }
        } else {
            remaining = -1;
        }
        return this;
    }

    @Override
    public Appendable append(CharSequence csq, int start, int end) throws IOException {
        if (remaining > 0) {
            if (remaining >= end - start) {
                appendable.append(csq, start, end);
                remaining -= end - start;
            } else {
                appendable.append(csq, start, start + (int) remaining);
                remaining = -1;
            }
        } else {
            remaining = -1;
        }
        return this;
    }

    @Override
    public Appendable append(char c) throws IOException {
        if (remaining > 0) {
            appendable.append(c);
            remaining--;
        } else {
            remaining = -1;
        }
        return this;
    }
}
